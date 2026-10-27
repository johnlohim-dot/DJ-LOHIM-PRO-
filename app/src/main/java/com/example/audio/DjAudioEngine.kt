package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class DjAudioEngine(private val context: Context) {

  private val scope = CoroutineScope(Dispatchers.IO)

  private var playerA: MediaPlayer? = null
  private var playerB: MediaPlayer? = null

  private var trackA: DjTrack? = null
  private var trackB: DjTrack? = null

  // Volume & Crossfade
  private var volA: Float = 0.85f
  private var volB: Float = 0.85f
  private var crossfader: Float = 0f // -1f (Deck A) .. 0f (Center) .. +1f (Deck B)
  private var masterVol: Float = 1.0f

  // Playback rates
  private var rateA: Float = 1.0f
  private var rateB: Float = 1.0f

  // Built-in tracks
  val builtInTracks = mutableListOf<DjTrack>()

  // SoundPool for instant zero-latency Sampler Pads
  private val soundPool: SoundPool
  private val soundMap = mutableMapOf<SoundFxType, Int>()

  init {
    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_MEDIA)
      .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
      .build()

    soundPool = SoundPool.Builder()
      .setMaxStreams(8)
      .setAudioAttributes(audioAttributes)
      .build()

    initializeAudioAssets()
  }

  private fun initializeAudioAssets() {
    scope.launch {
      val audioDir = File(context.cacheDir, "dj_audio").apply { mkdirs() }

      // 1. Generate built-in tracks if not present
      val trackConfigs = listOf(
        Triple("anthem_128", "Ibiza Club Anthem", 128f),
        Triple("minimal_124", "Deep Tech Horizon", 124f),
        Triple("bass_126", "Miami Sub Bassline", 126f),
        Triple("break_120", "Funky Electro Groove", 120f)
      )

      trackConfigs.forEachIndexed { index, (filePrefix, title, bpm) ->
        val file = File(audioDir, "$filePrefix.wav")
        if (!file.exists() || file.length() < 1000) {
          try {
            val bytes = WavGenerator.generateRhythmTrack(bpm, bars = 16)
            WavGenerator.saveByteArrayToFile(bytes, file)
          } catch (e: Exception) {
            Log.e("DjAudioEngine", "Failed to generate track $title", e)
          }
        }
        val durationMs = ((16 * 4 * (60.0 / bpm)) * 1000).toLong()
        val track = DjTrack(
          id = filePrefix,
          title = title,
          artist = "LOHIM PRO",
          bpm = bpm,
          durationMs = durationMs,
          filePath = file.absolutePath,
          waveform = DjTrack.generateWaveform(sampleCount = 70, seed = (index + 1) * 31415L),
          isBuiltIn = true
        )
        builtInTracks.add(track)
      }

      // 2. Generate Sound FX and load into SoundPool
      SoundFxType.entries.forEach { fx ->
        val fxFile = File(audioDir, "fx_${fx.name.lowercase()}.wav")
        if (!fxFile.exists() || fxFile.length() < 500) {
          try {
            val bytes = WavGenerator.generateSoundFx(fx)
            WavGenerator.saveByteArrayToFile(bytes, fxFile)
          } catch (e: Exception) {
            Log.e("DjAudioEngine", "Failed to generate fx ${fx.name}", e)
          }
        }
        val soundId = soundPool.load(fxFile.absolutePath, 1)
        soundMap[fx] = soundId
      }
    }
  }

  // MARK: - Deck Setup & Loading

  fun loadTrack(deckKey: DeckKey, track: DjTrack, onPrepared: () -> Unit) {
    if (deckKey == DeckKey.A) {
      trackA = track
      playerA?.release()
      playerA = createPlayer(track) {
        applyVolumes()
        applyPitch(DeckKey.A, rateA)
        onPrepared()
      }
    } else {
      trackB = track
      playerB?.release()
      playerB = createPlayer(track) {
        applyVolumes()
        applyPitch(DeckKey.B, rateB)
        onPrepared()
      }
    }
  }

  private fun createPlayer(track: DjTrack, onPrepared: () -> Unit): MediaPlayer {
    val mp = MediaPlayer()
    mp.setAudioAttributes(
      AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    )
    mp.isLooping = true // Continuous DJ loop mode by default

    try {
      if (track.filePath != null) {
        mp.setDataSource(track.filePath)
      } else if (track.uri != null) {
        mp.setDataSource(context, track.uri)
      }
      mp.setOnPreparedListener {
        onPrepared()
      }
      mp.prepareAsync()
    } catch (e: Exception) {
      Log.e("DjAudioEngine", "Failed to set data source for ${track.title}", e)
    }

    return mp
  }

  // MARK: - Playback Controls

  fun play(deckKey: DeckKey) {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    player?.let {
      if (!it.isPlaying) {
        it.start()
        applyPitch(deckKey, if (deckKey == DeckKey.A) rateA else rateB)
      }
    }
  }

  fun pause(deckKey: DeckKey) {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    player?.let {
      if (it.isPlaying) {
        it.pause()
      }
    }
  }

  fun isPlaying(deckKey: DeckKey): Boolean {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    return try {
      player?.isPlaying == true
    } catch (e: Exception) {
      false
    }
  }

  fun getCurrentPosition(deckKey: DeckKey): Long {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    return try {
      player?.currentPosition?.toLong() ?: 0L
    } catch (e: Exception) {
      0L
    }
  }

  fun getDuration(deckKey: DeckKey): Long {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    return try {
      val dur = player?.duration?.toLong() ?: 0L
      if (dur > 0) dur else (if (deckKey == DeckKey.A) trackA?.durationMs else trackB?.durationMs) ?: 0L
    } catch (e: Exception) {
      0L
    }
  }

  fun seekTo(deckKey: DeckKey, positionMs: Long) {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    try {
      player?.seekTo(positionMs.toInt())
    } catch (e: Exception) {
      Log.e("DjAudioEngine", "Error seeking $deckKey", e)
    }
  }

  // Pitch / Playback Rate
  fun setPitchRate(deckKey: DeckKey, rate: Float) {
    val clamped = rate.coerceIn(0.5f, 1.5f)
    if (deckKey == DeckKey.A) {
      rateA = clamped
    } else {
      rateB = clamped
    }
    applyPitch(deckKey, clamped)
  }

  private fun applyPitch(deckKey: DeckKey, rate: Float) {
    val player = if (deckKey == DeckKey.A) playerA else playerB
    player?.let {
      try {
        if (it.isPlaying) {
          val params = it.playbackParams
          params.speed = rate
          it.playbackParams = params
        }
      } catch (e: Exception) {
        Log.e("DjAudioEngine", "Failed to apply pitch for $deckKey", e)
      }
    }
  }

  // Volume & Crossfade
  fun setDeckVolume(deckKey: DeckKey, volume: Float) {
    if (deckKey == DeckKey.A) {
      volA = volume.coerceIn(0f, 1f)
    } else {
      volB = volume.coerceIn(0f, 1f)
    }
    applyVolumes()
  }

  fun setCrossfader(pos: Float) {
    crossfader = pos.coerceIn(-1f, 1f)
    applyVolumes()
  }

  fun setMasterVolume(vol: Float) {
    masterVol = vol.coerceIn(0f, 1f)
    applyVolumes()
  }

  private fun applyVolumes() {
    // Equal-power crossfade curve
    val norm = (crossfader + 1f) * 0.25f * PI.toFloat()
    val gainA = cos(norm)
    val gainB = sin(norm)

    val finalVolA = (volA * gainA * masterVol).coerceIn(0f, 1f)
    val finalVolB = (volB * gainB * masterVol).coerceIn(0f, 1f)

    try {
      playerA?.setVolume(finalVolA, finalVolA)
    } catch (e: Exception) {
      // Ignored
    }

    try {
      playerB?.setVolume(finalVolB, finalVolB)
    } catch (e: Exception) {
      // Ignored
    }
  }

  // Sound FX Trigger
  fun triggerSoundFx(type: SoundFxType) {
    val soundId = soundMap[type] ?: return
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
  }

  // Vinyl scratch effect during jog wheel drag
  fun triggerScratchTick(pitch: Float = 1.0f) {
    val soundId = soundMap[SoundFxType.SCRATCH] ?: return
    soundPool.play(soundId, 0.45f, 0.45f, 1, 0, pitch.coerceIn(0.6f, 1.8f))
  }

  fun release() {
    try {
      playerA?.release()
      playerB?.release()
      soundPool.release()
    } catch (e: Exception) {
      Log.e("DjAudioEngine", "Error releasing audio engine", e)
    }
  }
}

enum class DeckKey {
  A, B
}

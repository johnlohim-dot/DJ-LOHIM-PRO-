package com.example.ui

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.DeckKey
import com.example.audio.DjAudioEngine
import com.example.audio.DjTrack
import com.example.audio.SoundFxType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

data class DeckUiState(
  val deckKey: DeckKey,
  val track: DjTrack? = null,
  val isPlaying: Boolean = false,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val originalBpm: Float = 120f,
  val currentBpm: Float = 120f,
  val pitchRate: Float = 1.0f,
  val volume: Float = 0.85f,
  val cuePositionMs: Long = 0L,
  val hotCues: List<Long?> = listOf(null, null, null, null),
  val activeLoop: Float? = null, // In beats: 0.5, 1, 2, 4
  val loopStartMs: Long? = null,
  val rotationAngle: Float = 0f,
  val eqHigh: Float = 1.0f,
  val eqMid: Float = 1.0f,
  val eqLow: Float = 1.0f,
  val filter: Float = 0.0f // -1.0 (LPF) to 0 (flat) to +1.0 (HPF)
)

data class MixerUiState(
  val crossfader: Float = 0f, // -1f to 1f
  val masterVolume: Float = 0.9f,
  val vuLeft: Float = 0.1f,
  val vuRight: Float = 0.1f
)

enum class ActiveScreenTab {
  DUAL_DECKS,
  DECK_A,
  MIXER,
  DECK_B,
  SAMPLER
}

class DjViewModel(application: Application) : AndroidViewModel(application) {

  val audioEngine = DjAudioEngine(application)

  private val _deckA = MutableStateFlow(DeckUiState(deckKey = DeckKey.A))
  val deckA: StateFlow<DeckUiState> = _deckA.asStateFlow()

  private val _deckB = MutableStateFlow(DeckUiState(deckKey = DeckKey.B))
  val deckB: StateFlow<DeckUiState> = _deckB.asStateFlow()

  private val _mixer = MutableStateFlow(MixerUiState())
  val mixer: StateFlow<MixerUiState> = _mixer.asStateFlow()

  private val _selectedTab = MutableStateFlow(ActiveScreenTab.DUAL_DECKS)
  val selectedTab: StateFlow<ActiveScreenTab> = _selectedTab.asStateFlow()

  private val _builtInTracks = MutableStateFlow<List<DjTrack>>(emptyList())
  val builtInTracks: StateFlow<List<DjTrack>> = _builtInTracks.asStateFlow()

  private var playbackLoopJob: Job? = null
  private var lastTickTime = SystemClock.uptimeMillis()

  init {
    viewModelScope.launch {
      // Wait briefly for built-in tracks to initialize, then load default tracks to both decks
      delay(400)
      while (audioEngine.builtInTracks.isEmpty()) {
        delay(100)
      }
      val tracks = audioEngine.builtInTracks
      _builtInTracks.value = tracks

      if (tracks.isNotEmpty()) {
        loadTrack(DeckKey.A, tracks[0])
      }
      if (tracks.size > 1) {
        loadTrack(DeckKey.B, tracks[1])
      }
    }

    startPlaybackPoller()
  }

  fun setTab(tab: ActiveScreenTab) {
    _selectedTab.value = tab
  }

  // MARK: - Polling & Animation Loop

  private fun startPlaybackPoller() {
    playbackLoopJob?.cancel()
    playbackLoopJob = viewModelScope.launch {
      while (isActive) {
        val now = SystemClock.uptimeMillis()
        val dt = (now - lastTickTime).coerceIn(10L, 100L)
        lastTickTime = now

        updateDeckPlayback(DeckKey.A, dt)
        updateDeckPlayback(DeckKey.B, dt)
        updateVuMeters()

        delay(33) // ~30 fps update rate for smooth rotation & time display
      }
    }
  }

  private fun updateDeckPlayback(deckKey: DeckKey, dt: Long) {
    val isPlaying = audioEngine.isPlaying(deckKey)
    val pos = audioEngine.getCurrentPosition(deckKey)
    val dur = audioEngine.getDuration(deckKey)

    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value

    var newAngle = current.rotationAngle
    if (isPlaying) {
      // 33.3 RPM = 360 deg in ~1.8 seconds * pitchRate
      val speedDegPerSec = (360f / 1.8f) * current.pitchRate
      newAngle = (newAngle + speedDegPerSec * (dt / 1000f)) % 360f
    }

    // Handle Active Looping
    var updatedPos = pos
    if (isPlaying && current.activeLoop != null && current.loopStartMs != null) {
      val beats = current.activeLoop
      val loopDurationMs = (beats * (60000f / current.currentBpm)).toLong()
      if (pos >= current.loopStartMs + loopDurationMs || pos < current.loopStartMs) {
        audioEngine.seekTo(deckKey, current.loopStartMs)
        updatedPos = current.loopStartMs
      }
    }

    targetFlow.update {
      it.copy(
        isPlaying = isPlaying,
        currentPositionMs = updatedPos,
        durationMs = if (dur > 0) dur else it.durationMs,
        rotationAngle = newAngle
      )
    }
  }

  private fun updateVuMeters() {
    val playA = _deckA.value.isPlaying
    val playB = _deckB.value.isPlaying
    val volA = _deckA.value.volume
    val volB = _deckB.value.volume
    val xfade = _mixer.value.crossfader

    // Equal power gains
    val norm = (xfade + 1f) * 0.25f * PI.toFloat()
    val gainA = kotlin.math.cos(norm)
    val gainB = kotlin.math.sin(norm)

    val time = System.currentTimeMillis() / 80.0
    val dynamicJitterA = if (playA) (0.6f + 0.35f * sin(time * 1.5).toFloat() + (Math.random() * 0.15f).toFloat()) else 0.05f
    val dynamicJitterB = if (playB) (0.6f + 0.35f * sin(time * 1.7 + 1.0).toFloat() + (Math.random() * 0.15f).toFloat()) else 0.05f

    val finalVuA = (dynamicJitterA * volA * gainA * _mixer.value.masterVolume).coerceIn(0f, 1f)
    val finalVuB = (dynamicJitterB * volB * gainB * _mixer.value.masterVolume).coerceIn(0f, 1f)

    _mixer.update {
      it.copy(
        vuLeft = finalVuA,
        vuRight = finalVuB
      )
    }
  }

  // MARK: - Deck Playback Actions

  fun togglePlay(deckKey: DeckKey) {
    val isPlaying = audioEngine.isPlaying(deckKey)
    if (isPlaying) {
      audioEngine.pause(deckKey)
    } else {
      audioEngine.play(deckKey)
    }
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(isPlaying = !isPlaying) }
  }

  fun pressCue(deckKey: DeckKey) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value
    if (current.isPlaying) {
      audioEngine.pause(deckKey)
      audioEngine.seekTo(deckKey, current.cuePositionMs)
      targetFlow.update { it.copy(isPlaying = false, currentPositionMs = current.cuePositionMs) }
    } else {
      // Stutter cue / play while held
      audioEngine.seekTo(deckKey, current.cuePositionMs)
      audioEngine.play(deckKey)
      targetFlow.update { it.copy(isPlaying = true) }
    }
  }

  fun releaseCue(deckKey: DeckKey) {
    // If paused before pressing cue, return to cue point and pause
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value
    audioEngine.pause(deckKey)
    audioEngine.seekTo(deckKey, current.cuePositionMs)
    targetFlow.update { it.copy(isPlaying = false, currentPositionMs = current.cuePositionMs) }
  }

  fun setCuePoint(deckKey: DeckKey) {
    val pos = audioEngine.getCurrentPosition(deckKey)
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(cuePositionMs = pos) }
  }

  // Hot Cues
  fun handleHotCue(deckKey: DeckKey, index: Int, isLongPress: Boolean) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value
    val currentHotCues = current.hotCues.toMutableList()

    if (isLongPress || currentHotCues[index] == null) {
      // Set hot cue at current position
      val pos = audioEngine.getCurrentPosition(deckKey)
      currentHotCues[index] = pos
      targetFlow.update { it.copy(hotCues = currentHotCues) }
    } else {
      // Jump to hot cue
      val cuePos = currentHotCues[index] ?: 0L
      audioEngine.seekTo(deckKey, cuePos)
      targetFlow.update { it.copy(currentPositionMs = cuePos) }
    }
  }

  // Beat Looping
  fun toggleBeatLoop(deckKey: DeckKey, beats: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value
    if (current.activeLoop == beats) {
      // Turn off loop
      targetFlow.update { it.copy(activeLoop = null, loopStartMs = null) }
    } else {
      // Activate loop from current position
      val pos = audioEngine.getCurrentPosition(deckKey)
      targetFlow.update { it.copy(activeLoop = beats, loopStartMs = pos) }
    }
  }

  // Pitch / Tempo Slider
  fun setPitchRate(deckKey: DeckKey, rate: Float) {
    val clamped = rate.coerceIn(0.8f, 1.2f)
    audioEngine.setPitchRate(deckKey, clamped)
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update {
      it.copy(
        pitchRate = clamped,
        currentBpm = it.originalBpm * clamped
      )
    }
  }

  fun nudgePitch(deckKey: DeckKey, delta: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value
    val newRate = (current.pitchRate + delta).coerceIn(0.8f, 1.2f)
    setPitchRate(deckKey, newRate)
  }

  fun resetPitch(deckKey: DeckKey) {
    setPitchRate(deckKey, 1.0f)
  }

  // Auto Sync
  fun autoSync(targetDeck: DeckKey) {
    val sourceState = if (targetDeck == DeckKey.A) _deckB.value else _deckA.value
    val targetState = if (targetDeck == DeckKey.A) _deckA.value else _deckB.value

    if (targetState.originalBpm > 0f) {
      val requiredRate = (sourceState.currentBpm / targetState.originalBpm).coerceIn(0.8f, 1.2f)
      setPitchRate(targetDeck, requiredRate)
    }
  }

  // Jog Wheel Scratching / Scrubbing
  fun onJogWheelScrub(deckKey: DeckKey, deltaDegrees: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    val current = targetFlow.value
    val dur = current.durationMs
    if (dur <= 0) return

    // Convert degrees to time scrub (e.g. 360 deg = 1.8 seconds)
    val scrubMs = ((deltaDegrees / 360f) * 1800f).toLong()
    val newPos = (current.currentPositionMs + scrubMs).coerceIn(0L, dur)
    audioEngine.seekTo(deckKey, newPos)

    if (abs(deltaDegrees) > 6f) {
      audioEngine.triggerScratchTick(1.0f + (deltaDegrees / 120f))
    }

    targetFlow.update {
      it.copy(
        currentPositionMs = newPos,
        rotationAngle = (it.rotationAngle + deltaDegrees) % 360f
      )
    }
  }

  fun seekTo(deckKey: DeckKey, positionMs: Long) {
    audioEngine.seekTo(deckKey, positionMs)
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(currentPositionMs = positionMs) }
  }

  // Mixer Controls
  fun setDeckVolume(deckKey: DeckKey, volume: Float) {
    audioEngine.setDeckVolume(deckKey, volume)
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(volume = volume) }
  }

  fun setCrossfader(pos: Float) {
    audioEngine.setCrossfader(pos)
    _mixer.update { it.copy(crossfader = pos) }
  }

  fun setMasterVolume(vol: Float) {
    audioEngine.setMasterVolume(vol)
    _mixer.update { it.copy(masterVolume = vol) }
  }

  fun setEqHigh(deckKey: DeckKey, value: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(eqHigh = value.coerceIn(0f, 2f)) }
  }

  fun setEqMid(deckKey: DeckKey, value: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(eqMid = value.coerceIn(0f, 2f)) }
  }

  fun setEqLow(deckKey: DeckKey, value: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(eqLow = value.coerceIn(0f, 2f)) }
  }

  fun setFilter(deckKey: DeckKey, value: Float) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update { it.copy(filter = value.coerceIn(-1f, 1f)) }
  }

  // Sampler
  fun triggerSoundFx(type: SoundFxType) {
    audioEngine.triggerSoundFx(type)
  }

  // Track Loading
  fun loadTrack(deckKey: DeckKey, track: DjTrack) {
    val targetFlow = if (deckKey == DeckKey.A) _deckA else _deckB
    targetFlow.update {
      it.copy(
        track = track,
        originalBpm = track.bpm,
        currentBpm = track.bpm * it.pitchRate,
        durationMs = track.durationMs,
        currentPositionMs = 0L,
        isPlaying = false,
        activeLoop = null,
        loopStartMs = null,
        hotCues = listOf(null, null, null, null)
      )
    }

    audioEngine.loadTrack(deckKey, track) {
      val dur = audioEngine.getDuration(deckKey)
      targetFlow.update {
        it.copy(durationMs = if (dur > 0) dur else track.durationMs)
      }
    }
  }

  fun loadFromUri(deckKey: DeckKey, uri: Uri, title: String) {
    val track = DjTrack(
      id = uri.toString(),
      title = title,
      artist = "User Audio",
      bpm = 125.0f,
      durationMs = 180000L,
      uri = uri,
      waveform = DjTrack.generateWaveform(sampleCount = 70, seed = System.currentTimeMillis()),
      isBuiltIn = false
    )
    loadTrack(deckKey, track)
  }

  override fun onCleared() {
    super.onCleared()
    playbackLoopJob?.cancel()
    audioEngine.release()
  }
}

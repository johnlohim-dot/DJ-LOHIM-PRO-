package com.example.audio

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object WavGenerator {

  private const val SAMPLE_RATE = 44100
  private const val CHANNELS = 2

  fun generateRhythmTrack(bpm: Float, bars: Int = 8): ByteArray {
    val beatsPerBar = 4
    val totalBeats = bars * beatsPerBar
    val secondsPerBeat = 60.0 / bpm
    val totalSeconds = totalBeats * secondsPerBeat
    val totalSamples = (totalSeconds * SAMPLE_RATE).toInt()

    val pcm = ShortArray(totalSamples * CHANNELS)
    val samplesPerBeat = (secondsPerBeat * SAMPLE_RATE).toInt()
    val samplesPer16th = samplesPerBeat / 4

    for (beat in 0 until totalBeats) {
      val beatStartSample = beat * samplesPerBeat

      // Kick on every beat (4-on-the-floor)
      addKick(pcm, beatStartSample)

      // Snare / Clap on beats 2 and 4 (0-indexed: 1 and 3)
      if (beat % 2 == 1) {
        addClap(pcm, beatStartSample)
      }

      // Hi-hats on offbeats (every 8th note) and 16th notes
      for (sixteenth in 0 until 4) {
        val hatSample = beatStartSample + sixteenth * samplesPer16th
        val isOffBeat = sixteenth == 2
        addHiHat(pcm, hatSample, isOffBeat)
      }

      // Bassline note
      val noteFreq = when ((beat / 2) % 4) {
        0 -> 65.41 // C2
        1 -> 77.78 // Eb2
        2 -> 87.31 // F2
        else -> 58.27 // Bb1
      }
      addBassSynth(pcm, beatStartSample, samplesPerBeat, noteFreq)
    }

    return createWavBytes(pcm, SAMPLE_RATE, CHANNELS)
  }

  fun generateSoundFx(type: SoundFxType): ByteArray {
    val durationSec = when (type) {
      SoundFxType.AIR_HORN -> 1.4
      SoundFxType.SCRATCH -> 0.7
      SoundFxType.DROP -> 1.5
      SoundFxType.CLAP_808 -> 0.5
      SoundFxType.LASER -> 0.6
      SoundFxType.REWIND -> 1.2
    }
    val totalSamples = (durationSec * SAMPLE_RATE).toInt()
    val pcm = ShortArray(totalSamples * CHANNELS)

    when (type) {
      SoundFxType.AIR_HORN -> {
        // Reggae / DJ Air Horn blast (dual frequency with vibrato)
        for (i in 0 until totalSamples) {
          val t = i.toDouble() / SAMPLE_RATE
          val env = when {
            t < 0.05 -> t / 0.05
            t < 0.9 -> 1.0
            else -> (durationSec - t) / 0.5
          }.coerceIn(0.0, 1.0)

          val pulse = sin(2.0 * PI * 12.0 * t) * 0.1
          val s1 = sin(2.0 * PI * (466.16 + pulse * 20.0) * t) // Bb4
          val s2 = sin(2.0 * PI * (587.33 + pulse * 20.0) * t) // D5
          val s3 = sin(2.0 * PI * (698.46 + pulse * 20.0) * t) * 0.5 // F5

          val sample = ((s1 + s2 + s3) * 0.3 * env * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
          pcm[i * 2] = sample
          pcm[i * 2 + 1] = sample
        }
      }
      SoundFxType.SCRATCH -> {
        for (i in 0 until totalSamples) {
          val t = i.toDouble() / SAMPLE_RATE
          val freq = 400.0 + sin(2.0 * PI * 4.0 * t) * 350.0
          val noise = (Math.random() * 2.0 - 1.0) * 0.4
          val wave = sin(2.0 * PI * freq * t) * 0.6 + noise
          val env = exp(-t * 3.5)
          val sample = (wave * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
          pcm[i * 2] = sample
          pcm[i * 2 + 1] = sample
        }
      }
      SoundFxType.DROP -> {
        // Heavy 808 sub bass drop
        for (i in 0 until totalSamples) {
          val t = i.toDouble() / SAMPLE_RATE
          val freq = 140.0 * exp(-t * 2.8) + 38.0
          val wave = sin(2.0 * PI * freq * t)
          val env = exp(-t * 1.8)
          val sample = (wave * env * 30000.0).toInt().coerceIn(-32768, 32767).toShort()
          pcm[i * 2] = sample
          pcm[i * 2 + 1] = sample
        }
      }
      SoundFxType.CLAP_808 -> {
        for (i in 0 until totalSamples) {
          val t = i.toDouble() / SAMPLE_RATE
          val noise = (Math.random() * 2.0 - 1.0)
          val env = if (t < 0.03) {
            sin(t / 0.03 * PI * 3).coerceAtLeast(0.0)
          } else {
            exp(-(t - 0.03) * 12.0)
          }
          val sample = (noise * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
          pcm[i * 2] = sample
          pcm[i * 2 + 1] = sample
        }
      }
      SoundFxType.LASER -> {
        for (i in 0 until totalSamples) {
          val t = i.toDouble() / SAMPLE_RATE
          val freq = 2200.0 * exp(-t * 7.0) + 120.0
          val wave = sin(2.0 * PI * freq * t)
          val env = exp(-t * 4.5)
          val sample = (wave * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
          pcm[i * 2] = sample
          pcm[i * 2 + 1] = sample
        }
      }
      SoundFxType.REWIND -> {
        for (i in 0 until totalSamples) {
          val t = i.toDouble() / SAMPLE_RATE
          val progress = t / durationSec
          val freq = (800.0 * (1.0 - progress) + 80.0) * (1.0 + sin(2.0 * PI * 20.0 * t) * 0.2)
          val noise = (Math.random() * 2.0 - 1.0) * 0.3
          val wave = sin(2.0 * PI * freq * t) * 0.7 + noise
          val env = (1.0 - progress * 0.8)
          val sample = (wave * env * 26000.0).toInt().coerceIn(-32768, 32767).toShort()
          pcm[i * 2] = sample
          pcm[i * 2 + 1] = sample
        }
      }
    }

    return createWavBytes(pcm, SAMPLE_RATE, CHANNELS)
  }

  private fun addKick(pcm: ShortArray, startSample: Int) {
    val durationSamples = (0.28 * SAMPLE_RATE).toInt()
    for (i in 0 until durationSamples) {
      val target = (startSample + i) * CHANNELS
      if (target + 1 >= pcm.size) break
      val t = i.toDouble() / SAMPLE_RATE
      val freq = 150.0 * exp(-t * 32.0) + 45.0
      val wave = sin(2.0 * PI * freq * t)
      val env = exp(-t * 12.0)
      val kickVal = (wave * env * 22000.0).toInt()

      pcm[target] = (pcm[target] + kickVal).coerceIn(-32768, 32767).toShort()
      pcm[target + 1] = (pcm[target + 1] + kickVal).coerceIn(-32768, 32767).toShort()
    }
  }

  private fun addClap(pcm: ShortArray, startSample: Int) {
    val durationSamples = (0.2 * SAMPLE_RATE).toInt()
    for (i in 0 until durationSamples) {
      val target = (startSample + i) * CHANNELS
      if (target + 1 >= pcm.size) break
      val t = i.toDouble() / SAMPLE_RATE
      val noise = (Math.random() * 2.0 - 1.0)
      val env = exp(-t * 22.0)
      val clapVal = (noise * env * 14000.0).toInt()

      pcm[target] = (pcm[target] + clapVal).coerceIn(-32768, 32767).toShort()
      pcm[target + 1] = (pcm[target + 1] + clapVal).coerceIn(-32768, 32767).toShort()
    }
  }

  private fun addHiHat(pcm: ShortArray, startSample: Int, isOpen: Boolean) {
    val decay = if (isOpen) 35.0 else 90.0
    val durationSamples = (0.12 * SAMPLE_RATE).toInt()
    for (i in 0 until durationSamples) {
      val target = (startSample + i) * CHANNELS
      if (target + 1 >= pcm.size) break
      val t = i.toDouble() / SAMPLE_RATE
      val noise = (Math.random() * 2.0 - 1.0)
      val env = exp(-t * decay)
      val hatVal = (noise * env * (if (isOpen) 10000.0 else 6000.0)).toInt()

      pcm[target] = (pcm[target] + hatVal).coerceIn(-32768, 32767).toShort()
      pcm[target + 1] = (pcm[target + 1] + hatVal).coerceIn(-32768, 32767).toShort()
    }
  }

  private fun addBassSynth(pcm: ShortArray, startSample: Int, samplesPerBeat: Int, freq: Double) {
    val durationSamples = (samplesPerBeat * 0.85).toInt()
    for (i in 0 until durationSamples) {
      val target = (startSample + i) * CHANNELS
      if (target + 1 >= pcm.size) break
      val t = i.toDouble() / SAMPLE_RATE
      // Warm sawtooth-like square wave
      val s1 = sin(2.0 * PI * freq * t)
      val s2 = sin(2.0 * PI * (freq * 2.0) * t) * 0.4
      val s3 = sin(2.0 * PI * (freq * 3.0) * t) * 0.2
      val env = (1.0 - (i.toDouble() / durationSamples) * 0.4)
      val bassVal = ((s1 + s2 + s3) * env * 12000.0).toInt()

      pcm[target] = (pcm[target] + bassVal).coerceIn(-32768, 32767).toShort()
      pcm[target + 1] = (pcm[target + 1] + bassVal).coerceIn(-32768, 32767).toShort()
    }
  }

  fun saveByteArrayToFile(bytes: ByteArray, file: File) {
    file.parentFile?.mkdirs()
    FileOutputStream(file).use { it.write(bytes) }
  }

  private fun createWavBytes(pcmData: ShortArray, sampleRate: Int, channels: Int): ByteArray {
    val totalAudioLen = pcmData.size * 2
    val totalDataLen = totalAudioLen + 36
    val byteRate = sampleRate * channels * 2

    val out = ByteArrayOutputStream(totalAudioLen + 44)

    // RIFF/WAVE header
    out.write("RIFF".toByteArray())
    out.write(intToLittleEndian(totalDataLen))
    out.write("WAVE".toByteArray())
    out.write("fmt ".toByteArray())
    out.write(intToLittleEndian(16)) // subchunk1size (16 for PCM)
    out.write(shortToLittleEndian(1)) // audio format 1 = PCM
    out.write(shortToLittleEndian(channels.toShort()))
    out.write(intToLittleEndian(sampleRate))
    out.write(intToLittleEndian(byteRate))
    out.write(shortToLittleEndian((channels * 2).toShort())) // block align
    out.write(shortToLittleEndian(16)) // bits per sample
    out.write("data".toByteArray())
    out.write(intToLittleEndian(totalAudioLen))

    // PCM Data
    for (sample in pcmData) {
      out.write(sample.toInt() and 0xFF)
      out.write((sample.toInt() shr 8) and 0xFF)
    }

    return out.toByteArray()
  }

  private fun intToLittleEndian(value: Int): ByteArray = byteArrayOf(
    (value and 0xFF).toByte(),
    ((value shr 8) and 0xFF).toByte(),
    ((value shr 16) and 0xFF).toByte(),
    ((value shr 24) and 0xFF).toByte()
  )

  private fun shortToLittleEndian(value: Short): ByteArray = byteArrayOf(
    (value.toInt() and 0xFF).toByte(),
    ((value.toInt() shr 8) and 0xFF).toByte()
  )
}

enum class SoundFxType(val displayName: String) {
  AIR_HORN("AIR HORN"),
  SCRATCH("SCRATCH"),
  DROP("808 DROP"),
  CLAP_808("808 CLAP"),
  LASER("LASER FX"),
  REWIND("VINYL REWIND")
}

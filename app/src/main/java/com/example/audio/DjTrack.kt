package com.example.audio

import android.net.Uri

data class DjTrack(
  val id: String,
  val title: String,
  val artist: String,
  val bpm: Float,
  val durationMs: Long,
  val uri: Uri? = null,
  val filePath: String? = null,
  val waveform: List<Float> = emptyList(),
  val isBuiltIn: Boolean = false
) {
  companion object {
    fun generateWaveform(sampleCount: Int = 80, seed: Long = 12345L): List<Float> {
      val random = java.util.Random(seed)
      return List(sampleCount) { index ->
        val beatPhase = (index % 4) / 4f
        val kickEmphasis = if (index % 4 == 0) 0.85f else 0.35f
        val noise = random.nextFloat() * 0.4f
        (kickEmphasis + noise).coerceIn(0.15f, 1.0f)
      }
    }
  }
}

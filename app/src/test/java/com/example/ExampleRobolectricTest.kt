package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundFxType
import com.example.audio.WavGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DJ Lohim Pro", appName)
  }

  @Test
  fun `wavGenerator generates valid RIFF header`() {
    val trackBytes = WavGenerator.generateRhythmTrack(bpm = 128f, bars = 1)
    assertTrue("Track should have valid WAV length", trackBytes.size > 44)

    val riffHeader = String(trackBytes.sliceArray(0..3))
    val waveHeader = String(trackBytes.sliceArray(8..11))
    assertEquals("RIFF", riffHeader)
    assertEquals("WAVE", waveHeader)
  }

  @Test
  fun `soundFx generates valid audio bytes`() {
    val fxBytes = WavGenerator.generateSoundFx(SoundFxType.AIR_HORN)
    assertTrue("Sound FX should generate audio data", fxBytes.size > 44)
    val riffHeader = String(fxBytes.sliceArray(0..3))
    assertEquals("RIFF", riffHeader)
  }
}

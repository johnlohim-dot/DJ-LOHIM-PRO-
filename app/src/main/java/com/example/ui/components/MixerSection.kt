package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MixerUiState
import com.example.ui.theme.DjAmber
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjGoldBright
import com.example.ui.theme.DjGreen
import com.example.ui.theme.DjPanelBg
import com.example.ui.theme.DjRed

@Composable
fun MixerSection(
  mixerState: MixerUiState,
  deckAVolume: Float,
  deckBVolume: Float,
  onDeckAVolumeChange: (Float) -> Unit,
  onDeckBVolumeChange: (Float) -> Unit,
  onCrossfaderChange: (Float) -> Unit,
  onMasterVolumeChange: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val view = LocalView.current

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFF2C2C34), RoundedCornerShape(16.dp))
      .testTag("mixer_section"),
    color = DjPanelBg,
    tonalElevation = 6.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DJ MIXER",
          color = DjGold,
          fontWeight = FontWeight.Black,
          fontSize = 14.sp,
          letterSpacing = 1.5.sp
        )

        // Master Volume
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "MASTER: ${(mixerState.masterVolume * 100).toInt()}%",
            color = Color(0xFFAAAAAA),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Master Volume Slider
      Slider(
        value = mixerState.masterVolume,
        onValueChange = onMasterVolumeChange,
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(
          thumbColor = DjGold,
          activeTrackColor = DjGold,
          inactiveTrackColor = Color(0xFF33333E)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(28.dp)
          .testTag("master_volume_slider")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Dual Channel Faders and Stereo VU Meter in middle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Channel A Volume Fader (Deck 1)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = "CH 1",
            color = DjCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "${(deckAVolume * 100).toInt()}%",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )

          Slider(
            value = deckAVolume,
            onValueChange = onDeckAVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
              thumbColor = DjCyan,
              activeTrackColor = DjCyan,
              inactiveTrackColor = Color(0xFF282830)
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("deck_a_volume_slider")
          )
        }

        // Stereo Master LED VU Meter
        StereoVuMeter(
          levelLeft = mixerState.vuLeft,
          levelRight = mixerState.vuRight,
          modifier = Modifier
            .width(42.dp)
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
        )

        // Channel B Volume Fader (Deck 2)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = "CH 2",
            color = DjAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "${(deckBVolume * 100).toInt()}%",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )

          Slider(
            value = deckBVolume,
            onValueChange = onDeckBVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
              thumbColor = DjAmber,
              activeTrackColor = DjAmber,
              inactiveTrackColor = Color(0xFF282830)
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("deck_b_volume_slider")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Crossfader Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF131316))
          .border(1.dp, Color(0xFF25252C), RoundedCornerShape(10.dp))
          .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DECK 1 (A)",
            color = DjCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
          Text(
            text = "CROSSFADER",
            color = DjGoldBright,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            letterSpacing = 1.sp
          )
          Text(
            text = "DECK 2 (B)",
            color = DjAmber,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }

        // Horizontal Crossfader Slider
        Slider(
          value = mixerState.crossfader,
          onValueChange = onCrossfaderChange,
          valueRange = -1f..1f,
          colors = SliderDefaults.colors(
            thumbColor = DjGold,
            activeTrackColor = DjGold,
            inactiveTrackColor = Color(0xFF2E2E38)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("crossfader_slider")
        )

        // Quick Cut Presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Button(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
              onCrossfaderChange(-1f)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (mixerState.crossfader <= -0.95f) DjCyan.copy(alpha = 0.3f) else Color(0xFF22222A),
              contentColor = DjCyan
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(28.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
          ) {
            Text("◀ DECK A", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
              onCrossfaderChange(0f)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (kotlin.math.abs(mixerState.crossfader) < 0.1f) DjGold.copy(alpha = 0.3f) else Color(0xFF22222A),
              contentColor = DjGoldBright
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(28.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 2.dp)
          ) {
            Text("CENTER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
              onCrossfaderChange(1f)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (mixerState.crossfader >= 0.95f) DjAmber.copy(alpha = 0.3f) else Color(0xFF22222A),
              contentColor = DjAmber
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(28.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
          ) {
            Text("DECK B ▶", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun StereoVuMeter(
  levelLeft: Float,
  levelRight: Float,
  modifier: Modifier = Modifier
) {
  val segmentCount = 12

  Canvas(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFF0A0A0C))
      .border(0.5.dp, Color(0xFF222228), RoundedCornerShape(4.dp))
      .padding(3.dp)
  ) {
    val barWidth = (size.width - 2.dp.toPx()) / 2f
    val gap = 2.dp.toPx()
    val totalHeight = size.height
    val segHeight = (totalHeight - (segmentCount - 1) * gap) / segmentCount

    for (s in 0 until segmentCount) {
      // 0 is bottom, segmentCount-1 is top
      val segmentIndexFromBottom = (segmentCount - 1) - s
      val y = s * (segHeight + gap)
      val threshold = segmentIndexFromBottom.toFloat() / segmentCount

      val segColor = when {
        segmentIndexFromBottom >= 10 -> DjRed
        segmentIndexFromBottom >= 7 -> DjAmber
        else -> DjGreen
      }

      // Left bar
      val isLeftOn = levelLeft >= threshold
      drawRoundRect(
        color = if (isLeftOn) segColor else segColor.copy(alpha = 0.12f),
        topLeft = Offset(0f, y),
        size = Size(barWidth, segHeight),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
      )

      // Right bar
      val isRightOn = levelRight >= threshold
      val rightX = barWidth + 2.dp.toPx()
      drawRoundRect(
        color = if (isRightOn) segColor else segColor.copy(alpha = 0.12f),
        topLeft = Offset(rightX, y),
        size = Size(barWidth, segHeight),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
      )
    }
  }
}

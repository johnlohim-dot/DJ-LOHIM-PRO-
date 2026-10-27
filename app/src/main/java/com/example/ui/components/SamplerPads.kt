package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundFxType
import com.example.ui.theme.DjAmber
import com.example.ui.theme.DjBlue
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjGreen
import com.example.ui.theme.DjPanelBg
import com.example.ui.theme.DjPurple
import com.example.ui.theme.DjRed

@Composable
fun SamplerPads(
  onTriggerFx: (SoundFxType) -> Unit,
  modifier: Modifier = Modifier
) {
  val fxItems = listOf(
    SoundFxType.AIR_HORN to DjAmber,
    SoundFxType.SCRATCH to DjCyan,
    SoundFxType.DROP to DjRed,
    SoundFxType.CLAP_808 to DjGreen,
    SoundFxType.LASER to DjPurple,
    SoundFxType.REWIND to DjBlue
  )

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFF2C2C34), RoundedCornerShape(16.dp))
      .testTag("sampler_pads_panel"),
    color = DjPanelBg,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Text(
        text = "PERFORMANCE SAMPLER PADS",
        color = DjGold,
        fontWeight = FontWeight.Black,
        fontSize = 13.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (i in 0 until 3) {
            val (fx, color) = fxItems[i]
            SamplerPadItem(
              fx = fx,
              color = color,
              onTrigger = onTriggerFx,
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Row 2
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (i in 3 until 6) {
            val (fx, color) = fxItems[i]
            SamplerPadItem(
              fx = fx,
              color = color,
              onTrigger = onTriggerFx,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SamplerPadItem(
  fx: SoundFxType,
  color: Color,
  onTrigger: (SoundFxType) -> Unit,
  modifier: Modifier = Modifier
) {
  val view = LocalView.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1f,
    label = "pad_press"
  )

  Box(
    modifier = modifier
      .height(68.dp)
      .scale(scale)
      .clip(RoundedCornerShape(10.dp))
      .background(if (isPressed) color.copy(alpha = 0.5f) else Color(0xFF1E1E24))
      .border(
        width = if (isPressed) 2.dp else 1.dp,
        color = if (isPressed) color else color.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        onTrigger(fx)
      }
      .testTag("sampler_pad_${fx.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(4.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.5f)
          .height(3.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(color)
      )
      Text(
        text = fx.displayName,
        color = if (isPressed) Color.White else color,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

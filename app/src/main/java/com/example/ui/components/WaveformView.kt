package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjRed

@Composable
fun WaveformView(
  waveform: List<Float>,
  currentPositionMs: Long,
  durationMs: Long,
  cuePositionMs: Long,
  hotCues: List<Long?>,
  onSeek: (positionMs: Long) -> Unit,
  modifier: Modifier = Modifier,
  primaryColor: Color = DjCyan
) {
  val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF0C0C0F))
      .pointerInput(durationMs) {
        detectTapGestures { offset ->
          if (durationMs > 0) {
            val ratio = (offset.x / size.width).coerceIn(0f, 1f)
            val seekTarget = (ratio * durationMs).toLong()
            onSeek(seekTarget)
          }
        }
      }
      .testTag("waveform_view")
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val width = size.width
      val height = size.height
      val midY = height / 2f

      // Draw center line
      drawLine(
        color = Color(0xFF222228),
        start = Offset(0f, midY),
        end = Offset(width, midY),
        strokeWidth = 1.dp.toPx()
      )

      if (waveform.isNotEmpty()) {
        val barWidth = width / waveform.size
        val playX = progress * width

        waveform.forEachIndexed { i, amp ->
          val barX = i * barWidth + barWidth / 2f
          val barHeight = (height * 0.42f) * amp
          val isPlayed = barX <= playX

          val barColor = if (isPlayed) {
            primaryColor
          } else {
            primaryColor.copy(alpha = 0.35f)
          }

          // Top and bottom mirrored peaks
          drawLine(
            color = barColor,
            start = Offset(barX, midY - barHeight),
            end = Offset(barX, midY + barHeight),
            strokeWidth = (barWidth * 0.7f).coerceAtLeast(1.5f)
          )
        }
      }

      // Draw Cue position marker
      if (durationMs > 0 && cuePositionMs in 0..durationMs) {
        val cueX = (cuePositionMs.toFloat() / durationMs) * width
        drawLine(
          color = DjGold,
          start = Offset(cueX, 0f),
          end = Offset(cueX, height),
          strokeWidth = 2.dp.toPx()
        )
      }

      // Draw Hot Cue flags
      hotCues.forEachIndexed { index, hotCueMs ->
        if (hotCueMs != null && durationMs > 0) {
          val hotCueX = (hotCueMs.toFloat() / durationMs) * width
          val cueColor = when (index) {
            0 -> Color(0xFFFF5252)
            1 -> Color(0xFFFFAB00)
            2 -> Color(0xFF00E676)
            else -> Color(0xFF448AFF)
          }
          drawLine(
            color = cueColor,
            start = Offset(hotCueX, 0f),
            end = Offset(hotCueX, height * 0.4f),
            strokeWidth = 2.5.dp.toPx()
          )
        }
      }

      // Draw Playhead / Play needle
      val playX = progress * width
      drawLine(
        color = Color.White,
        start = Offset(playX, 0f),
        end = Offset(playX, height),
        strokeWidth = 2.dp.toPx()
      )
    }
  }
}

package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjGoldBright
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GoldJogWheel(
  bpm: Float,
  currentPositionMs: Long,
  isPlaying: Boolean,
  rotationAngle: Float,
  onScrub: (deltaDegrees: Float) -> Unit,
  modifier: Modifier = Modifier,
  deckLabel: String = "DECK 1"
) {
  val view = LocalView.current

  var lastTouchAngle by remember { mutableFloatStateOf(0f) }

  BoxWithConstraints(
    modifier = modifier
      .aspectRatio(1f)
      .clip(CircleShape)
      .pointerInput(isPlaying) {
        detectDragGestures(
          onDragStart = { offset ->
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val rad = atan2(offset.y - centerY, offset.x - centerX)
            lastTouchAngle = Math.toDegrees(rad.toDouble()).toFloat()
          },
          onDrag = { change, _ ->
            change.consume()
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val rad = atan2(change.position.y - centerY, change.position.x - centerX)
            val currentAngle = Math.toDegrees(rad.toDouble()).toFloat()
            var delta = currentAngle - lastTouchAngle

            // Normalize jump across 180 / -180 boundary
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f

            lastTouchAngle = currentAngle
            onScrub(delta)
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
          }
        )
      }
      .testTag("jog_wheel_${deckLabel.lowercase().replace(" ", "_")}"),
    contentAlignment = Alignment.Center
  ) {
    val totalSize = maxWidth

    // 1. Platter Outer Base & Vinyl Grooves (Rotates with platter angle)
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .rotate(rotationAngle)
    ) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = size.minDimension / 2f

      // Outer gold platter gradient
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFFFFE57F), // Light gold highlight
            Color(0xFFD4AF37), // Pioneer DJ Rich Gold
            Color(0xFFC59B27),
            Color(0xFF7A5C0B), // Bronze shadow
            Color(0xFFB8860B)  // Outer gold rim
          ),
          center = center,
          radius = radius
        ),
        radius = radius,
        center = center
      )

      // Outer heavy metal beveled border
      drawCircle(
        color = Color(0xFF5A4406),
        radius = radius - 1.dp.toPx(),
        center = center,
        style = Stroke(width = 5.dp.toPx())
      )

      // Strobe / Tachometer dots around outer rim
      val dotCount = 48
      val dotRadius = radius - 9.dp.toPx()
      for (i in 0 until dotCount) {
        val angleRad = (i * 2 * PI / dotCount)
        val dotCenter = Offset(
          center.x + (dotRadius * cos(angleRad)).toFloat(),
          center.y + (dotRadius * sin(angleRad)).toFloat()
        )
        drawCircle(
          color = if (i % 4 == 0) Color(0xFFFFF6B8) else Color(0xFF8A6510),
          radius = 1.8.dp.toPx(),
          center = dotCenter
        )
      }

      // Vinyl black concentric groove ring
      val vinylOuterRadius = radius - 18.dp.toPx()
      val vinylInnerRadius = radius * 0.52f
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFF161616),
            Color(0xFF0F0F0F),
            Color(0xFF222222),
            Color(0xFF0D0D0D)
          ),
          center = center,
          radius = vinylOuterRadius
        ),
        radius = vinylOuterRadius,
        center = center
      )

      // Fine vinyl micro grooves
      val numGrooves = 10
      for (g in 1..numGrooves) {
        val gRadius = vinylInnerRadius + (vinylOuterRadius - vinylInnerRadius) * (g / (numGrooves + 1f))
        drawCircle(
          color = Color(0x33444444),
          radius = gRadius,
          center = center,
          style = Stroke(width = 0.8.dp.toPx())
        )
      }

      // Rotating Needle / Position Marker line extending outward
      val markerStart = center + Offset((vinylInnerRadius * cos(0.0)).toFloat(), 0f)
      val markerEnd = center + Offset((vinylOuterRadius * cos(0.0)).toFloat(), 0f)
      drawLine(
        color = Color(0xFFFF3333),
        start = markerStart,
        end = markerEnd,
        strokeWidth = 3.dp.toPx()
      )
    }

    // 2. Central Static Display (Pioneer DJ Center Display Pod)
    val displaySize = totalSize * 0.56f

    Box(
      modifier = Modifier
        .size(displaySize)
        .shadow(8.dp, CircleShape)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF141414),
              Color(0xFF080808),
              Color(0xFF000000)
            )
          )
        )
        .padding(4.dp),
      contentAlignment = Alignment.Center
    ) {
      // Metallic inner ring
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
          color = Color(0xFF2E2E2E),
          style = Stroke(width = 2.dp.toPx())
        )
        // Rotating position indicator on inner display rim
        val innerRadius = (size.minDimension / 2f) - 3.dp.toPx()
        val rad = Math.toRadians(rotationAngle.toDouble())
        val indicatorPos = Offset(
          (size.width / 2f) + (innerRadius * cos(rad)).toFloat(),
          (size.height / 2f) + (innerRadius * sin(rad)).toFloat()
        )
        drawCircle(
          color = DjCyan,
          radius = 3.5.dp.toPx(),
          center = indicatorPos
        )
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Pioneer DJ",
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          textAlign = TextAlign.Center
        )

        Text(
          text = "BPM",
          color = Color(0xFF888888),
          fontSize = 9.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(top = 2.dp)
        )

        Text(
          text = String.format(java.util.Locale.US, "%.1f", bpm),
          color = DjCyan,
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )

        val totalSec = currentPositionMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val tenths = (currentPositionMs % 1000) / 100
        Text(
          text = String.format(java.util.Locale.US, "%02d:%02d.%d", min, sec, tenths),
          color = Color.White,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold
        )

        Text(
          text = "LOHIM PRO",
          color = DjGoldBright,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }
  }
}

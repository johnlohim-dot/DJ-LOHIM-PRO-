package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DeckKey
import com.example.ui.DeckUiState
import com.example.ui.theme.DjAmber
import com.example.ui.theme.DjBlue
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjDeckBg
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjGoldBright
import com.example.ui.theme.DjGreen
import com.example.ui.theme.DjRed
import com.example.ui.theme.DjSurfaceVariant

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DeckControlPanel(
  deckState: DeckUiState,
  deckTitle: String, // "DECK 1" or "DECK 2"
  accentColor: Color,
  onTogglePlay: () -> Unit,
  onPressCue: () -> Unit,
  onReleaseCue: () -> Unit,
  onAutoSync: () -> Unit,
  onPitchChange: (Float) -> Unit,
  onNudgePitch: (Float) -> Unit,
  onResetPitch: () -> Unit,
  onScrubJogWheel: (Float) -> Unit,
  onSeek: (Long) -> Unit,
  onHotCueClick: (Int, Boolean) -> Unit,
  onLoopClick: (Float) -> Unit,
  onOpenTrackSelector: () -> Unit,
  modifier: Modifier = Modifier,
  isCompact: Boolean = false
) {
  val view = LocalView.current

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(16.dp))
      .testTag("deck_panel_${deckState.deckKey.name.lowercase()}"),
    color = DjDeckBg,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Deck Header: Title, BPM Badge, Track Name & Load Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (deckState.isPlaying) DjGreen else accentColor)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = deckTitle,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            letterSpacing = 1.sp
          )
        }

        // Load Track Button
        FilledTonalButton(
          onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            onOpenTrackSelector()
          },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFF282830),
            contentColor = DjGoldBright
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = ButtonDefaults.ContentPadding,
          modifier = Modifier
            .height(34.dp)
            .testTag("load_track_button_${deckState.deckKey.name.lowercase()}")
        ) {
          Icon(
            imageVector = Icons.Default.Audiotrack,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("LOAD TRACK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      // Track Title & Info
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF1B1B20))
          .padding(horizontal = 8.dp, vertical = 5.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = deckState.track?.title ?: "No Track Loaded",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = deckState.track?.artist ?: "Tap 'Load Track' to select",
              color = Color(0xFF888890),
              fontSize = 10.sp,
              maxLines = 1
            )
          }

          // Live BPM Display Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF0F0F12))
              .border(0.5.dp, DjCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${String.format(java.util.Locale.US, "%.1f", deckState.currentBpm)} BPM",
              color = DjCyan,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Waveform Visualizer
      WaveformView(
        waveform = deckState.track?.waveform ?: emptyList(),
        currentPositionMs = deckState.currentPositionMs,
        durationMs = deckState.durationMs,
        cuePositionMs = deckState.cuePositionMs,
        hotCues = deckState.hotCues,
        onSeek = onSeek,
        primaryColor = accentColor,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      // 2. The Gold Turntable Jog Wheel!
      val jogSize = if (isCompact) 180.dp else 220.dp
      GoldJogWheel(
        bpm = deckState.currentBpm,
        currentPositionMs = deckState.currentPositionMs,
        isPlaying = deckState.isPlaying,
        rotationAngle = deckState.rotationAngle,
        onScrub = onScrubJogWheel,
        deckLabel = deckTitle,
        modifier = Modifier
          .size(jogSize)
          .padding(vertical = 4.dp)
      )

      // 3. Primary Pioneer Transport Controls (PLAY/PAUSE, CUE, AUTO SYNC)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // PLAY / PAUSE BUTTON
        val playBg = if (deckState.isPlaying) DjRed else DjGreen
        Button(
          onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onTogglePlay()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = playBg,
            contentColor = Color.Black
          ),
          shape = CircleShape,
          modifier = Modifier
            .size(54.dp)
            .shadow(6.dp, CircleShape)
            .border(2.dp, if (deckState.isPlaying) Color(0xFFFF8A80) else Color(0xFFB9F6CA), CircleShape)
            .testTag("play_button_${deckState.deckKey.name.lowercase()}")
        ) {
          Icon(
            imageVector = if (deckState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (deckState.isPlaying) "Pause" else "Play",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
          )
        }

        // CUE BUTTON
        Button(
          onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onPressCue()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = DjAmber,
            contentColor = Color.Black
          ),
          shape = CircleShape,
          modifier = Modifier
            .size(50.dp)
            .shadow(4.dp, CircleShape)
            .border(2.dp, Color(0xFFFFD180), CircleShape)
            .testTag("cue_button_${deckState.deckKey.name.lowercase()}")
        ) {
          Text(
            text = "CUE",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = Color.Black
          )
        }

        // AUTO SYNC BUTTON
        Button(
          onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onAutoSync()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = DjGold,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .height(44.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .testTag("sync_button_${deckState.deckKey.name.lowercase()}")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Sync,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "SYNC",
              fontWeight = FontWeight.Black,
              fontSize = 12.sp,
              color = Color.Black
            )
          }
        }
      }

      // 4. Pitch / Tempo Slider & Nudge Controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF19191E))
          .padding(8.dp)
      ) {
        val pitchPercent = ((deckState.pitchRate - 1.0f) * 100f)
        val pitchSign = if (pitchPercent >= 0) "+" else ""

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TEMPO / PITCH",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAAAAAA)
          )

          // Pitch percentage badge
          Text(
            text = "$pitchSign${String.format(java.util.Locale.US, "%.1f", pitchPercent)}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (pitchPercent == 0f) Color.White else DjGoldBright
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Pitch Bend - Nudge
          IconButton(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
              onNudgePitch(-0.005f)
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("pitch_nudge_minus_${deckState.deckKey.name.lowercase()}")
          ) {
            Icon(
              imageVector = Icons.Default.Remove,
              contentDescription = "Pitch down nudge",
              tint = Color.White
            )
          }

          Slider(
            value = deckState.pitchRate,
            onValueChange = onPitchChange,
            valueRange = 0.8f..1.2f,
            colors = SliderDefaults.colors(
              thumbColor = DjGold,
              activeTrackColor = DjGold,
              inactiveTrackColor = Color(0xFF33333C)
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("pitch_slider_${deckState.deckKey.name.lowercase()}")
          )

          // Pitch Bend + Nudge
          IconButton(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
              onNudgePitch(0.005f)
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("pitch_nudge_plus_${deckState.deckKey.name.lowercase()}")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Pitch up nudge",
              tint = Color.White
            )
          }
        }

        // Reset to 0% chip
        if (deckState.pitchRate != 1.0f) {
          Button(
            onClick = {
              view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
              onResetPitch()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF282832),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .height(24.dp)
              .align(Alignment.CenterHorizontally),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text("RESET 0.0%", fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // 5. Hot Cues & Auto Loop Pads
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Hot Cues (1..4)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "HOT CUES (TAP: JUMP, HOLD: SET)",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF888890),
            modifier = Modifier.padding(bottom = 4.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf(0, 1, 2, 3).forEach { index ->
              val cueTime = deckState.hotCues.getOrNull(index)
              val isSet = cueTime != null
              val padColor = when (index) {
                0 -> DjRed
                1 -> DjAmber
                2 -> DjGreen
                else -> DjBlue
              }

              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(34.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .border(
                    width = 1.dp,
                    color = if (isSet) padColor else Color(0xFF33333D),
                    shape = RoundedCornerShape(6.dp)
                  )
                  .combinedClickable(
                    onClick = {
                      view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                      onHotCueClick(index, false)
                    },
                    onLongClick = {
                      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                      onHotCueClick(index, true)
                    }
                  )
                  .testTag("hot_cue_${index + 1}_${deckState.deckKey.name.lowercase()}"),
                color = if (isSet) padColor.copy(alpha = 0.28f) else Color(0xFF1E1E24)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "${index + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isSet) padColor else Color(0xFF777777)
                  )
                }
              }
            }
          }
        }

        // Beat Loops (1/2, 1, 2, 4)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "BEAT LOOP",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF888890),
            modifier = Modifier.padding(bottom = 4.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf(0.5f to "1/2", 1f to "1", 2f to "2", 4f to "4").forEach { (beats, label) ->
              val isLoopActive = deckState.activeLoop == beats

              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(34.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .border(
                    width = 1.dp,
                    color = if (isLoopActive) DjCyan else Color(0xFF33333D),
                    shape = RoundedCornerShape(6.dp)
                  )
                  .combinedClickable(
                    onClick = {
                      view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                      onLoopClick(beats)
                    }
                  )
                  .testTag("loop_${label}_${deckState.deckKey.name.lowercase()}"),
                color = if (isLoopActive) DjCyan.copy(alpha = 0.3f) else Color(0xFF1E1E24)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isLoopActive) DjCyan else Color.White
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

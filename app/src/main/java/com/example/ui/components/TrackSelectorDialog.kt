package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.DeckKey
import com.example.audio.DjTrack
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjGoldBright
import com.example.ui.theme.DjPanelBg

@Composable
fun TrackSelectorDialog(
  deckKey: DeckKey,
  tracks: List<DjTrack>,
  onSelectTrack: (DjTrack) -> Unit,
  onSelectUri: (Uri, String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val view = LocalView.current

  // System Audio File Picker
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      var displayName = "Custom Audio Track"
      try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1 && cursor.moveToFirst()) {
            displayName = cursor.getString(nameIndex) ?: displayName
          }
        }
      } catch (e: Exception) {
        // Fallback name
      }
      onSelectUri(uri, displayName)
      onDismiss()
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = DjPanelBg,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DjGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        .testTag("track_selector_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "LOAD TRACK",
              color = DjGold,
              fontWeight = FontWeight.Black,
              fontSize = 16.sp,
              letterSpacing = 1.sp
            )
            Text(
              text = "Target: DECK ${deckKey.name}",
              color = DjCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_track_selector")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Open Device Audio button
        Button(
          onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            filePickerLauncher.launch("audio/*")
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2E2E38),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(1.dp, DjCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .testTag("pick_device_audio_button")
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = DjCyan,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Browse Device Audio Files",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = DjCyan
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "BUILT-IN DJ LOOPS & STEMS",
          color = Color(0xFFAAAAAA),
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(tracks) { track ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131316))
                .border(1.dp, Color(0xFF282830), RoundedCornerShape(8.dp))
                .clickable {
                  view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                  onSelectTrack(track)
                  onDismiss()
                }
                .padding(10.dp)
                .testTag("track_item_${track.id}"),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(DjGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = DjGoldBright,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = track.title,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "${track.artist} • 8-Bar Beat Loop",
                  color = Color(0xFF888890),
                  fontSize = 11.sp
                )
              }

              // BPM Pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF1E1E26))
                  .border(0.5.dp, DjCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${track.bpm.toInt()} BPM",
                  color = DjCyan,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}

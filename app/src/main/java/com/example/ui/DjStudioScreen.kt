package com.example.ui

import android.content.res.Configuration
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DeckKey
import com.example.ui.components.DeckControlPanel
import com.example.ui.components.MixerSection
import com.example.ui.components.SamplerPads
import com.example.ui.components.TrackSelectorDialog
import com.example.ui.theme.DjAmber
import com.example.ui.theme.DjCyan
import com.example.ui.theme.DjDarkBg
import com.example.ui.theme.DjGold
import com.example.ui.theme.DjGoldBright
import com.example.ui.theme.DjPanelBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DjStudioScreen(
  viewModel: DjViewModel,
  modifier: Modifier = Modifier
) {
  val deckA by viewModel.deckA.collectAsState()
  val deckB by viewModel.deckB.collectAsState()
  val mixer by viewModel.mixer.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val builtInTracks by viewModel.builtInTracks.collectAsState()

  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val view = LocalView.current

  var selectingTrackForDeck by remember { mutableStateOf<DeckKey?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = DjDarkBg,
    contentWindowInsets = WindowInsets.statusBars,
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DjGold)
                .border(1.dp, DjGoldBright, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "DJ LOHIM PRO",
                color = DjGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
              )
              Text(
                text = "PIONEER DJ STUDIO CONSOLE",
                color = Color(0xFF9E9EAA),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }
          }
        },
        actions = {
          // Master Cue / Live indicators
          Row(
            modifier = Modifier
              .padding(end = 12.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1B1B20))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "BPM A: ${deckA.currentBpm.toInt()}",
              color = DjCyan,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "•",
              color = Color.Gray,
              fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "BPM B: ${deckB.currentBpm.toInt()}",
              color = DjAmber,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = DjPanelBg
        )
      )
    },
    bottomBar = {
      // Global Quick Crossfader Bar (always accessible at the bottom)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color(0xFF111114),
        tonalElevation = 8.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "DECK 1 (A)",
              color = DjCyan,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "MASTER CROSSFADER",
              color = DjGoldBright,
              fontSize = 10.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "DECK 2 (B)",
              color = DjAmber,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Slider(
            value = mixer.crossfader,
            onValueChange = { viewModel.setCrossfader(it) },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(
              thumbColor = DjGold,
              activeTrackColor = DjGold,
              inactiveTrackColor = Color(0xFF2B2B36)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(28.dp)
              .testTag("global_bottom_crossfader")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (isLandscape) {
        // Landscape Mode: Side-by-side Dual Decks with Mixer in the center!
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Deck 1 (Left)
          DeckControlPanel(
            deckState = deckA,
            deckTitle = "DECK 1",
            accentColor = DjCyan,
            onTogglePlay = { viewModel.togglePlay(DeckKey.A) },
            onPressCue = { viewModel.pressCue(DeckKey.A) },
            onReleaseCue = { viewModel.releaseCue(DeckKey.A) },
            onAutoSync = { viewModel.autoSync(DeckKey.A) },
            onPitchChange = { viewModel.setPitchRate(DeckKey.A, it) },
            onNudgePitch = { viewModel.nudgePitch(DeckKey.A, it) },
            onResetPitch = { viewModel.resetPitch(DeckKey.A) },
            onScrubJogWheel = { viewModel.onJogWheelScrub(DeckKey.A, it) },
            onSeek = { viewModel.seekTo(DeckKey.A, it) },
            onHotCueClick = { idx, long -> viewModel.handleHotCue(DeckKey.A, idx, long) },
            onLoopClick = { viewModel.toggleBeatLoop(DeckKey.A, it) },
            onOpenTrackSelector = { selectingTrackForDeck = DeckKey.A },
            isCompact = true,
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
          )

          // Center Mixer & Sampler
          Column(
            modifier = Modifier
              .width(280.dp)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            MixerSection(
              mixerState = mixer,
              deckAVolume = deckA.volume,
              deckBVolume = deckB.volume,
              onDeckAVolumeChange = { viewModel.setDeckVolume(DeckKey.A, it) },
              onDeckBVolumeChange = { viewModel.setDeckVolume(DeckKey.B, it) },
              onCrossfaderChange = { viewModel.setCrossfader(it) },
              onMasterVolumeChange = { viewModel.setMasterVolume(it) }
            )

            SamplerPads(
              onTriggerFx = { viewModel.triggerSoundFx(it) }
            )
          }

          // Deck 2 (Right)
          DeckControlPanel(
            deckState = deckB,
            deckTitle = "DECK 2",
            accentColor = DjAmber,
            onTogglePlay = { viewModel.togglePlay(DeckKey.B) },
            onPressCue = { viewModel.pressCue(DeckKey.B) },
            onReleaseCue = { viewModel.releaseCue(DeckKey.B) },
            onAutoSync = { viewModel.autoSync(DeckKey.B) },
            onPitchChange = { viewModel.setPitchRate(DeckKey.B, it) },
            onNudgePitch = { viewModel.nudgePitch(DeckKey.B, it) },
            onResetPitch = { viewModel.resetPitch(DeckKey.B) },
            onScrubJogWheel = { viewModel.onJogWheelScrub(DeckKey.B, it) },
            onSeek = { viewModel.seekTo(DeckKey.B, it) },
            onHotCueClick = { idx, long -> viewModel.handleHotCue(DeckKey.B, idx, long) },
            onLoopClick = { viewModel.toggleBeatLoop(DeckKey.B, it) },
            onOpenTrackSelector = { selectingTrackForDeck = DeckKey.B },
            isCompact = true,
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
          )
        }
      } else {
        // Portrait Mode: Interactive Screen Tabs for Full Control
        Column(modifier = Modifier.fillMaxSize()) {
          // Tab Switcher Bar
          ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color(0xFF141418),
            contentColor = DjGold,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = DjGold
              )
            }
          ) {
            Tab(
              selected = selectedTab == ActiveScreenTab.DUAL_DECKS,
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.setTab(ActiveScreenTab.DUAL_DECKS)
              },
              text = { Text("DUAL DECKS", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
              selectedContentColor = DjGold,
              unselectedContentColor = Color(0xFF888890)
            )
            Tab(
              selected = selectedTab == ActiveScreenTab.DECK_A,
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.setTab(ActiveScreenTab.DECK_A)
              },
              text = { Text("DECK 1 (A)", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
              selectedContentColor = DjCyan,
              unselectedContentColor = Color(0xFF888890)
            )
            Tab(
              selected = selectedTab == ActiveScreenTab.MIXER,
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.setTab(ActiveScreenTab.MIXER)
              },
              text = { Text("MIXER & VU", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
              selectedContentColor = DjGold,
              unselectedContentColor = Color(0xFF888890)
            )
            Tab(
              selected = selectedTab == ActiveScreenTab.DECK_B,
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.setTab(ActiveScreenTab.DECK_B)
              },
              text = { Text("DECK 2 (B)", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
              selectedContentColor = DjAmber,
              unselectedContentColor = Color(0xFF888890)
            )
            Tab(
              selected = selectedTab == ActiveScreenTab.SAMPLER,
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.setTab(ActiveScreenTab.SAMPLER)
              },
              text = { Text("SAMPLER FX", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
              selectedContentColor = DjGold,
              unselectedContentColor = Color(0xFF888890)
            )
          }

          // Tab Content
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
          ) {
            when (selectedTab) {
              ActiveScreenTab.DUAL_DECKS -> {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  DeckControlPanel(
                    deckState = deckA,
                    deckTitle = "DECK 1",
                    accentColor = DjCyan,
                    onTogglePlay = { viewModel.togglePlay(DeckKey.A) },
                    onPressCue = { viewModel.pressCue(DeckKey.A) },
                    onReleaseCue = { viewModel.releaseCue(DeckKey.A) },
                    onAutoSync = { viewModel.autoSync(DeckKey.A) },
                    onPitchChange = { viewModel.setPitchRate(DeckKey.A, it) },
                    onNudgePitch = { viewModel.nudgePitch(DeckKey.A, it) },
                    onResetPitch = { viewModel.resetPitch(DeckKey.A) },
                    onScrubJogWheel = { viewModel.onJogWheelScrub(DeckKey.A, it) },
                    onSeek = { viewModel.seekTo(DeckKey.A, it) },
                    onHotCueClick = { idx, long -> viewModel.handleHotCue(DeckKey.A, idx, long) },
                    onLoopClick = { viewModel.toggleBeatLoop(DeckKey.A, it) },
                    onOpenTrackSelector = { selectingTrackForDeck = DeckKey.A },
                    isCompact = true
                  )

                  MixerSection(
                    mixerState = mixer,
                    deckAVolume = deckA.volume,
                    deckBVolume = deckB.volume,
                    onDeckAVolumeChange = { viewModel.setDeckVolume(DeckKey.A, it) },
                    onDeckBVolumeChange = { viewModel.setDeckVolume(DeckKey.B, it) },
                    onCrossfaderChange = { viewModel.setCrossfader(it) },
                    onMasterVolumeChange = { viewModel.setMasterVolume(it) }
                  )

                  DeckControlPanel(
                    deckState = deckB,
                    deckTitle = "DECK 2",
                    accentColor = DjAmber,
                    onTogglePlay = { viewModel.togglePlay(DeckKey.B) },
                    onPressCue = { viewModel.pressCue(DeckKey.B) },
                    onReleaseCue = { viewModel.releaseCue(DeckKey.B) },
                    onAutoSync = { viewModel.autoSync(DeckKey.B) },
                    onPitchChange = { viewModel.setPitchRate(DeckKey.B, it) },
                    onNudgePitch = { viewModel.nudgePitch(DeckKey.B, it) },
                    onResetPitch = { viewModel.resetPitch(DeckKey.B) },
                    onScrubJogWheel = { viewModel.onJogWheelScrub(DeckKey.B, it) },
                    onSeek = { viewModel.seekTo(DeckKey.B, it) },
                    onHotCueClick = { idx, long -> viewModel.handleHotCue(DeckKey.B, idx, long) },
                    onLoopClick = { viewModel.toggleBeatLoop(DeckKey.B, it) },
                    onOpenTrackSelector = { selectingTrackForDeck = DeckKey.B },
                    isCompact = true
                  )

                  SamplerPads(
                    onTriggerFx = { viewModel.triggerSoundFx(it) }
                  )
                }
              }

              ActiveScreenTab.DECK_A -> {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
                ) {
                  DeckControlPanel(
                    deckState = deckA,
                    deckTitle = "DECK 1 (A)",
                    accentColor = DjCyan,
                    onTogglePlay = { viewModel.togglePlay(DeckKey.A) },
                    onPressCue = { viewModel.pressCue(DeckKey.A) },
                    onReleaseCue = { viewModel.releaseCue(DeckKey.A) },
                    onAutoSync = { viewModel.autoSync(DeckKey.A) },
                    onPitchChange = { viewModel.setPitchRate(DeckKey.A, it) },
                    onNudgePitch = { viewModel.nudgePitch(DeckKey.A, it) },
                    onResetPitch = { viewModel.resetPitch(DeckKey.A) },
                    onScrubJogWheel = { viewModel.onJogWheelScrub(DeckKey.A, it) },
                    onSeek = { viewModel.seekTo(DeckKey.A, it) },
                    onHotCueClick = { idx, long -> viewModel.handleHotCue(DeckKey.A, idx, long) },
                    onLoopClick = { viewModel.toggleBeatLoop(DeckKey.A, it) },
                    onOpenTrackSelector = { selectingTrackForDeck = DeckKey.A },
                    isCompact = false
                  )
                }
              }

              ActiveScreenTab.MIXER -> {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  MixerSection(
                    mixerState = mixer,
                    deckAVolume = deckA.volume,
                    deckBVolume = deckB.volume,
                    onDeckAVolumeChange = { viewModel.setDeckVolume(DeckKey.A, it) },
                    onDeckBVolumeChange = { viewModel.setDeckVolume(DeckKey.B, it) },
                    onCrossfaderChange = { viewModel.setCrossfader(it) },
                    onMasterVolumeChange = { viewModel.setMasterVolume(it) }
                  )

                  SamplerPads(
                    onTriggerFx = { viewModel.triggerSoundFx(it) }
                  )
                }
              }

              ActiveScreenTab.DECK_B -> {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
                ) {
                  DeckControlPanel(
                    deckState = deckB,
                    deckTitle = "DECK 2 (B)",
                    accentColor = DjAmber,
                    onTogglePlay = { viewModel.togglePlay(DeckKey.B) },
                    onPressCue = { viewModel.pressCue(DeckKey.B) },
                    onReleaseCue = { viewModel.releaseCue(DeckKey.B) },
                    onAutoSync = { viewModel.autoSync(DeckKey.B) },
                    onPitchChange = { viewModel.setPitchRate(DeckKey.B, it) },
                    onNudgePitch = { viewModel.nudgePitch(DeckKey.B, it) },
                    onResetPitch = { viewModel.resetPitch(DeckKey.B) },
                    onScrubJogWheel = { viewModel.onJogWheelScrub(DeckKey.B, it) },
                    onSeek = { viewModel.seekTo(DeckKey.B, it) },
                    onHotCueClick = { idx, long -> viewModel.handleHotCue(DeckKey.B, idx, long) },
                    onLoopClick = { viewModel.toggleBeatLoop(DeckKey.B, it) },
                    onOpenTrackSelector = { selectingTrackForDeck = DeckKey.B },
                    isCompact = false
                  )
                }
              }

              ActiveScreenTab.SAMPLER -> {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  SamplerPads(
                    onTriggerFx = { viewModel.triggerSoundFx(it) }
                  )

                  MixerSection(
                    mixerState = mixer,
                    deckAVolume = deckA.volume,
                    deckBVolume = deckB.volume,
                    onDeckAVolumeChange = { viewModel.setDeckVolume(DeckKey.A, it) },
                    onDeckBVolumeChange = { viewModel.setDeckVolume(DeckKey.B, it) },
                    onCrossfaderChange = { viewModel.setCrossfader(it) },
                    onMasterVolumeChange = { viewModel.setMasterVolume(it) }
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Track Selector Modal Dialog
  selectingTrackForDeck?.let { deckKey ->
    TrackSelectorDialog(
      deckKey = deckKey,
      tracks = builtInTracks,
      onSelectTrack = { track ->
        viewModel.loadTrack(deckKey, track)
        selectingTrackForDeck = null
      },
      onSelectUri = { uri, name ->
        viewModel.loadFromUri(deckKey, uri, name)
        selectingTrackForDeck = null
      },
      onDismiss = { selectingTrackForDeck = null }
    )
  }
}

/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.wearable.dat.externalsampleapps.openwebuibridge.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meta.wearable.dat.camera.types.StreamSessionState
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.R
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.StreamViewModel
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.wearables.WearablesViewModel

private enum class BottomTab { CHAT, CONNECTIONS }

@Composable
fun StreamScreen(
    wearablesViewModel: WearablesViewModel,
    modifier: Modifier = Modifier,
    streamViewModel: StreamViewModel =
        viewModel(
            factory =
                StreamViewModel.Factory(
                    application = (LocalActivity.current as ComponentActivity).application,
                    wearablesViewModel = wearablesViewModel,
                ),
        ),
) {
  val streamUiState by streamViewModel.uiState.collectAsStateWithLifecycle()
  val wearablesUiState by wearablesViewModel.uiState.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current
  val isCameraStreaming = streamUiState.streamSessionState == StreamSessionState.STREAMING
  val isCameraStarting = streamUiState.streamSessionState == StreamSessionState.STARTING
  val isCameraEnabled = streamUiState.streamSessionState != StreamSessionState.STOPPED
  var selectedTab by remember { mutableStateOf(BottomTab.CHAT) }

  LaunchedEffect(Unit) { streamViewModel.startStream() }

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        AppHeader(
            onSettings = { selectedTab = BottomTab.CONNECTIONS },
            onNewChat = { streamViewModel.startNewOpenWebUiChat() },
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          when (selectedTab) {
            BottomTab.CHAT ->
                ChatTab(
                    state = streamUiState,
                    onSelectModel = streamViewModel::updateOpenWebUiModel,
                    onOpenModelMenu = streamViewModel::refreshOpenWebUiModels,
                    onSnapshotAsk = { streamViewModel.askOpenWebUiAboutSnapshot() },
                    onToggleCamera = { streamViewModel.toggleCameraStream() },
                    onQuickPrompt = { prompt ->
                      if (isCameraStreaming) {
                        streamViewModel.askOpenWebUiAboutSnapshot(prompt)
                      } else {
                        streamViewModel.updateOpenWebUiPrompt(prompt)
                      }
                    },
                    onMicTap = { streamViewModel.startVoiceAsk() },
                    onStopListening = { streamViewModel.stopVoiceAsk() },
                    onCopyResponse = { response ->
                      clipboardManager.setText(AnnotatedString(response))
                    },
                    onSpeakResponse = { streamViewModel.speakResponse() },
                    onStopSpeakingResponse = { streamViewModel.stopSpeakingResponse() },
                    modifier = Modifier.fillMaxSize(),
                )
            BottomTab.CONNECTIONS ->
                ConnectionsTab(
                    streamViewModel = streamViewModel,
                    state = streamUiState,
                    isCameraStreaming = isCameraStreaming,
                    isCameraEnabled = isCameraEnabled,
                    isBridgeRunning = streamUiState.isBridgeRunning,
                    themeMode = wearablesUiState.appThemeMode,
                    onThemeModeChange = wearablesViewModel::updateAppThemeMode,
                    onToggleCamera = { streamViewModel.toggleCameraStream() },
                    onStopBridge = {
                      streamViewModel.stopStream()
                      wearablesViewModel.navigateToDeviceSelection()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
          }
        }
        BottomNavBar(selected = selectedTab, onSelect = { selectedTab = it })
      }

      if (isCameraStarting) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
    }
  }

  streamUiState.capturedPhoto?.let { _ ->
    if (streamUiState.isShareDialogVisible) {
      SharePhotoDialog(
          photo = streamUiState.capturedPhoto!!,
          onDismiss = { streamViewModel.hideShareDialog() },
          onShare = { bitmap ->
            streamViewModel.sharePhoto(bitmap)
            streamViewModel.hideShareDialog()
          },
      )
    }
  }
}

@Composable
private fun AppHeader(onSettings: () -> Unit, onNewChat: () -> Unit) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
          text = stringResource(R.string.brand_title_primary),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
      )
      val gradient = Brush.linearGradient(
          colors = listOf(AppColor.BrandPurple, AppColor.BrandBlue),
      )
      Text(
          text = buildAnnotatedString {
            withStyle(SpanStyle(brush = gradient, fontWeight = FontWeight.SemiBold)) {
              append(stringResource(R.string.brand_title_accent))
            }
          },
          style = MaterialTheme.typography.titleMedium,
      )
    }
    HeaderIcon(onClick = onNewChat, icon = Icons.Default.Chat)
    Spacer(modifier = Modifier.size(8.dp))
    HeaderIcon(onClick = onSettings, icon = Icons.Default.Hub)
  }
}

@Composable
private fun HeaderIcon(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
  Box(
      modifier = Modifier
          .size(36.dp)
          .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.CircleShape)
          .clickable(onClick = onClick),
      contentAlignment = Alignment.Center,
  ) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(18.dp),
    )
  }
}

@Composable
private fun BottomNavBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
  Surface(
      modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
      color = MaterialTheme.colorScheme.background,
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      NavTabItem(
          label = stringResource(R.string.tab_chat),
          icon = Icons.Default.Chat,
          selected = selected == BottomTab.CHAT,
          onClick = { onSelect(BottomTab.CHAT) },
          modifier = Modifier.weight(1f),
      )
      NavTabItem(
          label = stringResource(R.string.tab_connections),
          icon = Icons.Default.Hub,
          selected = selected == BottomTab.CONNECTIONS,
          onClick = { onSelect(BottomTab.CONNECTIONS) },
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val tint = if (selected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.onSurfaceVariant
  Column(
      modifier = modifier
          .clickable(onClick = onClick)
          .padding(vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(22.dp),
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = tint,
    )
  }
}

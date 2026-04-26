/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.wearable.dat.externalsampleapps.openwebuibridge.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meta.wearable.dat.camera.types.StreamSessionState
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.R
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.StreamUiState

@Composable
fun ChatTab(
    state: StreamUiState,
    onSelectModel: (String) -> Unit,
    onOpenModelMenu: () -> Unit,
    onSnapshotAsk: () -> Unit,
    onToggleCamera: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onMicTap: () -> Unit,
    onStopListening: () -> Unit,
    onCopyResponse: (String) -> Unit,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      ConnectionStatusPill(
          isConnected = state.isBridgeRunning && state.openWebUiBaseUrl.isNotBlank(),
      )
      ModelCard(
          model = state.openWebUiModel,
          availableModels = state.openWebUiModels,
          loadingModels = state.isLoadingOpenWebUiModels,
          onSelectModel = onSelectModel,
          onOpenMenu = onOpenModelMenu,
          ready = state.openWebUiModel.isNotBlank(),
          modifier = Modifier.weight(1f),
      )
    }

    Column(
        modifier = Modifier
            .weight(1f, fill = true)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      ChatMessages(
          state = state,
          onCopyResponse = onCopyResponse,
          onSpeakResponse = onSpeakResponse,
          onStopSpeakingResponse = onStopSpeakingResponse,
      )
    }

    InteractionMenu(
        listening = state.isListeningForVoice,
        streamState = state.streamSessionState,
        busy = state.isAskingOpenWebUi,
        capturing = state.isCapturing,
        onSpeak = if (state.isListeningForVoice) onStopListening else onMicTap,
        onToggleCamera = onToggleCamera,
        onSnapshotAsk = onSnapshotAsk,
    )
  }
}

@Composable
private fun ConnectionStatusPill(isConnected: Boolean) {
  Surface(
      shape = RoundedCornerShape(999.dp),
      color = if (isConnected) AppColor.Green.copy(alpha = 0.12f)
          else MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isConnected) AppColor.Green.copy(alpha = 0.28f)
          else MaterialTheme.colorScheme.outline,
      ),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
          modifier = Modifier
              .size(8.dp)
              .background(
                  color = if (isConnected) AppColor.Green else MaterialTheme.colorScheme.outline,
                  shape = CircleShape,
              ),
      )
      Text(
          text = stringResource(
              if (isConnected) R.string.status_connected else R.string.status_disconnected
          ),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
private fun ModelCard(
    model: String,
    availableModels: List<String>,
    loadingModels: Boolean,
    onSelectModel: (String) -> Unit,
    onOpenMenu: () -> Unit,
    ready: Boolean,
    modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  Surface(
      modifier = modifier,
      shape = RoundedCornerShape(999.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Box {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .clickable {
                menuExpanded = true
                onOpenMenu()
              }
              .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = model.ifBlank { stringResource(R.string.openwebui_model_not_selected) },
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
          )
        }
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
      ) {
        when {
          loadingModels -> {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.openwebui_loading_models)) },
                onClick = {},
                enabled = false,
            )
          }
          availableModels.isEmpty() -> {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.openwebui_no_models_available)) },
                onClick = {},
                enabled = false,
            )
          }
          else -> {
            availableModels.forEach { m ->
              DropdownMenuItem(
                  text = { Text(m) },
                  onClick = {
                    onSelectModel(m)
                    menuExpanded = false
                  },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CameraActionRow(
    streamState: StreamSessionState,
    busy: Boolean,
    onSnapshotAsk: () -> Unit,
    onToggleCamera: () -> Unit,
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CompactActionChip(
        label =
            when (streamState) {
              StreamSessionState.STREAMING -> stringResource(R.string.camera_stream_running_short)
              StreamSessionState.STARTING -> stringResource(R.string.bridge_camera_starting)
              else -> stringResource(R.string.camera_stream_off_short)
            },
        icon =
            if (streamState == StreamSessionState.STREAMING) Icons.Default.Videocam
            else Icons.Default.VideocamOff,
        enabled = streamState != StreamSessionState.STARTING,
        onClick = onToggleCamera,
        modifier = if (streamState == StreamSessionState.STREAMING) Modifier.weight(1f) else Modifier.fillMaxWidth(),
    )
    if (streamState == StreamSessionState.STREAMING) {
      CompactActionChip(
          label = stringResource(R.string.openwebui_snapshot_ask_short),
          icon = Icons.Default.CameraAlt,
          enabled = !busy,
          onClick = onSnapshotAsk,
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun CompactActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier = modifier.clickable(enabled = enabled, onClick = onClick),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp),
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
      )
    }
  }
}

@Composable
fun BrandAvatar(size: androidx.compose.ui.unit.Dp) {
  Box(
      modifier = Modifier
          .size(size)
          .background(
              brush = Brush.linearGradient(
                  colors = listOf(AppColor.BrandPurple, AppColor.BrandBlue),
              ),
              shape = RoundedCornerShape(percent = 30),
          ),
      contentAlignment = Alignment.Center,
  ) {
    Text(
        text = "OI",
        color = Color.White,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun ChatMessages(
    state: StreamUiState,
    onCopyResponse: (String) -> Unit,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
) {
  val hasConversation =
      state.openWebUiResponse != null ||
          state.openWebUiError != null ||
          state.isAskingOpenWebUi ||
          state.voiceTranscript != null

  if (!hasConversation) {
    EmptyConversation()
    return
  }

  val userText = state.voiceTranscript ?: state.openWebUiPrompt
  if (userText.isNotBlank()) {
    UserBubble(text = userText)
  }

  when {
    state.isAskingOpenWebUi -> AssistantBubble(text = "Thinking...")
    state.openWebUiError != null -> ErrorBubble(text = state.openWebUiError)
    state.openWebUiResponse != null ->
        AssistantBubble(
            text = state.openWebUiResponse,
            isSpeaking = state.isSpeakingResponse,
            onCopyResponse = { onCopyResponse(state.openWebUiResponse) },
            onSpeakResponse = onSpeakResponse,
            onStopSpeakingResponse = onStopSpeakingResponse,
        )
  }
}

@Composable
private fun EmptyConversation() {
  Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = AppColor.BrandPurpleSoft,
        modifier = Modifier.size(28.dp),
    )
    Text(
        text = "Ask anything about what you see.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun UserBubble(text: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Surface(
          modifier = Modifier.widthIn(max = 280.dp),
          shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
          color = AppColor.BrandPurple.copy(alpha = 0.18f),
          border = androidx.compose.foundation.BorderStroke(
              1.dp, AppColor.BrandPurple.copy(alpha = 0.35f)
          ),
      ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = AppColor.BrandPurple,
          modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun AssistantBubble(
    text: String,
    isSpeaking: Boolean = false,
    onCopyResponse: (() -> Unit)? = null,
    onSpeakResponse: (() -> Unit)? = null,
    onStopSpeakingResponse: (() -> Unit)? = null,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
      Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = AppColor.Green,
          modifier = Modifier.size(18.dp).padding(top = 6.dp),
      )
      Spacer(modifier = Modifier.size(6.dp))
      Surface(
          modifier = Modifier.widthIn(max = 280.dp),
          shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
      ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
    if (onCopyResponse != null && onSpeakResponse != null && onStopSpeakingResponse != null) {
      ResponseActionRow(
          isSpeaking = isSpeaking,
          onCopyResponse = onCopyResponse,
          onSpeakResponse = onSpeakResponse,
          onStopSpeakingResponse = onStopSpeakingResponse,
      )
    }
  }
}

@Composable
private fun ResponseActionRow(
    isSpeaking: Boolean,
    onCopyResponse: () -> Unit,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CompactActionChip(
        label = stringResource(R.string.copy_response),
        icon = Icons.Default.ContentCopy,
        enabled = true,
        onClick = onCopyResponse,
        modifier = Modifier.weight(1f),
    )
    CompactActionChip(
        label = stringResource(if (isSpeaking) R.string.stop_speaking_response else R.string.speak_response),
        icon = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
        enabled = true,
        onClick = if (isSpeaking) onStopSpeakingResponse else onSpeakResponse,
        modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun ErrorBubble(text: String) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.errorContainer,
  ) {
    Text(
        text = text,
        modifier = Modifier.padding(12.dp),
        color = MaterialTheme.colorScheme.onErrorContainer,
        style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun QuickActionRow(enabled: Boolean, onQuickPrompt: (String) -> Unit) {
  val summarize = stringResource(R.string.prompt_summarize)
  val explain = stringResource(R.string.prompt_explain)
  val translate = stringResource(R.string.prompt_translate)
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    QuickChip(
        label = stringResource(R.string.quick_summarize),
        enabled = enabled,
        onClick = { onQuickPrompt(summarize) },
        modifier = Modifier.weight(1f),
    )
    QuickChip(
        label = stringResource(R.string.quick_explain),
        enabled = enabled,
        onClick = { onQuickPrompt(explain) },
        modifier = Modifier.weight(1f),
    )
    QuickChip(
        label = stringResource(R.string.quick_translate),
        enabled = enabled,
        onClick = { onQuickPrompt(translate) },
        modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun QuickChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier = modifier
          .height(36.dp)
          .clickable(enabled = enabled, onClick = onClick),
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = if (enabled) MaterialTheme.colorScheme.onSurface
              else MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
      )
    }
  }
}

@Composable
private fun InteractionMenu(
    listening: Boolean,
    streamState: StreamSessionState,
    busy: Boolean,
    capturing: Boolean,
    onSpeak: () -> Unit,
    onToggleCamera: () -> Unit,
    onSnapshotAsk: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val transition = rememberInfiniteTransition(label = "interact-pulse")
  val pulse by transition.animateFloat(
      initialValue = 1f,
      targetValue = if (listening) 1.12f else 1f,
      animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 700),
          repeatMode = RepeatMode.Reverse,
      ),
      label = "interact-pulse",
  )
  val streamEnabled = streamState != StreamSessionState.STARTING
  val snapshotEnabled = streamState == StreamSessionState.STREAMING && !busy && !capturing

  Column(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (expanded) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        InteractionMenuButton(
            label = stringResource(if (listening) R.string.stop_speaking_response else R.string.interact_speak),
            icon = if (listening) Icons.Default.Stop else Icons.Default.Mic,
            enabled = !busy || listening,
            onClick = {
              onSpeak()
              if (!listening) expanded = false
            },
            modifier = Modifier.weight(1f),
        )
        InteractionMenuButton(
            label = stringResource(R.string.interact_stream),
            icon = if (streamState == StreamSessionState.STREAMING) Icons.Default.Videocam else Icons.Default.VideocamOff,
            enabled = streamEnabled,
            onClick = {
              onToggleCamera()
              expanded = false
            },
            modifier = Modifier.weight(1f),
        )
        InteractionMenuButton(
            label = stringResource(R.string.interact_snapshot),
            icon = Icons.Default.CameraAlt,
            enabled = snapshotEnabled,
            onClick = {
              onSnapshotAsk()
              expanded = false
            },
            modifier = Modifier.weight(1f),
        )
      }
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(if (listening) pulse else 1f)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(AppColor.BrandPurple, AppColor.BrandBlue),
                ),
                shape = CircleShape,
            )
            .border(2.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable(enabled = !busy || listening) { expanded = !expanded },
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = if (listening) Icons.Default.Stop else Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(32.dp),
      )
    }
    Text(
        text = stringResource(R.string.tap_to_interact),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun InteractionMenuButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier = modifier
          .height(64.dp)
          .clickable(enabled = enabled, onClick = onClick),
      shape = RoundedCornerShape(22.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (enabled) AppColor.BrandPurple.copy(alpha = 0.28f)
          else MaterialTheme.colorScheme.outline,
      ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (enabled) MaterialTheme.colorScheme.onSurface
              else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = if (enabled) MaterialTheme.colorScheme.onSurface
              else MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
      )
    }
  }
}

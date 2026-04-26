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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.R
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.StreamUiState

@Composable
fun ChatTab(
    state: StreamUiState,
    onSelectModel: (String) -> Unit,
    onQuickPrompt: (String) -> Unit,
    onMicTap: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    ConnectionStatusCard(
        isConnected = state.isBridgeRunning && state.openWebUiBaseUrl.isNotBlank(),
        endpoint = state.openWebUiBaseUrl.ifBlank { stringResource(R.string.openwebui_base_url) },
    )

    ModelCard(
        model = state.openWebUiModel,
        availableModels = state.openWebUiModels,
        onSelectModel = onSelectModel,
        ready = state.openWebUiModel.isNotBlank(),
    )

    Column(
        modifier = Modifier
            .weight(1f, fill = true)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      ChatMessages(state = state)
    }

    QuickActionRow(
        enabled = !state.isAskingOpenWebUi,
        onQuickPrompt = onQuickPrompt,
    )

    MicCenterpiece(
        listening = state.isListeningForVoice,
        busy = state.isAskingOpenWebUi,
        onTap = if (state.isListeningForVoice) onStopListening else onMicTap,
    )
  }
}

@Composable
private fun ConnectionStatusCard(isConnected: Boolean, endpoint: String) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = stringResource(
                if (isConnected) R.string.status_connected else R.string.status_disconnected
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = endpoint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun ModelCard(
    model: String,
    availableModels: List<String>,
    onSelectModel: (String) -> Unit,
    ready: Boolean,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Box {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .clickable(enabled = availableModels.isNotEmpty()) { menuExpanded = true }
              .padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        BrandAvatar(size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = model.ifBlank { stringResource(R.string.openwebui_model_not_selected) },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
          )
          Text(
              text = stringResource(
                  if (ready) R.string.model_ready_caption else R.string.model_select_caption
              ),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun ChatMessages(state: StreamUiState) {
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
    state.openWebUiResponse != null -> AssistantBubble(text = state.openWebUiResponse)
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
private fun AssistantBubble(text: String) {
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
private fun MicCenterpiece(listening: Boolean, busy: Boolean, onTap: () -> Unit) {
  val transition = rememberInfiniteTransition(label = "mic-pulse")
  val pulse by transition.animateFloat(
      initialValue = 1f,
      targetValue = if (listening) 1.12f else 1f,
      animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 700),
          repeatMode = RepeatMode.Reverse,
      ),
      label = "mic-pulse",
  )

  Column(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
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
            .clickable(enabled = !busy, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = if (listening) Icons.Default.Stop else Icons.Default.Mic,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(32.dp),
      )
    }
    Text(
        text = stringResource(R.string.tap_to_speak),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.say_hey_meta),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

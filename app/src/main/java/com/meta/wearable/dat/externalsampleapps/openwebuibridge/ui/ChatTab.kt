/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.wearable.dat.externalsampleapps.openwebuibridge.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import com.meta.wearable.dat.camera.types.StreamSessionState
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.R
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.openwebui.OpenWebUiChatMessage
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.openwebui.OpenWebUiChatSummary
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.StreamUiState
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ChatTab(
    state: StreamUiState,
    onSelectModel: (String) -> Unit,
    onOpenModelMenu: () -> Unit,
    onSelectChat: (OpenWebUiChatSummary) -> Unit,
    onOpenChatMenu: () -> Unit,
    onNewChat: () -> Unit,
    isCameraAvailable: Boolean,
    onSnapshotAsk: () -> Unit,
    onToggleCamera: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onMicTap: () -> Unit,
    onStopListening: () -> Unit,
    onCopyResponse: (String) -> Unit,
    onDownloadFile: (String, String, String) -> Unit,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
    modifier: Modifier = Modifier,
) {
  var previewImage by remember { mutableStateOf<ImagePreview?>(null) }
  val messageScrollState = rememberScrollState()
  LaunchedEffect(
      state.openWebUiChatId,
      state.openWebUiChatMessages.size,
      state.isLoadingOpenWebUiChatHistory,
      state.isAskingOpenWebUi,
      state.openWebUiResponse,
      state.openWebUiError,
  ) {
    if (!state.isLoadingOpenWebUiChatHistory) {
      messageScrollState.animateScrollTo(messageScrollState.maxValue)
    }
  }
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
    ChatPickerRow(
        state = state,
        onSelectChat = onSelectChat,
        onOpenChatMenu = onOpenChatMenu,
        onNewChat = onNewChat,
    )

    Column(
        modifier = Modifier
            .weight(1f, fill = true)
            .fillMaxWidth()
            .verticalScroll(messageScrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      ChatMessages(
          state = state,
          onCopyResponse = onCopyResponse,
          onDownloadFile = onDownloadFile,
          onOpenImage = { bitmap, label -> previewImage = ImagePreview(bitmap, label) },
          onSpeakResponse = onSpeakResponse,
          onStopSpeakingResponse = onStopSpeakingResponse,
      )
    }

    InteractionMenu(
        listening = state.isListeningForVoice,
        streamState = state.streamSessionState,
        cameraAvailable = isCameraAvailable,
        busy = state.isAskingOpenWebUi,
        capturing = state.isCapturing,
        onSpeak = if (state.isListeningForVoice) onStopListening else onMicTap,
        onToggleCamera = onToggleCamera,
        onSnapshotAsk = onSnapshotAsk,
    )
  }

  previewImage?.let { image ->
    FullscreenImageDialog(image = image, onDismiss = { previewImage = null })
  }
}

@Composable
private fun ChatPickerRow(
    state: StreamUiState,
    onSelectChat: (OpenWebUiChatSummary) -> Unit,
    onOpenChatMenu: () -> Unit,
    onNewChat: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
      Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  menuExpanded = true
                  onOpenChatMenu()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
          )
          Text(
              text = currentChatTitle(state),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
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
            state.isLoadingOpenWebUiChats -> {
              DropdownMenuItem(
                  text = { Text(stringResource(R.string.openwebui_loading_chats)) },
                  onClick = {},
                  enabled = false,
              )
            }
            state.openWebUiChats.isEmpty() -> {
              DropdownMenuItem(
                  text = { Text(stringResource(R.string.openwebui_no_chats_available)) },
                  onClick = {},
                  enabled = false,
              )
            }
            else -> {
              state.openWebUiChats.forEach { chat ->
                DropdownMenuItem(
                    text = { Text(chat.title, maxLines = 1) },
                    onClick = {
                      onSelectChat(chat)
                      menuExpanded = false
                    },
                )
              }
            }
          }
        }
      }
    }
    CompactActionChip(
        label = stringResource(R.string.openwebui_new_chat_short),
        icon = Icons.Default.Add,
        enabled = true,
        onClick = onNewChat,
        modifier = Modifier.widthIn(min = 88.dp, max = 112.dp),
    )
  }
}

@Composable
private fun currentChatTitle(state: StreamUiState): String =
    when {
      state.openWebUiChatTitle.isNotBlank() -> state.openWebUiChatTitle
      state.openWebUiChatId.isNotBlank() -> stringResource(R.string.openwebui_chat_id, state.openWebUiChatId.take(8))
      else -> stringResource(R.string.openwebui_new_chat_selected)
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
    onDownloadFile: (String, String, String) -> Unit,
    onOpenImage: (Bitmap, String) -> Unit,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
) {
  val hasConversation =
      state.openWebUiChatMessages.isNotEmpty() ||
          state.openWebUiResponse != null ||
          state.openWebUiError != null ||
          state.isAskingOpenWebUi ||
          state.voiceTranscript != null ||
          state.isLoadingOpenWebUiChatHistory

  if (!hasConversation) {
    EmptyConversation()
    return
  }

  if (state.isLoadingOpenWebUiChatHistory) {
    LoadingHistory()
  }

  val lastAssistantMessage = state.openWebUiChatMessages.lastOrNull { it.role == "assistant" }
  state.openWebUiChatMessages.forEach { message ->
    ChatHistoryBubble(
        message = message,
        baseUrl = state.openWebUiBaseUrl,
        apiKey = state.openWebUiApiKey,
        isLastAssistant = message.id == lastAssistantMessage?.id,
        isSpeaking = state.isSpeakingResponse,
        onCopyResponse = onCopyResponse,
        onDownloadFile = onDownloadFile,
        onOpenImage = onOpenImage,
        onSpeakResponse = onSpeakResponse,
        onStopSpeakingResponse = onStopSpeakingResponse,
    )
  }

  when {
    state.isAskingOpenWebUi -> {
      val userText = state.voiceTranscript ?: state.openWebUiPrompt
      if (userText.isNotBlank()) {
        UserBubble(
            text = userText,
            baseUrl = state.openWebUiBaseUrl,
            apiKey = state.openWebUiApiKey,
            onCopyText = onCopyResponse,
            onDownloadFile = onDownloadFile,
            onOpenImage = onOpenImage,
        )
      }
      AssistantBubble(text = stringResource(R.string.openwebui_thinking))
    }
    state.openWebUiError != null -> ErrorBubble(text = state.openWebUiError, onCopyText = onCopyResponse)
    state.openWebUiChatMessages.isEmpty() && state.openWebUiResponse != null ->
        AssistantBubble(
            text = state.openWebUiResponse,
            baseUrl = state.openWebUiBaseUrl,
            apiKey = state.openWebUiApiKey,
            isSpeaking = state.isSpeakingResponse,
            onCopyText = onCopyResponse,
            onDownloadFile = onDownloadFile,
            onOpenImage = onOpenImage,
            onSpeakResponse = onSpeakResponse,
            onStopSpeakingResponse = onStopSpeakingResponse,
        )
  }
}

@Composable
private fun ChatHistoryBubble(
    message: OpenWebUiChatMessage,
    baseUrl: String,
    apiKey: String,
    isLastAssistant: Boolean,
    isSpeaking: Boolean,
    onCopyResponse: (String) -> Unit,
    onDownloadFile: (String, String, String) -> Unit,
    onOpenImage: (Bitmap, String) -> Unit,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
) {
  when (message.role) {
    "user" ->
        UserBubble(
            text = message.content,
            baseUrl = baseUrl,
            apiKey = apiKey,
            onCopyText = onCopyResponse,
            onDownloadFile = onDownloadFile,
            onOpenImage = onOpenImage,
        )
    "assistant" ->
        AssistantBubble(
            text = message.content,
            baseUrl = baseUrl,
            apiKey = apiKey,
            isSpeaking = isSpeaking && isLastAssistant,
            onCopyText = onCopyResponse,
            onDownloadFile = onDownloadFile,
            onOpenImage = onOpenImage,
            onSpeakResponse = if (isLastAssistant) onSpeakResponse else null,
            onStopSpeakingResponse = if (isLastAssistant) onStopSpeakingResponse else null,
        )
  }
}

@Composable
private fun LoadingHistory() {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
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
private fun UserBubble(
    text: String,
    baseUrl: String = "",
    apiKey: String = "",
    onCopyText: ((String) -> Unit)? = null,
    onDownloadFile: ((String, String, String) -> Unit)? = null,
    onOpenImage: ((Bitmap, String) -> Unit)? = null,
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Surface(
          modifier = Modifier.widthIn(max = 280.dp).copyOnLongPress(text, onCopyText),
          shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
          color = AppColor.BrandPurple.copy(alpha = 0.18f),
          border = androidx.compose.foundation.BorderStroke(
              1.dp, AppColor.BrandPurple.copy(alpha = 0.35f)
          ),
      ) {
        MarkdownMessageContent(
            text = text,
            baseUrl = baseUrl,
            apiKey = apiKey,
            onDownloadFile = onDownloadFile,
            onOpenImage = onOpenImage,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
    baseUrl: String = "",
    apiKey: String = "",
    isSpeaking: Boolean = false,
    onCopyText: ((String) -> Unit)? = null,
    onDownloadFile: ((String, String, String) -> Unit)? = null,
    onOpenImage: ((Bitmap, String) -> Unit)? = null,
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
          modifier = Modifier.widthIn(max = 280.dp).copyOnLongPress(text, onCopyText),
          shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
      ) {
        MarkdownMessageContent(
            text = text,
            baseUrl = baseUrl,
            apiKey = apiKey,
            onDownloadFile = onDownloadFile,
            onOpenImage = onOpenImage,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
      }
    }
    if (onSpeakResponse != null && onStopSpeakingResponse != null) {
      ResponseActionRow(
          isSpeaking = isSpeaking,
          onSpeakResponse = onSpeakResponse,
          onStopSpeakingResponse = onStopSpeakingResponse,
      )
    }
  }
}

@Composable
private fun ResponseActionRow(
    isSpeaking: Boolean,
    onSpeakResponse: () -> Unit,
    onStopSpeakingResponse: () -> Unit,
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CompactActionChip(
        label = stringResource(if (isSpeaking) R.string.stop_speaking_response else R.string.speak_response),
        icon = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
        enabled = true,
        onClick = if (isSpeaking) onStopSpeakingResponse else onSpeakResponse,
        modifier = Modifier.weight(1f),
    )
  }
}

private fun Modifier.copyOnLongPress(
    text: String,
    onCopyText: ((String) -> Unit)?,
): Modifier =
    if (text.isBlank() || onCopyText == null) {
      this
    } else {
      pointerInput(text, onCopyText) {
        detectTapGestures(onLongPress = { onCopyText(text) })
      }
    }

private enum class MarkdownFileType { IMAGE, VIDEO, AUDIO, FILE }

private sealed interface MarkdownPart {
  data class Text(val value: String) : MarkdownPart

  data class File(
      val label: String,
      val url: String,
      val type: MarkdownFileType,
      val mimeType: String,
  ) : MarkdownPart
}

private data class ImagePreview(
    val bitmap: Bitmap,
    val label: String,
)

@Composable
private fun MarkdownMessageContent(
    text: String,
    baseUrl: String,
    apiKey: String,
    onDownloadFile: ((String, String, String) -> Unit)?,
    onOpenImage: ((Bitmap, String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
  val parts = remember(text) { parseMarkdownParts(text) }
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    parts.forEach { part ->
      when (part) {
        is MarkdownPart.Text ->
            if (part.value.isNotBlank()) {
              Text(
                  text = part.value.trim(),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
              )
            }
        is MarkdownPart.File ->
            MarkdownFileAttachment(
                file = part,
                absoluteUrl = absoluteUrl(baseUrl, part.url),
                apiKey = apiKey,
                onDownloadFile = onDownloadFile,
                onOpenImage = onOpenImage,
            )
      }
    }
  }
}

@Composable
private fun MarkdownFileAttachment(
    file: MarkdownPart.File,
    absoluteUrl: String,
    apiKey: String,
    onDownloadFile: ((String, String, String) -> Unit)?,
    onOpenImage: ((Bitmap, String) -> Unit)?,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    if (file.type == MarkdownFileType.IMAGE) {
      val bitmap by produceState<Bitmap?>(initialValue = null, absoluteUrl, apiKey) {
        value = loadBitmap(absoluteUrl, apiKey)
      }
      if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = file.label,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable(enabled = onOpenImage != null) {
                      onOpenImage?.invoke(bitmap!!, file.label)
                    },
            contentScale = ContentScale.Crop,
        )
      }
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            imageVector =
                when (file.type) {
                  MarkdownFileType.IMAGE -> Icons.Default.Image
                  MarkdownFileType.VIDEO -> Icons.Default.Movie
                  MarkdownFileType.AUDIO -> Icons.Default.MusicNote
                  MarkdownFileType.FILE -> Icons.Default.InsertDriveFile
                },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = file.label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = stringResource(R.string.download_file),
            tint = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .size(22.dp)
                    .clickable(enabled = onDownloadFile != null) {
                      onDownloadFile?.invoke(absoluteUrl, file.label, file.mimeType)
                    },
        )
      }
    }
  }
}

@Composable
private fun FullscreenImageDialog(
    image: ImagePreview,
    onDismiss: () -> Unit,
) {
  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
      Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = image.bitmap.asImageBitmap(),
            contentDescription = image.label,
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentScale = ContentScale.Fit,
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        ) {
          Icon(
              imageVector = Icons.Default.Close,
              contentDescription = stringResource(R.string.close_image_preview),
              tint = Color.White,
          )
        }
      }
    }
  }
}

private fun parseMarkdownParts(text: String): List<MarkdownPart> {
  val regex = Regex("""(!?)\[([^\]]*)]\(([^)\s]+)(?:\s+"[^"]*")?\)""")
  val parts = mutableListOf<MarkdownPart>()
  var cursor = 0
  regex.findAll(text).forEach { match ->
    if (match.range.first > cursor) {
      parts += MarkdownPart.Text(text.substring(cursor, match.range.first))
    }
    val label = match.groupValues[2].ifBlank { fileNameFromUrl(match.groupValues[3]) }
    val url = match.groupValues[3]
    val fileName = label.ifBlank { fileNameFromUrl(url) }
    val type = markdownFileType(fileName, url, match.groupValues[1] == "!")
    parts += MarkdownPart.File(
        label = fileName.ifBlank { type.name.lowercase() },
        url = url,
        type = type,
        mimeType = mimeTypeFor(type, fileName, url),
    )
    cursor = match.range.last + 1
  }
  if (cursor < text.length) {
    parts += MarkdownPart.Text(text.substring(cursor))
  }
  return parts.ifEmpty { listOf(MarkdownPart.Text(text)) }
}

private fun markdownFileType(label: String, url: String, isImageSyntax: Boolean): MarkdownFileType {
  val source = "$label $url".lowercase()
  return when {
    isImageSyntax || source.endsWith(".png") || source.endsWith(".jpg") || source.endsWith(".jpeg") ||
        source.endsWith(".gif") || source.endsWith(".webp") -> MarkdownFileType.IMAGE
    source.endsWith(".mp4") || source.endsWith(".mov") || source.endsWith(".webm") ||
        source.endsWith(".mkv") -> MarkdownFileType.VIDEO
    source.endsWith(".mp3") || source.endsWith(".wav") || source.endsWith(".m4a") ||
        source.endsWith(".ogg") || source.endsWith(".flac") -> MarkdownFileType.AUDIO
    else -> MarkdownFileType.FILE
  }
}

private fun mimeTypeFor(type: MarkdownFileType, label: String, url: String): String {
  val source = "$label $url".lowercase()
  return when {
    source.contains(".png") -> "image/png"
    source.contains(".gif") -> "image/gif"
    source.contains(".webp") -> "image/webp"
    source.contains(".jpg") || source.contains(".jpeg") -> "image/jpeg"
    source.contains(".mp4") -> "video/mp4"
    source.contains(".mov") -> "video/quicktime"
    source.contains(".webm") -> "video/webm"
    source.contains(".mp3") -> "audio/mpeg"
    source.contains(".wav") -> "audio/wav"
    source.contains(".m4a") -> "audio/mp4"
    type == MarkdownFileType.IMAGE -> "image/*"
    type == MarkdownFileType.VIDEO -> "video/*"
    type == MarkdownFileType.AUDIO -> "audio/*"
    else -> "application/octet-stream"
  }
}

private fun fileNameFromUrl(url: String): String =
    url.substringBefore("?").substringAfterLast("/").ifBlank { "file" }

private fun absoluteUrl(baseUrl: String, url: String): String {
  val trimmed = url.trim()
  if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:")) {
    return trimmed
  }
  val root = baseUrl.trim().trimEnd('/').removeSuffix("/api/v1").removeSuffix("/api")
  return if (trimmed.startsWith("/")) "$root$trimmed" else "$root/$trimmed"
}

private suspend fun loadBitmap(url: String, apiKey: String): Bitmap? =
    withContext(Dispatchers.IO) {
      runCatching {
            if (url.startsWith("data:image")) {
              val encoded = url.substringAfter("base64,", "")
              val bytes = Base64.decode(encoded, Base64.DEFAULT)
              BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
              val connection =
                  (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    if (apiKey.isNotBlank()) {
                      setRequestProperty("Authorization", "Bearer $apiKey")
                    }
                  }
              try {
                connection.inputStream.use { BitmapFactory.decodeStream(it) }
              } finally {
                connection.disconnect()
              }
            }
          }
          .getOrNull()
    }

@Composable
private fun ErrorBubble(
    text: String,
    onCopyText: ((String) -> Unit)? = null,
) {
  Surface(
      modifier = Modifier.fillMaxWidth().copyOnLongPress(text, onCopyText),
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
    cameraAvailable: Boolean,
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
  val streamEnabled = cameraAvailable && streamState != StreamSessionState.STARTING
  val snapshotEnabled = cameraAvailable && streamState == StreamSessionState.STREAMING && !busy && !capturing

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

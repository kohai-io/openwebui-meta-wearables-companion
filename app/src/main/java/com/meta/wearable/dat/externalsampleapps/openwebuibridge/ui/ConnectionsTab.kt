/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.wearable.dat.externalsampleapps.openwebuibridge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.R
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.SnapshotImageQuality
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.StreamUiState
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.stream.StreamViewModel
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.wearables.AppThemeMode

@Composable
fun ConnectionsTab(
    streamViewModel: StreamViewModel,
    state: StreamUiState,
    isCameraStreaming: Boolean,
    isCameraEnabled: Boolean,
    isBridgeRunning: Boolean,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onToggleCamera: () -> Unit,
    onStopBridge: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    SectionCard(title = "Server") {
      OutlinedTextField(
          value = state.openWebUiBaseUrl,
          onValueChange = streamViewModel::updateOpenWebUiBaseUrl,
          label = { Text(stringResource(R.string.openwebui_base_url)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
          value = state.openWebUiApiKey,
          onValueChange = streamViewModel::updateOpenWebUiApiKey,
          label = { Text(stringResource(R.string.openwebui_api_key)) },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
      )
    }

    SectionCard(title = "Model") {
      var modelMenuExpanded by remember { mutableStateOf(false) }
      OutlinedTextField(
          value = state.openWebUiModel,
          onValueChange = streamViewModel::updateOpenWebUiModel,
          label = { Text(stringResource(R.string.openwebui_model)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
      )
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        TextButton(
            onClick = { streamViewModel.refreshOpenWebUiModels() },
            enabled = !state.isLoadingOpenWebUiModels,
            modifier = Modifier.weight(1f),
        ) {
          Text(
              if (state.isLoadingOpenWebUiModels) stringResource(R.string.openwebui_loading_models)
              else stringResource(R.string.openwebui_load_models)
          )
        }
        Box(modifier = Modifier.weight(1f)) {
          TextButton(
              onClick = { modelMenuExpanded = true },
              enabled = state.openWebUiModels.isNotEmpty(),
              modifier = Modifier.fillMaxWidth(),
          ) {
            Text(stringResource(R.string.openwebui_select_model))
          }
          DropdownMenu(
              expanded = modelMenuExpanded,
              onDismissRequest = { modelMenuExpanded = false },
          ) {
            state.openWebUiModels.forEach { m ->
              DropdownMenuItem(
                  text = { Text(m) },
                  onClick = {
                    streamViewModel.updateOpenWebUiModel(m)
                    modelMenuExpanded = false
                  },
              )
            }
          }
        }
      }
    }

    SectionCard(title = "Glasses") {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SwitchButton(
            label = if (isCameraEnabled)
              stringResource(R.string.stop_camera_stream_button_title)
            else stringResource(R.string.start_camera_stream_button_title),
            onClick = onToggleCamera,
            icon = if (isCameraEnabled)
              Icons.Default.VideocamOff
            else Icons.Default.CameraAlt,
            enabled = isBridgeRunning,
            modifier = Modifier.weight(1f),
        )
        SwitchButton(
            label = stringResource(R.string.stop_bridge_button_title),
            onClick = onStopBridge,
            icon = Icons.Default.StopCircle,
            isDestructive = true,
            modifier = Modifier.weight(1f),
        )
      }
    }

    SectionCard(title = "Behavior") {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = stringResource(R.string.auto_speak_responses),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = state.isAutoSpeakResponseEnabled,
            onCheckedChange = streamViewModel::updateAutoSpeakResponseEnabled,
        )
      }
      var qualityMenuExpanded by remember { mutableStateOf(false) }
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = stringResource(R.string.snapshot_image_quality),
            style = MaterialTheme.typography.bodyLarge,
        )
        Box {
          TextButton(onClick = { qualityMenuExpanded = true }) {
            Text(snapshotImageQualityLabel(state.snapshotImageQuality))
          }
          DropdownMenu(
              expanded = qualityMenuExpanded,
              onDismissRequest = { qualityMenuExpanded = false },
          ) {
            SnapshotImageQuality.values().forEach { q ->
              DropdownMenuItem(
                  text = { Text(snapshotImageQualityLabel(q)) },
                  onClick = {
                    streamViewModel.updateSnapshotImageQuality(q)
                    qualityMenuExpanded = false
                  },
              )
            }
          }
        }
      }
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = stringResource(R.string.app_theme_mode),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          ThemePill(
              label = stringResource(R.string.app_theme_system),
              selected = themeMode == AppThemeMode.SYSTEM,
              onClick = { onThemeModeChange(AppThemeMode.SYSTEM) },
          )
          ThemePill(
              label = stringResource(R.string.app_theme_light),
              selected = themeMode == AppThemeMode.LIGHT,
              onClick = { onThemeModeChange(AppThemeMode.LIGHT) },
          )
          ThemePill(
              label = stringResource(R.string.app_theme_dark),
              selected = themeMode == AppThemeMode.DARK,
              onClick = { onThemeModeChange(AppThemeMode.DARK) },
          )
        }
      }
      TextButton(
          onClick = { streamViewModel.startNewOpenWebUiChat() },
          modifier = Modifier.fillMaxWidth(),
      ) {
        Text(stringResource(R.string.openwebui_new_chat))
      }
    }

    SectionCard(title = "System prompt") {
      OutlinedTextField(
          value = state.openWebUiSystemPrompt,
          onValueChange = streamViewModel::updateOpenWebUiSystemPrompt,
          label = { Text(stringResource(R.string.openwebui_system_prompt)) },
          minLines = 3,
          modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold,
      )
      content()
    }
  }
}

@Composable
private fun ThemePill(label: String, selected: Boolean, onClick: () -> Unit) {
  Surface(
      shape = RoundedCornerShape(10.dp),
      color = if (selected) MaterialTheme.colorScheme.primaryContainer
          else MaterialTheme.colorScheme.surfaceVariant,
      onClick = onClick,
  ) {
    Text(
        text = label,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun snapshotImageQualityLabel(quality: SnapshotImageQuality): String =
    when (quality) {
      SnapshotImageQuality.STANDARD -> stringResource(R.string.snapshot_image_quality_standard)
      SnapshotImageQuality.HIGH -> stringResource(R.string.snapshot_image_quality_high)
      SnapshotImageQuality.ORIGINAL -> stringResource(R.string.snapshot_image_quality_original)
    }

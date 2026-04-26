/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.wearable.dat.externalsampleapps.openwebuibridge.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.meta.wearable.dat.externalsampleapps.openwebuibridge.wearables.AppThemeMode

private val CompanionDarkColors =
    darkColorScheme(
        primary = AppColor.BrandPurple,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2A1F66),
        onPrimaryContainer = AppColor.BrandPurpleSoft,
        secondary = AppColor.BrandBlue,
        onSecondary = Color.White,
        background = AppColor.BackgroundDark,
        onBackground = Color.White,
        surface = AppColor.SurfaceDark,
        onSurface = Color.White,
        surfaceVariant = AppColor.SurfaceDarkVariant,
        onSurfaceVariant = AppColor.OnSurfaceMuted,
        outline = AppColor.OutlineDark,
        outlineVariant = AppColor.OutlineDark,
    )

private val CompanionLightColors =
    lightColorScheme(
        primary = AppColor.BrandPurple,
        secondary = AppColor.BrandBlue,
    )

@Composable
fun OpenWebUIBridgeTheme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit,
) {
  val useDarkTheme =
      when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
      }

  MaterialTheme(
      colorScheme = if (useDarkTheme) CompanionDarkColors else CompanionLightColors,
      content = content,
  )
}

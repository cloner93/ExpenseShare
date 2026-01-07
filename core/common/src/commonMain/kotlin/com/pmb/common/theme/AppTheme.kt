package com.pmb.common.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object AppTheme {
    /**
     * Unified color access that merges Material ColorScheme and AppCustomColors.
     */
    val colors: AppThemeColors
        @Composable
        @ReadOnlyComposable
        get() = AppThemeColors(
            materialColors = MaterialTheme.colorScheme,
            customColors = LocalAppCustomColors.current
        )
    /**
     * Access typography from Material Theme
     */
    val typography
        @Composable
//        @ReadOnlyComposable
        get() = MaterialTheme.typography

    /**
     * Access shapes from Material Theme
     */
    val shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}

data class AppThemeColors(
    private val materialColors: ColorScheme,
    private val customColors: AppCustomColors,
) {
    // Material 3 Standard Colors
    val primary: Color get() = materialColors.primary
    val onPrimary: Color get() = materialColors.onPrimary
    val primaryContainer: Color get() = materialColors.primaryContainer
    val onPrimaryContainer: Color get() = materialColors.onPrimaryContainer

    val secondary: Color get() = materialColors.secondary
    val onSecondary: Color get() = materialColors.onSecondary
    val secondaryContainer: Color get() = materialColors.secondaryContainer
    val onSecondaryContainer: Color get() = materialColors.onSecondaryContainer

    val tertiary: Color get() = materialColors.tertiary
    val onTertiary: Color get() = materialColors.onTertiary
    val tertiaryContainer: Color get() = materialColors.tertiaryContainer
    val onTertiaryContainer: Color get() = materialColors.onTertiaryContainer

    val error: Color get() = materialColors.error
    val onError: Color get() = materialColors.onError
    val errorContainer: Color get() = materialColors.errorContainer
    val onErrorContainer: Color get() = materialColors.onErrorContainer

    val background: Color get() = materialColors.background
    val onBackground: Color get() = materialColors.onBackground

    val surface: Color get() = materialColors.surface
    val onSurface: Color get() = materialColors.onSurface
    val surfaceVariant: Color get() = materialColors.surfaceVariant
    val onSurfaceVariant: Color get() = materialColors.onSurfaceVariant

    val surfaceTint: Color get() = materialColors.surfaceTint
    val inverseSurface: Color get() = materialColors.inverseSurface
    val inverseOnSurface: Color get() = materialColors.inverseOnSurface
    val inversePrimary: Color get() = materialColors.inversePrimary

    val outline: Color get() = materialColors.outline
    val outlineVariant: Color get() = materialColors.outlineVariant
    val scrim: Color get() = materialColors.scrim

    val surfaceBright: Color get() = materialColors.surfaceBright
    val surfaceDim: Color get() = materialColors.surfaceDim
    val surfaceContainer: Color get() = materialColors.surfaceContainer
    val surfaceContainerHigh: Color get() = materialColors.surfaceContainerHigh
    val surfaceContainerHighest: Color get() = materialColors.surfaceContainerHighest
    val surfaceContainerLow: Color get() = materialColors.surfaceContainerLow
    val surfaceContainerLowest: Color get() = materialColors.surfaceContainerLowest

    // Custom Colors
    val success: Color get() = customColors.success
    val onSuccess: Color get() = customColors.onSuccess
    val successContainer: Color get() = customColors.successContainer
    val onSuccessContainer: Color get() = customColors.onSuccessContainer

    // Add more custom color accessors as needed
}
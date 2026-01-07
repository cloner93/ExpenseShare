package com.pmb.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color scheme for custom colors not part of Material 3 ColorScheme.
 * Add any custom color categories here (brand colors, status colors, etc.)
 */
@Immutable
data class AppCustomColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
)

/**
 * Light theme custom colors
 */
val lightCustomColors = AppCustomColors(
    success = successLight,
    onSuccess = onSuccessLight,
    successContainer = successContainerLight,
    onSuccessContainer = onSuccessContainerLight,
)

/**
 * Light Medium Contrast custom colors
 */
val lightMediumContrastCustomColors = AppCustomColors(
    success = successLightMediumContrast,
    onSuccess = onSuccessLightMediumContrast,
    successContainer = successContainerLightMediumContrast,
    onSuccessContainer = onSuccessContainerLightMediumContrast,
)

/**
 * Light High Contrast custom colors
 */
val lightHighContrastCustomColors = AppCustomColors(
    success = successLightHighContrast,
    onSuccess = onSuccessLightHighContrast,
    successContainer = successContainerLightHighContrast,
    onSuccessContainer = onSuccessContainerLightHighContrast,
)

/**
 * Dark theme custom colors
 */
val darkCustomColors = AppCustomColors(
    success = successDark,
    onSuccess = onSuccessDark,
    successContainer = successContainerDark,
    onSuccessContainer = onSuccessContainerDark,
)

/**
 * Dark Medium Contrast custom colors
 */
val darkMediumContrastCustomColors = AppCustomColors(
    success = successDarkMediumContrast,
    onSuccess = onSuccessDarkMediumContrast,
    successContainer = successContainerDarkMediumContrast,
    onSuccessContainer = onSuccessContainerDarkMediumContrast,
)

/**
 * Dark High Contrast custom colors
 */
val darkHighContrastCustomColors = AppCustomColors(
    success = successDarkHighContrast,
    onSuccess = onSuccessDarkHighContrast,
    successContainer = successContainerDarkHighContrast,
    onSuccessContainer = onSuccessContainerDarkHighContrast,
)

/**
 * CompositionLocal for custom colors.
 * Provides a default value to prevent crashes if not provided.
 */
val LocalAppCustomColors = staticCompositionLocalOf {
    lightCustomColors
}
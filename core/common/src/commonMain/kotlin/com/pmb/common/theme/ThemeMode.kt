package com.pmb.common.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * Holds the active [ThemeMode] override for the application.
 */
class ThemeStateHolder(initialMode: ThemeMode = ThemeMode.SYSTEM) {
    private val _themeMode = MutableStateFlow(initialMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
}

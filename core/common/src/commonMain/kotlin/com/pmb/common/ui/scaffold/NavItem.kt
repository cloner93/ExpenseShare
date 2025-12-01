package com.pmb.common.ui.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

enum class NavItem(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Friends("Friends", Icons.Default.People),
    Profile("Profile", Icons.Default.Settings)
}

@Composable
fun calculateAppScreenSize(adaptiveInfo: WindowAdaptiveInfo, windowWidth: Dp) = when {
    adaptiveInfo.windowPosture.isTabletop -> NavigationSuiteType.NavigationBar
    adaptiveInfo.windowSizeClass.isCompact() -> NavigationSuiteType.NavigationBar
    adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED && windowWidth >= 1200.dp -> NavigationSuiteType.NavigationDrawer

    else -> NavigationSuiteType.NavigationRail
}

private fun WindowSizeClass.isCompact() =
    windowWidthSizeClass == WindowWidthSizeClass.COMPACT || windowHeightSizeClass == WindowHeightSizeClass.COMPACT


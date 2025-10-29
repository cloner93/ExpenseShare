// AppScreenSize.kt
package org.milad.expense_share.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class NavItem(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Settings("Settings", Icons.Default.Settings)
}

enum class AppScreenSize {
    Compact,
    Medium,
    Expanded
}

@Composable
fun calculateAppScreenSize(windowWidth: Dp): AppScreenSize {
    return when {
        windowWidth < 600.dp -> AppScreenSize.Compact
        windowWidth < 900.dp -> AppScreenSize.Medium
        else -> AppScreenSize.Expanded
    }
}
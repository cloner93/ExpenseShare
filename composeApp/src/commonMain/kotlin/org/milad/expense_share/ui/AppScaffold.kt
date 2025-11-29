package org.milad.expense_share.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
fun AppScaffold(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    content: @Composable (AppScreenSize) -> Unit,
) {
    BoxWithConstraints {
        val windowWidth: Dp = maxWidth
        val appScreenSize: AppScreenSize = calculateAppScreenSize(windowWidth)

        val customSuiteType = when (appScreenSize) {
            AppScreenSize.Compact -> NavigationSuiteType.ShortNavigationBarCompact
            AppScreenSize.Medium -> NavigationSuiteType.WideNavigationRailCollapsed
            AppScreenSize.Expanded -> NavigationSuiteType.WideNavigationRailExpanded
        }

        NavigationSuiteScaffold(
            layoutType = customSuiteType,
            navigationSuiteItems = {
                NavItem.entries.forEach { navItem ->
                    item(
                        selected = selectedItem == navItem,
                        onClick = { onItemSelected(navItem) },
                        icon = { Icon(navItem.icon, contentDescription = navItem.title) },
                        label = { Text(navItem.title) }
                    )
                }
            },
            navigationSuiteColors = NavigationSuiteDefaults.colors(
                shortNavigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                shortNavigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                navigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                navigationRailContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                navigationDrawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                navigationDrawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                wideNavigationRailColors = WideNavigationRailDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    modalContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modalScrimColor = MaterialTheme.colorScheme.scrim,
                    modalContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        ) {
            content(appScreenSize)
        }
    }
}
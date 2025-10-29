package org.milad.expense_share.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
            AppScreenSize.Compact -> NavigationSuiteType.NavigationBar
            AppScreenSize.Medium -> NavigationSuiteType.NavigationRail
            AppScreenSize.Expanded -> NavigationSuiteType.NavigationDrawer
        }

        NavigationSuiteScaffold(
            layoutType = customSuiteType,
            navigationSuiteItems = {
                NavItem.entries.forEach { navItem ->
                    item(
                        selected = selectedItem == navItem,
                        onClick = { onItemSelected(navItem) },
                        icon = { Icon(navItem.icon, contentDescription = navItem.title) },
                        label = {
                            if (appScreenSize == AppScreenSize.Compact) {
                                Text(navItem.title)
                            }
                        }
                    )
                }
            }
        ) {
            content(appScreenSize)
        }
    }
}
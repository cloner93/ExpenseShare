package org.milad.expense_share.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
                    val selected = selectedItem == navItem

                    item(
                        selected = selected,
                        onClick = { onItemSelected(navItem) },
                        icon = {
                            val iconColor =
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant

                            val indicatorColor =
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(indicatorColor, shape = CircleShape)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.title,
                                    tint = iconColor
                                )
                            }
                        },
                        label = {
                            val textColor =
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant

                            Text(
                                navItem.title,
                                color = textColor,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            },
            navigationSuiteColors = NavigationSuiteDefaults.colors(
                shortNavigationBarContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                shortNavigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                navigationBarContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                navigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                navigationRailContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                navigationRailContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                navigationDrawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                navigationDrawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                wideNavigationRailColors = WideNavigationRailDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    modalContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modalScrimColor = MaterialTheme.colorScheme.scrim,
                    modalContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        ) {
            content(appScreenSize)
        }
    }
}
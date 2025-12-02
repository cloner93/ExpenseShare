package com.pmb.common.ui.scaffold.navigation.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.scaffold.NavItem
import com.pmb.common.ui.scaffold.components.ExtendedAddGroupButton
import com.pmb.common.ui.scaffold.model.NavigationContentPosition
import com.pmb.common.ui.scaffold.model.NavigationLayoutType

/**
 * Permanent drawer content for expanded screen sizes
 */
@Composable
fun PermanentDrawerContent(
    contentPosition: NavigationContentPosition,
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onAddGroupClick: () -> Unit,
    showAddGroupButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    PermanentDrawerSheet(
        modifier = modifier.sizeIn(minWidth = 200.dp, maxWidth = 300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Layout(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.inverseOnSurface)
                .padding(16.dp),
            content = {
                // Header section
                Column(
                    modifier = Modifier.layoutId(NavigationLayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DrawerHeader()
                    AnimatedVisibility(
                        visible = showAddGroupButton,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(durationMillis = 300, easing = EaseOutCubic)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 300)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(durationMillis = 300, easing = EaseInCubic)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 300)
                        )
                    ) {
                        ExtendedAddGroupButton(onClick = onAddGroupClick)
                    }
                }

                // Content section with navigation items
                Column(
                    modifier = Modifier
                        .layoutId(NavigationLayoutType.CONTENT)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DrawerNavigationItems(
                        selectedItem = selectedItem,
                        onItemSelected = onItemSelected
                    )
                }
            },
            measurePolicy = drawerMeasurePolicy(contentPosition),
        )
    }
}

/**
 * Header section with app title (no close button for permanent drawer)
 */
@Composable
private fun DrawerHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "expense share".uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Navigation items list for drawer
 */
@Composable
private fun DrawerNavigationItems(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        NavItem.entries.forEach { navItem ->
            NavigationDrawerItem(
                selected = selectedItem == navItem,
                label = {
                    Text(
                        text = navItem.title,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                },
                icon = {
                    Icon(
                        imageVector = navItem.icon,
                        contentDescription = navItem.title
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                ),
                onClick = { onItemSelected(navItem) },
            )
        }
    }
}
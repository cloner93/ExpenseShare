package com.pmb.common.ui.scaffold.navigation.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
 * Modal drawer content shown when drawer is opened
 */
@Composable
fun ModalDrawerContent(
    contentPosition: NavigationContentPosition,
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onDrawerClicked: () -> Unit,
    onAddGroupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
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
                    DrawerHeader(onCloseClick = onDrawerClicked)
                    ExtendedAddGroupButton(onClick = onAddGroupClick)
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
 * Header section with app title and close button
 */
@Composable
private fun DrawerHeader(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
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
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Default.MenuOpen,
                contentDescription = "Close Drawer",
            )
        }
    }
}

/**
 * Navigation items list for drawer
 */
@Composable
private fun DrawerNavigationItems(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
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
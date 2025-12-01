package com.pmb.common.ui.scaffold.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.scaffold.NavItem
import com.pmb.common.ui.scaffold.components.CompactAddGroupButton
import com.pmb.common.ui.scaffold.model.NavigationLayoutType

/**
 * Navigation rail for medium screen sizes
 */
@Composable
fun NavigationRailLayout(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onAddGroupClick: () -> Unit,
    onDrawerClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        // Header section with menu button and add group FAB
        Column(
            modifier = Modifier.layoutId(NavigationLayoutType.HEADER),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavigationRailItem(
                selected = false,
                onClick = onDrawerClicked,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                    )
                },
            )
            
            CompactAddGroupButton(onClick = onAddGroupClick)
            
            Spacer(Modifier.height(8.dp))  // NavigationRailHeaderPadding
            Spacer(Modifier.height(4.dp))  // NavigationRailVerticalPadding
        }

        // Content section with navigation items
        Column(
            modifier = Modifier.layoutId(NavigationLayoutType.CONTENT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavItem.entries.forEach { navItem ->
                NavigationRailItem(
                    selected = selectedItem == navItem,
                    onClick = { onItemSelected(navItem) },
                    icon = { 
                        Icon(
                            imageVector = navItem.icon,
                            contentDescription = navItem.title
                        ) 
                    },
                )
            }
        }
    }
}
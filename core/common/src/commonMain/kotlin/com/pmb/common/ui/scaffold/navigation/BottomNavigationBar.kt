package com.pmb.common.ui.scaffold.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pmb.common.ui.scaffold.NavItem

/**
 * Bottom navigation bar for compact screen sizes
 */
@Composable
fun BottomNavigationBar(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier.fillMaxWidth()) {
        NavItem.entries.forEach { navItem ->
            NavigationBarItem(
                selected = selectedItem == navItem,
                onClick = { onItemSelected(navItem) },
                icon = { 
                    Icon(
                        imageVector = navItem.icon,
                        contentDescription = navItem.title
                    ) 
                },
                label = { Text(navItem.title) }
            )
        }
    }
}
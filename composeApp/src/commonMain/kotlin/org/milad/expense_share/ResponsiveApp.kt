package org.milad.expense_share

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milad.navigation.MainRoute
import com.pmb.common.ui.scaffold.AppScaffold
import com.pmb.common.ui.scaffold.NavItem
import org.milad.expense_share.dashboard.DashboardScreen
import org.milad.expense_share.friends.FriendsScreen
import org.milad.expense_share.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveApp(
    onLogout: () -> Unit,
) {
    var selectedItem by remember { mutableStateOf(NavItem.Dashboard) }
    val navController = rememberNavController()
    var triggerAddGroup by remember { mutableStateOf(false) }

    AppScaffold(
        selectedItem = selectedItem,
        onItemSelected = { item ->
            selectedItem = item
            when (item) {
                NavItem.Dashboard -> navController.navigate(MainRoute.Dashboard) {
                    popUpTo(MainRoute.Dashboard) { inclusive = true }
                    launchSingleTop = true
                }

                NavItem.Friends -> navController.navigate(MainRoute.Friends) {
                    popUpTo(MainRoute.Dashboard)
                    launchSingleTop = true
                }

                NavItem.Profile -> navController.navigate(MainRoute.Profile) {
                    popUpTo(MainRoute.Dashboard)
                    launchSingleTop = true
                }
            }
        },
        onAddGroupClick = {
            if (selectedItem != NavItem.Dashboard) {
                selectedItem = NavItem.Dashboard
                navController.navigate(MainRoute.Dashboard) {
                    popUpTo(MainRoute.Dashboard) { inclusive = true }
                    launchSingleTop = true
                }
            }
            triggerAddGroup = true
        },
        showAddGroupButton = selectedItem == NavItem.Dashboard
    ) { navLayoutType ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Dashboard
        ) {
            composable<MainRoute.Dashboard> {
                DashboardScreen(
                    navLayoutType = navLayoutType,
                    shouldOpenAddGroup = triggerAddGroup,
                    onAddGroupConsumed = { triggerAddGroup = false }
                )
            }

            composable<MainRoute.Friends> {
                FriendsScreen()
            }

            composable<MainRoute.Profile> {
                ProfileScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}
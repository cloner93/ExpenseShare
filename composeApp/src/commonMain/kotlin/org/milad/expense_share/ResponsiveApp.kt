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
import org.milad.expense_share.dashboard.DashboardScreen
import org.milad.expense_share.friends.FriendsScreen
import org.milad.expense_share.profile.ProfileScreen
import org.milad.expense_share.ui.AppScaffold
import org.milad.expense_share.ui.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveApp(
    onLogout: () -> Unit
) {
    var selectedItem by remember { mutableStateOf(NavItem.Dashboard) }
    val navController = rememberNavController()

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
        }
    ) { appScreenSize ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Dashboard
        ) {
            composable<MainRoute.Dashboard> {
                DashboardScreen(appScreenSize = appScreenSize)
            }

            composable<MainRoute.Friends> {
                FriendsScreen(appScreenSize = appScreenSize)
            }

            composable<MainRoute.Profile> {
                ProfileScreen(
                    appScreenSize = appScreenSize,
                    onLogout = onLogout
                )
            }
        }
    }
}
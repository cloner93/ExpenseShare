package org.milad.expense_share

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milad.navigation.RootRoute
import org.milad.expense_share.auth.AuthNavHost


@Composable
fun AppEntryPoint() {
    val navController = rememberNavController()

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = RootRoute.Auth
        ) {

            composable<RootRoute.Auth> {
                AuthNavHost(
                    onAuthSuccess = {
                        navController.navigate(RootRoute.Main) {
                            popUpTo(RootRoute.Auth) { inclusive = true }
                        }
                    }
                )
            }

            composable<RootRoute.Main> {
                ResponsiveApp(
                    onLogout = {
                        navController.navigate(RootRoute.Auth) {
                            popUpTo(RootRoute.Main) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
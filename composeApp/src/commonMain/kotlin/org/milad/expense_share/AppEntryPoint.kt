package org.milad.expense_share

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milad.navigation.RootRoute
import com.pmb.common.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.milad.expense_share.auth.AuthNavHost
import org.milad.expense_share.di.appModules


@Composable
fun AppEntryPoint() {
    val navController = rememberNavController()

    AppTheme {
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


@Preview(showBackground = true)
@Composable
fun ReplyAppPreview() {
    val navController = rememberNavController()
    startKoin {
        modules(appModules)
    }
    AppTheme {
        NavHost(
            navController = navController,
            startDestination = RootRoute.Main
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
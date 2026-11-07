package org.milad.expense_share

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milad.navigation.RootRoute
import com.pmb.common.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.milad.expense_share.auth.AuthNavHost
import org.milad.expense_share.di.appModules
import repository.UserRepository
import repository.UserRepositoryImpl
import token.TokenProvider

@Composable
fun AppEntryPoint(
    tokenProvider: TokenProvider = koinInject(),
    userRepository: UserRepository = koinInject(),
) {
    val isAuthenticated = remember { tokenProvider.getToken() != null }
    val startDestination = if (isAuthenticated) RootRoute.Main else RootRoute.Auth
    val navController = rememberNavController()

    AppTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
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
                        tokenProvider.clearToken()
                        (userRepository as? UserRepositoryImpl)?.clear()
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
            startDestination = RootRoute.Main,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
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
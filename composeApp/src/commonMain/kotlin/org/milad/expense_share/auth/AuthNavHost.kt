package org.milad.expense_share.auth

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milad.navigation.AuthRoute
import org.milad.expense_share.auth.login.LoginScreen
import org.milad.expense_share.auth.register.RegisterScreen

@Composable
fun AuthNavHost(
    onAuthSuccess: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoute.Login,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<AuthRoute.Login> {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                showBackButton = false,
                onNavigateToRegister = {
                    navController.navigate(AuthRoute.Register)
                }
            )
        }

        composable<AuthRoute.Register> {
            RegisterScreen(
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
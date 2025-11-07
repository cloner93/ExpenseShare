package org.milad.expense_share

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.milad.expense_share.auth.login.LoginScreen
import org.milad.expense_share.auth.register.RegisterScreen


@Composable
fun AppEntryPoint() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
        /*RegisterScreen(
            onRegisterSuccess = {},
            onNavigateToLogin = {}
        )*/
    }
}
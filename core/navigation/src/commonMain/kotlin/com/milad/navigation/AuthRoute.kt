package com.milad.navigation
import kotlinx.serialization.Serializable

sealed interface AuthRoute {
    @Serializable
    data object Login : AuthRoute

    @Serializable
    data object Register : AuthRoute
}
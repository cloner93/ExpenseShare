package com.milad.navigation
import kotlinx.serialization.Serializable

sealed interface MainRoute {
    @Serializable
    data object Dashboard : MainRoute

    @Serializable
    data object Friends : MainRoute

    @Serializable
    data object Profile : MainRoute
}
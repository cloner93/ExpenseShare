package com.milad.navigation

import kotlinx.serialization.Serializable

sealed interface RootRoute {
    @Serializable
    data object Auth : RootRoute

    @Serializable
    data object Main : RootRoute
}
package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AutoIconRoute {
    @Serializable
    data object Home : AutoIconRoute

    @Serializable
    data object Editor : AutoIconRoute

    @Serializable
    data object Apps : AutoIconRoute

    @Serializable
    data object Themes : AutoIconRoute

    @Serializable
    data object Automation : AutoIconRoute
}

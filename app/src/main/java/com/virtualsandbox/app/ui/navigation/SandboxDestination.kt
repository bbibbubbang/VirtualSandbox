package com.virtualsandbox.app.ui.navigation

sealed class SandboxDestination(val route: String) {
    data object Spaces : SandboxDestination("spaces")
    data object SpaceDetail : SandboxDestination("space/{spaceId}") {
        fun create(spaceId: Long) = "space/$spaceId"
        const val ARG_SPACE_ID = "spaceId"
    }
}

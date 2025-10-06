package com.virtualsandbox.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.virtualsandbox.app.ui.detail.SpaceDetailRoute
import com.virtualsandbox.app.ui.navigation.SandboxDestination
import com.virtualsandbox.app.ui.spaces.SpacesRoute
import com.virtualsandbox.app.ui.theme.VirtualSandboxTheme

@Composable
fun VirtualSandboxApp() {
    val navController = rememberNavController()
    val isDarkTheme = isSystemInDarkTheme()
    VirtualSandboxTheme(darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = SandboxDestination.Spaces.route,
            ) {
                composable(SandboxDestination.Spaces.route) {
                    SpacesRoute(
                        onSpaceSelected = { spaceId ->
                            navController.navigate(SandboxDestination.SpaceDetail.create(spaceId))
                        },
                    )
                }
                composable(
                    route = SandboxDestination.SpaceDetail.route,
                    arguments = listOf(
                        navArgument(SandboxDestination.SpaceDetail.ARG_SPACE_ID) { type = NavType.LongType }
                    ),
                ) {
                    SpaceDetailRoute(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

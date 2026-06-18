package com.example.app_proprietario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_proprietario.data.Routes
import com.example.app_proprietario.data.SampleData
import com.example.app_proprietario.ui.screens.PropertyDetailsScreen
import com.example.app_proprietario.ui.screens.PropertyListScreen
import com.example.app_proprietario.ui.screens.RoomDetailsScreen

@Composable
fun MonitorNavGraph() {
    val navController = rememberNavController()
    val properties = remember { SampleData.properties }

    NavHost(
        navController = navController,
        startDestination = Routes.PROPERTY_LIST
    ) {
        composable(Routes.PROPERTY_LIST) {
            PropertyListScreen(
                properties = properties,
                onPropertyClick = { property ->
                    navController.navigate(Routes.propertyDetails(property.id))
                }
            )
        }

        composable(
            route = Routes.PROPERTY_DETAILS,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: return@composable
            val property = properties.find { it.id == propertyId } ?: return@composable

            PropertyDetailsScreen(
                property = property,
                onBack = {
                    navController.popBackStack()
                },
                onRoomClick = { room ->
                    navController.navigate(Routes.roomDetails(property.id, room.id))
                }
            )
        }

        composable(
            route = Routes.ROOM_DETAILS,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: return@composable
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            val property = properties.find { it.id == propertyId } ?: return@composable
            val room = property.rooms.find { it.id == roomId } ?: return@composable

            RoomDetailsScreen(
                propertyName = property.name,
                room = room,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

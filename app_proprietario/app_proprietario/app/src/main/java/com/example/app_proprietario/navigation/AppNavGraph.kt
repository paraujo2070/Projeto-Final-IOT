package com.example.app_proprietario.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_proprietario.data.Routes
import com.example.app_proprietario.ui.screens.IntrusionHistoryScreen
import com.example.app_proprietario.ui.screens.PropertyDetailsScreen
import com.example.app_proprietario.ui.screens.PropertyListScreen
import com.example.app_proprietario.ui.screens.RoomDetailsScreen
import com.example.app_proprietario.ui.screens.viewmodel.IntrusionHistoryViewModel
import com.example.app_proprietario.ui.viewmodel.PropertyDetailsUiState
import com.example.app_proprietario.ui.viewmodel.PropertyDetailsViewModel
import com.example.app_proprietario.ui.viewmodel.PropertyListViewModel
import com.example.app_proprietario.ui.viewmodel.RoomDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MonitorNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.PROPERTY_LIST
    ) {
        composable(Routes.PROPERTY_LIST) {
            val viewModel: PropertyListViewModel = koinViewModel()

            PropertyListScreen(
                viewModel = viewModel,
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
            val rawPropertyId = backStackEntry.arguments?.getString("propertyId") ?: return@composable
            val propertyId = Routes.decode(rawPropertyId)
            val viewModel: PropertyDetailsViewModel = koinViewModel { parametersOf(propertyId) }

            PropertyDetailsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRoomClick = { room ->
                    val propertyName = (viewModel.uiState.value as? PropertyDetailsUiState.Success)
                        ?.property?.name ?: ""
                    navController.navigate(Routes.roomDetails(propertyId, propertyName, room.id))
                },
                onIntrusionHistoryClick = {
                    navController.navigate(Routes.intrusionHistory(propertyId))
                }
            )
        }

        composable(
            route = Routes.ROOM_DETAILS,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.StringType },
                navArgument("propertyName") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawPropertyId = backStackEntry.arguments?.getString("propertyId") ?: return@composable
            val propertyId = Routes.decode(rawPropertyId)
            val rawPropertyName = backStackEntry.arguments?.getString("propertyName") ?: ""
            val propertyName = Routes.decode(rawPropertyName)
            val rawRoomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            val roomId = Routes.decode(rawRoomId)
            val viewModel: RoomDetailsViewModel = koinViewModel {
                parametersOf(propertyId, propertyName, roomId)
            }

            RoomDetailsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.INTRUSION_HISTORY,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawPropertyId = backStackEntry.arguments?.getString("propertyId") ?: return@composable
            val propertyId = Routes.decode(rawPropertyId)
            val viewModel: IntrusionHistoryViewModel = koinViewModel { parametersOf(propertyId) }

            IntrusionHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

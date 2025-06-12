package com.example.tailorbillpro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tailorbillpro.ui.screens.dashboard.DashboardScreen
import com.example.tailorbillpro.ui.screens.newbill.NewBillScreen
import com.example.tailorbillpro.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object NewBill : Screen("new_bill")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToNewBill = { navController.navigate(Screen.NewBill.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.NewBill.route) {
            NewBillScreen(
                onNavigateUp = { navController.navigateUp() },
                onBillGenerated = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}

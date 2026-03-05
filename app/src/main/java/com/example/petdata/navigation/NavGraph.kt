package com.example.petdata.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.petdata.data.local.TokenManager
import com.example.petdata.ui.screens.*
import com.example.petdata.ui.viemodel.HomeViewModel
import com.example.petdata.ui.viemodel.RegisterViewModel
import com.example.petdata.ui.viewmodel.LoginViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Map : Screen("map")
    object Report : Screen("report/{mode}") {
        fun createRoute(mode: String) = "report/$mode"
    }
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object ReportDetail : Screen("report_detail/{reporteId}") {
        fun createRoute(reporteId: Int) = "report_detail/$reporteId"
    }
    object RescuerHistory : Screen("rescuer_history")
    object RescuerActiveCases : Screen("rescuer_active_cases")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    rolId: Int,
    onRolIdUpdated: (Int) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(tokenManager)
            )
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { rol ->
                    onRolIdUpdated(rol)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            val viewModel: RegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = RegisterViewModel.Factory(tokenManager)
            )
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(tokenManager)
            )
            HomeScreen(
                rolId = rolId,
                viewModel = viewModel,
                onNavigateToReport = { mode ->
                    navController.navigate(Screen.Report.createRoute(mode))
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.ReportDetail.createRoute(id))
                },
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                rolId = rolId,
                tokenManager = tokenManager,
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(Screen.Report.route) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "crear"
            ReportFormScreen(
                mode = mode,
                rolId = rolId,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                rolId = rolId,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ReportDetail.route) { backStackEntry ->
            val reporteId = backStackEntry.arguments?.getString("reporteId")?.toInt() ?: 0
            ReportDetailScreen(
                reporteId = reporteId,
                rolId = rolId,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.RescuerHistory.route) {
            RescuerHistoryScreen(
                rolId = rolId,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.ReportDetail.createRoute(id)) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.RescuerActiveCases.route) {
            RescuerActiveCasesScreen(
                rolId = rolId,
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.ReportDetail.createRoute(id)) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}
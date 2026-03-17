package com.example.petdata.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.petdata.MainActivity
import com.example.petdata.data.local.TokenManager
import com.example.petdata.ui.screens.*
import com.example.petdata.ui.viemodel.HomeViewModel
import com.example.petdata.ui.viemodel.RegisterViewModel
import com.example.petdata.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
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

    object Map : Screen("map?lat={lat}&lng={lng}") {
        fun createRoute(lat: Double? = null, lng: Double? = null): String {
            return if (lat != null && lng != null) "map?lat=$lat&lng=$lng"
            else "map"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    rolId: Int,
    onRolIdUpdated: (Int) -> Unit
) {

    // Verificar sesión activa al arrancar
    LaunchedEffect(Unit) {
        val token = tokenManager.getValidToken()
        if (token != null) {
            val rolIdStr = tokenManager.rolId.first()
            val rol = rolIdStr?.toIntOrNull() ?: 1
            onRolIdUpdated(rol)
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(tokenManager)
            )

            // AGREGA ESTO — observar cuando Google auth complete
            val googleRolId by MainActivity.googleAuthResult.collectAsStateWithLifecycle()
            LaunchedEffect(googleRolId) {
                googleRolId?.let { rol ->
                    MainActivity.googleAuthResult.value = null
                    onRolIdUpdated(rol)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current  // AGREGA ESTO

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
                },
                onGoogleSignIn = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://api-gateway-production-db18.up.railway.app/auth/google")
                    )
                    context.startActivity(intent)
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
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = "map?lat={lat}&lng={lng}",
            arguments = listOf(
                androidx.navigation.navArgument("lat") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("lng") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
            MapScreen(
                rolId = rolId,
                tokenManager = tokenManager,
                focusLat = lat,
                focusLng = lng,
                onNavigate = { route -> navController.navigate(route) }
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
                rolId = rolId,
                tokenManager = tokenManager,
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

        composable("personal_info") {
            PersonalInfoScreen(
                tokenManager = tokenManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
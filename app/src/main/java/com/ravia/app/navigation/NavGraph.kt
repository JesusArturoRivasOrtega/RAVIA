package com.ravia.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ravia.app.presentation.alerts.AlertSettingsScreen
import com.ravia.app.presentation.alerts.AlertsScreen
import com.ravia.app.presentation.auth.*
import com.ravia.app.presentation.chatbot.ChatbotScreen
import com.ravia.app.presentation.home.HomeScreen
import com.ravia.app.presentation.map.MapScreen
import com.ravia.app.presentation.map.RiskZoneDetailScreen
import com.ravia.app.presentation.missingpersons.*
import com.ravia.app.presentation.moderation.ModerationDashboardScreen
import com.ravia.app.presentation.profile.*
import com.ravia.app.presentation.reports.*

@Composable
fun RaviaNavGraph(navController: NavHostController, startDestination: String = Screen.Splash.route) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ─── Auth ────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // ─── Main tabs ───────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Moderation.route) {
            ModerationDashboardScreen(navController = navController)
        }
        composable(Screen.Map.route) {
            MapScreen(navController = navController)
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // ─── Reports ─────────────────────────────────────────────────────────
        composable(Screen.CreateReport.route) {
            CreateReportScreen(navController = navController)
        }
        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            ReportDetailScreen(
                navController = navController,
                reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            )
        }
        composable(Screen.ReportAiAnalysis.route) {
            ReportAiAnalysisScreen(navController = navController)
        }
        composable(
            route = Screen.ReportSubmitted.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            ReportSubmittedScreen(
                navController = navController,
                reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            )
        }

        // ─── Map extras ──────────────────────────────────────────────────────
        composable(
            route = Screen.RiskZoneDetail.route,
            arguments = listOf(navArgument("zoneId") { type = NavType.StringType })
        ) { backStackEntry ->
            RiskZoneDetailScreen(
                navController = navController,
                zoneId = backStackEntry.arguments?.getString("zoneId") ?: ""
            )
        }

        // ─── Alert settings ──────────────────────────────────────────────────
        composable(Screen.AlertSettings.route) {
            AlertSettingsScreen(navController = navController)
        }

        // ─── Chatbot ─────────────────────────────────────────────────────────
        composable(Screen.Chatbot.route) {
            ChatbotScreen(navController = navController)
        }

        // ─── Missing persons ─────────────────────────────────────────────────
        composable(Screen.MissingPersons.route) {
            MissingPersonsScreen(navController = navController)
        }
        composable(
            route = Screen.MissingPersonDetail.route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStackEntry ->
            MissingPersonDetailScreen(
                navController = navController,
                personId = backStackEntry.arguments?.getString("personId") ?: ""
            )
        }
        composable(Screen.CreateMissingPerson.route) {
            CreateMissingPersonScreen(navController = navController)
        }
        composable(
            route = Screen.ReportSighting.route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStackEntry ->
            ReportSightingScreen(
                navController = navController,
                personId = backStackEntry.arguments?.getString("personId") ?: ""
            )
        }

        // ─── Profile sub-screens ─────────────────────────────────────────────
        composable(Screen.MyReports.route) {
            MyReportsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Reputation.route) {
            ReputationScreen(navController = navController)
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
    }
}

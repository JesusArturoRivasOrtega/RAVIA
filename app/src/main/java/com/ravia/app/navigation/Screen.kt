package com.ravia.app.navigation

sealed class Screen(val route: String) {
    // ─── Auth flow ───────────────────────────────────────────────────────────
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // ─── Main tabs ───────────────────────────────────────────────────────────
    object Home : Screen("home")
    object Moderation : Screen("moderation")
    object Map : Screen("map")
    object Alerts : Screen("alerts")
    object Profile : Screen("profile")

    // ─── Reports ─────────────────────────────────────────────────────────────
    object CreateReport : Screen("create_report")
    object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    object ReportAiAnalysis : Screen("report_ai_analysis")
    object ReportSubmitted : Screen("report_submitted/{reportId}") {
        fun createRoute(reportId: String) = "report_submitted/$reportId"
    }

    // ─── Map extras ──────────────────────────────────────────────────────────
    object RiskZoneDetail : Screen("risk_zone/{zoneId}") {
        fun createRoute(zoneId: String) = "risk_zone/$zoneId"
    }

    // ─── Alerts extras ───────────────────────────────────────────────────────
    object AlertSettings : Screen("alert_settings")

    // ─── Chatbot ─────────────────────────────────────────────────────────────
    object Chatbot : Screen("chatbot")

    // ─── Missing persons ─────────────────────────────────────────────────────
    object MissingPersons : Screen("missing_persons")
    object MissingPersonDetail : Screen("missing_person/{personId}") {
        fun createRoute(personId: String) = "missing_person/$personId"
    }
    object CreateMissingPerson : Screen("create_missing_person")
    object ReportSighting : Screen("report_sighting/{personId}") {
        fun createRoute(personId: String) = "report_sighting/$personId"
    }

    // ─── Profile sub-screens ─────────────────────────────────────────────────
    object MyReports : Screen("my_reports")
    object Settings : Screen("settings")
    object Reputation : Screen("reputation")
    object EditProfile : Screen("edit_profile")
}

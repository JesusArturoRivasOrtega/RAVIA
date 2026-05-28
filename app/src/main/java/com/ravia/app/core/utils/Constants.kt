package com.ravia.app.core.utils

object Constants {
    const val DATASTORE_NAME = "ravia_prefs"
    const val KEY_IS_FIRST_TIME = "is_first_time"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_TOKEN = "user_token"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_FCM_TOKEN = "fcm_token"
    const val KEY_ALERT_RADIUS_KM = "alert_radius_km"

    const val DEFAULT_NEARBY_RADIUS_KM = 5.0
    const val DEFAULT_ALERT_RADIUS_KM = 10.0
    const val MAX_REPORT_IMAGES = 5
    const val MAX_REPORT_VIDEO_MB = 50
    const val MAX_REPORT_AUDIO_SECONDS = 120

    const val REPORT_CONFIDENCE_THRESHOLD = 0.6
    const val CRITICAL_CONFIRMATIONS_NEEDED = 3
    const val FALSE_REPORT_THRESHOLD = 5

    // Firebase Storage paths
    const val STORAGE_REPORTS = "reportes"
    const val STORAGE_MISSING = "desaparecidos"
    const val STORAGE_AVATARS = "usuarios"

    // FCM topics
    const val TOPIC_ALL_ALERTS = "all_alerts"
    const val TOPIC_CRITICAL = "critical_alerts"
}

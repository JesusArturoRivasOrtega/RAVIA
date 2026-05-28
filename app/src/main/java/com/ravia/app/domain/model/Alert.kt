package com.ravia.app.domain.model

import java.util.Date

enum class AlertSeverity {
    INFO, CAUTION, URGENT, CRITICAL;

    fun displayName(): String = when (this) {
        INFO -> "Informativa"
        CAUTION -> "Precaución"
        URGENT -> "Urgente"
        CRITICAL -> "Crítica"
    }
}

data class Alert(
    val id: String,
    val reportId: String? = null,
    val title: String,
    val message: String,
    val severity: AlertSeverity = AlertSeverity.INFO,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Double? = null,
    val distanceKm: Double? = null,
    val isRead: Boolean = false,
    val createdAt: Date = Date()
)

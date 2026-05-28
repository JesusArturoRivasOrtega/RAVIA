package com.ravia.app.domain.model

import java.util.Date

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL;

    fun displayName(): String = when (this) {
        LOW -> "Bajo"
        MEDIUM -> "Medio"
        HIGH -> "Alto"
        CRITICAL -> "Crítico"
    }
}

data class RiskZone(
    val id: String,
    val name: String,
    val category: String,
    val riskLevel: RiskLevel,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val description: String,
    val recommendations: String,
    val incidentCount: Int = 0,
    val distanceKm: Double? = null,
    val createdAt: Date = Date()
)

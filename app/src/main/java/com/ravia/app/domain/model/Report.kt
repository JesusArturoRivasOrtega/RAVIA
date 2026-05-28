package com.ravia.app.domain.model

import java.util.Date

enum class ReportStatus {
    PENDING, VERIFYING, CONFIRMED, CRITICAL, IN_PROGRESS, RESOLVED, FALSE, DUPLICATED;

    fun displayName(): String = when (this) {
        PENDING -> "Pendiente"
        VERIFYING -> "Verificando"
        CONFIRMED -> "Confirmado"
        CRITICAL -> "Crítico"
        IN_PROGRESS -> "En atención"
        RESOLVED -> "Resuelto"
        FALSE -> "Falso"
        DUPLICATED -> "Duplicado"
    }
}

fun ReportStatus.isTerminal(): Boolean = this == ReportStatus.RESOLVED ||
    this == ReportStatus.FALSE ||
    this == ReportStatus.DUPLICATED

fun ReportStatus.isVisibleOnMap(): Boolean = !isTerminal()

fun ReportStatus.allowedModeratorTransitions(): List<ReportStatus> = when (this) {
    ReportStatus.PENDING -> listOf(
        ReportStatus.VERIFYING,
        ReportStatus.CONFIRMED,
        ReportStatus.CRITICAL,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED
    )
    ReportStatus.VERIFYING -> listOf(
        ReportStatus.CONFIRMED,
        ReportStatus.CRITICAL,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED
    )
    ReportStatus.CONFIRMED -> listOf(
        ReportStatus.CRITICAL,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED
    )
    ReportStatus.CRITICAL -> listOf(
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED
    )
    ReportStatus.IN_PROGRESS -> listOf(
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED
    )
    ReportStatus.RESOLVED,
    ReportStatus.FALSE,
    ReportStatus.DUPLICATED -> emptyList()
}

enum class ReportPriority {
    LOW, MEDIUM, HIGH, CRITICAL;

    fun displayName(): String = when (this) {
        LOW -> "Bajo"
        MEDIUM -> "Medio"
        HIGH -> "Alto"
        CRITICAL -> "Crítico"
    }
}

enum class ReportCategory(val displayName: String, val emoji: String) {
    SECURITY("Seguridad pública", "🚨"),
    ACCIDENT("Accidente vial", "🚗"),
    INJURED_PERSON("Persona herida o en riesgo", "🚑"),
    FIRE("Incendio / Humo", "🔥"),
    FLOOD("Inundación", "🌊"),
    DANGEROUS_ANIMAL("Animal peligroso", "⚠️"),
    MISSING_PERSON("Persona desaparecida", "🔍"),
    VIOLENCE("Violencia o amenaza", "🆘"),
    INFRASTRUCTURE("Infraestructura dañada", "🏗️"),
    RISK_ZONE("Zona peligrosa", "⛔"),
    OTHER("Otro", "📋")
}

data class ReportMedia(
    val id: String,
    val reportId: String,
    val type: MediaType,
    val url: String,
    val createdAt: Date = Date()
)

enum class MediaType { IMAGE, VIDEO, AUDIO }

data class ReportConfirmation(
    val id: String,
    val reportId: String,
    val userId: String,
    val type: ConfirmationType,
    val comment: String? = null,
    val createdAt: Date = Date()
)

enum class ConfirmationType {
    CONFIRM, FALSE, DUPLICATE, NO_LONGER_HAPPENING, URGENT, MORE_INFO;

    fun displayName(): String = when (this) {
        CONFIRM -> "Lo confirmo"
        FALSE -> "Parece falso"
        DUPLICATE -> "Es duplicado"
        NO_LONGER_HAPPENING -> "Ya no está ocurriendo"
        URGENT -> "Necesita atención urgente"
        MORE_INFO -> "Tengo más información"
    }
}

data class ReportStatusHistory(
    val id: String,
    val reportId: String,
    val status: ReportStatus,
    val changedBy: String,
    val comment: String? = null,
    val createdAt: Date = Date()
)

data class Report(
    val id: String,
    val userId: String,
    val userName: String? = null,
    val sourceRole: UserRole = UserRole.CITIZEN,
    val sourceReputation: Int = 0,
    val sourceReportCount: Int = 0,
    val sourceConfirmedReports: Int = 0,
    val sourceTrustScore: Double = 0.5,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val priority: ReportPriority = ReportPriority.MEDIUM,
    val status: ReportStatus = ReportStatus.PENDING,
    val confidence: Double = 0.5,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val anonymous: Boolean = false,
    val aiSummary: String? = null,
    val media: List<ReportMedia> = emptyList(),
    val confirmCount: Int = 0,
    val falseCount: Int = 0,
    val duplicateCount: Int = 0,
    val urgentCount: Int = 0,
    val resolvedSignalCount: Int = 0,
    val confirmations: List<ReportConfirmation> = emptyList(),
    val statusHistory: List<ReportStatusHistory> = emptyList(),
    val distanceKm: Double? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

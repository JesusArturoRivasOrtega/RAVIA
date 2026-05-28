package com.ravia.app.data.mapper

import com.ravia.app.data.dto.*
import com.ravia.app.data.firebase.distanceKm
import com.ravia.app.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val isoFormats = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'"
).map {
    SimpleDateFormat(it, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
}

fun parseApiDate(value: String?): Date =
    value?.let { raw -> isoFormats.firstNotNullOfOrNull { runCatching { it.parse(raw) }.getOrNull() } } ?: Date()

private fun backendCategory(category: ReportCategory): String = when (category) {
    ReportCategory.SECURITY -> "theft"
    ReportCategory.ACCIDENT -> "accident"
    ReportCategory.INJURED_PERSON -> "medical"
    ReportCategory.FIRE -> "fire"
    ReportCategory.FLOOD -> "flood"
    ReportCategory.DANGEROUS_ANIMAL -> "other"
    ReportCategory.MISSING_PERSON -> "missing_person"
    ReportCategory.VIOLENCE -> "assault"
    ReportCategory.INFRASTRUCTURE -> "infrastructure"
    ReportCategory.RISK_ZONE -> "suspicious"
    ReportCategory.OTHER -> "other"
}

private fun appCategory(value: String?): ReportCategory = when (value?.lowercase()) {
    "fire" -> ReportCategory.FIRE
    "accident" -> ReportCategory.ACCIDENT
    "flood" -> ReportCategory.FLOOD
    "theft", "suspicious" -> ReportCategory.SECURITY
    "assault" -> ReportCategory.VIOLENCE
    "missing_person" -> ReportCategory.MISSING_PERSON
    "infrastructure" -> ReportCategory.INFRASTRUCTURE
    "gas_leak" -> ReportCategory.FIRE
    "medical" -> ReportCategory.INJURED_PERSON
    else -> ReportCategory.OTHER
}

private fun backendPriority(priority: ReportPriority): String = priority.name.lowercase()
private fun appPriority(value: String?): ReportPriority =
    runCatching { ReportPriority.valueOf(value.orEmpty().uppercase()) }.getOrDefault(ReportPriority.MEDIUM)

private fun appStatus(value: String?): ReportStatus = when (value?.lowercase()) {
    "pending" -> ReportStatus.PENDING
    "verifying" -> ReportStatus.VERIFYING
    "confirmed" -> ReportStatus.CONFIRMED
    "critical" -> ReportStatus.CRITICAL
    "in_progress" -> ReportStatus.IN_PROGRESS
    "resolved" -> ReportStatus.RESOLVED
    "false" -> ReportStatus.FALSE
    "duplicated" -> ReportStatus.DUPLICATED
    else -> ReportStatus.PENDING
}

private fun appSeverity(value: String?): AlertSeverity =
    runCatching { AlertSeverity.valueOf(value.orEmpty().uppercase()) }.getOrDefault(AlertSeverity.INFO)

private fun appRiskLevel(value: String?): RiskLevel =
    runCatching { RiskLevel.valueOf(value.orEmpty().uppercase()) }.getOrDefault(RiskLevel.MEDIUM)

private fun appMissingStatus(value: String?): MissingPersonStatus = when (value?.lowercase()) {
    "pending_review" -> MissingPersonStatus.PENDING_REVIEW
    "active" -> MissingPersonStatus.ACTIVE
    "found" -> MissingPersonStatus.FOUND
    "cancelled" -> MissingPersonStatus.CANCELLED
    "closed" -> MissingPersonStatus.CLOSED
    else -> MissingPersonStatus.CLOSED
}

fun UserDto.toDomain(): User = User(
    id = id,
    firebaseUid = id,
    name = displayName ?: email?.substringBefore("@") ?: "Vecino RAVIA",
    email = email.orEmpty(),
    role = runCatching { UserRole.valueOf(role.orEmpty().uppercase()) }.getOrDefault(UserRole.CITIZEN),
    zone = zone,
    avatarUrl = photoUrl,
    reputation = reputationPoints ?: 0,
    status = runCatching { UserStatus.valueOf(status.orEmpty().uppercase()) }.getOrDefault(UserStatus.ACTIVE),
    reportsCount = reportCount ?: 0,
    confirmedReportsCount = confirmedReports ?: 0,
    createdAt = parseApiDate(createdAt)
)

fun ReportDto.toDomain(referenceLat: Double? = null, referenceLng: Double? = null): Report {
    val lat = location?.lat ?: 0.0
    val lng = location?.lng ?: 0.0
    val source = sourceProfile
    val parsedStatus = appStatus(status)
    val confirms = confirmCount ?: 0
    val falses = falseCount ?: 0
    // Match backend weights: confirms reduce uncertainty, falses dampen it.
    val computedConfidence = when (parsedStatus) {
        ReportStatus.CONFIRMED, ReportStatus.CRITICAL, ReportStatus.IN_PROGRESS -> 0.92
        ReportStatus.RESOLVED -> 1.0
        ReportStatus.FALSE -> 0.05
        ReportStatus.DUPLICATED -> 0.4
        else -> {
            val raw = 0.5 + (confirms * 0.12) - (falses * 0.15)
            raw.coerceIn(0.0, 0.95)
        }
    }
    val history = statusHistory.orEmpty().mapNotNull { entry ->
        ReportStatusHistory(
            id = "${entry.status}-${entry.changedBy.orEmpty()}-${entry.timestamp ?: ""}",
            reportId = id,
            status = appStatus(entry.status),
            changedBy = entry.changedBy.orEmpty(),
            comment = entry.reason,
            createdAt = parseHistoryTimestamp(entry.timestamp)
        )
    }
    return Report(
        id = id,
        userId = authorId.orEmpty(),
        userName = if (isAnonymous == true) null else source?.displayName,
        sourceRole = runCatching { UserRole.valueOf(source?.role.orEmpty().uppercase()) }.getOrDefault(UserRole.CITIZEN),
        sourceReputation = source?.reputationPoints ?: 0,
        sourceReportCount = source?.reportCount ?: 0,
        sourceConfirmedReports = source?.confirmedReports ?: 0,
        sourceTrustScore = source?.trustScore ?: 0.5,
        title = title,
        description = description,
        category = appCategory(category),
        priority = appPriority(priority),
        status = parsedStatus,
        confidence = computedConfidence,
        latitude = lat,
        longitude = lng,
        address = address,
        anonymous = isAnonymous ?: false,
        aiSummary = aiAnalysis?.summary,
        media = media.orEmpty().map {
            ReportMedia(
                id = it.id,
                reportId = id,
                type = when (it.type.lowercase()) {
                    "video" -> MediaType.VIDEO
                    "audio" -> MediaType.AUDIO
                    else -> MediaType.IMAGE
                },
                url = it.url,
                createdAt = parseApiDate(createdAt)
            )
        },
        confirmCount = confirms,
        falseCount = falses,
        duplicateCount = duplicateCount ?: 0,
        urgentCount = urgentCount ?: 0,
        resolvedSignalCount = resolvedSignalCount ?: 0,
        confirmations = emptyList(),
        statusHistory = history,
        distanceKm = if (referenceLat != null && referenceLng != null) distanceKm(referenceLat, referenceLng, lat, lng) else null,
        createdAt = parseApiDate(createdAt),
        updatedAt = parseApiDate(updatedAt)
    )
}

private fun parseHistoryTimestamp(value: Any?): Date = when (value) {
    is String -> parseApiDate(value)
    is Number -> Date(value.toLong())
    is Map<*, *> -> {
        val seconds = (value["_seconds"] as? Number) ?: (value["seconds"] as? Number)
        if (seconds != null) Date(seconds.toLong() * 1000) else Date()
    }
    else -> Date()
}

fun ReportDto.analysisOrFallback(description: String): AiAnalysis =
    aiAnalysis?.toDomain() ?: simpleAiAnalysis(description)

fun AiAnalysisResponseDto.toDomain(): AiAnalysis = AiAnalysis(
    suggestedCategory = appCategory(suggestedCategory),
    suggestedPriority = appPriority(suggestedPriority),
    confidence = confidence,
    summary = summary,
    missingInfo = missingInfo.orEmpty(),
    possibleDuplicate = possibleDuplicate,
    duplicateReportId = duplicateReportId
)

fun simpleAiAnalysis(description: String): AiAnalysis {
    val lower = description.lowercase()
    val category = when {
        "incendio" in lower || "fuego" in lower || "humo" in lower -> ReportCategory.FIRE
        "accidente" in lower || "choque" in lower -> ReportCategory.ACCIDENT
        "herido" in lower || "inconsciente" in lower -> ReportCategory.INJURED_PERSON
        "robo" in lower || "asalto" in lower -> ReportCategory.SECURITY
        else -> ReportCategory.OTHER
    }
    val priority = if (category == ReportCategory.FIRE || category == ReportCategory.INJURED_PERSON) {
        ReportPriority.CRITICAL
    } else {
        ReportPriority.MEDIUM
    }
    return AiAnalysis(category, priority, 0.72, "Clasificación preliminar del reporte.", emptyList(), false)
}

fun buildCreateReportRequest(
    title: String,
    description: String,
    category: ReportCategory,
    priority: ReportPriority,
    latitude: Double,
    longitude: Double,
    address: String?,
    anonymous: Boolean,
    media: List<ReportMediaDto>
): CreateReportRequestDto = CreateReportRequestDto(
    title = title,
    description = description,
    category = backendCategory(category),
    priority = backendPriority(priority),
    lat = latitude,
    lng = longitude,
    address = address,
    isAnonymous = anonymous,
    media = media
)

fun ConfirmationType.toBackendValue(): String = when (this) {
    ConfirmationType.CONFIRM -> "confirm"
    ConfirmationType.FALSE -> "false_report"
    ConfirmationType.DUPLICATE -> "duplicate"
    ConfirmationType.NO_LONGER_HAPPENING -> "no_longer_happening"
    ConfirmationType.URGENT -> "urgent"
    ConfirmationType.MORE_INFO -> "more_info"
}

fun ReportStatus.toBackendValue(): String = name.lowercase()

fun AlertDto.toDomain(): Alert = Alert(
    id = id,
    title = title,
    message = message ?: description.orEmpty(),
    severity = appSeverity(severity),
    isRead = false,
    createdAt = parseApiDate(createdAt)
)

fun RiskZoneDto.toDomain(referenceLat: Double? = null, referenceLng: Double? = null): RiskZone {
    val lat = centerLat ?: 0.0
    val lng = centerLng ?: 0.0
    return RiskZone(
        id = id,
        name = name,
        category = "General",
        riskLevel = appRiskLevel(riskLevel),
        latitude = lat,
        longitude = lng,
        radius = radiusMeters ?: 300.0,
        description = description,
        recommendations = "Mantente alerta y evita la zona si hay incidentes activos.",
        incidentCount = reportCount ?: 0,
        distanceKm = if (referenceLat != null && referenceLng != null) distanceKm(referenceLat, referenceLng, lat, lng) else null,
        createdAt = parseApiDate(createdAt)
    )
}

fun MissingPersonDto.toDomain(): MissingPerson = MissingPerson(
    id = id,
    createdBy = reportedBy.orEmpty(),
    name = name,
    age = age,
    photoUrl = photoUrl,
    lastSeenLocation = lastSeenLocation,
    lastSeenLatitude = lastSeenLat,
    lastSeenLongitude = lastSeenLng,
    clothing = clothing,
    distinctiveSigns = distinctiveSigns,
    description = description,
    contactInfo = contactInfo,
    status = appMissingStatus(status),
    sightings = sightings.orEmpty().map {
        Sighting(
            id = it.id,
            missingPersonId = id,
            userId = it.reportedBy.orEmpty(),
            latitude = it.lat,
            longitude = it.lng,
            comment = it.comment,
            photoUrl = it.photoUrl,
            createdAt = parseApiDate(it.createdAt)
        )
    },
    createdAt = parseApiDate(createdAt)
)

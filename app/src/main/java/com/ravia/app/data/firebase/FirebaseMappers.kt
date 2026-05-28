package com.ravia.app.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.ravia.app.domain.model.*
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object FirebaseCollections {
    const val USERS = "users"
    const val REPORTS = "reports"
    const val REPORT_CONFIRMATIONS = "report_confirmations"
    const val ALERTS = "alerts"
    const val RISK_ZONES = "risk_zones"
    const val MISSING_PERSONS = "missing_persons"
    const val MISSING_PERSON_SIGHTINGS = "missing_person_sightings"
    const val CHATBOT_MESSAGES = "chatbot_messages"
}

inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T =
    value?.let {
        runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull()
    } ?: fallback

private fun reportCategoryValue(value: String?): ReportCategory = when (value?.lowercase()) {
    "fire" -> ReportCategory.FIRE
    "accident" -> ReportCategory.ACCIDENT
    "flood" -> ReportCategory.FLOOD
    "theft", "suspicious" -> ReportCategory.SECURITY
    "assault" -> ReportCategory.VIOLENCE
    "missing_person" -> ReportCategory.MISSING_PERSON
    "infrastructure" -> ReportCategory.INFRASTRUCTURE
    "gas_leak" -> ReportCategory.FIRE
    "medical" -> ReportCategory.INJURED_PERSON
    else -> enumValue(value, ReportCategory.OTHER)
}

private fun confirmationTypeValue(value: String?): ConfirmationType = when (value?.lowercase()) {
    "confirm" -> ConfirmationType.CONFIRM
    "false_report", "false" -> ConfirmationType.FALSE
    "duplicate", "duplicated" -> ConfirmationType.DUPLICATE
    "no_longer_happening" -> ConfirmationType.NO_LONGER_HAPPENING
    "urgent" -> ConfirmationType.URGENT
    "more_info" -> ConfirmationType.MORE_INFO
    else -> ConfirmationType.CONFIRM
}

fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
    return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "firebaseUid" to firebaseUid,
    "name" to name,
    "email" to email,
    "phone" to phone,
    "role" to role.name,
    "zone" to zone,
    "avatarUrl" to avatarUrl,
    "reputation" to reputation,
    "status" to status.name,
    "reportsCount" to reportsCount,
    "confirmedReportsCount" to confirmedReportsCount,
    "createdAt" to createdAt,
    "updatedAt" to Date()
)

fun DocumentSnapshot.toUser(): User {
    val docId = getString("id") ?: id
    val uid = getString("firebaseUid") ?: docId
    return User(
        id = docId,
        firebaseUid = uid,
        name = getString("name") ?: getString("email")?.substringBefore("@") ?: "Vecino RAVIA",
        email = getString("email") ?: "",
        phone = getString("phone"),
        role = enumValue(getString("role"), UserRole.CITIZEN),
        zone = getString("zone"),
        avatarUrl = getString("avatarUrl"),
        reputation = getLong("reputation")?.toInt() ?: 0,
        status = enumValue(getString("status"), UserStatus.ACTIVE),
        reportsCount = getLong("reportsCount")?.toInt() ?: 0,
        confirmedReportsCount = getLong("confirmedReportsCount")?.toInt() ?: 0,
        createdAt = getDate("createdAt") ?: Date()
    )
}

fun Report.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "userName" to userName,
    "title" to title,
    "description" to description,
    "category" to category.name,
    "priority" to priority.name,
    "status" to status.name,
    "confidence" to confidence,
    "latitude" to latitude,
    "longitude" to longitude,
    "address" to address,
    "anonymous" to anonymous,
    "aiSummary" to aiSummary,
    "media" to media.map { it.toFirestoreMap() },
    "confirmationsCount" to confirmations.size,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

fun ReportMedia.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to reportId,
    "type" to type.name,
    "url" to url,
    "createdAt" to createdAt
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toReport(referenceLat: Double? = null, referenceLng: Double? = null): Report {
    val location = get("location") as? Map<*, *>
    val latitude = getDouble("latitude") ?: (location?.get("lat") as? Number)?.toDouble() ?: 0.0
    val longitude = getDouble("longitude") ?: (location?.get("lng") as? Number)?.toDouble() ?: 0.0
    val mediaMaps = get("media") as? List<Map<String, Any?>>
    val sourceProfile = get("sourceProfile") as? Map<String, Any?>
    val media = mediaMaps.orEmpty().mapNotNull { mediaMap ->
        val mediaId = mediaMap["id"] as? String ?: return@mapNotNull null
        ReportMedia(
            id = mediaId,
            reportId = mediaMap["reportId"] as? String ?: id,
            type = enumValue(mediaMap["type"] as? String, MediaType.IMAGE),
            url = mediaMap["url"] as? String ?: "",
            createdAt = mediaMap["createdAt"] as? Date ?: Date()
        )
    }

    return Report(
        id = getString("id") ?: id,
        userId = getString("userId") ?: getString("authorId") ?: "",
        userName = if (getBoolean("anonymous") ?: getBoolean("isAnonymous") ?: false) null
        else sourceProfile?.get("displayName") as? String ?: getString("userName"),
        sourceRole = enumValue(sourceProfile?.get("role") as? String, UserRole.CITIZEN),
        sourceReputation = (sourceProfile?.get("reputationPoints") as? Number)?.toInt() ?: 0,
        sourceReportCount = (sourceProfile?.get("reportCount") as? Number)?.toInt() ?: 0,
        sourceConfirmedReports = (sourceProfile?.get("confirmedReports") as? Number)?.toInt() ?: 0,
        sourceTrustScore = (sourceProfile?.get("trustScore") as? Number)?.toDouble() ?: 0.5,
        title = getString("title") ?: "Reporte ciudadano",
        description = getString("description") ?: "",
        category = reportCategoryValue(getString("category")),
        priority = enumValue(getString("priority"), ReportPriority.MEDIUM),
        status = enumValue(getString("status"), ReportStatus.PENDING),
        confidence = getDouble("confidence") ?: 0.5,
        latitude = latitude,
        longitude = longitude,
        address = getString("address"),
        anonymous = getBoolean("anonymous") ?: getBoolean("isAnonymous") ?: false,
        aiSummary = getString("aiSummary"),
        media = media,
        confirmCount = getLong("confirmCount")?.toInt() ?: getLong("confirmationsCount")?.toInt() ?: 0,
        falseCount = getLong("falseCount")?.toInt() ?: 0,
        duplicateCount = getLong("duplicateCount")?.toInt() ?: 0,
        urgentCount = getLong("urgentCount")?.toInt() ?: 0,
        resolvedSignalCount = getLong("resolvedSignalCount")?.toInt() ?: 0,
        distanceKm = if (referenceLat != null && referenceLng != null) {
            distanceKm(referenceLat, referenceLng, latitude, longitude)
        } else null,
        createdAt = getDate("createdAt") ?: Date(),
        updatedAt = getDate("updatedAt") ?: Date()
    )
}

fun ReportConfirmation.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to reportId,
    "userId" to userId,
    "type" to type.name,
    "comment" to comment,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toReportConfirmation(): ReportConfirmation = ReportConfirmation(
    id = getString("id") ?: id,
    reportId = getString("reportId") ?: "",
    userId = getString("userId") ?: "",
    type = confirmationTypeValue(getString("type")),
    comment = getString("comment"),
    createdAt = getDate("createdAt") ?: Date()
)

fun Alert.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to reportId,
    "title" to title,
    "message" to message,
    "severity" to severity.name,
    "latitude" to latitude,
    "longitude" to longitude,
    "radius" to radius,
    "readBy" to emptyList<String>(),
    "createdAt" to createdAt
)

fun DocumentSnapshot.toAlert(currentUserId: String? = null): Alert {
    val readBy = get("readBy") as? List<*> ?: emptyList<Any>()
    return Alert(
        id = getString("id") ?: id,
        reportId = getString("reportId"),
        title = getString("title") ?: "Alerta RAVIA",
        message = getString("message") ?: getString("description") ?: "",
        severity = enumValue(getString("severity"), AlertSeverity.INFO),
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
        radius = getDouble("radius"),
        isRead = currentUserId != null && currentUserId in readBy.filterIsInstance<String>(),
        createdAt = getDate("createdAt") ?: Date()
    )
}

fun RiskZone.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "category" to category,
    "riskLevel" to riskLevel.name,
    "latitude" to latitude,
    "longitude" to longitude,
    "radius" to radius,
    "description" to description,
    "recommendations" to recommendations,
    "incidentCount" to incidentCount,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toRiskZone(referenceLat: Double? = null, referenceLng: Double? = null): RiskZone {
    val latitude = getDouble("latitude") ?: 0.0
    val longitude = getDouble("longitude") ?: 0.0
    return RiskZone(
        id = getString("id") ?: id,
        name = getString("name") ?: "Zona de riesgo",
        category = getString("category") ?: "General",
        riskLevel = enumValue(getString("riskLevel"), RiskLevel.MEDIUM),
        latitude = latitude,
        longitude = longitude,
        radius = getDouble("radius") ?: 300.0,
        description = getString("description") ?: "",
        recommendations = getString("recommendations") ?: "",
        incidentCount = getLong("incidentCount")?.toInt() ?: 0,
        distanceKm = if (referenceLat != null && referenceLng != null) {
            distanceKm(referenceLat, referenceLng, latitude, longitude)
        } else null,
        createdAt = getDate("createdAt") ?: Date()
    )
}

fun MissingPerson.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "createdBy" to createdBy,
    "name" to name,
    "age" to age,
    "photoUrl" to photoUrl,
    "lastSeenLocation" to lastSeenLocation,
    "lastSeenLatitude" to lastSeenLatitude,
    "lastSeenLongitude" to lastSeenLongitude,
    "clothing" to clothing,
    "distinctiveSigns" to distinctiveSigns,
    "description" to description,
    "contactInfo" to contactInfo,
    "status" to status.name,
    "sightings" to sightings.map { it.toFirestoreMap() },
    "createdAt" to createdAt
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toMissingPerson(): MissingPerson {
    val sightingsMaps = get("sightings") as? List<Map<String, Any?>>
    return MissingPerson(
        id = getString("id") ?: id,
        createdBy = getString("createdBy") ?: getString("reportedBy") ?: "",
        name = getString("name") ?: "Persona desaparecida",
        age = getLong("age")?.toInt(),
        photoUrl = getString("photoUrl"),
        lastSeenLocation = getString("lastSeenLocation") ?: "",
        lastSeenLatitude = getDouble("lastSeenLatitude") ?: getDouble("lastSeenLat"),
        lastSeenLongitude = getDouble("lastSeenLongitude") ?: getDouble("lastSeenLng"),
        clothing = getString("clothing"),
        distinctiveSigns = getString("distinctiveSigns"),
        description = getString("description") ?: "",
        contactInfo = getString("contactInfo") ?: "",
        status = enumValue(getString("status"), MissingPersonStatus.ACTIVE),
        sightings = sightingsMaps.orEmpty().mapNotNull { it.toSightingOrNull() },
        createdAt = getDate("createdAt") ?: Date()
    )
}

fun Sighting.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "missingPersonId" to missingPersonId,
    "userId" to userId,
    "latitude" to latitude,
    "longitude" to longitude,
    "address" to address,
    "comment" to comment,
    "photoUrl" to photoUrl,
    "createdAt" to createdAt
)

fun Map<String, Any?>.toSightingOrNull(): Sighting? {
    val sightingId = this["id"] as? String ?: return null
    return Sighting(
        id = sightingId,
        missingPersonId = this["missingPersonId"] as? String ?: "",
        userId = this["userId"] as? String ?: this["reportedBy"] as? String ?: "",
        latitude = (this["latitude"] as? Number)?.toDouble() ?: (this["lat"] as? Number)?.toDouble() ?: 0.0,
        longitude = (this["longitude"] as? Number)?.toDouble() ?: (this["lng"] as? Number)?.toDouble() ?: 0.0,
        address = this["address"] as? String,
        comment = this["comment"] as? String,
        photoUrl = this["photoUrl"] as? String,
        createdAt = this["createdAt"] as? Date ?: Date()
    )
}

fun ChatMessage.toFirestoreMap(userId: String): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "content" to content,
    "sender" to sender.name,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toChatMessage(): ChatMessage = ChatMessage(
    id = getString("id") ?: id,
    content = getString("content") ?: "",
    sender = enumValue(getString("sender"), MessageSender.BOT),
    createdAt = getDate("createdAt") ?: Date()
)

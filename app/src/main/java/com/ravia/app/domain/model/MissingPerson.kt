package com.ravia.app.domain.model

import java.util.Date

enum class MissingPersonStatus {
    PENDING_REVIEW, ACTIVE, FOUND, CANCELLED, CLOSED;

    fun displayName(): String = when (this) {
        PENDING_REVIEW -> "Pendiente de verificacion"
        ACTIVE -> "Activo"
        FOUND -> "Encontrado"
        CANCELLED -> "Cancelado"
        CLOSED -> "Cerrado"
    }
}

data class Sighting(
    val id: String,
    val missingPersonId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val comment: String? = null,
    val photoUrl: String? = null,
    val createdAt: Date = Date()
)

data class MissingPerson(
    val id: String,
    val createdBy: String,
    val name: String,
    val age: Int? = null,
    val photoUrl: String? = null,
    val lastSeenLocation: String,
    val lastSeenLatitude: Double? = null,
    val lastSeenLongitude: Double? = null,
    val clothing: String? = null,
    val distinctiveSigns: String? = null,
    val description: String,
    val contactInfo: String,
    val status: MissingPersonStatus = MissingPersonStatus.ACTIVE,
    val sightings: List<Sighting> = emptyList(),
    val createdAt: Date = Date()
)

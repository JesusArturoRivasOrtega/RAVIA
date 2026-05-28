package com.ravia.app.domain.repository

import com.ravia.app.domain.model.MissingPerson
import com.ravia.app.domain.model.Sighting
import kotlinx.coroutines.flow.Flow

interface MissingPersonsRepository {
    fun getMissingPersons(): Flow<List<MissingPerson>>
    suspend fun getMissingPersonById(id: String): Result<MissingPerson>
    suspend fun createMissingPerson(
        name: String,
        age: Int?,
        photoUri: String?,
        lastSeenLocation: String,
        lastSeenLat: Double?,
        lastSeenLng: Double?,
        clothing: String?,
        distinctiveSigns: String?,
        description: String,
        contactInfo: String
    ): Result<MissingPerson>
    suspend fun reportSighting(
        missingPersonId: String,
        latitude: Double,
        longitude: Double,
        comment: String?,
        photoUri: String?
    ): Result<Sighting>
}

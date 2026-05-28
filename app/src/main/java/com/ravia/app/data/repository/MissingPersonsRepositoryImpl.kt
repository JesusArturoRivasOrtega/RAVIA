package com.ravia.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ravia.app.data.dto.CreateMissingPersonRequestDto
import com.ravia.app.data.dto.ReportSightingRequestDto
import com.ravia.app.data.firebase.FirebaseCollections
import com.ravia.app.data.firebase.toMissingPerson
import com.ravia.app.data.mapper.toDomain
import com.ravia.app.data.media.imageUriToJpegDataUri
import com.ravia.app.data.remote.MissingPersonsApi
import com.ravia.app.domain.model.MissingPerson
import com.ravia.app.domain.model.Sighting
import com.ravia.app.domain.repository.MissingPersonsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class MissingPersonsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?,
    private val missingPersonsApi: MissingPersonsApi
) : MissingPersonsRepository {

    override fun getMissingPersons(): Flow<List<MissingPerson>> = flow {
        val persons = runCatching {
            missingPersonsApi.getMissingPersons()
                .requireBody("No se pudieron cargar las fichas de busqueda")
                .map { it.toDomain() }
        }.getOrElse {
            loadMissingPersonsFromFirestore()
        }
        emit(persons)
    }

    override suspend fun getMissingPersonById(id: String): Result<MissingPerson> = runCatching {
        runCatching {
            missingPersonsApi.getMissingPersonById(id)
                .requireBody("No se pudo cargar la ficha de busqueda")
                .toDomain()
        }.getOrElse {
            loadMissingPersonFromFirestore(id) ?: throw it
        }
    }

    override suspend fun createMissingPerson(
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
    ): Result<MissingPerson> = runCatching {
        firebaseAuth?.currentUser ?: throw IllegalStateException("Sesion no iniciada.")
        val photoDataUri = imageUriToJpegDataUri(context, photoUri)

        missingPersonsApi.createMissingPerson(
            CreateMissingPersonRequestDto(
                name = name,
                age = age,
                photoUrl = photoDataUri,
                lastSeenLocation = lastSeenLocation,
                lastSeenLat = lastSeenLat,
                lastSeenLng = lastSeenLng,
                clothing = clothing,
                distinctiveSigns = distinctiveSigns,
                description = description,
                contactInfo = contactInfo
            )
        ).requireBody("No se pudo publicar la ficha de busqueda")
            .toDomain()
    }

    override suspend fun reportSighting(
        missingPersonId: String,
        latitude: Double,
        longitude: Double,
        comment: String?,
        photoUri: String?
    ): Result<Sighting> = runCatching {
        firebaseAuth?.currentUser ?: throw IllegalStateException("Sesion no iniciada.")
        val photoDataUri = imageUriToJpegDataUri(context, photoUri)

        missingPersonsApi.reportSighting(
            missingPersonId,
            ReportSightingRequestDto(latitude, longitude, comment, photoDataUri)
        ).requireBody("No se pudo reportar el avistamiento")
            .toDomain()
            .sightings
            .lastOrNull()
            ?: throw IllegalStateException("El servidor no devolvio el avistamiento creado.")
    }

    private suspend fun loadMissingPersonsFromFirestore(): List<MissingPerson> {
        val db = firestore ?: return emptyList()
        return db.collection(FirebaseCollections.MISSING_PERSONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toMissingPerson() }
    }

    private suspend fun loadMissingPersonFromFirestore(id: String): MissingPerson? {
        val db = firestore ?: return null
        val doc = db.collection(FirebaseCollections.MISSING_PERSONS).document(id).get().await()
        return doc.takeIf { it.exists() }?.toMissingPerson()
    }
}

private fun <T> Response<T>.requireBody(action: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException("$action: respuesta vacia.")
    }
    val detail = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    throw IllegalStateException("$action (${code()}): $detail")
}

package com.ravia.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ravia.app.data.dto.ConfirmReportRequestDto
import com.ravia.app.data.dto.ReportMediaDto
import com.ravia.app.data.dto.UpdateReportRequestDto
import com.ravia.app.data.dto.UpdateReportStatusRequestDto
import com.ravia.app.data.firebase.FirebaseCollections
import com.ravia.app.data.firebase.toReport
import com.ravia.app.data.mapper.buildCreateReportRequest
import com.ravia.app.data.mapper.simpleAiAnalysis
import com.ravia.app.data.mapper.toBackendValue
import com.ravia.app.data.mapper.toDomain
import com.ravia.app.data.media.imageUriToJpegDataUri
import com.ravia.app.data.remote.ReportsApi
import com.ravia.app.domain.model.AiAnalysis
import com.ravia.app.domain.model.ConfirmationType
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.domain.model.ReportStatus
import com.ravia.app.domain.model.isVisibleOnMap
import com.ravia.app.domain.repository.ReportsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?,
    private val reportsApi: ReportsApi
) : ReportsRepository {

    override fun getNearbyReports(lat: Double, lng: Double, radiusKm: Double): Flow<List<Report>> = flow {
        val reports = runCatching {
            reportsApi.getNearbyReports(lat, lng, radiusKm)
                .requireBody("No se pudieron cargar los reportes")
                .map { it.toDomain(lat, lng) }
                .filter { it.status.isVisibleOnMap() }
                .filter { (it.distanceKm ?: Double.MAX_VALUE) <= radiusKm }
                .sortedBy { it.distanceKm }
        }.getOrElse {
            loadNearbyReportsFromFirestore(lat, lng, radiusKm)
        }
        emit(reports)
    }

    override suspend fun getReportById(id: String): Result<Report> = runCatching {
        runCatching {
            reportsApi.getReportById(id)
                .requireBody("No se pudo cargar el reporte")
                .toDomain()
        }.getOrElse {
            loadReportFromFirestore(id) ?: throw it
        }
    }

    override fun getUserReports(userId: String): Flow<List<Report>> = flow {
        val reports = runCatching {
            reportsApi.getUserReports().requireBody("No se pudieron cargar tus reportes")
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }
        }.getOrElse {
            loadUserReportsFromFirestore(userId)
        }
        emit(reports)
    }

    override suspend fun createReport(
        title: String,
        description: String,
        category: ReportCategory,
        priority: ReportPriority,
        latitude: Double,
        longitude: Double,
        address: String?,
        anonymous: Boolean,
        imageUris: List<String>
    ): Result<Report> = runCatching {
        firebaseAuth?.currentUser ?: throw IllegalStateException("Sesion no iniciada.")

        val mediaDtos = imageUris.mapIndexedNotNull { index, uri ->
            val url = imageUriToJpegDataUri(context, uri) ?: return@mapIndexedNotNull null
            ReportMediaDto(
                id = "media_$index",
                url = url,
                type = "image"
            )
        }

        reportsApi.createReport(
            buildCreateReportRequest(
                title = title,
                description = description,
                category = category,
                priority = priority,
                latitude = latitude,
                longitude = longitude,
                address = address,
                anonymous = anonymous,
                media = mediaDtos
            )
        ).requireBody("No se pudo crear el reporte")
            .toDomain(latitude, longitude)
    }

    override suspend fun analyzeReport(description: String, imageUrl: String?): Result<AiAnalysis> =
        Result.success(simpleAiAnalysis(description))

    override suspend fun confirmReport(
        reportId: String,
        type: ConfirmationType,
        comment: String?
    ): Result<Unit> = runCatching {
        reportsApi.confirmReport(reportId, ConfirmReportRequestDto(type.toBackendValue(), comment))
            .requireSuccessful("No se pudo enviar la validacion")
    }

    override suspend fun updateReportStatus(reportId: String, status: ReportStatus): Result<Unit> = runCatching {
        reportsApi.updateStatus(reportId, UpdateReportStatusRequestDto(status.toBackendValue()))
            .requireSuccessful("No se pudo actualizar el estado")
    }

    override suspend fun updateReportPriority(reportId: String, priority: ReportPriority): Result<Unit> = runCatching {
        reportsApi.updateReport(
            reportId,
            UpdateReportRequestDto(priority = priority.name.lowercase())
        ).requireSuccessful("No se pudo actualizar la prioridad")
    }

    private suspend fun loadNearbyReportsFromFirestore(
        lat: Double,
        lng: Double,
        radiusKm: Double
    ): List<Report> {
        val db = firestore ?: return emptyList()
        return db.collection(FirebaseCollections.REPORTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toReport(lat, lng) }
            .filter { it.status.isVisibleOnMap() }
            .filter { (it.distanceKm ?: Double.MAX_VALUE) <= radiusKm }
            .sortedBy { it.distanceKm }
    }

    private suspend fun loadReportFromFirestore(id: String): Report? {
        val db = firestore ?: return null
        val doc = db.collection(FirebaseCollections.REPORTS).document(id).get().await()
        return doc.takeIf { it.exists() }?.toReport()
    }

    private suspend fun loadUserReportsFromFirestore(userId: String): List<Report> {
        val activeUserId = firebaseAuth?.currentUser?.uid ?: userId
        if (activeUserId.isBlank()) return emptyList()
        val db = firestore ?: return emptyList()
        return db.collection(FirebaseCollections.REPORTS)
            .whereEqualTo("authorId", activeUserId)
            .get()
            .await()
            .documents
            .map { it.toReport() }
            .sortedByDescending { it.createdAt }
    }
}

private fun <T> Response<T>.requireBody(action: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException("$action: respuesta vacia.")
    }
    throw IllegalStateException("$action (${code()}): ${errorText()}")
}

private fun Response<*>.requireSuccessful(action: String) {
    if (!isSuccessful) {
        throw IllegalStateException("$action (${code()}): ${errorText()}")
    }
}

private fun Response<*>.errorText(): String {
    val raw = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    val parsedMessage = runCatching {
        JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
    }.getOrNull()
    return (parsedMessage ?: raw).toFriendlyApiMessage()
}

private fun String.toFriendlyApiMessage(): String {
    val normalized = lowercase()
    return when {
        "already confirmed" in normalized -> "Ya habias validado este reporte."
        "unauthorized" in normalized -> "Tu sesion expiro. Inicia sesion de nuevo."
        "forbidden" in normalized -> "No tienes permisos para realizar esta accion."
        else -> this
    }
}

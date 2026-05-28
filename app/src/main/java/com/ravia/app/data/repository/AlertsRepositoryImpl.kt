package com.ravia.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ravia.app.data.firebase.FirebaseCollections
import com.ravia.app.data.firebase.toAlert
import com.ravia.app.data.mapper.toDomain
import com.ravia.app.data.remote.AlertsApi
import com.ravia.app.domain.model.Alert
import com.ravia.app.domain.repository.AlertsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AlertsRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?,
    private val alertsApi: AlertsApi
) : AlertsRepository {

    private val userId: String?
        get() = firebaseAuth?.currentUser?.uid

    override fun getAlerts(): Flow<List<Alert>> = flow {
        val alerts = runCatching {
            alertsApi.getAlerts()
                .requireBody("No se pudieron cargar las alertas")
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }
        }.getOrElse {
            loadAlertsFromFirestore()
        }
        emit(alerts)
    }

    override fun getRecentAlerts(limit: Int): Flow<List<Alert>> =
        getAlerts().map { it.take(limit) }

    override suspend fun getAlertById(id: String): Result<Alert> = runCatching {
        runCatching {
            alertsApi.getAlertById(id)
                .requireBody("No se pudo cargar la alerta")
                .toDomain()
        }.getOrElse {
            loadAlertFromFirestore(id) ?: throw it
        }
    }

    override suspend fun markAsRead(alertId: String): Result<Unit> = runCatching {
        val id = userId ?: throw IllegalStateException("Sesion no iniciada.")
        val db = firestore ?: return@runCatching
        db.collection(FirebaseCollections.ALERTS)
            .document(alertId)
            .update("readBy", FieldValue.arrayUnion(id))
            .await()
    }

    override suspend fun markAllAsRead(): Result<Unit> = runCatching {
        val id = userId ?: throw IllegalStateException("Sesion no iniciada.")
        val db = firestore ?: return@runCatching
        val docs = db.collection(FirebaseCollections.ALERTS).get().await().documents
        docs.forEach { it.reference.update("readBy", FieldValue.arrayUnion(id)).await() }
    }

    override fun getUnreadCount(): Flow<Int> =
        getAlerts().map { list -> list.count { !it.isRead } }

    private suspend fun loadAlertsFromFirestore(): List<Alert> {
        val db = firestore ?: return emptyList()
        return db.collection(FirebaseCollections.ALERTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toAlert(userId) }
    }

    private suspend fun loadAlertFromFirestore(id: String): Alert? {
        val db = firestore ?: return null
        val doc = db.collection(FirebaseCollections.ALERTS).document(id).get().await()
        return doc.takeIf { it.exists() }?.toAlert(userId)
    }
}

private fun <T> Response<T>.requireBody(action: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException("$action: respuesta vacia.")
    }
    val detail = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    throw IllegalStateException("$action (${code()}): $detail")
}

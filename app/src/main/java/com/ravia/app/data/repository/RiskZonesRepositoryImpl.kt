package com.ravia.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ravia.app.data.firebase.FirebaseCollections
import com.ravia.app.data.firebase.toRiskZone
import com.ravia.app.data.mapper.toDomain
import com.ravia.app.data.remote.RiskZonesApi
import com.ravia.app.domain.model.RiskZone
import com.ravia.app.domain.repository.RiskZonesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class RiskZonesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val riskZonesApi: RiskZonesApi
) : RiskZonesRepository {

    override fun getNearbyRiskZones(lat: Double, lng: Double, radiusKm: Double): Flow<List<RiskZone>> = flow {
        val zones = runCatching {
            riskZonesApi.getNearbyRiskZones()
                .requireBody("No se pudieron cargar las zonas de riesgo")
                .map { it.toDomain(lat, lng) }
        }.getOrElse {
            loadRiskZonesFromFirestore(lat, lng)
        }.filter { (it.distanceKm ?: Double.MAX_VALUE) <= radiusKm }
            .sortedByDescending { it.riskLevel.ordinal }

        emit(zones)
    }

    override suspend fun getRiskZoneById(id: String): Result<RiskZone> = runCatching {
        runCatching {
            riskZonesApi.getRiskZoneById(id)
                .requireBody("No se pudo cargar la zona de riesgo")
                .toDomain()
        }.getOrElse {
            loadRiskZoneFromFirestore(id) ?: throw it
        }
    }

    override fun getAllRiskZones(): Flow<List<RiskZone>> = flow {
        val zones = runCatching {
            riskZonesApi.getAllRiskZones()
                .requireBody("No se pudieron cargar las zonas de riesgo")
                .map { it.toDomain() }
        }.getOrElse {
            loadRiskZonesFromFirestore()
        }
        emit(zones)
    }

    private suspend fun loadRiskZonesFromFirestore(
        lat: Double? = null,
        lng: Double? = null
    ): List<RiskZone> {
        val db = firestore ?: return emptyList()
        return db.collection(FirebaseCollections.RISK_ZONES)
            .get()
            .await()
            .documents
            .map { if (lat != null && lng != null) it.toRiskZone(lat, lng) else it.toRiskZone() }
    }

    private suspend fun loadRiskZoneFromFirestore(id: String): RiskZone? {
        val db = firestore ?: return null
        val doc = db.collection(FirebaseCollections.RISK_ZONES).document(id).get().await()
        return doc.takeIf { it.exists() }?.toRiskZone()
    }
}

private fun <T> Response<T>.requireBody(action: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException("$action: respuesta vacia.")
    }
    val detail = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    throw IllegalStateException("$action (${code()}): $detail")
}

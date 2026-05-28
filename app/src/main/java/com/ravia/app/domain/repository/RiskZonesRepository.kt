package com.ravia.app.domain.repository

import com.ravia.app.domain.model.RiskZone
import kotlinx.coroutines.flow.Flow

interface RiskZonesRepository {
    fun getNearbyRiskZones(lat: Double, lng: Double, radiusKm: Double): Flow<List<RiskZone>>
    suspend fun getRiskZoneById(id: String): Result<RiskZone>
    fun getAllRiskZones(): Flow<List<RiskZone>>
}

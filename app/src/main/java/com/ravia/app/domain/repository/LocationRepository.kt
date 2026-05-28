package com.ravia.app.domain.repository

import com.ravia.app.domain.model.LocationPoint

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<LocationPoint>
    suspend fun getAddressForLocation(latitude: Double, longitude: Double): Result<String>
}

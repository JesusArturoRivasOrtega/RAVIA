package com.ravia.app.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.ravia.app.domain.model.LocationPoint
import com.ravia.app.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LocationPoint> = runCatching {
        if (!hasLocationPermission()) {
            throw IllegalStateException("Permiso de ubicacion no concedido.")
        }

        val location = locationClient.lastLocation.await()
            ?: locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await()
            ?: throw IllegalStateException("No se pudo obtener la ubicacion actual.")

        LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    override suspend fun getAddressForLocation(latitude: Double, longitude: Double): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?: throw IllegalStateException("No se pudo encontrar la calle de esa ubicacion.")

                val street = listOfNotNull(address.thoroughfare, address.subThoroughfare)
                    .joinToString(" ")
                    .trim()
                val area = listOfNotNull(address.subLocality, address.locality)
                    .distinct()
                    .joinToString(", ")
                    .trim()
                val compactAddress = listOf(street, area)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                compactAddress.ifBlank { address.getAddressLine(0).orEmpty() }
                    .takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("No se pudo encontrar la calle de esa ubicacion.")
            }
        }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }
}

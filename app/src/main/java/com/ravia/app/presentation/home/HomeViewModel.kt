package com.ravia.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.Alert
import com.ravia.app.domain.model.LocationPoint
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.RiskZone
import com.ravia.app.domain.model.User
import com.ravia.app.domain.repository.AlertsRepository
import com.ravia.app.domain.repository.AuthRepository
import com.ravia.app.domain.repository.LocationRepository
import com.ravia.app.domain.repository.MissingPersonsRepository
import com.ravia.app.domain.repository.ReportsRepository
import com.ravia.app.domain.repository.RiskZonesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_RADIUS = 5.0

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reportsRepository: ReportsRepository,
    private val alertsRepository: AlertsRepository,
    private val riskZonesRepository: RiskZonesRepository,
    private val missingPersonsRepository: MissingPersonsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _nearbyReports = MutableStateFlow<UiState<List<Report>>>(UiState.Loading)
    val nearbyReports: StateFlow<UiState<List<Report>>> = _nearbyReports.asStateFlow()

    private val _recentAlerts = MutableStateFlow<UiState<List<Alert>>>(UiState.Loading)
    val recentAlerts: StateFlow<UiState<List<Alert>>> = _recentAlerts.asStateFlow()

    private val _riskZones = MutableStateFlow<UiState<List<RiskZone>>>(UiState.Loading)
    val riskZones: StateFlow<UiState<List<RiskZone>>> = _riskZones.asStateFlow()

    private val _unreadAlertCount = MutableStateFlow(0)
    val unreadAlertCount: StateFlow<Int> = _unreadAlertCount.asStateFlow()

    private val _missingPersonsCount = MutableStateFlow(0)
    val missingPersonsCount: StateFlow<Int> = _missingPersonsCount.asStateFlow()

    private val _locationState = MutableStateFlow<UiState<LocationPoint>>(UiState.Empty)
    val locationState: StateFlow<UiState<LocationPoint>> = _locationState.asStateFlow()

    private var currentLocation: LocationPoint? = null

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                _currentUser.value = user
            }
        }
        loadRecentAlerts()
        loadMissingPersonsCount()
        observeUnreadCount()
    }

    private fun loadNearbyReports() {
        val location = currentLocation
        if (location == null) {
            _nearbyReports.value = UiState.Empty
            return
        }
        viewModelScope.launch {
            reportsRepository.getNearbyReports(location.latitude, location.longitude, DEFAULT_RADIUS)
                .collectLatest { reports ->
                    _nearbyReports.value = if (reports.isEmpty()) UiState.Empty
                    else UiState.Success(reports.take(5))
                }
        }
    }

    private fun loadRecentAlerts() {
        viewModelScope.launch {
            alertsRepository.getRecentAlerts(5).collectLatest { alerts ->
                _recentAlerts.value = if (alerts.isEmpty()) UiState.Empty
                else UiState.Success(alerts)
            }
        }
    }

    private fun loadRiskZones() {
        val location = currentLocation
        if (location == null) {
            _riskZones.value = UiState.Empty
            return
        }
        viewModelScope.launch {
            riskZonesRepository.getNearbyRiskZones(location.latitude, location.longitude, 10.0)
                .collectLatest { zones ->
                    _riskZones.value = if (zones.isEmpty()) UiState.Empty
                    else UiState.Success(zones)
                }
        }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            alertsRepository.getUnreadCount().collectLatest { count ->
                _unreadAlertCount.value = count
            }
        }
    }

    private fun loadMissingPersonsCount() {
        viewModelScope.launch {
            missingPersonsRepository.getMissingPersons().collectLatest { persons ->
                _missingPersonsCount.value = persons.size
            }
        }
    }

    fun loadCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = UiState.Loading
            locationRepository.getCurrentLocation().fold(
                onSuccess = { location ->
                    currentLocation = location
                    _locationState.value = UiState.Success(location)
                    loadNearbyReports()
                    loadRiskZones()
                },
                onFailure = {
                    _locationState.value = UiState.Error(it.message ?: "No se pudo obtener ubicacion")
                    _nearbyReports.value = UiState.Empty
                    _riskZones.value = UiState.Empty
                }
            )
        }
    }

    fun refresh() {
        loadAll()
        if (currentLocation == null) {
            loadCurrentLocation()
        } else {
            loadNearbyReports()
            loadRiskZones()
        }
    }
}

package com.ravia.app.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.LocationPoint
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.RiskZone
import com.ravia.app.domain.model.isVisibleOnMap
import com.ravia.app.domain.repository.LocationRepository
import com.ravia.app.domain.repository.ReportsRepository
import com.ravia.app.domain.repository.RiskZonesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    private val riskZonesRepository: RiskZonesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _reports = MutableStateFlow<UiState<List<Report>>>(UiState.Loading)
    val reports: StateFlow<UiState<List<Report>>> = _reports.asStateFlow()

    private val _riskZones = MutableStateFlow<List<RiskZone>>(emptyList())
    val riskZones: StateFlow<List<RiskZone>> = _riskZones.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ReportCategory?>(null)
    val selectedCategory: StateFlow<ReportCategory?> = _selectedCategory.asStateFlow()

    private val _showRiskZones = MutableStateFlow(true)
    val showRiskZones: StateFlow<Boolean> = _showRiskZones.asStateFlow()

    private val _selectedReport = MutableStateFlow<Report?>(null)
    val selectedReport: StateFlow<Report?> = _selectedReport.asStateFlow()

    private val _riskZoneDetail = MutableStateFlow<UiState<RiskZone>>(UiState.Loading)
    val riskZoneDetail: StateFlow<UiState<RiskZone>> = _riskZoneDetail.asStateFlow()

    private val _locationState = MutableStateFlow<UiState<LocationPoint>>(UiState.Empty)
    val locationState: StateFlow<UiState<LocationPoint>> = _locationState.asStateFlow()

    private var currentLocation: LocationPoint? = null
    private var allNearbyReports: List<Report> = emptyList()
    private var reportsJob: Job? = null
    private var riskZonesJob: Job? = null

    private fun applyReportFilters() {
        val category = _selectedCategory.value
        val filtered = allNearbyReports
            .filter { it.status.isVisibleOnMap() }
            .filter { category == null || it.category == category }
        _reports.value = if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered)
        val selected = _selectedReport.value
        if (selected != null && filtered.none { it.id == selected.id }) {
            _selectedReport.value = null
        }
    }

    private fun load() {
        val location = currentLocation
        if (location == null) {
            allNearbyReports = emptyList()
            _reports.value = UiState.Empty
            _riskZones.value = emptyList()
            return
        }

        reportsJob?.cancel()
        riskZonesJob?.cancel()

        reportsJob = viewModelScope.launch {
            reportsRepository.getNearbyReports(location.latitude, location.longitude, 10.0).collectLatest { list ->
                allNearbyReports = list.filter { it.status.isVisibleOnMap() }
                applyReportFilters()
            }
        }
        riskZonesJob = viewModelScope.launch {
            riskZonesRepository.getNearbyRiskZones(location.latitude, location.longitude, 15.0).collectLatest {
                _riskZones.value = it
            }
        }
    }

    fun setCategory(category: ReportCategory?) {
        _selectedCategory.value = category
        applyReportFilters()
    }

    fun toggleRiskZones() {
        _showRiskZones.value = !_showRiskZones.value
    }

    fun selectReport(report: Report?) {
        _selectedReport.value = report
    }

    fun loadRiskZoneDetail(zoneId: String) {
        viewModelScope.launch {
            _riskZoneDetail.value = UiState.Loading
            riskZonesRepository.getRiskZoneById(zoneId).fold(
                onSuccess = { _riskZoneDetail.value = UiState.Success(it) },
                onFailure = { _riskZoneDetail.value = UiState.Error(it.message ?: "No se pudo cargar la zona") }
            )
        }
    }

    fun loadCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = UiState.Loading
            locationRepository.getCurrentLocation().fold(
                onSuccess = { location ->
                    currentLocation = location
                    _locationState.value = UiState.Success(location)
                    load()
                },
                onFailure = {
                    _locationState.value = UiState.Error(it.message ?: "No se pudo obtener ubicacion")
                    _reports.value = UiState.Empty
                    _riskZones.value = emptyList()
                }
            )
        }
    }
}

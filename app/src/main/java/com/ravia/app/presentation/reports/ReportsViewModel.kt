package com.ravia.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.*
import com.ravia.app.domain.repository.LocationRepository
import com.ravia.app.domain.usecase.auth.GetCurrentUserUseCase
import com.ravia.app.domain.usecase.reports.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateReportForm(
    val title: String = "",
    val description: String = "",
    val category: ReportCategory? = null,
    val priority: ReportPriority = ReportPriority.MEDIUM,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val anonymous: Boolean = false,
    val imageUris: List<String> = emptyList()
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val createReportUseCase: CreateReportUseCase,
    private val analyzeReportUseCase: AnalyzeReportUseCase,
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val confirmReportUseCase: ConfirmReportUseCase,
    private val getUserReportsUseCase: GetUserReportsUseCase,
    private val updateReportStatusUseCase: UpdateReportStatusUseCase,
    private val locationRepository: LocationRepository,
    getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentUserId: StateFlow<String?> = currentUser
        .map { it?.firebaseUid ?: it?.id }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentUserRole: StateFlow<UserRole> = currentUser
        .map { it?.role ?: UserRole.CITIZEN }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserRole.CITIZEN)

    // ─── Create flow ─────────────────────────────────────────────────────────
    private val _form = MutableStateFlow(CreateReportForm())
    val form: StateFlow<CreateReportForm> = _form.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<UiState<AiAnalysis>?>(null)
    val aiAnalysis: StateFlow<UiState<AiAnalysis>?> = _aiAnalysis.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Report>?>(null)
    val createState: StateFlow<UiState<Report>?> = _createState.asStateFlow()

    private val _locationState = MutableStateFlow<UiState<LocationPoint>>(UiState.Empty)
    val locationState: StateFlow<UiState<LocationPoint>> = _locationState.asStateFlow()

    // ─── Detail ───────────────────────────────────────────────────────────────
    private val _reportDetail = MutableStateFlow<UiState<Report>>(UiState.Loading)
    val reportDetail: StateFlow<UiState<Report>> = _reportDetail.asStateFlow()

    private val _confirmState = MutableStateFlow<UiState<Unit>?>(null)
    val confirmState: StateFlow<UiState<Unit>?> = _confirmState.asStateFlow()

    // ─── My reports ───────────────────────────────────────────────────────────
    private val _myReports = MutableStateFlow<UiState<List<Report>>>(UiState.Loading)
    val myReports: StateFlow<UiState<List<Report>>> = _myReports.asStateFlow()

    // ─── Form mutations ───────────────────────────────────────────────────────
    fun updateForm(update: CreateReportForm.() -> CreateReportForm) {
        _form.value = _form.value.update()
    }

    fun analyzeReport() {
        val f = _form.value
        if (f.description.isBlank()) return
        viewModelScope.launch {
            _aiAnalysis.value = UiState.Loading
            analyzeReportUseCase(f.description).fold(
                onSuccess = {
                    _aiAnalysis.value = UiState.Success(it)
                    // Auto-fill suggested values if category not yet chosen
                    if (f.category == null) {
                        _form.value = f.copy(category = it.suggestedCategory, priority = it.suggestedPriority)
                    }
                },
                onFailure = { _aiAnalysis.value = UiState.Error(it.message ?: "Error al analizar") }
            )
        }
    }

    fun submitReport() {
        val f = _form.value
        val category = f.category ?: ReportCategory.OTHER
        val latitude = f.latitude
        val longitude = f.longitude
        if (f.title.trim().length < 5) {
            _createState.value = UiState.Error("El titulo debe tener al menos 5 caracteres.")
            return
        }
        if (f.description.trim().length < 10) {
            _createState.value = UiState.Error("La descripcion debe tener al menos 10 caracteres.")
            return
        }
        if (latitude == null || longitude == null) {
            _createState.value = UiState.Error("Activa la ubicacion para enviar el reporte.")
            return
        }
        if (f.address.isBlank()) {
            _createState.value = UiState.Error("Agrega la calle o una referencia de donde ocurrio.")
            return
        }
        viewModelScope.launch {
            _createState.value = UiState.Loading
            createReportUseCase(
                title = f.title,
                description = f.description,
                category = category,
                priority = f.priority,
                latitude = latitude,
                longitude = longitude,
                address = f.address.ifBlank { null },
                anonymous = f.anonymous,
                imageUris = f.imageUris
            ).fold(
                onSuccess = { _createState.value = UiState.Success(it) },
                onFailure = { _createState.value = UiState.Error(it.message ?: "Error al crear reporte") }
            )
        }
    }

    fun loadCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = UiState.Loading
            locationRepository.getCurrentLocation().fold(
                onSuccess = { location ->
                    _locationState.value = UiState.Success(location)
                    _form.value = _form.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    resolveAddressForLocation(location.latitude, location.longitude)
                },
                onFailure = {
                    _locationState.value = UiState.Error(it.message ?: "No se pudo obtener ubicacion")
                }
            )
        }
    }

    fun setIncidentLocation(latitude: Double, longitude: Double) {
        val location = LocationPoint(latitude, longitude)
        _form.value = _form.value.copy(latitude = latitude, longitude = longitude)
        _locationState.value = UiState.Success(location)
        resolveAddressForLocation(latitude, longitude)
    }

    private fun resolveAddressForLocation(latitude: Double, longitude: Double) {
        if (_form.value.address.isNotBlank()) return
        viewModelScope.launch {
            locationRepository.getAddressForLocation(latitude, longitude).onSuccess { address ->
                val current = _form.value
                if (
                    current.address.isBlank() &&
                    current.latitude == latitude &&
                    current.longitude == longitude
                ) {
                    _form.value = current.copy(address = address)
                }
            }
        }
    }

    fun loadReportDetail(id: String) {
        viewModelScope.launch {
            _reportDetail.value = UiState.Loading
            getReportByIdUseCase(id).fold(
                onSuccess = { _reportDetail.value = UiState.Success(it) },
                onFailure = { _reportDetail.value = UiState.Error(it.message ?: "Error al cargar") }
            )
        }
    }

    fun confirmReport(reportId: String, type: ConfirmationType, comment: String? = null) {
        // Local guard: prevent the author from validating their own report
        val report = (reportDetail.value as? UiState.Success)?.data
        val activeUid = currentUserId.value
        if (report != null && activeUid != null && report.userId == activeUid) {
            _confirmState.value = UiState.Error("No puedes validar tu propio reporte")
            return
        }
        viewModelScope.launch {
            _confirmState.value = UiState.Loading
            confirmReportUseCase(reportId, type, comment).fold(
                onSuccess = {
                    _confirmState.value = UiState.Success(Unit)
                    loadReportDetail(reportId)
                },
                onFailure = { _confirmState.value = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun moderatorUpdateStatus(reportId: String, status: ReportStatus) {
        val role = currentUserRole.value
        if (role != UserRole.MODERATOR && role != UserRole.ADMIN) {
            _confirmState.value = UiState.Error("Solo moderadores pueden cambiar el estado")
            return
        }
        viewModelScope.launch {
            _confirmState.value = UiState.Loading
            updateReportStatusUseCase(reportId, status).fold(
                onSuccess = {
                    _confirmState.value = UiState.Success(Unit)
                    loadReportDetail(reportId)
                },
                onFailure = { _confirmState.value = UiState.Error(it.message ?: "No se pudo actualizar") }
            )
        }
    }

    fun loadMyReports() {
        viewModelScope.launch {
            getUserReportsUseCase("")
                .collect { list ->
                    _myReports.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
        }
    }

    fun resetCreateFlow() {
        _form.value = CreateReportForm()
        _aiAnalysis.value = null
        _createState.value = null
    }

    fun resetConfirmState() { _confirmState.value = null }
}

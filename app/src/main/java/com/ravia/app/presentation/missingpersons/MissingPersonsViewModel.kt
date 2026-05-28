package com.ravia.app.presentation.missingpersons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.LocationPoint
import com.ravia.app.domain.model.MissingPerson
import com.ravia.app.domain.repository.LocationRepository
import com.ravia.app.domain.repository.MissingPersonsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissingPersonsViewModel @Inject constructor(
    private val repo: MissingPersonsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _list = MutableStateFlow<UiState<List<MissingPerson>>>(UiState.Loading)
    val list: StateFlow<UiState<List<MissingPerson>>> = _list.asStateFlow()

    private val _detail = MutableStateFlow<UiState<MissingPerson>>(UiState.Loading)
    val detail: StateFlow<UiState<MissingPerson>> = _detail.asStateFlow()

    private val _createState = MutableStateFlow<UiState<MissingPerson>?>(null)
    val createState: StateFlow<UiState<MissingPerson>?> = _createState.asStateFlow()

    private val _locationState = MutableStateFlow<UiState<LocationPoint>>(UiState.Empty)
    val locationState: StateFlow<UiState<LocationPoint>> = _locationState.asStateFlow()

    init { loadList() }

    fun loadList() {
        viewModelScope.launch {
            repo.getMissingPersons().collectLatest { persons ->
                _list.value = if (persons.isEmpty()) UiState.Empty else UiState.Success(persons)
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detail.value = UiState.Loading
            repo.getMissingPersonById(id).fold(
                onSuccess = { _detail.value = UiState.Success(it) },
                onFailure = { _detail.value = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun createMissingPerson(
        name: String, age: Int?, photoUri: String?,
        lastSeenLocation: String, lat: Double?, lng: Double?,
        clothing: String?, distinctiveSigns: String?,
        description: String, contactInfo: String
    ) {
        val cleanName = name.trim()
        val cleanLastSeenLocation = lastSeenLocation.trim()
        val cleanDescription = description.trim()
        val cleanContactInfo = contactInfo.trim()

        if (cleanName.length < 3) {
            _createState.value = UiState.Error("El nombre debe tener al menos 3 caracteres.")
            return
        }
        if (cleanLastSeenLocation.length < 3) {
            _createState.value = UiState.Error("La ultima ubicacion debe tener al menos 3 caracteres.")
            return
        }
        if (cleanDescription.length < 10) {
            _createState.value = UiState.Error("La descripcion debe tener al menos 10 caracteres.")
            return
        }
        if (cleanContactInfo.length < 5) {
            _createState.value = UiState.Error("El contacto debe tener al menos 5 caracteres.")
            return
        }

        viewModelScope.launch {
            _createState.value = UiState.Loading
            repo.createMissingPerson(
                cleanName,
                age,
                photoUri,
                cleanLastSeenLocation,
                lat,
                lng,
                clothing?.trim()?.ifBlank { null },
                distinctiveSigns?.trim()?.ifBlank { null },
                cleanDescription,
                cleanContactInfo
            ).fold(
                onSuccess = { _createState.value = UiState.Success(it) },
                onFailure = { _createState.value = UiState.Error(it.message ?: "Error") }
            )
        }
    }

    fun reportSighting(personId: String, lat: Double, lng: Double, comment: String?) {
        viewModelScope.launch {
            repo.reportSighting(personId, lat, lng, comment, null)
            loadDetail(personId)
        }
    }

    fun loadCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = UiState.Loading
            locationRepository.getCurrentLocation().fold(
                onSuccess = { _locationState.value = UiState.Success(it) },
                onFailure = { _locationState.value = UiState.Error(it.message ?: "No se pudo obtener ubicacion") }
            )
        }
    }

    fun resetCreateState() { _createState.value = null }
}

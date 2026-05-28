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
        viewModelScope.launch {
            _createState.value = UiState.Loading
            repo.createMissingPerson(name, age, photoUri, lastSeenLocation, lat, lng, clothing, distinctiveSigns, description, contactInfo).fold(
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

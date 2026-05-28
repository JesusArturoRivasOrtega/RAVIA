package com.ravia.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.User
import com.ravia.app.domain.repository.AuthRepository
import com.ravia.app.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<User>?>(null)
    val updateState: StateFlow<UiState<User>?> = _updateState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { _currentUser.value = it }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.value = true
        }
    }

    fun updateProfile(name: String, zone: String?, phone: String?) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            authRepository.updateProfile(name, zone, phone).fold(
                onSuccess = { _updateState.value = UiState.Success(it) },
                onFailure = { _updateState.value = UiState.Error(it.message ?: "No se pudo guardar") }
            )
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }
}

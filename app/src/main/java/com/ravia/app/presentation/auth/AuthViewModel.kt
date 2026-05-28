package com.ravia.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.User
import com.ravia.app.domain.usecase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<User>>(UiState.Empty)
    val authState: StateFlow<UiState<User>> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _passwordResetState = MutableStateFlow<UiState<Unit>?>(null)
    val passwordResetState: StateFlow<UiState<Unit>?> = _passwordResetState.asStateFlow()

    init {
        observeCurrentUser()
        checkLoginStatus()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = isLoggedInUseCase()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            loginUseCase(email.trim(), password).fold(
                onSuccess = { _authState.value = UiState.Success(it) },
                onFailure = { _authState.value = UiState.Error(it.message ?: "Error al iniciar sesión") }
            )
        }
    }

    fun register(name: String, email: String, password: String, zone: String?) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            registerUseCase(name.trim(), email.trim(), password, zone?.trim()).fold(
                onSuccess = { _authState.value = UiState.Success(it) },
                onFailure = { _authState.value = UiState.Error(it.message ?: "Error al registrarse") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _isLoggedIn.value = false
            _authState.value = UiState.Empty
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _passwordResetState.value = UiState.Loading
            sendPasswordResetUseCase(email.trim()).fold(
                onSuccess = { _passwordResetState.value = UiState.Success(Unit) },
                onFailure = { _passwordResetState.value = UiState.Error(it.message ?: "Error al enviar correo") }
            )
        }
    }

    fun resetAuthState() {
        _authState.value = UiState.Empty
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = null
    }
}

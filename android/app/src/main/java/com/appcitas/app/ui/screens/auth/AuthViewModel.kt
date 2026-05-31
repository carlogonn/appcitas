package com.appcitas.app.ui.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthEvent {
    object NavigateToMain : AuthEvent()
    object NavigateToLogin : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val currentUser = mutableStateOf<User?>(null)

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.isLoggedIn().collect { isLoggedIn ->
                if (isLoggedIn) {
                    loadCurrentUser()
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    currentUser.value = user
                }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            error.value = "Email y contraseña son requeridos"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            authRepository.login(email, password)
                .onSuccess { user ->
                    isLoading.value = false
                    currentUser.value = user
                    _events.emit(AuthEvent.NavigateToMain)
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al iniciar sesión"
                }
        }
    }

    fun register(
        email: String,
        username: String,
        password: String,
        birthDate: String,
        gender: String?
    ) {
        if (email.isBlank() || username.isBlank() || password.isBlank() || birthDate.isBlank()) {
            error.value = "Todos los campos obligatorios deben ser completados"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            authRepository.register(email, username, password, birthDate, gender)
                .onSuccess { user ->
                    isLoading.value = false
                    currentUser.value = user
                    _events.emit(AuthEvent.NavigateToMain)
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al registrar"
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    currentUser.value = null
                    _events.emit(AuthEvent.NavigateToLogin)
                }
                .onFailure { exception ->
                    error.value = exception.message ?: "Error al cerrar sesión"
                }
        }
    }

    fun clearError() {
        error.value = null
    }
}

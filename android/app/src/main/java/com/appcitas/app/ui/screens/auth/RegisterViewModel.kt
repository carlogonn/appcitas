package com.appcitas.app.ui.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcitas.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    fun register(
        email: String,
        username: String,
        password: String,
        birthDate: String,
        gender: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            authRepository.register(email, username, password, birthDate, gender)
                .onSuccess {
                    isLoading.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al registrar"
                }
        }
    }
}

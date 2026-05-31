package com.appcitas.app.ui.screens.profile

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.model.UserPhoto
import com.appcitas.app.domain.repository.UserRepository
import com.appcitas.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileEvent {
    data class ShowError(val message: String) : ProfileEvent()
    object ProfileUpdated : ProfileEvent()
    object PhotoUploaded : ProfileEvent()
    object PhotoDeleted : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val currentUser = mutableStateOf<User?>(null)
    val userPhotos = mutableStateOf<List<UserPhoto>>(emptyList())

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            isLoading.value = true

            userRepository.getCurrentUser()
                .onSuccess { user ->
                    currentUser.value = user
                    isLoading.value = false
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al cargar perfil"
                }
        }
    }

    fun updateProfile(
        username: String?,
        bio: String?,
        phone: String?,
        showDistance: Boolean?
    ) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            val user = currentUser.value ?: return@launch

            val updatedUser = user.copy(
                username = username ?: user.username,
                bio = bio ?: user.bio,
                phone = phone ?: user.phone,
                showDistance = showDistance ?: user.showDistance
            )

            userRepository.updateProfile(updatedUser)
                .onSuccess { user ->
                    currentUser.value = user
                    isLoading.value = false
                    _events.emit(ProfileEvent.ProfileUpdated)
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al actualizar perfil"
                }
        }
    }

    fun uploadPhoto(uri: Uri, isPrimary: Boolean = false) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            // TODO: Convert URI to bytes and upload
            // For now, simulate upload
            isLoading.value = false
            _events.emit(ProfileEvent.PhotoUploaded)
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            userRepository.deletePhoto(photoId)
                .onSuccess {
                    isLoading.value = false
                    _events.emit(ProfileEvent.PhotoDeleted)
                    loadUserProfile()
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al eliminar foto"
                }
        }
    }

    fun reorderPhotos(photoIds: List<String>) {
        viewModelScope.launch {
            userRepository.reorderPhotos(photoIds)
                .onFailure { exception ->
                    error.value = exception.message ?: "Error al reordenar fotos"
                }
        }
    }

    fun clearError() {
        error.value = null
    }
}

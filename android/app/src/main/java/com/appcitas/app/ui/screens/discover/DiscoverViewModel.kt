package com.appcitas.app.ui.screens.discover

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DiscoverEvent {
    data class MatchFound(val matchId: String) : DiscoverEvent()
    data class Error(val message: String) : DiscoverEvent()
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val users = mutableStateOf<List<User>>(emptyList())
    val currentPage = mutableStateOf(1)

    private val _events = MutableSharedFlow<DiscoverEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoading.value = true

            matchRepository.getDiscoverUsers(page = currentPage.value)
                .onSuccess { userList ->
                    users.value = userList
                    isLoading.value = false
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al cargar usuarios"
                }
        }
    }

    fun swipe(userId: String, isLike: Boolean) {
        viewModelScope.launch {
            matchRepository.swipe(userId, isLike)
                .onSuccess { isMatch ->
                    // Remove swiped user from list
                    users.value = users.drop(1)

                    // If match, emit event
                    if (isMatch) {
                        _events.emit(DiscoverEvent.MatchFound(userId))
                    }

                    // Load more users if needed
                    if (users.value.isEmpty()) {
                        currentPage.value++
                        loadUsers()
                    }
                }
                .onFailure { exception ->
                    _events.emit(DiscoverEvent.Error(exception.message ?: "Error al hacer swipe"))
                }
        }
    }

    fun clearError() {
        error.value = null
    }
}

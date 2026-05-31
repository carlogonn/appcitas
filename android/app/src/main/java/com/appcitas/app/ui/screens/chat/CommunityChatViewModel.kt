package com.appcitas.app.ui.screens.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcitas.app.domain.model.CommunityChannel
import com.appcitas.app.domain.model.CommunityMessage
import com.appcitas.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CommunityChatEvent {
    data class NewMessage(val message: CommunityMessage) : CommunityChatEvent()
    data class Error(val message: String) : CommunityChatEvent()
    object MessageSent : CommunityChatEvent()
}

@HiltViewModel
class CommunityChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val channels = mutableStateOf<List<CommunityChannel>>(emptyList())
    val messages = mutableStateOf<List<CommunityMessage>>(emptyList())

    private val _events = MutableSharedFlow<CommunityChatEvent>()
    val events = _events.asSharedFlow()

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch {
            chatRepository.getCommunityChannels()
                .onSuccess { channelList ->
                    channels.value = channelList
                }
                .onFailure { exception ->
                    error.value = exception.message ?: "Error al cargar canales"
                }
        }
    }

    fun loadMessages(channelId: String) {
        viewModelScope.launch {
            isLoading.value = true

            chatRepository.getCommunityMessages(channelId)
                .onSuccess { messageList ->
                    messages.value = messageList
                    isLoading.value = false
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al cargar mensajes"
                }
        }
    }

    fun sendMessage(channelId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            chatRepository.sendCommunityMessage(channelId, content)
                .onSuccess { message ->
                    messages.value = messages.value + message
                    _events.emit(CommunityChatEvent.MessageSent)
                }
                .onFailure { exception ->
                    _events.emit(CommunityChatEvent.Error(exception.message ?: "Error al enviar mensaje"))
                }
        }
    }

    fun observeMessages(channelId: String) {
        viewModelScope.launch {
            chatRepository.observeCommunityMessages(channelId)
                .collect { message ->
                    messages.value = messages.value + message
                    _events.emit(CommunityChatEvent.NewMessage(message))
                }
        }
    }

    fun clearError() {
        error.value = null
    }
}

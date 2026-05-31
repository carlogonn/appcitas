package com.appcitas.app.ui.screens.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcitas.app.domain.model.Chat
import com.appcitas.app.domain.model.Message
import com.appcitas.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatEvent {
    data class NewMessage(val message: Message) : ChatEvent()
    data class Error(val message: String) : ChatEvent()
    object MessageSent : ChatEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val chats = mutableStateOf<List<Chat>>(emptyList())
    val messages = mutableStateOf<List<Message>>(emptyList())
    val currentChatUser = mutableStateOf<Chat?>(null)

    private val _events = MutableSharedFlow<ChatEvent>()
    val events = _events.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            isLoading.value = true

            chatRepository.getChats()
                .onSuccess { chatList ->
                    chats.value = chatList
                    isLoading.value = false
                }
                .onFailure { exception ->
                    isLoading.value = false
                    error.value = exception.message ?: "Error al cargar chats"
                }
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            isLoading.value = true

            chatRepository.getMessages(chatId)
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

    fun sendMessage(receiverId: String, content: String, contentType: String = "text") {
        if (content.isBlank()) return

        viewModelScope.launch {
            chatRepository.sendMessage(receiverId, content, contentType)
                .onSuccess { message ->
                    messages.value = messages.value + message
                    _events.emit(ChatEvent.MessageSent)
                }
                .onFailure { exception ->
                    _events.emit(ChatEvent.Error(exception.message ?: "Error al enviar mensaje"))
                }
        }
    }

    fun observeMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.observeMessages(chatId)
                .collect { message ->
                    messages.value = messages.value + message
                    _events.emit(ChatEvent.NewMessage(message))
                }
        }
    }

    fun markAsRead(chatId: String) {
        viewModelScope.launch {
            chatRepository.markAsRead(chatId)
        }
    }

    fun initEncryption() {
        viewModelScope.launch {
            chatRepository.initEncryption()
                .onSuccess {
                    _isConnected.value = true
                }
                .onFailure { exception ->
                    error.value = exception.message ?: "Error al inicializar encriptación"
                }
        }
    }

    fun clearError() {
        error.value = null
    }
}

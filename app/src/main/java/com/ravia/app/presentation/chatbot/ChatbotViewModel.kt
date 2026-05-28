package com.ravia.app.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.domain.model.ChatMessage
import com.ravia.app.domain.model.ChatSuggestion
import com.ravia.app.domain.model.MessageSender
import com.ravia.app.domain.repository.ChatbotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatbotRepository: ChatbotRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _suggestions = MutableStateFlow<List<ChatSuggestion>>(emptyList())
    val suggestions: StateFlow<List<ChatSuggestion>> = _suggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
        loadSuggestions()
        sendWelcome()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _messages.value = chatbotRepository.getChatHistory()
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            chatbotRepository.getSuggestions().onSuccess { _suggestions.value = it }
        }
    }

    private fun sendWelcome() {
        if (_messages.value.isEmpty()) {
            val welcome = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Hola 👋 Soy el asistente de orientación de **RAVIA**. Puedo ayudarte con guías de emergencia y orientación básica.\n\n⚠️ Recuerda: en una emergencia real, llama siempre al **911** primero.",
                sender = MessageSender.BOT,
                createdAt = Date()
            )
            _messages.value = listOf(welcome)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            sender = MessageSender.USER,
            createdAt = Date()
        )
        _messages.value = _messages.value + userMsg
        viewModelScope.launch {
            _isLoading.value = true
            chatbotRepository.sendMessage(text)
                .onSuccess { botMsg ->
                    _messages.value = _messages.value + botMsg
                }
                .onFailure { error ->
                    _messages.value = _messages.value + ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = error.message ?: "No se pudo conectar con el asistente.",
                        sender = MessageSender.BOT,
                        createdAt = Date()
                    )
                }
            _isLoading.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatbotRepository.clearHistory()
            _messages.value = emptyList()
            sendWelcome()
        }
    }
}

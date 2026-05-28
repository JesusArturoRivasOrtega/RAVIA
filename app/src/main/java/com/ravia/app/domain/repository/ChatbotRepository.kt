package com.ravia.app.domain.repository

import com.ravia.app.domain.model.ChatMessage
import com.ravia.app.domain.model.ChatSuggestion

interface ChatbotRepository {
    suspend fun sendMessage(message: String): Result<ChatMessage>
    suspend fun getSuggestions(): Result<List<ChatSuggestion>>
    suspend fun getChatHistory(): List<ChatMessage>
    suspend fun clearHistory()
}

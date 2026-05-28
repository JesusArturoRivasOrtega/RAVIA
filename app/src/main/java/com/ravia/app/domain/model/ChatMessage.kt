package com.ravia.app.domain.model

import java.util.Date

enum class MessageSender { USER, BOT }

data class ChatMessage(
    val id: String,
    val content: String,
    val sender: MessageSender,
    val createdAt: Date = Date()
)

data class ChatSuggestion(
    val id: String,
    val text: String,
    val query: String
)

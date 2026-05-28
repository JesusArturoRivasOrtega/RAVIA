package com.ravia.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ravia.app.data.dto.ChatMessageRequestDto
import com.ravia.app.data.firebase.FirebaseCollections
import com.ravia.app.data.firebase.toChatMessage
import com.ravia.app.data.firebase.toFirestoreMap
import com.ravia.app.data.remote.ChatbotApi
import com.ravia.app.domain.model.ChatMessage
import com.ravia.app.domain.model.ChatSuggestion
import com.ravia.app.domain.model.MessageSender
import com.ravia.app.domain.repository.ChatbotRepository
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?,
    private val chatbotApi: ChatbotApi
) : ChatbotRepository {

    override suspend fun sendMessage(message: String): Result<ChatMessage> = runCatching {
        val userId = firebaseAuth?.currentUser?.uid ?: throw IllegalStateException("Sesion no iniciada.")
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = message,
            sender = MessageSender.USER,
            createdAt = Date()
        )
        val botMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = chatbotApi.sendMessage(ChatMessageRequestDto(message))
                .requireBody("No se pudo consultar el asistente")
                .reply,
            sender = MessageSender.BOT,
            createdAt = Date()
        )

        firestore?.let { db ->
            db.collection(FirebaseCollections.CHATBOT_MESSAGES)
                .document(userMsg.id)
                .set(userMsg.toFirestoreMap(userId))
                .await()
            db.collection(FirebaseCollections.CHATBOT_MESSAGES)
                .document(botMsg.id)
                .set(botMsg.toFirestoreMap(userId))
                .await()
        }

        botMsg
    }

    override suspend fun getSuggestions(): Result<List<ChatSuggestion>> =
        runCatching {
            chatbotApi.getSuggestions()
                .requireBody("No se pudieron cargar las sugerencias")
                .map { ChatSuggestion(it.id, it.text, it.query) }
        }

    override suspend fun getChatHistory(): List<ChatMessage> {
        val userId = firebaseAuth?.currentUser?.uid ?: return emptyList()
        val db = firestore ?: return emptyList()

        return runCatching {
            db.collection(FirebaseCollections.CHATBOT_MESSAGES)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(80)
                .get()
                .await()
                .documents
                .map { it.toChatMessage() }
        }.getOrElse { emptyList() }
    }

    override suspend fun clearHistory() {
        val userId = firebaseAuth?.currentUser?.uid
        val db = firestore
        if (userId == null || db == null) {
            return
        }

        runCatching {
            db.collection(FirebaseCollections.CHATBOT_MESSAGES)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .forEach { it.reference.delete().await() }
        }
    }
}

private fun <T> Response<T>.requireBody(action: String): T {
    if (isSuccessful) {
        return body() ?: throw IllegalStateException("$action: respuesta vacia.")
    }
    val detail = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    throw IllegalStateException("$action (${code()}): $detail")
}

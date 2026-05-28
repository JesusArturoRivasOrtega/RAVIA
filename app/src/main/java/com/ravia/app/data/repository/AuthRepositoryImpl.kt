package com.ravia.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.ravia.app.core.utils.Constants
import com.ravia.app.data.dto.UpdateUserRequestDto
import com.ravia.app.data.mapper.toDomain
import com.ravia.app.data.remote.UsersApi
import com.ravia.app.domain.model.User
import com.ravia.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val firebaseMessaging: FirebaseMessaging?,
    private val usersApi: UsersApi,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    private val currentUserFlow = MutableStateFlow<User?>(null)

    private val keyUserId = stringPreferencesKey(Constants.KEY_USER_ID)
    private val keyUserToken = stringPreferencesKey(Constants.KEY_USER_TOKEN)

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        zone: String?
    ): Result<User> = runCatching {
        withTimeout(30_000) {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase no esta configurado.")
            val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user
                ?: throw IllegalStateException("No se pudo crear el usuario.")

            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            ).await()
            firebaseUser.getIdToken(true).await()

            val user = usersApi.updateMe(UpdateUserRequestDto(displayName = name, zone = zone))
                .requireBody("No se pudo guardar el perfil en la base")
                .toDomain()

            registerFcmToken()
            saveUserToPrefs(user)
            currentUserFlow.value = user
            user
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        withTimeout(30_000) {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase no esta configurado.")
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: throw IllegalStateException("No se pudo iniciar sesion.")

            val user = usersApi.getMe()
                .requireBody("No se pudo recuperar el perfil de la base")
                .toDomain()

            registerFcmToken()
            saveUserToPrefs(user)
            currentUserFlow.value = user
            user
        }
    }

    override suspend fun logout() {
        firebaseAuth?.signOut()
        dataStore.edit { prefs ->
            prefs.remove(keyUserId)
            prefs.remove(keyUserToken)
        }
        currentUserFlow.value = null
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        withTimeout(30_000) {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase no esta configurado.")
            auth.sendPasswordResetEmail(email).await()
            Unit
        }
    }

    override fun getCurrentUser(): Flow<User?> = currentUserFlow

    override suspend fun refreshToken(): String? =
        firebaseAuth?.currentUser?.getIdToken(false)?.await()?.token

    override suspend fun isLoggedIn(): Boolean {
        if (firebaseAuth?.currentUser == null) return false

        return runCatching {
            val user = usersApi.getMe()
                .requireBody("No se pudo recuperar la sesion")
                .toDomain()
            registerFcmToken()
            saveUserToPrefs(user)
            currentUserFlow.value = user
            true
        }.getOrDefault(false)
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = runCatching {
        firebaseAuth?.currentUser ?: throw IllegalStateException("Sesion no iniciada.")
        usersApi.updateMe(UpdateUserRequestDto(fcmToken = token))
            .requireBody("No se pudo actualizar el token de notificaciones")
        Unit
    }

    override suspend fun updateProfile(name: String, zone: String?, phone: String?): Result<User> = runCatching {
        val activeUser = firebaseAuth?.currentUser ?: throw IllegalStateException("Sesion no iniciada.")
        activeUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name).build()
        ).await()
        activeUser.getIdToken(true).await()

        val updated = usersApi.updateMe(UpdateUserRequestDto(displayName = name, zone = zone))
            .requireBody("No se pudo guardar el perfil en la base")
            .toDomain()
            .copy(phone = phone)
        saveUserToPrefs(updated)
        currentUserFlow.value = updated
        updated
    }

    private suspend fun saveUserToPrefs(user: User) {
        dataStore.edit { prefs ->
            prefs[keyUserId] = user.id
        }
    }

    private suspend fun registerFcmToken() {
        runCatching { firebaseMessaging?.subscribeToTopic(Constants.TOPIC_CRITICAL)?.await() }
        runCatching { firebaseMessaging?.subscribeToTopic(Constants.TOPIC_ALL_ALERTS)?.await() }
        val token = runCatching { firebaseMessaging?.token?.await() }.getOrNull() ?: return
        runCatching {
            usersApi.updateMe(UpdateUserRequestDto(fcmToken = token))
                .requireBody("No se pudo registrar el token de notificaciones")
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

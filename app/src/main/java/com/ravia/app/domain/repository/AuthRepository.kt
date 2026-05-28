package com.ravia.app.domain.repository

import com.ravia.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String, zone: String?): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    suspend fun refreshToken(): String?
    suspend fun isLoggedIn(): Boolean
    suspend fun updateFcmToken(token: String): Result<Unit>
    suspend fun updateProfile(name: String, zone: String?, phone: String?): Result<User>
}

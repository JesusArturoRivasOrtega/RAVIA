package com.ravia.app.domain.usecase.auth

import com.ravia.app.domain.model.User
import com.ravia.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repo.login(email, password)
}

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(
        name: String, email: String, password: String, zone: String?
    ): Result<User> = repo.register(name, email, password, zone)
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}

class GetCurrentUserUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(): Flow<User?> = repo.getCurrentUser()
}

class SendPasswordResetUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> = repo.sendPasswordReset(email)
}

class IsLoggedInUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(): Boolean = repo.isLoggedIn()
}

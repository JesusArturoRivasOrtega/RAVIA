package com.ravia.app.domain.model

import java.util.Date

enum class UserRole { CITIZEN, MODERATOR, ADMIN }
enum class UserStatus { ACTIVE, SUSPENDED, BLOCKED }

data class User(
    val id: String,
    val firebaseUid: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: UserRole = UserRole.CITIZEN,
    val zone: String? = null,
    val avatarUrl: String? = null,
    val reputation: Int = 0,
    val status: UserStatus = UserStatus.ACTIVE,
    val reportsCount: Int = 0,
    val confirmedReportsCount: Int = 0,
    val createdAt: Date = Date()
)

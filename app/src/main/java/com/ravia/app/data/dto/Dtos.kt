package com.ravia.app.data.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    val displayName: String,
    val email: String,
    val password: String,
    val zone: String?,
    val fcmToken: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val uid: String,
    val email: String?
)

data class UserDto(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val role: String?,
    val status: String?,
    val reputationPoints: Int?,
    val reportCount: Int?,
    val confirmedReports: Int?,
    val zone: String?,
    val fcmTokens: List<String>?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastLoginAt: String?
)

data class UpdateUserRequestDto(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val zone: String? = null,
    val fcmToken: String? = null
)

data class CreateReportRequestDto(
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val lat: Double,
    val lng: Double,
    val address: String?,
    val isAnonymous: Boolean,
    val requestAiAnalysis: Boolean = true,
    val media: List<ReportMediaDto> = emptyList()
)

data class LocationDto(
    val lat: Double,
    val lng: Double
)

data class ReportMediaDto(
    val id: String,
    val url: String,
    val type: String,
    val thumbnailUrl: String? = null
)

data class ReportSourceProfileDto(
    val displayName: String?,
    val role: String?,
    val reputationPoints: Int?,
    val reportCount: Int?,
    val confirmedReports: Int?,
    val trustScore: Double?
)

data class ReportDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val priority: String,
    val location: LocationDto?,
    val address: String?,
    val authorId: String?,
    val isAnonymous: Boolean?,
    val sourceProfile: ReportSourceProfileDto?,
    val media: List<ReportMediaDto>?,
    val confirmCount: Int?,
    val falseCount: Int?,
    val duplicateCount: Int?,
    val urgentCount: Int?,
    val resolvedSignalCount: Int?,
    val aiAnalysis: AiAnalysisResponseDto?,
    val statusHistory: List<ReportStatusHistoryDto>?,
    val resolvedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class ReportStatusHistoryDto(
    val status: String,
    val changedBy: String?,
    val reason: String?,
    val timestamp: Any?
)

data class AiAnalysisResponseDto(
    val suggestedCategory: String,
    val suggestedPriority: String,
    val confidence: Double,
    val summary: String,
    val missingInfo: List<String>?,
    val possibleDuplicate: Boolean,
    val duplicateReportId: String?,
    val analyzedAt: Any? = null
)

data class ConfirmReportRequestDto(
    val type: String,
    val comment: String? = null
)

data class UpdateReportStatusRequestDto(
    val status: String,
    val reason: String? = null
)

data class UpdateReportRequestDto(
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val address: String? = null
)

data class AlertDto(
    val id: String,
    val title: String,
    val description: String?,
    val message: String?,
    val severity: String,
    val affectedZones: List<String>?,
    val isActive: Boolean?,
    val authorId: String?,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class RiskZoneDto(
    val id: String,
    val name: String,
    val description: String,
    val riskLevel: String,
    val centerLat: Double?,
    val centerLng: Double?,
    val radiusMeters: Double?,
    val reportCount: Int?,
    val isActive: Boolean?,
    val authorId: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CreateMissingPersonRequestDto(
    val name: String,
    val age: Int?,
    val photoUrl: String?,
    val lastSeenLocation: String,
    val lastSeenLat: Double?,
    val lastSeenLng: Double?,
    val clothing: String?,
    val distinctiveSigns: String?,
    val description: String,
    val contactInfo: String
)

data class MissingPersonDto(
    val id: String,
    val name: String,
    val age: Int?,
    val photoUrl: String?,
    val lastSeenLocation: String,
    val lastSeenLat: Double?,
    val lastSeenLng: Double?,
    val clothing: String?,
    val distinctiveSigns: String?,
    val description: String,
    val contactInfo: String,
    val status: String,
    val reportedBy: String?,
    val sightings: List<SightingDto>?,
    val createdAt: String?,
    val updatedAt: String?
)

data class SightingDto(
    val id: String,
    val reportedBy: String?,
    val lat: Double,
    val lng: Double,
    val comment: String?,
    val photoUrl: String?,
    val createdAt: String?
)

data class ReportSightingRequestDto(
    val lat: Double,
    val lng: Double,
    val comment: String?,
    val photoUrl: String?
)

data class ChatMessageRequestDto(
    val message: String,
    val sessionId: String? = null
)

data class ChatMessageResponseDto(
    val reply: String,
    val suggestions: List<String>?
)

data class ChatSuggestionDto(
    val id: String,
    val text: String,
    val query: String
)

data class FcmTokenRequestDto(val token: String)

data class UpdateUserRoleRequestDto(val role: String)

data class UpdateUserStatusRequestDto(val status: String)

data class UpdateMissingPersonStatusRequestDto(val status: String)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

package com.ravia.app.data.remote

import com.ravia.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<AuthResponseDto>

    @POST("auth/reset-password")
    suspend fun passwordReset(@Body body: Map<String, String>): Response<Map<String, String>>
}

interface UsersApi {
    @GET("users/me")
    suspend fun getMe(): Response<UserDto>

    @PATCH("users/me")
    suspend fun updateMe(@Body body: UpdateUserRequestDto): Response<UserDto>

    @GET("users")
    suspend fun getUsers(@Query("limit") limit: Int = 50): Response<List<UserDto>>

    @PATCH("users/{id}/role")
    suspend fun updateRole(
        @Path("id") id: String,
        @Body body: UpdateUserRoleRequestDto
    ): Response<UserDto>

    @PATCH("users/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body body: UpdateUserStatusRequestDto
    ): Response<UserDto>
}

interface ReportsApi {
    @POST("reports")
    suspend fun createReport(@Body body: CreateReportRequestDto): Response<ReportDto>

    @GET("reports")
    suspend fun getReports(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("activeOnly") activeOnly: Boolean? = null
    ): Response<List<ReportDto>>

    @GET("reports")
    suspend fun getNearbyReports(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radius: Double,
        @Query("activeOnly") activeOnly: Boolean = true,
        @Query("limit") limit: Int = 50
    ): Response<List<ReportDto>>

    @GET("reports/{id}")
    suspend fun getReportById(@Path("id") id: String): Response<ReportDto>

    @GET("reports/my")
    suspend fun getUserReports(@Query("limit") limit: Int = 50): Response<List<ReportDto>>

    @POST("reports/{id}/confirmations")
    suspend fun confirmReport(
        @Path("id") id: String,
        @Body body: ConfirmReportRequestDto
    ): Response<Any>

    @PATCH("reports/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body body: UpdateReportStatusRequestDto
    ): Response<ReportDto>

    @PATCH("reports/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Body body: UpdateReportRequestDto
    ): Response<ReportDto>

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: String): Response<Unit>

    @POST("reports/{id}/analyze")
    suspend fun analyzeReportById(@Path("id") id: String): Response<ReportDto>
}

interface AlertsApi {
    @GET("alerts")
    suspend fun getAlerts(): Response<List<AlertDto>>

    @GET("alerts/{id}")
    suspend fun getAlertById(@Path("id") id: String): Response<AlertDto>
}

interface RiskZonesApi {
    @GET("risk-zones")
    suspend fun getNearbyRiskZones(): Response<List<RiskZoneDto>>

    @GET("risk-zones/{id}")
    suspend fun getRiskZoneById(@Path("id") id: String): Response<RiskZoneDto>

    @GET("risk-zones")
    suspend fun getAllRiskZones(): Response<List<RiskZoneDto>>
}

interface MissingPersonsApi {
    @GET("missing-persons")
    suspend fun getMissingPersons(): Response<List<MissingPersonDto>>

    @GET("missing-persons/review")
    suspend fun getReviewQueue(): Response<List<MissingPersonDto>>

    @GET("missing-persons/all")
    suspend fun getAllMissingPersons(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 100
    ): Response<List<MissingPersonDto>>

    @GET("missing-persons/{id}")
    suspend fun getMissingPersonById(@Path("id") id: String): Response<MissingPersonDto>

    @POST("missing-persons")
    suspend fun createMissingPerson(@Body body: CreateMissingPersonRequestDto): Response<MissingPersonDto>

    @POST("missing-persons/{id}/sightings")
    suspend fun reportSighting(
        @Path("id") id: String,
        @Body body: ReportSightingRequestDto
    ): Response<MissingPersonDto>

    @PATCH("missing-persons/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body body: UpdateMissingPersonStatusRequestDto
    ): Response<MissingPersonDto>

    @DELETE("missing-persons/{id}")
    suspend fun deleteMissingPerson(@Path("id") id: String): Response<Unit>
}

interface ChatbotApi {
    @POST("chatbot/message")
    suspend fun sendMessage(@Body body: ChatMessageRequestDto): Response<ChatMessageResponseDto>

    @GET("chatbot/suggestions")
    suspend fun getSuggestions(): Response<List<ChatSuggestionDto>>
}

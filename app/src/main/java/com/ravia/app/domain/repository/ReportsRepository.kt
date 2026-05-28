package com.ravia.app.domain.repository

import com.ravia.app.domain.model.AiAnalysis
import com.ravia.app.domain.model.ConfirmationType
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {
    fun getNearbyReports(lat: Double, lng: Double, radiusKm: Double): Flow<List<Report>>
    suspend fun getReportById(id: String): Result<Report>
    fun getUserReports(userId: String): Flow<List<Report>>
    suspend fun createReport(
        title: String,
        description: String,
        category: ReportCategory,
        priority: ReportPriority,
        latitude: Double,
        longitude: Double,
        address: String?,
        anonymous: Boolean,
        imageUris: List<String>
    ): Result<Report>
    suspend fun analyzeReport(description: String, imageUrl: String?): Result<AiAnalysis>
    suspend fun confirmReport(reportId: String, type: ConfirmationType, comment: String?): Result<Unit>
    suspend fun updateReportStatus(reportId: String, status: ReportStatus): Result<Unit>
    suspend fun updateReportPriority(reportId: String, priority: ReportPriority): Result<Unit>
}

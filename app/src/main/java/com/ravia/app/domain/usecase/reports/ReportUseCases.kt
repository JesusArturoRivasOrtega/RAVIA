package com.ravia.app.domain.usecase.reports

import com.ravia.app.domain.model.AiAnalysis
import com.ravia.app.domain.model.ConfirmationType
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.domain.model.ReportStatus
import com.ravia.app.domain.repository.ReportsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyReportsUseCase @Inject constructor(private val repo: ReportsRepository) {
    operator fun invoke(lat: Double, lng: Double, radiusKm: Double): Flow<List<Report>> =
        repo.getNearbyReports(lat, lng, radiusKm)
}

class GetReportByIdUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(id: String): Result<Report> = repo.getReportById(id)
}

class GetUserReportsUseCase @Inject constructor(private val repo: ReportsRepository) {
    operator fun invoke(userId: String): Flow<List<Report>> = repo.getUserReports(userId)
}

class CreateReportUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: ReportCategory,
        priority: ReportPriority,
        latitude: Double,
        longitude: Double,
        address: String?,
        anonymous: Boolean,
        imageUris: List<String>
    ): Result<Report> = repo.createReport(
        title, description, category, priority, latitude, longitude, address, anonymous, imageUris
    )
}

class AnalyzeReportUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(description: String, imageUrl: String? = null): Result<AiAnalysis> =
        repo.analyzeReport(description, imageUrl)
}

class ConfirmReportUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(
        reportId: String, type: ConfirmationType, comment: String? = null
    ): Result<Unit> = repo.confirmReport(reportId, type, comment)
}

class UpdateReportStatusUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(reportId: String, status: ReportStatus): Result<Unit> =
        repo.updateReportStatus(reportId, status)
}

package com.ravia.app.domain.model

data class AiAnalysis(
    val suggestedCategory: ReportCategory,
    val suggestedPriority: ReportPriority,
    val confidence: Double,
    val summary: String,
    val missingInfo: List<String> = emptyList(),
    val possibleDuplicate: Boolean = false,
    val duplicateReportId: String? = null
)

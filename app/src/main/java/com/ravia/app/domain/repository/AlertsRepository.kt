package com.ravia.app.domain.repository

import com.ravia.app.domain.model.Alert
import kotlinx.coroutines.flow.Flow

interface AlertsRepository {
    fun getAlerts(): Flow<List<Alert>>
    fun getRecentAlerts(limit: Int = 5): Flow<List<Alert>>
    suspend fun getAlertById(id: String): Result<Alert>
    suspend fun markAsRead(alertId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    fun getUnreadCount(): Flow<Int>
}

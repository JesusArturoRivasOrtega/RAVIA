package com.ravia.app.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.Alert
import com.ravia.app.domain.model.AlertSeverity
import com.ravia.app.domain.repository.AlertsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    private val _alerts = MutableStateFlow<UiState<List<Alert>>>(UiState.Loading)
    val alerts: StateFlow<UiState<List<Alert>>> = _alerts.asStateFlow()

    private val _filterSeverity = MutableStateFlow<AlertSeverity?>(null)
    val filterSeverity: StateFlow<AlertSeverity?> = _filterSeverity.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadAlerts()
        observeUnread()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            alertsRepository.getAlerts().collectLatest { list ->
                val filtered = if (_filterSeverity.value != null)
                    list.filter { it.severity == _filterSeverity.value }
                else list
                _alerts.value = if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered)
            }
        }
    }

    private fun observeUnread() {
        viewModelScope.launch {
            alertsRepository.getUnreadCount().collectLatest { _unreadCount.value = it }
        }
    }

    fun setFilter(severity: AlertSeverity?) {
        _filterSeverity.value = severity
        loadAlerts()
    }

    fun markAsRead(alertId: String) {
        viewModelScope.launch { alertsRepository.markAsRead(alertId) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { alertsRepository.markAllAsRead() }
    }
}

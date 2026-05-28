package com.ravia.app.presentation.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.AlertSeverity
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val filterSeverity by viewModel.filterSeverity.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Alertas", fontWeight = FontWeight.SemiBold)
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge { Text("$unreadCount") }
                        }
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            RaviaLineIcon(
                                kind = RaviaIconKind.Checkmark,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.AlertSettings.route) }) {
                        RaviaLineIcon(
                            kind = RaviaIconKind.Gear,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        },
        bottomBar = {
            RaviaBottomNavBar(navController = navController, unreadAlertCount = unreadCount)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterSeverity == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(AlertSeverity.values()) { severity ->
                        FilterChip(
                            selected = filterSeverity == severity,
                            onClick = { viewModel.setFilter(if (filterSeverity == severity) null else severity) },
                            leadingIcon = {
                                RaviaLineIcon(
                                    kind = severity.toRaviaIconKind(),
                                    tint = severity.toColor(),
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 1.8.dp
                                )
                            },
                            label = { Text(severity.displayName()) }
                        )
                    }
                }
            }

            when (val state = alerts) {
                is UiState.Loading -> item { LoadingState(Modifier.height(200.dp)) }
                is UiState.Empty -> item {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = "Sin alertas",
                        subtitle = "No hay alertas en tu zona en este momento",
                        modifier = Modifier.padding(32.dp)
                    )
                }
                is UiState.Success -> {
                    items(state.data, key = { it.id }) { alert ->
                        AlertCard(
                            alert = alert,
                            onClick = { viewModel.markAsRead(alert.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                is UiState.Error -> item { ErrorState(state.message) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsScreen(navController: NavController) {
    var radius by remember { mutableStateOf(10f) }
    var nightSilence by remember { mutableStateOf(false) }
    var criticalAlwaysOn by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Configuración de alertas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.medium),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                RaviaLineIcon(
                                    kind = RaviaIconKind.Radius,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(25.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Radio de notificación",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${radius.toInt()} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Slider(
                            value = radius,
                            onValueChange = { radius = it },
                            valueRange = 1f..50f,
                            steps = 48
                        )
                    }
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

            item {
                RaviaSettingSwitch(
                    kind = RaviaIconKind.Moon,
                    title = "Silencio nocturno",
                    subtitle = "Sin notificaciones de 11pm a 7am",
                    color = NavyPrimary,
                    checked = nightSilence,
                    onCheckedChange = { nightSilence = it }
                )
            }

            item {
                RaviaSettingSwitch(
                    kind = RaviaIconKind.Siren,
                    title = "Alertas críticas siempre activas",
                    subtitle = "Las emergencias críticas siempre notifican",
                    color = StatusRed,
                    checked = criticalAlwaysOn,
                    onCheckedChange = { criticalAlwaysOn = it }
                )
            }

            item {
                RaviaSettingSwitch(
                    kind = RaviaIconKind.Vibrate,
                    title = "Vibración",
                    subtitle = "Vibrar al recibir alertas",
                    color = StatusOrange,
                    checked = vibration,
                    onCheckedChange = { vibration = it }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
            item {
                RaviaPrimaryButton(
                    text = "Guardar cambios",
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

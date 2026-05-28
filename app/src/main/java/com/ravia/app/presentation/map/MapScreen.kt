package com.ravia.app.presentation.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.domain.model.RiskZone
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val riskZones by viewModel.riskZones.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showRiskZones by viewModel.showRiskZones.collectAsStateWithLifecycle()
    val selectedReport by viewModel.selectedReport.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.loadCurrentLocation()
        }
    }

    fun requestLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(Unit) {
        if (locationState is UiState.Empty) {
            requestLocation()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = { RaviaBottomNavBar(navController = navController) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = { viewModel.toggleRiskZones() },
                    containerColor = if (showRiskZones) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ) {
                    RaviaLineIcon(
                        kind = RaviaIconKind.Radar,
                        tint = if (showRiskZones) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(23.dp),
                        strokeWidth = 2.2.dp
                    )
                }
                FloatingActionButton(
                    onClick = {
                        requestLocation()
                    }
                ) {
                    RaviaLineIcon(
                        kind = RaviaIconKind.Compass,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(23.dp),
                        strokeWidth = 2.2.dp
                    )
                }
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateReport.route) },
                    icon = {
                        RaviaLineIcon(
                            kind = RaviaIconKind.Report,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.2.dp
                        )
                    },
                    text = { Text("Reportar") },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ─── Map ──────────────────────────────────────────────────────────
            val reportList = (reports as? UiState.Success<List<Report>>)?.data.orEmpty()
            val currentLocation = (locationState as? UiState.Success)?.data

            RaviaOsmMap(
                modifier = Modifier.fillMaxSize(),
                reports = reportList,
                riskZones = riskZones,
                currentLocation = currentLocation,
                showRiskZones = showRiskZones,
                onReportClick = { report ->
                    viewModel.selectReport(report)
                    showBottomSheet = true
                },
            )

            // ─── Filter chips ─────────────────────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("Todos") }
                    )
                }
                items(ReportCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setCategory(if (selectedCategory == cat) null else cat) },
                        leadingIcon = {
                            RaviaLineIcon(
                                kind = cat.toRaviaIconKind(),
                                tint = cat.toAccentColor(),
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 1.8.dp
                            )
                        },
                        label = { Text(cat.displayName.take(10)) }
                    )
                }
            }

            // ─── Report count chip ────────────────────────────────────────────
            if (reports is UiState.Success) {
                val count = (reports as UiState.Success<List<Report>>).data.size
                AssistChip(
                    onClick = { showBottomSheet = true },
                    label = { Text("$count reportes • Ver lista") },
                    leadingIcon = {
                        RaviaLineIcon(
                            kind = RaviaIconKind.File,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.8.dp
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }

    // ─── Bottom sheet: selected report or list ────────────────────────────────
    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false; viewModel.selectReport(null) }) {
            selectedReport?.let { report ->
                ReportBottomSheetContent(
                    report = report,
                    onViewDetail = {
                        showBottomSheet = false
                        navController.navigate(Screen.ReportDetail.createRoute(report.id))
                    }
                )
            } ?: run {
                if (reports is UiState.Success) {
                    ReportListBottomSheet(
                        reports = (reports as UiState.Success<List<Report>>).data,
                        onReportClick = { report ->
                            showBottomSheet = false
                            navController.navigate(Screen.ReportDetail.createRoute(report.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportBottomSheetContent(report: Report, onViewDetail: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryIconBadge(
                category = report.category,
                selected = report.priority == ReportPriority.CRITICAL,
                size = 36.dp,
                iconSize = 20.dp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(report.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            report.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(status = report.status)
            PriorityBadge(priority = report.priority)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onViewDetail, modifier = Modifier.fillMaxWidth()) {
            Text("Ver detalle completo")
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ReportListBottomSheet(reports: List<Report>, onReportClick: (Report) -> Unit) {
    Column {
        Text(
            "${reports.size} reportes en el mapa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(reports) { report ->
                ReportCard(
                    report = report,
                    onClick = { onReportClick(report) }
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskZoneDetailScreen(
    navController: NavController,
    zoneId: String,
    viewModel: MapViewModel = hiltViewModel()
) {
    val zoneState by viewModel.riskZoneDetail.collectAsStateWithLifecycle()

    LaunchedEffect(zoneId) {
        viewModel.loadRiskZoneDetail(zoneId)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text((zoneState as? UiState.Success)?.data?.name ?: "Zona de riesgo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = zoneState) {
            is UiState.Loading -> LoadingState(Modifier.padding(padding))
            is UiState.Error -> ErrorState(state.message, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                val z = state.data
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = z.riskLevel.toColor().copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(z.riskLevel.toColor(), androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Riesgo ${z.riskLevel.displayName()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = z.riskLevel.toColor()
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Categoría: ${z.category}", style = MaterialTheme.typography.bodyMedium)
                            Text("${z.incidentCount} incidentes reportados", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item {
                    Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(z.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(8.dp))
                                Text("Recomendaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(z.recommendations, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
            }
            else -> Unit
        }
    }
}

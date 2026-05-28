package com.ravia.app.presentation.reports

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.core.utils.toFormattedDate
import com.ravia.app.core.utils.toRelativeTime
import com.ravia.app.domain.model.*
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    navController: NavController,
    reportId: String,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportState by viewModel.reportDetail.collectAsStateWithLifecycle()
    val confirmState by viewModel.confirmState.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    var showConfirmSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reportId) { viewModel.loadReportDetail(reportId) }

    LaunchedEffect(confirmState) {
        if (confirmState is UiState.Success) {
            snackbarHostState.showSnackbar("✓ Validación enviada. ¡Gracias por contribuir!")
            viewModel.resetConfirmState()
        } else if (confirmState is UiState.Error) {
            snackbarHostState.showSnackbar((confirmState as UiState.Error).message)
            viewModel.resetConfirmState()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Outlined.Share, "Compartir")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            when (val state = reportState) {
                is UiState.Success -> {
                    val report = state.data
                    val isAuthor = currentUserId != null && report.userId == currentUserId
                    val terminal = report.status.isTerminal()
                    val canModerate = currentUserRole == UserRole.MODERATOR ||
                        currentUserRole == UserRole.ADMIN
                    val isConfirming = confirmState is UiState.Loading
                    when {
                        terminal -> Unit
                        isAuthor && !canModerate -> Surface(
                            modifier = Modifier.navigationBarsPadding(),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Este es tu reporte. La comunidad lo está validando.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> Surface(
                            modifier = Modifier.navigationBarsPadding(),
                            shadowElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.confirmReport(report.id, ConfirmationType.CONFIRM) },
                                        enabled = !isConfirming,
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                    ) {
                                        if (isConfirming) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.HowToVote, null, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isConfirming) "Enviando" else "Validar")
                                    }
                                    Button(
                                        onClick = { showConfirmSheet = true },
                                        enabled = !isConfirming,
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Aportar dato")
                                    }
                                }
                                if (canModerate) {
                                    val transitions = report.status.allowedModeratorTransitions()
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Acciones de moderador",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        transitions.forEach { nextStatus ->
                                            OutlinedButton(
                                                onClick = { viewModel.moderatorUpdateStatus(report.id, nextStatus) },
                                                enabled = !isConfirming
                                            ) {
                                                Text(
                                                    moderatorActionLabel(nextStatus),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    ) { padding ->
        when (val state = reportState) {
            is UiState.Loading -> LoadingState(Modifier.padding(padding))
            is UiState.Error -> ErrorState(state.message, { viewModel.loadReportDetail(reportId) }, Modifier.padding(padding))
            is UiState.Success -> {
                val report = state.data
                val animatedConfidence by animateFloatAsState(
                    targetValue = report.confidence.toFloat(),
                    animationSpec = tween(durationMillis = 650),
                    label = "detailConfidence"
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Header
                    item {
                        ReportHeader(report = report, modifier = Modifier.padding(16.dp))
                    }

                    // Description
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Descripción", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text(report.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // AI summary
                    if (!report.aiSummary.isNullOrBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("Resumen IA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text(report.aiSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Info row
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                DetailRow(Icons.Outlined.LocationOn, "Ubicacion", report.locationLabel())
                                DetailRow(Icons.Outlined.Person, "Reportado por", if (report.anonymous) "Anónimo" else (report.userName ?: "Perfil ciudadano"))
                                DetailRow(
                                    Icons.Outlined.VerifiedUser,
                                    "Confianza de la fuente",
                                    "${(report.sourceTrustScore * 100).toInt()}% - ${report.sourceReputation} pts, ${report.sourceReportCount} reportes"
                                )
                                DetailRow(Icons.Outlined.Schedule, "Fecha", report.createdAt.toFormattedDate())
                                DetailRow(
                                    Icons.Outlined.Verified,
                                    "Confianza",
                                    "${(report.confidence * 100).toInt()}% (${report.confirmCount} validaciones)"
                                )
                                if (report.falseCount > 0 || report.duplicateCount > 0 || report.resolvedSignalCount > 0) {
                                    DetailRow(
                                        Icons.Default.HowToVote,
                                        "Señales comunitarias",
                                        "${report.falseCount} falso, ${report.duplicateCount} duplicado, ${report.resolvedSignalCount} ya no ocurre"
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (report.media.isNotEmpty()) {
                        item {
                            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Evidencia",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    RaviaPhoto(
                                        model = report.media.first().url,
                                        contentDescription = "Foto del reporte",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .clip(MaterialTheme.shapes.large)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Confidence bar
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text("Nivel de confianza comunitaria", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { animatedConfidence },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small),
                                color = report.status.toColor(),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Status timeline
                    item {
                        Text(
                            "Historial de estados",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item {
                        if (report.statusHistory.isNotEmpty()) {
                            HistoryTimeline(history = report.statusHistory, modifier = Modifier.padding(horizontal = 16.dp))
                        } else {
                            StatusTimeline(status = report.status, createdAt = report.createdAt, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    // Validation bottom sheet
    if (showConfirmSheet) {
        ModalBottomSheet(onDismissRequest = { showConfirmSheet = false }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Aportar informacion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                listOf(
                    ConfirmationType.URGENT,
                    ConfirmationType.MORE_INFO,
                    ConfirmationType.NO_LONGER_HAPPENING,
                    ConfirmationType.DUPLICATE,
                    ConfirmationType.FALSE
                ).forEach { type ->
                    OutlinedButton(
                        onClick = {
                            showConfirmSheet = false
                            viewModel.confirmReport(reportId, type)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) { Text(type.displayName()) }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun moderatorActionLabel(status: ReportStatus): String = when (status) {
    ReportStatus.PENDING -> "Pendiente"
    ReportStatus.VERIFYING -> "Verificar"
    ReportStatus.CONFIRMED -> "Confirmar"
    ReportStatus.CRITICAL -> "Crítico"
    ReportStatus.IN_PROGRESS -> "En atención"
    ReportStatus.RESOLVED -> "Resolver"
    ReportStatus.FALSE -> "Falso"
    ReportStatus.DUPLICATED -> "Duplicado"
}

@Composable
private fun ReportHeader(report: Report, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryIconBadge(
                category = report.category,
                selected = report.priority == ReportPriority.CRITICAL,
                size = 48.dp,
                iconSize = 26.dp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                report.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusBadge(status = report.status)
            PriorityBadge(priority = report.priority)
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun Report.locationLabel(): String =
    address?.takeIf { it.isNotBlank() } ?: "${latitude.compactCoordinate()}, ${longitude.compactCoordinate()}"

private fun Double.compactCoordinate(): String = String.format(Locale.US, "%.5f", this)

@Composable
private fun HistoryTimeline(history: List<ReportStatusHistory>, modifier: Modifier = Modifier) {
    val sorted = remember(history) { history.sortedBy { it.createdAt } }
    val lastIndex = sorted.lastIndex
    Column(modifier = modifier) {
        sorted.forEachIndexed { idx, entry ->
            val isLatest = idx == lastIndex
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (isLatest) entry.status.toColor() else StatusGreenDark,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLatest) entry.status.toIcon() else Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (idx < lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(StatusGreenDark)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        entry.status.displayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLatest) entry.status.toColor() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        entry.createdAt.toRelativeTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!entry.comment.isNullOrBlank()) {
                        Text(
                            entry.comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTimeline(status: ReportStatus, createdAt: java.util.Date, modifier: Modifier = Modifier) {
    val allStates = listOf(
        ReportStatus.PENDING,
        ReportStatus.VERIFYING,
        ReportStatus.CONFIRMED,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED
    )
    val currentIdx = allStates.indexOf(status).coerceAtLeast(0)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
        allStates.forEachIndexed { idx, s ->
            val isPast = idx < currentIdx
            val isCurrent = idx == currentIdx
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                when {
                                    isCurrent -> s.toColor()
                                    isPast -> StatusGreenDark
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPast) Icons.Default.Check else s.toIcon(),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (idx < allStates.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(if (isPast) StatusGreenDark else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        s.displayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) s.toColor() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (idx == 0) {
                        Text(createdAt.toRelativeTime(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

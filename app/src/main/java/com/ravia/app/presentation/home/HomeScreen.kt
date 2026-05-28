package com.ravia.app.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.R
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.domain.model.RiskLevel
import com.ravia.app.domain.model.RiskZone
import com.ravia.app.domain.model.UserRole
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val nearbyReports by viewModel.nearbyReports.collectAsStateWithLifecycle()
    val recentAlerts by viewModel.recentAlerts.collectAsStateWithLifecycle()
    val riskZones by viewModel.riskZones.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadAlertCount.collectAsStateWithLifecycle()
    val missingPersonsCount by viewModel.missingPersonsCount.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val canOpenModeration = currentUser?.role == UserRole.MODERATOR || currentUser?.role == UserRole.ADMIN
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.loadCurrentLocation()
        }
    }
    val reportsForStatus = (nearbyReports as? UiState.Success)?.data.orEmpty()
    val zonesForStatus = (riskZones as? UiState.Success)?.data.orEmpty()
    val highRiskStatus = reportsForStatus.any { it.priority == ReportPriority.CRITICAL || it.priority == ReportPriority.HIGH } ||
        zonesForStatus.any { it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH }
    val nearestReportDistance = reportsForStatus.mapNotNull { it.distanceKm }.minOrNull()
    val fabTransition = rememberInfiniteTransition(label = "reportFab")
    val fabIconScale by fabTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reportFabScale"
    )

    LaunchedEffect(Unit) {
        if (locationState is UiState.Empty) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Hola, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "vecino"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            currentUser?.zone ?: "Tu comunidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (canOpenModeration) {
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Moderation.route) {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.AdminPanelSettings, "Volver al panel")
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) Badge { Text("$unreadCount") }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate(Screen.Alerts.route) }) {
                            Icon(Icons.Outlined.Notifications, "Alertas")
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Chatbot.route) }) {
                        Icon(Icons.Outlined.SupportAgent, "Chatbot")
                    }
                }
            )
        },
        bottomBar = {
            RaviaBottomNavBar(navController = navController, unreadAlertCount = unreadCount)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.CreateReport.route) },
                icon = {
                    RaviaLineIcon(
                        kind = RaviaIconKind.Report,
                        tint = Color.White,
                        modifier = Modifier.graphicsLayer {
                            scaleX = fabIconScale
                            scaleY = fabIconScale
                        }.size(20.dp),
                        strokeWidth = 2.2.dp
                    )
                },
                text = { Text("Reportar") },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ─── Security zone status banner ──────────────────────────────────
            item {
                SecurityStatusBanner(
                    activeReportCount = reportsForStatus.size,
                    nearestDistanceKm = nearestReportDistance,
                    highRisk = highRiskStatus,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ─── Quick actions ────────────────────────────────────────────────
            if (canOpenModeration) {
                item {
                    ModeratorReturnButton(
                        role = currentUser?.role,
                        onClick = {
                            navController.navigate(Screen.Moderation.route) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                QuickActionsRow(
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // ─── Recent alerts ────────────────────────────────────────────────
            item {
                SectionHeader(
                    title = "Alertas recientes",
                    actionText = "Ver todas",
                    onAction = { navController.navigate(Screen.Alerts.route) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            when (val state = recentAlerts) {
                is UiState.Loading -> item { LoadingState(Modifier.height(120.dp)) }
                is UiState.Empty -> item {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = "Sin alertas recientes",
                        subtitle = "Tu zona está tranquila",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is UiState.Success -> {
                    items(state.data.take(3)) { alert ->
                        AlertCard(
                            alert = alert,
                            onClick = { navController.navigate(Screen.Alerts.route) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                is UiState.Error -> item { ErrorState(state.message, modifier = Modifier.padding(16.dp)) }
            }

            // ─── Nearby reports ───────────────────────────────────────────────
            item {
                SectionHeader(
                    title = "Reportes cercanos",
                    actionText = "Ver mapa",
                    onAction = { navController.navigate(Screen.Map.route) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            when (val state = nearbyReports) {
                is UiState.Loading -> item { LoadingState(Modifier.height(160.dp)) }
                is UiState.Empty -> item {
                    EmptyState(
                        icon = Icons.Outlined.LocationOff,
                        title = "Sin reportes cercanos",
                        subtitle = "Tu zona está sin incidentes activos",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is UiState.Success -> {
                    items(state.data) { report ->
                        ReportCard(
                            report = report,
                            onClick = { navController.navigate(Screen.ReportDetail.createRoute(report.id)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                is UiState.Error -> item { ErrorState(state.message, { viewModel.refresh() }, Modifier.padding(16.dp)) }
            }

            // ─── Risk zones ───────────────────────────────────────────────────
            when (val state = riskZones) {
                is UiState.Success -> {
                    item {
                        SectionHeader(
                            title = "Zonas de riesgo cercanas",
                            actionText = "Ver mapa",
                            onAction = { navController.navigate(Screen.Map.route) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data) { zone -> RiskZoneCard(zone, navController) }
                        }
                    }
                }
                else -> Unit
            }

            // ─── Missing persons quick access ─────────────────────────────────
            item {
                MissingPersonsBanner(
                    activeCount = missingPersonsCount,
                    onClick = { navController.navigate(Screen.MissingPersons.route) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeratorReturnButton(
    role: UserRole?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelName = if (role == UserRole.ADMIN) "admin" else "moderador"

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(Icons.Outlined.AdminPanelSettings, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Volver al panel $panelName",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SecurityStatusBanner(
    activeReportCount: Int,
    nearestDistanceKm: Double?,
    highRisk: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        highRisk -> "Riesgo alto"
        activeReportCount > 0 -> "Precaucion"
        else -> "Zona tranquila"
    }
    val detailText = if (activeReportCount > 0) {
        val distanceText = nearestDistanceKm?.let { " - ${String.format("%.1f", it)} km" }.orEmpty()
        "$activeReportCount incidentes activos$distanceText"
    } else {
        "Sin incidentes activos cercanos"
    }
    val iconTint = when {
        highRisk -> StatusRed
        activeReportCount > 0 -> StatusAmber
        else -> StatusGreen
    }
    val animatedTint by animateColorAsState(
        targetValue = iconTint,
        animationSpec = tween(durationMillis = 450),
        label = "zoneStatusTint"
    )
    val heroTransition = rememberInfiniteTransition(label = "zoneHero")
    val heroScale by heroTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoneHeroScale"
    )
    val distanceValue = nearestDistanceKm?.let { "${String.format("%.1f", it)} km" } ?: "--"

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 236.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Navy950, NavyPrimary, Blue700, Cyan600)
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ravia_safety_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = 0.30f
                        scaleX = heroScale
                        scaleY = heroScale
                    }
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.16f),
                                Navy950.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Estado de tu zona",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            statusText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        AnimatedSignalRings(
                            color = animatedTint,
                            modifier = Modifier.matchParentSize(),
                            ringCount = if (highRisk) 3 else 2
                        )
                        PulsingIconBadge(
                            imageVector = if (highRisk) Icons.Default.Warning else Icons.Outlined.Shield,
                            tint = animatedTint,
                            containerColor = animatedTint.copy(alpha = 0.20f),
                            pulse = highRisk,
                            size = 54.dp,
                            iconSize = 30.dp,
                            contentDescription = "Estado de precaucion"
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f)
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RaviaMetricPill(
                        icon = Icons.Outlined.ReportProblem,
                        label = "Activos",
                        value = activeReportCount.toString(),
                        color = animatedTint,
                        modifier = Modifier.weight(1f)
                    )
                    RaviaMetricPill(
                        icon = Icons.Outlined.LocationOn,
                        label = "Mas cerca",
                        value = distanceValue,
                        color = Cyan400,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(navController: NavController, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            QuickActionSpec(RaviaIconKind.Compass, "Mapa", Screen.Map.route, Blue500),
            QuickActionSpec(RaviaIconKind.People, "Busquedas", Screen.MissingPersons.route, StatusPurple),
            QuickActionSpec(RaviaIconKind.Chat, "Chat IA", Screen.Chatbot.route, Cyan600),
            QuickActionSpec(RaviaIconKind.Siren, "Alertas", Screen.Alerts.route, StatusRed)
        ).forEach { action ->
            QuickActionItem(
                action = action,
                onClick = { navController.navigate(action.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class QuickActionSpec(
    val kind: RaviaIconKind,
    val label: String,
    val route: String,
    val color: Color
)

@Composable
private fun QuickActionItem(
    action: QuickActionSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconMotion = rememberInfiniteTransition(label = "quickActionMotion")
    val iconScale by iconMotion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quickActionIconScale"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "quickActionPress"
    )

    ElevatedCard(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            action.color.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .clip(MaterialTheme.shapes.medium)
                    .background(action.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                RaviaLineIcon(
                    kind = action.kind,
                    tint = action.color,
                    modifier = Modifier.size(21.dp),
                    strokeWidth = 1.8.dp
                )
            }
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(22.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun RiskZoneCard(zone: RiskZone, navController: NavController) {
    val zoneColor = zone.riskLevel.toColor()
    ElevatedCard(
        modifier = Modifier
            .width(212.dp)
            .border(1.dp, zoneColor.copy(alpha = 0.18f), MaterialTheme.shapes.large)
            .clickable { navController.navigate(Screen.RiskZoneDetail.createRoute(zone.id)) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(zoneColor.copy(alpha = 0.12f), MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(zoneColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    RaviaLineIcon(
                        kind = RaviaIconKind.Radar,
                        tint = zoneColor,
                        modifier = Modifier.size(17.dp),
                        strokeWidth = 1.8.dp
                    )
                }
                Spacer(Modifier.width(7.dp))
                AnimatedUnreadDot(
                    color = zoneColor,
                    modifier = Modifier.size(7.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    zone.riskLevel.displayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = zoneColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                zone.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${zone.incidentCount} incidentes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (zone.distanceKm != null) {
                Text(
                    "${String.format("%.1f", zone.distanceKm)} km",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MissingPersonsBanner(activeCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.18f), MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                RaviaLineIcon(
                    kind = RaviaIconKind.People,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(25.dp),
                    strokeWidth = 2.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Personas desaparecidas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    if (activeCount == 0) "Sin busquedas activas en tu zona" else "$activeCount busquedas activas en tu zona",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

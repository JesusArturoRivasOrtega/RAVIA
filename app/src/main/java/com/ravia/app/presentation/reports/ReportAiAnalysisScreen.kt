package com.ravia.app.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.AiAnalysis
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportAiAnalysisScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val aiState by viewModel.aiAnalysis.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (aiState == null || aiState is UiState.Error) {
            viewModel.analyzeReport()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Análisis inteligente") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = aiState) {
            null, is UiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PulsingIconBadge(
                        imageVector = Icons.Default.AutoAwesome,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        pulse = true,
                        size = 72.dp,
                        iconSize = 36.dp,
                        contentDescription = null
                    )
                    Spacer(Modifier.height(20.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Analizando tu reporte con IA...", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Esto tarda unos segundos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is UiState.Success -> {
                AiResultContent(
                    analysis = state.data,
                    modifier = Modifier.padding(padding),
                    onConfirm = { navController.navigateUp() },
                    onEdit = { navController.navigateUp() }
                )
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No se pudo analizar", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.analyzeReport() }) { Text("Reintentar") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { navController.navigateUp() }) { Text("Continuar sin análisis") }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun AiResultContent(
    analysis: AiAnalysis,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // AI header card
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(NavyPrimary, Blue500)))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.15f), MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Análisis completado", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Confianza: ${(analysis.confidence * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            // Summary
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Resumen del incidente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(analysis.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            // Suggested category + priority
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Categoría", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        CategoryIconBadge(
                            category = analysis.suggestedCategory,
                            selected = true,
                            size = 44.dp,
                            iconSize = 24.dp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(analysis.suggestedCategory.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Prioridad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(analysis.suggestedPriority.toColor().copy(alpha = 0.15f), MaterialTheme.shapes.small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                analysis.suggestedPriority.displayName(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = analysis.suggestedPriority.toColor()
                            )
                        }
                    }
                }
            }
        }

        if (analysis.missingInfo.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = StatusAmberBg)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = StatusAmberDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Información que podría ayudar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = StatusAmberDark)
                        }
                        Spacer(Modifier.height(8.dp))
                        analysis.missingInfo.forEach { info ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("•", color = StatusAmberDark, modifier = Modifier.padding(end = 6.dp))
                                Text(info, style = MaterialTheme.typography.bodySmall, color = StatusAmberDark)
                            }
                        }
                    }
                }
            }
        }

        if (analysis.possibleDuplicate) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = StatusPurpleBg)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCopy, null, tint = StatusPurple)
                        Spacer(Modifier.width(8.dp))
                        Text("Posible reporte duplicado detectado. Revisa antes de enviar.", style = MaterialTheme.typography.bodySmall, color = StatusPurple)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                Button(onClick = onConfirm, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Confirmar")
                }
            }
        }
    }
}

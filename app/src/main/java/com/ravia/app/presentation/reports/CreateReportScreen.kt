package com.ravia.app.presentation.reports

import android.Manifest
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.loadCurrentLocation()
        } else {
            viewModel.updateForm { copy(latitude = null, longitude = null) }
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateForm { copy(imageUris = listOf(uri.toString())) }
        }
    }

    var currentStep by remember { mutableStateOf(0) }
    var requestedLocation by remember { mutableStateOf(false) }
    val steps = listOf("Categoría", "Descripción", "Ubicación", "Extras")

    fun requestLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(createState) {
        when (createState) {
            is UiState.Success -> {
                val reportId = (createState as UiState.Success).data.id
                navController.navigate(Screen.ReportSubmitted.createRoute(reportId)) {
                    popUpTo(Screen.CreateReport.route) { inclusive = true }
                }
                viewModel.resetCreateFlow()
            }
            is UiState.Error -> snackbarHostState.showSnackbar((createState as UiState.Error).message)
            else -> Unit
        }
    }

    LaunchedEffect(currentStep) {
        if (currentStep == 2 && !requestedLocation) {
            requestedLocation = true
            requestLocation()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Nuevo reporte") },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 0) currentStep-- else navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
        ) {
            // Step indicator
            LinearProgressIndicator(
                progress = { (currentStep + 1) / steps.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { idx, step ->
                    StepIndicator(
                        step = step,
                        stepNumber = idx + 1,
                        isActive = idx == currentStep,
                        isDone = idx < currentStep
                    )
                }
            }

            HorizontalDivider()

            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
                },
                label = "reportStepContent"
            ) { step ->
                when (step) {
                    0 -> CategoryStep(
                        selected = form.category,
                        onSelect = { viewModel.updateForm { copy(category = it) }; currentStep = 1 }
                    )
                    1 -> DescriptionStep(
                        title = form.title,
                        description = form.description,
                        onTitleChange = { viewModel.updateForm { copy(title = it) } },
                        onDescriptionChange = { viewModel.updateForm { copy(description = it) } },
                        onNext = { if (form.title.isNotBlank() && form.description.isNotBlank()) currentStep = 2 }
                    )
                    2 -> LocationStep(
                        address = form.address,
                        latitude = form.latitude,
                        longitude = form.longitude,
                        hasLocation = form.latitude != null && form.longitude != null,
                        locationState = locationState,
                        onAddressChange = { viewModel.updateForm { copy(address = it) } },
                        onLocationChange = { lat, lng -> viewModel.setIncidentLocation(lat, lng) },
                        onRequestLocation = { requestLocation() },
                        onNext = { currentStep = 3 }
                    )
                    3 -> ExtrasStep(
                        priority = form.priority,
                        anonymous = form.anonymous,
                        imageUris = form.imageUris,
                        onPriorityChange = { viewModel.updateForm { copy(priority = it) } },
                        onAnonymousChange = { viewModel.updateForm { copy(anonymous = it) } },
                        onPickImage = { imagePickerLauncher.launch("image/*") },
                        onRemoveImage = { viewModel.updateForm { copy(imageUris = emptyList()) } },
                        isLoading = createState is UiState.Loading,
                        onSubmit = { viewModel.submitReport() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: String, stepNumber: Int, isActive: Boolean, isDone: Boolean) {
    val targetColor = when {
        isDone -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val containerColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 220),
        label = "stepIndicatorColor"
    )
    val indicatorSize by animateDpAsState(
        targetValue = if (isActive) 28.dp else 24.dp,
        animationSpec = tween(durationMillis = 220),
        label = "stepIndicatorSize"
    )
    val iconKind = when (stepNumber) {
        1 -> RaviaIconKind.Siren
        2 -> RaviaIconKind.File
        3 -> RaviaIconKind.Location
        else -> RaviaIconKind.Spark
    }
    val iconTint = when {
        isDone -> MaterialTheme.colorScheme.onPrimary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(indicatorSize)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                RaviaLineIcon(
                    kind = RaviaIconKind.Checkmark,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(15.dp),
                    strokeWidth = 1.8.dp
                )
            } else {
                RaviaLineIcon(
                    kind = iconKind,
                    tint = iconTint,
                    modifier = Modifier.size(if (isActive) 16.dp else 14.dp),
                    strokeWidth = if (isActive) 2.dp else 1.6.dp
                )
            }
        }
        if (stepNumber < 4) {
            Text(
                step,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryStep(selected: ReportCategory?, onSelect: (ReportCategory) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "¿Qué tipo de incidente es?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Selecciona la categoría que mejor describe la situación",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ReportCategory.values()) { category ->
                val isSelected = selected == category
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(durationMillis = 220),
                    label = "categoryCardColor"
                )
                val cardElevation by animateDpAsState(
                    targetValue = if (isSelected) 5.dp else 1.dp,
                    animationSpec = tween(durationMillis = 220),
                    label = "categoryCardElevation"
                )
                val cardScale by animateFloatAsState(
                    targetValue = when {
                        pressed -> 0.97f
                        isSelected -> 1.03f
                        else -> 1f
                    },
                    animationSpec = tween(durationMillis = 160),
                    label = "categoryCardScale"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 136.dp)
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelect(category) }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = MaterialTheme.shapes.large
                        ),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CategoryIconBadge(
                            category = category,
                            selected = isSelected,
                            size = 42.dp,
                            iconSize = 24.dp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionStep(
    title: String, description: String,
    onTitleChange: (String) -> Unit, onDescriptionChange: (String) -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Describe el incidente", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Sé preciso. Más detalles = mejor respuesta.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(
                value = title, onValueChange = onTitleChange,
                label = { Text("Título del incidente") },
                placeholder = { Text("Ej: Pelea en la calle Juárez") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = description, onValueChange = onDescriptionChange,
                label = { Text("Descripción detallada") },
                placeholder = { Text("Describe qué está pasando, cuántas personas involucradas, si hay heridos, etc.") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                shape = MaterialTheme.shapes.medium,
                maxLines = 8,
                supportingText = { Text("${description.length}/500") }
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Blue100)) {
                Row(modifier = Modifier.padding(12.dp)) {
                    RaviaLineIcon(
                        kind = RaviaIconKind.Spark,
                        tint = Blue700,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 1.8.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tip: Incluye calle o referencia, número de personas, y si hay heridos.",
                        style = MaterialTheme.typography.bodySmall, color = Blue700
                    )
                }
            }
        }
        item {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = title.isNotBlank() && description.length >= 10,
                shape = MaterialTheme.shapes.medium
            ) { Text("Continuar") }
        }
    }
}

@Composable
private fun LocationStep(
    address: String,
    latitude: Double?,
    longitude: Double?,
    hasLocation: Boolean,
    locationState: UiState<com.ravia.app.domain.model.LocationPoint>,
    onAddressChange: (String) -> Unit,
    onLocationChange: (Double, Double) -> Unit,
    onRequestLocation: () -> Unit,
    onNext: () -> Unit
) {
    var latText by remember { mutableStateOf(latitude?.formatCoordinate().orEmpty()) }
    var lngText by remember { mutableStateOf(longitude?.formatCoordinate().orEmpty()) }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null) latText = latitude.formatCoordinate()
        if (longitude != null) lngText = longitude.formatCoordinate()
    }

    fun applyManualCoordinates(nextLat: String = latText, nextLng: String = lngText) {
        val lat = nextLat.toDoubleOrNull()
        val lng = nextLng.toDoubleOrNull()
        if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
            onLocationChange(lat, lng)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Ubicación del incidente", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("La ubicación ayuda a notificar a vecinos cercanos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Auto location card
        item {
            Card(
                onClick = onRequestLocation,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    RaviaLineIcon(
                        kind = RaviaIconKind.Location,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(23.dp),
                        strokeWidth = 2.dp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Usar ubicacion actual", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        when (locationState) {
                            is UiState.Loading -> "Obteniendo ubicacion..."
                            is UiState.Success -> "Ubicacion actual lista"
                            is UiState.Error -> "Toca para volver a intentar"
                            else -> "Toca para obtener tu ubicacion"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    if (locationState is UiState.Error) {
                        Text(
                            locationState.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                when {
                    locationState is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    hasLocation -> RaviaLineIcon(RaviaIconKind.Checkmark, MaterialTheme.colorScheme.primary, Modifier.size(22.dp), 2.dp)
                    else -> RaviaLineIcon(RaviaIconKind.Chevron, MaterialTheme.colorScheme.primary, Modifier.size(20.dp), 2.dp)
                }
            }
            }
        }

        item {
            OutlinedTextField(
                value = address, onValueChange = onAddressChange,
                label = { Text("Calle o referencia del incidente *") },
                placeholder = { Text("Ej: Calle Juarez, frente a la panaderia La Rosa") },
                leadingIcon = { RaviaLineIcon(RaviaIconKind.Compass, MaterialTheme.colorScheme.primary, Modifier.size(20.dp), 1.8.dp) },
                supportingText = {
                    Text(
                        if (address.isBlank()) "Se guardara junto con las coordenadas."
                        else "Esta direccion aparecera en el reporte."
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = latText,
                onValueChange = {
                    latText = it
                    applyManualCoordinates(nextLat = it)
                },
                label = { Text("Latitud") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = lngText,
                onValueChange = {
                    lngText = it
                    applyManualCoordinates(nextLng = it)
                },
                label = { Text("Longitud") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            }
        }

        item {
            if (latitude != null && longitude != null) {
            LocationPickerMap(
                latitude = latitude,
                longitude = longitude,
                onLocationChange = { lat, lng ->
                    latText = lat.formatCoordinate()
                    lngText = lng.formatCoordinate()
                    onLocationChange(lat, lng)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    RaviaLineIcon(RaviaIconKind.Location, MaterialTheme.colorScheme.primary, Modifier.size(22.dp), 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Usa tu ubicacion actual o escribe coordenadas para colocar el pin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            }
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = hasLocation && address.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) { Text("Continuar") }
        }
    }
}

@Composable
private fun LocationPickerMap(
    latitude: Double,
    longitude: Double,
    onLocationChange: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 3.0
            maxZoomLevel = 20.0
            controller.setZoom(16.0)
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            val point = GeoPoint(latitude, longitude)
            map.overlays.clear()
            map.controller.setCenter(point)
            map.controller.setZoom(16.0)

            map.overlays.add(object : Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val tapped = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    onLocationChange(tapped.latitude, tapped.longitude)
                    return true
                }
            })

            map.overlays.add(
                Marker(map).apply {
                    position = point
                    title = "Ubicacion del incidente"
                    snippet = "Arrastra o toca el mapa para ajustar"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    isDraggable = true
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker?) = Unit
                        override fun onMarkerDragStart(marker: Marker?) = Unit
                        override fun onMarkerDragEnd(marker: Marker?) {
                            marker?.position?.let { onLocationChange(it.latitude, it.longitude) }
                        }
                    })
                }
            )

            map.invalidate()
        }
    )
}

private fun Double.formatCoordinate(): String = String.format(Locale.US, "%.6f", this)

@Composable
private fun ExtrasStep(
    priority: ReportPriority,
    anonymous: Boolean,
    imageUris: List<String>,
    onPriorityChange: (ReportPriority) -> Unit,
    onAnonymousChange: (Boolean) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Últimos detalles", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            Text("Prioridad del incidente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportPriority.values().forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { onPriorityChange(p) },
                        leadingIcon = {
                            RaviaLineIcon(
                                kind = p.toRaviaIconKind(),
                                tint = p.toColor(),
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 1.8.dp
                            )
                        },
                        label = { Text(p.displayName()) }
                    )
                }
            }
        }

        item {
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        RaviaLineIcon(
                            kind = RaviaIconKind.Eye,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(23.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reportar de forma anónima", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Tu nombre no aparecerá en el reporte", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = anonymous, onCheckedChange = onAnonymousChange)
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Foto del incidente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Se guardara comprimida en Base64 para recuperarla con el reporte.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val selectedImage = imageUris.firstOrNull()
                    if (selectedImage != null) {
                        RaviaPhoto(
                            model = selectedImage,
                            contentDescription = "Foto seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(MaterialTheme.shapes.large)
                        )
                        OutlinedButton(
                            onClick = onRemoveImage,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Quitar foto")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onPickImage,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar foto")
                        }
                    }
                }
            }
        }

        item { HorizontalDivider() }

        item {
            RaviaPrimaryButton(
                text = "Enviar reporte",
                onClick = onSubmit,
                icon = Icons.Default.Send,
                isLoading = isLoading
            )
        }
    }
}

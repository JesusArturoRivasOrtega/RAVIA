package com.ravia.app.presentation.missingpersons

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.core.utils.toRelativeTime
import com.ravia.app.domain.model.MissingPerson
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.*
import com.ravia.app.ui.theme.StatusRed
import com.ravia.app.ui.theme.StatusRedBg
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPersonsScreen(
    navController: NavController,
    viewModel: MissingPersonsViewModel = hiltViewModel()
) {
    val list by viewModel.list.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Personas desaparecidas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.CreateMissingPerson.route) },
                icon = {
                    RaviaLineIcon(
                        kind = RaviaIconKind.People,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.2.dp
                    )
                },
                text = { Text("Reportar desaparecido") },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (val state = list) {
                is UiState.Loading -> item { LoadingState(Modifier.height(200.dp)) }
                is UiState.Empty -> item {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "Sin reportes activos",
                        subtitle = "No hay personas desaparecidas reportadas en tu zona",
                        modifier = Modifier.padding(32.dp)
                    )
                }
                is UiState.Success -> {
                    items(state.data, key = { it.id }) { person ->
                        MissingPersonCard(
                            person = person,
                            onClick = { navController.navigate(Screen.MissingPersonDetail.createRoute(person.id)) }
                        )
                    }
                }
                is UiState.Error -> item { ErrorState(state.message) }
            }
        }
    }
}

@Composable
private fun MissingPersonCard(person: MissingPerson, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = StatusRedBg)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(StatusRed.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!person.photoUrl.isNullOrBlank()) {
                    RaviaPhoto(
                        model = person.photoUrl,
                        contentDescription = "Foto de ${person.name}",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    RaviaLineIcon(
                        kind = RaviaIconKind.User,
                        tint = StatusRed,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.2.dp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (person.age != null) {
                    Text("${person.age} años", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(person.lastSeenLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(person.createdAt.toRelativeTime(), style = MaterialTheme.typography.labelSmall, color = StatusRed)
            }
            RaviaLineIcon(
                kind = RaviaIconKind.Chevron,
                tint = StatusRed,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPersonDetailScreen(
    navController: NavController,
    personId: String,
    viewModel: MissingPersonsViewModel = hiltViewModel()
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()

    LaunchedEffect(personId) { viewModel.loadDetail(personId) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Ficha de búsqueda") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* share */ }) { Icon(Icons.Outlined.Share, "Compartir") }
                }
            )
        }
    ) { padding ->
        when (val state = detail) {
            is UiState.Loading -> LoadingState(Modifier.padding(padding))
            is UiState.Error -> ErrorState(state.message, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                val person = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Header card
                        Card(colors = CardDefaults.cardColors(containerColor = StatusRedBg)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(StatusRed.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!person.photoUrl.isNullOrBlank()) {
                                            RaviaPhoto(
                                                model = person.photoUrl,
                                                contentDescription = "Foto de ${person.name}",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Icon(Icons.Default.Person, null, tint = StatusRed, modifier = Modifier.size(44.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(person.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusRed)
                                        if (person.age != null) Text("${person.age} años", style = MaterialTheme.typography.bodyMedium)
                                        Badge(containerColor = StatusRed, contentColor = androidx.compose.ui.graphics.Color.White) {
                                            Text(person.status.displayName())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!person.photoUrl.isNullOrBlank()) {
                        item {
                            RaviaPhoto(
                                model = person.photoUrl,
                                contentDescription = "Foto principal de ${person.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }
                    }

                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                InfoItem("Última ubicación conocida", person.lastSeenLocation, Icons.Outlined.LocationOn)
                                if (!person.clothing.isNullOrBlank()) InfoItem("Ropa", person.clothing, Icons.Outlined.Checkroom)
                                if (!person.distinctiveSigns.isNullOrBlank()) InfoItem("Señas particulares", person.distinctiveSigns, Icons.Outlined.Info)
                                InfoItem("Descripción", person.description, Icons.Outlined.Description)
                            }
                        }
                    }

                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Contacto autorizado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(person.contactInfo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (person.sightings.isNotEmpty()) {
                        item {
                            Text("Avistamientos (${person.sightings.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        items(person.sightings) { sighting ->
                            Card {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = StatusRed, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(sighting.comment ?: "Sin comentario", style = MaterialTheme.typography.bodySmall)
                                        Text(sighting.createdAt.toRelativeTime(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { navController.navigate(Screen.ReportSighting.createRoute(person.id)) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                        ) {
                            Icon(Icons.Default.AddLocationAlt, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Reportar avistamiento")
                        }
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMissingPersonScreen(navController: NavController, viewModel: MissingPersonsViewModel = hiltViewModel()) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var lastSeen by remember { mutableStateOf("") }
    var clothing by remember { mutableStateOf("") }
    var signs by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri?.toString() }

    LaunchedEffect(createState) {
        if (createState is UiState.Success) {
            navController.navigateUp()
            viewModel.resetCreateState()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Reportar desaparecido") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item { Text("Información de la persona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre completo *") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item { OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Edad aproximada") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item {
                Card {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Foto de la persona", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Se enviara comprimida en Base64 y quedara pendiente de verificacion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (photoUri != null) {
                            RaviaPhoto(
                                model = photoUri,
                                contentDescription = "Foto seleccionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                            OutlinedButton(onClick = { photoUri = null }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Quitar foto")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
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
            item { OutlinedTextField(value = lastSeen, onValueChange = { lastSeen = it }, label = { Text("Última ubicación conocida *") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item { OutlinedTextField(value = clothing, onValueChange = { clothing = it }, label = { Text("Descripción de ropa") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item { OutlinedTextField(value = signs, onValueChange = { signs = it }, label = { Text("Señas particulares") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción adicional *") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), shape = MaterialTheme.shapes.medium, maxLines = 5) }
            item { OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Teléfono de contacto autorizado *") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item {
                LoadingButton(
                    onClick = {
                        if (name.isNotBlank() && lastSeen.isNotBlank() && description.isNotBlank() && contact.isNotBlank()) {
                            viewModel.createMissingPerson(name, age.toIntOrNull(), photoUri, lastSeen, null, null, clothing.ifBlank { null }, signs.ifBlank { null }, description, contact)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    isLoading = createState is UiState.Loading,
                    enabled = name.isNotBlank() && lastSeen.isNotBlank() && description.isNotBlank() && contact.isNotBlank() && createState !is UiState.Loading,
                    shape = MaterialTheme.shapes.medium
                ) { Text("Publicar búsqueda") }
            }
        }
    }
}

@Composable
private fun LoadingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = shape
    ) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        else content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSightingScreen(navController: NavController, personId: String, viewModel: MissingPersonsViewModel = hiltViewModel()) {
    var comment by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
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
        requestLocation()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Reportar avistamiento") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    onClick = { requestLocation() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Se usara tu ubicacion actual", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                when (locationState) {
                                    is UiState.Loading -> "Obteniendo ubicacion..."
                                    is UiState.Success -> "Ubicacion actual lista"
                                    is UiState.Error -> "Toca para volver a intentar"
                                    else -> "Toca para obtener tu ubicacion"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        if (locationState is UiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (locationState is UiState.Success) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    label = { Text("Describe el avistamiento") },
                    placeholder = { Text("¿Dónde la viste? ¿Cómo estaba?") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = MaterialTheme.shapes.medium, maxLines = 6
                )
            }

            item {
                Button(
                    onClick = {
                        val location = (locationState as? UiState.Success)?.data
                        if (location == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Activa la ubicacion para enviar el avistamiento.")
                            }
                        } else {
                            viewModel.reportSighting(personId, location.latitude, location.longitude, comment.ifBlank { null })
                            navController.navigateUp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = locationState is UiState.Success,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Enviar avistamiento")
                }
            }
        }
    }
}

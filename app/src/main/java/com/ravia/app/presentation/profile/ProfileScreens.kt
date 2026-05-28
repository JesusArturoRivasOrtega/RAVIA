package com.ravia.app.presentation.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.domain.model.User
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.*
import com.ravia.app.presentation.reports.ReportsViewModel
import com.ravia.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(loggedOut) {
        if (loggedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
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
        bottomBar = { RaviaBottomNavBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { ProfileHeaderCard(user = user, onEditClick = { navController.navigate(Screen.EditProfile.route) }) }
            item { Spacer(Modifier.height(8.dp)) }

            // Stats row
            user?.let { u ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(RaviaIconKind.File, "Reportes", "${u.reportsCount}", Blue600, Modifier.weight(1f))
                        StatCard(RaviaIconKind.Checkmark, "Confirmados", "${u.confirmedReportsCount}", StatusGreen, Modifier.weight(1f))
                        StatCard(RaviaIconKind.Trophy, "Reputación", "${u.reputation}", StatusAmberDark, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Menu items
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MenuSection(title = "Mi actividad") {
                        MenuItem(RaviaIconKind.File, "Mis reportes", "Ver historial de reportes", Blue600) {
                            navController.navigate(Screen.MyReports.route)
                        }
                        MenuItem(RaviaIconKind.Trophy, "Mi reputación", "Nivel de confianza comunitaria", StatusAmberDark) {
                            navController.navigate(Screen.Reputation.route)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    MenuSection(title = "Cuenta") {
                        MenuItem(RaviaIconKind.Edit, "Editar perfil", "Nombre, foto, zona", MaterialTheme.colorScheme.primary) {
                            navController.navigate(Screen.EditProfile.route)
                        }
                        MenuItem(RaviaIconKind.Bell, "Notificaciones", "Configurar alertas", StatusOrange) {
                            navController.navigate(Screen.AlertSettings.route)
                        }
                        MenuItem(RaviaIconKind.Lock, "Privacidad", "Datos y privacidad", StatusPurple) { }
                        MenuItem(RaviaIconKind.Chat, "Ayuda", "Preguntas frecuentes", Cyan600) {
                            navController.navigate(Screen.Chatbot.route)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // Logout
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RaviaLineIcon(
                                kind = RaviaIconKind.Logout,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Cerrar sesión",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(); showLogoutDialog = false }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfileHeaderCard(user: User?, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(NavyPrimary, Blue500)))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                RaviaLineIcon(
                    kind = RaviaIconKind.User,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 2.3.dp
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(user?.name ?: "Usuario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            val userZone = user?.zone
            if (!userZone.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    Text(userZone, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onEditClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                RaviaLineIcon(
                    kind = RaviaIconKind.Edit,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 1.8.dp
                )
                Spacer(Modifier.width(4.dp))
                Text("Editar perfil")
            }
        }
    }
}

@Composable
private fun StatCard(kind: RaviaIconKind, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                RaviaLineIcon(kind = kind, tint = color, modifier = Modifier.size(17.dp), strokeWidth = 1.8.dp)
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(shape = MaterialTheme.shapes.large) {
            Column { content() }
        }
    }
}

@Composable
private fun MenuItem(kind: RaviaIconKind, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    RaviaOptionRow(
        kind = kind,
        title = title,
        subtitle = subtitle,
        color = color,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(navController: NavController) {
    val reportsViewModel: ReportsViewModel = hiltViewModel()
    val myReports by reportsViewModel.myReports.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { reportsViewModel.loadMyReports() }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Mis reportes") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (val state = myReports) {
                is UiState.Loading -> item { LoadingState(Modifier.height(200.dp)) }
                is UiState.Empty -> item {
                    EmptyState(
                        icon = Icons.Outlined.Description,
                        title = "Sin reportes aún",
                        subtitle = "Tus reportes enviados aparecerán aquí",
                        action = "Crear primer reporte" to { navController.navigate(Screen.CreateReport.route) }
                    )
                }
                is UiState.Success -> {
                    items(state.data, key = { it.id }) { report ->
                        ReportCard(
                            report = report,
                            onClick = { navController.navigate(Screen.ReportDetail.createRoute(report.id)) }
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
fun ReputationScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Mi reputación") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        if (user == null) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }

        val activeUser = user ?: return@Scaffold
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(NavyPrimary, Blue500)))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${activeUser.reputation}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Puntos de reputación", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (activeUser.reputation / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = StatusGreen,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Nivel: Vecino Confiable", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            item {
                Text("¿Cómo mejorar tu reputación?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                listOf(
                    ReputationTip(RaviaIconKind.Checkmark, "Reportes confirmados", "+10 pts", StatusGreen),
                    ReputationTip(RaviaIconKind.Eye, "Confirmar reportes vecinos", "+2 pts", Blue600),
                    ReputationTip(RaviaIconKind.Camera, "Agregar evidencia", "+5 pts", Cyan600),
                    ReputationTip(RaviaIconKind.Ban, "Reporte marcado como falso", "-15 pts", StatusRed)
                ).forEach { tip ->
                    ReputationTipCard(tip = tip)
                }
            }

            item {
                Text("Insignias", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        Triple(RaviaIconKind.Shield, "Guardian", MaterialTheme.colorScheme.primary),
                        Triple(RaviaIconKind.Siren, "Reportero", StatusOrange),
                        Triple(RaviaIconKind.Checkmark, "Verificador", StatusGreen)
                    ).forEach { (kind, name, color) ->
                        Card(modifier = Modifier.size(80.dp), shape = MaterialTheme.shapes.large) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color.copy(alpha = 0.12f), MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RaviaLineIcon(
                                        kind = kind,
                                        tint = color,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 1.9.dp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var name by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.id) {
        currentUser?.let {
            name = it.name
            zone = it.zone.orEmpty()
            phone = it.phone.orEmpty()
        }
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UiState.Success -> {
                viewModel.resetUpdateState()
                navController.navigateUp()
            }
            is UiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Volver") } },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateProfile(
                                name = name.trim(),
                                zone = zone.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null }
                            )
                        },
                        enabled = name.isNotBlank() && updateState !is UiState.Loading
                    ) { Text(if (updateState is UiState.Loading) "Guardando" else "Guardar") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        RaviaLineIcon(
                            kind = RaviaIconKind.User,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 2.4.dp
                        )
                    }
                }
            }
            item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item { OutlinedTextField(value = zone, onValueChange = { zone = it }, label = { Text("Colonia / Zona") }, leadingIcon = { RaviaLineIcon(RaviaIconKind.Location, MaterialTheme.colorScheme.primary, Modifier.size(20.dp), 1.8.dp) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
            item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono (opcional)") }, leadingIcon = { RaviaLineIcon(RaviaIconKind.Phone, MaterialTheme.colorScheme.primary, Modifier.size(20.dp), 1.8.dp) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    var darkMode by remember { mutableStateOf(false) }
    var locationAlways by remember { mutableStateOf(true) }
    var analyticsEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()

    LaunchedEffect(loggedOut) {
        if (loggedOut) navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { SettingGroup("Apariencia") }
            item {
                RaviaSettingSwitch(
                    kind = RaviaIconKind.Moon,
                    title = "Tema oscuro",
                    subtitle = "Usar fondo oscuro en la app",
                    color = MaterialTheme.colorScheme.primary,
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }
            item { SettingGroup("Privacidad") }
            item {
                RaviaSettingSwitch(
                    kind = RaviaIconKind.Location,
                    title = "Ubicación en segundo plano",
                    subtitle = "Para alertas geolocalizadas",
                    color = StatusGreen,
                    checked = locationAlways,
                    onCheckedChange = { locationAlways = it }
                )
            }
            item {
                RaviaSettingSwitch(
                    kind = RaviaIconKind.Spark,
                    title = "Analítica de uso",
                    subtitle = "Ayuda a mejorar la app",
                    color = Cyan600,
                    checked = analyticsEnabled,
                    onCheckedChange = { analyticsEnabled = it }
                )
            }
            item { SettingGroup("Cuenta") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showLogoutDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RaviaLineIcon(
                            kind = RaviaIconKind.Logout,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Cerrar sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir?") },
            confirmButton = { TextButton(onClick = { viewModel.logout() }) { Text("Salir", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }
}

private data class ReputationTip(
    val kind: RaviaIconKind,
    val title: String,
    val value: String,
    val color: Color
)

@Composable
private fun ReputationTipCard(tip: ReputationTip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(tip.color.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                RaviaLineIcon(tip.kind, tip.color, Modifier.size(19.dp), strokeWidth = 1.8.dp)
            }
            Spacer(Modifier.width(10.dp))
            Text(tip.title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(tip.value, style = MaterialTheme.typography.labelMedium, color = tip.color)
        }
    }
}

@Composable
private fun SettingGroup(title: String) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
}

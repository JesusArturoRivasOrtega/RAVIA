package com.ravia.app.presentation.moderation

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.toRelativeTime
import com.ravia.app.data.dto.UpdateMissingPersonStatusRequestDto
import com.ravia.app.data.dto.UpdateReportStatusRequestDto
import com.ravia.app.data.dto.UpdateUserRoleRequestDto
import com.ravia.app.data.dto.UpdateUserStatusRequestDto
import com.ravia.app.data.mapper.toBackendValue
import com.ravia.app.data.mapper.toDomain
import com.ravia.app.data.remote.MissingPersonsApi
import com.ravia.app.data.remote.ReportsApi
import com.ravia.app.data.remote.UsersApi
import com.ravia.app.domain.model.MissingPerson
import com.ravia.app.domain.model.MissingPersonStatus
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportStatus
import com.ravia.app.domain.model.User
import com.ravia.app.domain.model.UserRole
import com.ravia.app.domain.model.UserStatus
import com.ravia.app.domain.model.isTerminal
import com.ravia.app.domain.repository.AuthRepository
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.EmptyState
import com.ravia.app.presentation.components.ErrorState
import com.ravia.app.presentation.components.LoadingState
import com.ravia.app.presentation.components.PriorityBadge
import com.ravia.app.presentation.components.RaviaPhoto
import com.ravia.app.presentation.components.StatusBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

data class ModerationUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val pendingReports: List<Report> = emptyList(),
    val pendingMissingPersons: List<MissingPerson> = emptyList(),
    val users: List<User> = emptyList(),
    val warnings: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ModerationDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reportsApi: ReportsApi,
    private val missingPersonsApi: MissingPersonsApi,
    private val usersApi: UsersApi
) : ViewModel() {
    private companion object {
        const val TAG = "ModerationDashboard"
    }

    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, warnings = emptyList())

            val user = runCatching {
                authRepository.getCurrentUser().first()
                    ?: usersApi.getMe().requireBody("No se pudo cargar tu perfil").toDomain()
            }.onFailure {
                Log.e(TAG, "Failed to load current user", it)
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Error al cargar tu perfil")
            }.getOrNull() ?: return@launch

            val warnings = mutableListOf<String>()

            val isAdmin = user.role == UserRole.ADMIN

            val reports = runCatching {
                reportsApi.getReports(
                    status = if (isAdmin) null else "pending",
                    limit = if (isAdmin) 100 else 50,
                    activeOnly = if (isAdmin) false else null
                )
                    .requireBody(if (isAdmin) "No se pudieron cargar reportes" else "No se pudieron cargar reportes pendientes")
                    .map { it.toDomain() }
            }.onFailure {
                Log.e(TAG, "Failed to load reports", it)
                warnings += it.message ?: "No se pudieron cargar reportes"
            }.getOrDefault(emptyList())

            val missing = runCatching {
                if (isAdmin) {
                    missingPersonsApi.getAllMissingPersons(limit = 100)
                } else {
                    missingPersonsApi.getReviewQueue()
                }
                    .requireBody(if (isAdmin) "No se pudieron cargar fichas" else "No se pudieron cargar fichas pendientes")
                    .map { it.toDomain() }
            }.onFailure {
                Log.e(TAG, "Failed to load missing persons", it)
                warnings += it.message ?: "No se pudieron cargar fichas"
            }.getOrDefault(emptyList())

            val users = if (isAdmin) {
                runCatching {
                    usersApi.getUsers(limit = 100).requireBody("No se pudieron cargar usuarios").map { it.toDomain() }
                }.onFailure {
                    Log.e(TAG, "Failed to load users", it)
                    warnings += it.message ?: "No se pudieron cargar usuarios"
                }.getOrDefault(emptyList())
            } else {
                emptyList()
            }

            _uiState.value = ModerationUiState(
                isLoading = false,
                currentUser = user,
                pendingReports = reports,
                pendingMissingPersons = missing,
                users = users,
                warnings = warnings
            )
        }
    }

    fun updateReport(reportId: String, status: ReportStatus) {
        viewModelScope.launch {
            runCatching {
                reportsApi.updateStatus(reportId, UpdateReportStatusRequestDto(status.toBackendValue()))
                    .requireBody("No se pudo actualizar el reporte")
            }.onSuccess {
                refresh()
            }.onFailure {
                Log.e(TAG, "Failed to update report $reportId", it)
                _uiState.value = _uiState.value.copy(warnings = listOf(it.message ?: "No se pudo actualizar el reporte"))
            }
        }
    }

    fun updateMissingPerson(personId: String, status: String) {
        viewModelScope.launch {
            runCatching {
                missingPersonsApi.updateStatus(personId, UpdateMissingPersonStatusRequestDto(status))
                    .requireBody("No se pudo actualizar la ficha")
            }.onSuccess {
                refresh()
            }.onFailure {
                Log.e(TAG, "Failed to update missing person $personId", it)
                _uiState.value = _uiState.value.copy(warnings = listOf(it.message ?: "No se pudo actualizar la ficha"))
            }
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            runCatching {
                reportsApi.deleteReport(reportId).requireUnit("No se pudo eliminar el reporte")
            }.onSuccess {
                refresh()
            }.onFailure {
                Log.e(TAG, "Failed to delete report $reportId", it)
                _uiState.value = _uiState.value.copy(warnings = listOf(it.message ?: "No se pudo eliminar el reporte"))
            }
        }
    }

    fun deleteMissingPerson(personId: String) {
        viewModelScope.launch {
            runCatching {
                missingPersonsApi.deleteMissingPerson(personId).requireUnit("No se pudo eliminar la ficha")
            }.onSuccess {
                refresh()
            }.onFailure {
                Log.e(TAG, "Failed to delete missing person $personId", it)
                _uiState.value = _uiState.value.copy(warnings = listOf(it.message ?: "No se pudo eliminar la ficha"))
            }
        }
    }

    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            runCatching {
                usersApi.updateRole(userId, UpdateUserRoleRequestDto(role.name.lowercase()))
                    .requireBody("No se pudo cambiar el rol")
            }.onSuccess {
                refresh()
            }.onFailure {
                Log.e(TAG, "Failed to update role for $userId", it)
                _uiState.value = _uiState.value.copy(warnings = listOf(it.message ?: "No se pudo cambiar el rol"))
            }
        }
    }

    fun updateUserStatus(userId: String, status: UserStatus) {
        viewModelScope.launch {
            runCatching {
                usersApi.updateStatus(userId, UpdateUserStatusRequestDto(status.name.lowercase()))
                    .requireBody("No se pudo cambiar el estado")
            }.onSuccess {
                refresh()
            }.onFailure {
                Log.e(TAG, "Failed to update status for $userId", it)
                _uiState.value = _uiState.value.copy(warnings = listOf(it.message ?: "No se pudo cambiar el estado"))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationDashboardScreen(
    navController: NavController,
    viewModel: ModerationDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.currentUser) {
        if (state.currentUser?.role == UserRole.CITIZEN) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Moderation.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.currentUser?.role == UserRole.ADMIN) "Panel admin" else "Panel moderador")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                        Icon(Icons.Default.ArrowBack, "Ir al inicio ciudadano")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Outlined.Refresh, "Actualizar")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Outlined.Person, "Perfil")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(state.error.orEmpty(), onRetry = viewModel::refresh, modifier = Modifier.padding(padding))
            else -> ModerationContent(
                state = state,
                onOpenHome = { navController.navigate(Screen.Home.route) },
                onOpenReport = { navController.navigate(Screen.ReportDetail.createRoute(it)) },
                onApproveReport = { viewModel.updateReport(it, ReportStatus.CONFIRMED) },
                onCriticalReport = { viewModel.updateReport(it, ReportStatus.CRITICAL) },
                onProgressReport = { viewModel.updateReport(it, ReportStatus.IN_PROGRESS) },
                onResolveReport = { viewModel.updateReport(it, ReportStatus.RESOLVED) },
                onDuplicateReport = { viewModel.updateReport(it, ReportStatus.DUPLICATED) },
                onRejectReport = { viewModel.updateReport(it, ReportStatus.FALSE) },
                onDeleteReport = viewModel::deleteReport,
                onApproveMissing = { viewModel.updateMissingPerson(it, "active") },
                onFoundMissing = { viewModel.updateMissingPerson(it, "found") },
                onRejectMissing = { viewModel.updateMissingPerson(it, "cancelled") },
                onDeleteMissing = viewModel::deleteMissingPerson,
                onUpdateUserRole = viewModel::updateUserRole,
                onUpdateUserStatus = viewModel::updateUserStatus,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ModerationContent(
    state: ModerationUiState,
    onOpenHome: () -> Unit,
    onOpenReport: (String) -> Unit,
    onApproveReport: (String) -> Unit,
    onCriticalReport: (String) -> Unit,
    onProgressReport: (String) -> Unit,
    onResolveReport: (String) -> Unit,
    onDuplicateReport: (String) -> Unit,
    onRejectReport: (String) -> Unit,
    onDeleteReport: (String) -> Unit,
    onApproveMissing: (String) -> Unit,
    onFoundMissing: (String) -> Unit,
    onRejectMissing: (String) -> Unit,
    onDeleteMissing: (String) -> Unit,
    onUpdateUserRole: (String, UserRole) -> Unit,
    onUpdateUserStatus: (String, UserStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = state.currentUser?.role == UserRole.ADMIN
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ModerationHero(
                isAdmin = state.currentUser?.role == UserRole.ADMIN,
                pendingReports = state.pendingReports.size,
                pendingMissingPersons = state.pendingMissingPersons.size,
                onOpenHome = onOpenHome
            )
        }

        if (state.warnings.isNotEmpty()) {
            item {
                WarningPanel(state.warnings)
            }
        }

        item {
            SectionHeader(if (isAdmin) "Todos los reportes" else "Reportes pendientes", state.pendingReports.size)
        }
        if (state.pendingReports.isEmpty()) {
            item { EmptyState(title = "Sin reportes pendientes", subtitle = "La cola esta limpia por ahora.") }
        } else {
            items(state.pendingReports, key = { it.id }) { report ->
                PendingReportCard(
                    report = report,
                    isAdmin = isAdmin,
                    onOpen = onOpenReport,
                    onApprove = onApproveReport,
                    onCritical = onCriticalReport,
                    onProgress = onProgressReport,
                    onResolve = onResolveReport,
                    onDuplicate = onDuplicateReport,
                    onReject = onRejectReport,
                    onDelete = onDeleteReport
                )
            }
        }

        item {
            SectionHeader(if (isAdmin) "Personas desaparecidas" else "Personas desaparecidas por verificar", state.pendingMissingPersons.size)
        }
        if (state.pendingMissingPersons.isEmpty()) {
            item { EmptyState(title = "Sin fichas pendientes", subtitle = "No hay solicitudes nuevas de busqueda.") }
        } else {
            items(state.pendingMissingPersons, key = { it.id }) { person ->
                PendingMissingPersonCard(
                    person = person,
                    isAdmin = isAdmin,
                    onApprove = onApproveMissing,
                    onFound = onFoundMissing,
                    onReject = onRejectMissing,
                    onDelete = onDeleteMissing
                )
            }
        }

        if (state.currentUser?.role == UserRole.ADMIN) {
            item { SectionHeader("Usuarios", state.users.size) }
            items(state.users, key = { it.id }) { user ->
                UserRow(user, onUpdateUserRole, onUpdateUserStatus)
            }
        }
    }
}

@Composable
private fun ModerationHero(
    isAdmin: Boolean,
    pendingReports: Int,
    pendingMissingPersons: Int,
    onOpenHome: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isAdmin) "Operacion administrativa" else "Revision operativa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Valida reportes, prioriza incidentes y publica fichas revisadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ModerationMetric("Reportes", pendingReports, Modifier.weight(1f))
                ModerationMetric("Fichas", pendingMissingPersons, Modifier.weight(1f))
            }

            OutlinedButton(onClick = onOpenHome, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Home, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Abrir vista ciudadana")
            }
        }
    }
}

@Composable
private fun ModerationMetric(label: String, count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WarningPanel(warnings: List<String>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Hay secciones que no cargaron", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            warnings.distinct().forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Badge(
            containerColor = if (count > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (count > 0) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text(count.toString())
        }
    }
}

@Composable
private fun PendingReportCard(
    report: Report,
    isAdmin: Boolean,
    onOpen: (String) -> Unit,
    onApprove: (String) -> Unit,
    onCritical: (String) -> Unit,
    onProgress: (String) -> Unit,
    onResolve: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onReject: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        report.createdAt.toRelativeTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(report.status)
            }
            Text(
                report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(report.priority)
                AssistChip(onClick = { onOpen(report.id) }, label = { Text("Detalle") })
            }
            Button(
                onClick = { onApprove(report.id) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Confirmar reporte")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { onCritical(report.id) },
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                ) {
                    Text("Critico")
                }
                OutlinedButton(
                    onClick = { onReject(report.id) },
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                ) {
                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Falso")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (report.status != ReportStatus.IN_PROGRESS && !report.status.isTerminal()) {
                    AssistChip(onClick = { onProgress(report.id) }, label = { Text("En atencion") })
                }
                if (report.status != ReportStatus.RESOLVED) {
                    AssistChip(onClick = { onResolve(report.id) }, label = { Text("Resolver") })
                }
                if (report.status != ReportStatus.DUPLICATED) {
                    AssistChip(onClick = { onDuplicate(report.id) }, label = { Text("Duplicado") })
                }
                if (isAdmin) {
                    AssistChip(
                        onClick = { onDelete(report.id) },
                        label = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingMissingPersonCard(
    person: MissingPerson,
    isAdmin: Boolean,
    onApprove: (String) -> Unit,
    onFound: (String) -> Unit,
    onReject: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RaviaPhoto(
                    model = person.photoUrl,
                    contentDescription = "Foto de ${person.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.medium)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        person.status.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        person.lastSeenLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(person.createdAt.toRelativeTime(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                person.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (person.status != MissingPersonStatus.ACTIVE) {
                    Button(onClick = { onApprove(person.id) }, modifier = Modifier.heightIn(min = 44.dp)) {
                        Text("Publicar")
                    }
                }
                if (person.status != MissingPersonStatus.FOUND) {
                    OutlinedButton(onClick = { onFound(person.id) }, modifier = Modifier.heightIn(min = 44.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Encontrada")
                    }
                }
                OutlinedButton(onClick = { onReject(person.id) }, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text("Rechazar")
                }
                if (isAdmin) {
                    OutlinedButton(onClick = { onDelete(person.id) }, modifier = Modifier.heightIn(min = 44.dp)) {
                        Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: User,
    onUpdateRole: (String, UserRole) -> Unit,
    onUpdateStatus: (String, UserStatus) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(user.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${user.reputation} pts, ${user.reportsCount} reportes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    AssistChip(onClick = {}, label = { Text(user.role.name.lowercase()) })
                    AssistChip(onClick = {}, label = { Text(user.status.name.lowercase()) })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(UserRole.CITIZEN, UserRole.MODERATOR, UserRole.ADMIN)
                    .filter { it != user.role }
                    .forEach { role ->
                        OutlinedButton(onClick = { onUpdateRole(user.id, role) }) {
                            Text(role.name.lowercase())
                        }
                    }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.status != UserStatus.ACTIVE) {
                    Button(onClick = { onUpdateStatus(user.id, UserStatus.ACTIVE) }) {
                        Text("Activar")
                    }
                }
                if (user.status != UserStatus.SUSPENDED) {
                    OutlinedButton(onClick = { onUpdateStatus(user.id, UserStatus.SUSPENDED) }) {
                        Text("Suspender")
                    }
                }
                if (user.status != UserStatus.BANNED) {
                    OutlinedButton(onClick = { onUpdateStatus(user.id, UserStatus.BANNED) }) {
                        Text("Banear")
                    }
                }
            }
        }
    }
}

private fun <T> Response<T>.requireBody(action: String): T {
    if (isSuccessful) return body() ?: throw IllegalStateException("$action: respuesta vacia.")
    val raw = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    val parsedMessage = runCatching {
        JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
    }.getOrNull()
    throw IllegalStateException("$action (${code()}): ${parsedMessage ?: raw}")
}

private fun Response<Unit>.requireUnit(action: String) {
    if (isSuccessful) return
    val raw = errorBody()?.string()?.takeIf { it.isNotBlank() } ?: message()
    val parsedMessage = runCatching {
        JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
    }.getOrNull()
    throw IllegalStateException("$action (${code()}): ${parsedMessage ?: raw}")
}

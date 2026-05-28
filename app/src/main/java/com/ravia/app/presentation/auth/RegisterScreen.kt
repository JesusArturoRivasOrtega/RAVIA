package com.ravia.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ravia.app.core.utils.UiState
import com.ravia.app.core.utils.isValidEmail
import com.ravia.app.navigation.Screen
import com.ravia.app.navigation.roleHomeRoute
import com.ravia.app.presentation.components.AnimatedSignalRings
import com.ravia.app.presentation.components.RaviaIconKind
import com.ravia.app.presentation.components.RaviaLineIcon
import com.ravia.app.presentation.components.RaviaPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(authState) {
        when (authState) {
            is UiState.Success -> {
                val user = (authState as UiState.Success).data
                navController.navigate(user.roleHomeRoute()) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((authState as UiState.Error).message)
            }
            else -> Unit
        }
    }

    fun validate(): Boolean {
        val newErrors = mutableMapOf<String, String>()
        if (name.trim().length < 2) newErrors["name"] = "Ingresa tu nombre completo"
        if (!email.isValidEmail()) newErrors["email"] = "Correo no válido"
        if (password.length < 6) newErrors["password"] = "Mínimo 6 caracteres"
        if (password != confirmPassword) newErrors["confirm"] = "Las contraseñas no coinciden"
        if (!acceptedTerms) newErrors["terms"] = "Debes aceptar los términos"
        errors = newErrors
        return newErrors.isEmpty()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Únete a RAVIA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Crea tu cuenta para reportar y recibir alertas en tu comunidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                        AnimatedSignalRings(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.matchParentSize(),
                            ringCount = 2
                        )
                        RaviaLineIcon(
                            kind = RaviaIconKind.Shield,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp),
                            strokeWidth = 2.2.dp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Comunidad verificada",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Tu perfil ayuda a coordinar reportes reales y alertas utiles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it; errors = errors - "name" },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Outlined.Person, null) },
                isError = errors.containsKey("name"),
                supportingText = errors["name"]?.let {{ Text(it) }},
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true
            )

            OutlinedTextField(
                value = email, onValueChange = { email = it; errors = errors - "email" },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                isError = errors.containsKey("email"),
                supportingText = errors["email"]?.let {{ Text(it) }},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it; errors = errors - "password" },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                isError = errors.containsKey("password"),
                supportingText = errors["password"]?.let {{ Text(it) }},
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it; errors = errors - "confirm" },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                isError = errors.containsKey("confirm"),
                supportingText = errors["confirm"]?.let {{ Text(it) }},
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true
            )

            OutlinedTextField(
                value = zone, onValueChange = { zone = it },
                label = { Text("Colonia / Zona (opcional)") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it; errors = errors - "terms" }
                )
                Text(
                    "Acepto los términos y condiciones de uso y la política de privacidad",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (errors.containsKey("terms")) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            RaviaPrimaryButton(
                text = "Crear cuenta",
                onClick = { if (validate()) viewModel.register(name, email, password, zone.ifBlank { null }) },
                isLoading = authState is UiState.Loading && name.isNotEmpty()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes cuenta?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }) { Text("Inicia sesión") }
            }
        }
    }
}

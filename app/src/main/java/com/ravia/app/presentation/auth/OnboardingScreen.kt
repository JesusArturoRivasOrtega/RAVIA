package com.ravia.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ravia.app.R
import com.ravia.app.navigation.Screen
import com.ravia.app.presentation.components.AnimatedSignalRings
import com.ravia.app.presentation.components.RaviaIconKind
import com.ravia.app.presentation.components.RaviaLineIcon
import com.ravia.app.presentation.components.RaviaPrimaryButton
import com.ravia.app.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: RaviaIconKind,
    val iconBg: Color,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        icon = RaviaIconKind.Report,
        iconBg = StatusRed,
        title = "Reporta emergencias\ncercanas",
        description = "Informa en segundos sobre incidentes de seguridad, accidentes, incendios y más. Tu reporte puede salvar vidas."
    ),
    OnboardingPage(
        icon = RaviaIconKind.Siren,
        iconBg = StatusAmberDark,
        title = "Recibe alertas de\ntu comunidad",
        description = "Notificaciones en tiempo real de lo que ocurre cerca de ti. Siempre informado, siempre seguro."
    ),
    OnboardingPage(
        icon = RaviaIconKind.Compass,
        iconBg = Blue600,
        title = "Consulta el mapa\nde incidentes",
        description = "Visualiza todos los reportes activos en tu zona, zonas de riesgo y niveles de alerta en un mapa interactivo."
    ),
    OnboardingPage(
        icon = RaviaIconKind.People,
        iconBg = StatusGreen,
        title = "Ayuda a confirmar\ninformación",
        description = "La validación comunitaria mejora la precisión. Tu confirmación aumenta la credibilidad de los reportes."
    ),
    OnboardingPage(
        icon = RaviaIconKind.Chat,
        iconBg = Cyan600,
        title = "Orientación\nrápida con IA",
        description = "Nuestro asistente inteligente te guía en emergencias. Información actualizada cuando más la necesitas."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }) {
                Text("Omitir", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        // Bottom section
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dots indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == idx) 24.dp else 8.dp, 8.dp)
                            .background(
                                color = if (pagerState.currentPage == idx)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (pagerState.currentPage < pages.size - 1) {
                RaviaPrimaryButton(
                    text = "Siguiente",
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                )
            } else {
                RaviaPrimaryButton(
                    text = "Comenzar",
                    onClick = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ya tengo una cuenta", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Navy900),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ravia_safety_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .background(Navy900)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.05f), Navy950.copy(alpha = 0.72f))
                        )
                    )
            )
            AnimatedSignalRings(
                color = page.iconBg,
                modifier = Modifier.size(136.dp),
                ringCount = 3
            )
            Surface(
                modifier = Modifier.size(92.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    RaviaLineIcon(
                        kind = page.icon,
                        tint = page.iconBg,
                        modifier = Modifier.size(50.dp),
                        strokeWidth = 2.3.dp
                    )
                }
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 26.sp
        )
    }
}

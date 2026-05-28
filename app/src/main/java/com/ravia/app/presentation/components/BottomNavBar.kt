package com.ravia.app.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ravia.app.navigation.Screen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: RaviaIconKind
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Inicio", RaviaIconKind.Shield),
    BottomNavItem(Screen.Map.route, "Mapa", RaviaIconKind.Compass),
    BottomNavItem(Screen.Alerts.route, "Alertas", RaviaIconKind.Siren),
    BottomNavItem(Screen.Profile.route, "Perfil", RaviaIconKind.People),
)

@Composable
fun RaviaBottomNavBar(
    navController: NavController,
    unreadAlertCount: Int = 0
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                alwaysShowLabel = true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    if (item.route == Screen.Alerts.route && unreadAlertCount > 0) {
                        BadgedBox(badge = {
                            Badge { Text(if (unreadAlertCount > 9) "9+" else "$unreadAlertCount") }
                        }) {
                            AnimatedBottomNavIcon(item = item, selected = selected)
                        }
                    } else {
                        AnimatedBottomNavIcon(item = item, selected = selected)
                    }
                },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun AnimatedBottomNavIcon(
    item: BottomNavItem,
    selected: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "bottomNavIconScale"
    )

    Crossfade(
        targetState = selected,
        animationSpec = tween(durationMillis = 160),
        label = "bottomNavIcon"
    ) { isSelected ->
        val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        RaviaLineIcon(
            kind = item.icon,
            tint = tint,
            strokeWidth = if (isSelected) 2.3.dp else 1.8.dp,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

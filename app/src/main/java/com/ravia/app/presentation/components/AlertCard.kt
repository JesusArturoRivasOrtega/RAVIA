package com.ravia.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ravia.app.core.utils.toRelativeTime
import com.ravia.app.domain.model.Alert
import com.ravia.app.domain.model.AlertSeverity
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AlertCard(
    alert: Alert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetBorderColor = if (!alert.isRead) alert.severity.toColor() else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val targetBgColor = if (!alert.isRead) alert.severity.toBgColor() else MaterialTheme.colorScheme.surface
    val borderColor by animateColorAsState(targetBorderColor, tween(350), label = "alertBorder")
    val bgColor by animateColorAsState(targetBgColor, tween(350), label = "alertBackground")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.987f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "alertCardPress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(1.dp, MaterialTheme.shapes.large, clip = false)
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        bgColor,
                        alert.severity.toColor().copy(alpha = if (!alert.isRead) 0.09f else 0.035f)
                    )
                )
            )
            .animateContentSize(animationSpec = tween(durationMillis = 250))
            .border(
                width = if (!alert.isRead) 1.5.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(58.dp)
                .clip(MaterialTheme.shapes.small)
                .background(alert.severity.toColor())
        )

        Spacer(Modifier.width(10.dp))

        Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
            if (!alert.isRead && alert.severity == AlertSeverity.CRITICAL) {
                AnimatedSignalRings(
                    color = alert.severity.toColor(),
                    modifier = Modifier.matchParentSize(),
                    ringCount = 2
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(alert.severity.toColor().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                RaviaLineIcon(
                    kind = alert.severity.toRaviaIconKind(),
                    tint = alert.severity.toColor(),
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (!alert.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (!alert.isRead) {
                    Spacer(Modifier.width(6.dp))
                    AnimatedUnreadDot(color = alert.severity.toColor())
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SeverityBadge(severity = alert.severity)
                Spacer(Modifier.weight(1f))
                if (alert.distanceKm != null) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${String.format("%.1f", alert.distanceKm)} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp, end = 8.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = alert.createdAt.toRelativeTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

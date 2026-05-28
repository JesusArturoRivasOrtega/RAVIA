package com.ravia.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravia.app.domain.model.*
import com.ravia.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

fun ReportStatus.toColor(): Color = when (this) {
    ReportStatus.PENDING      -> StatusGray
    ReportStatus.VERIFYING    -> StatusAmber
    ReportStatus.CONFIRMED    -> StatusGreen
    ReportStatus.CRITICAL     -> StatusRed
    ReportStatus.IN_PROGRESS  -> StatusBlue
    ReportStatus.RESOLVED     -> StatusGreenDark
    ReportStatus.FALSE        -> StatusGrayDark
    ReportStatus.DUPLICATED   -> StatusPurple
}

fun ReportStatus.toBackgroundColor(): Color = when (this) {
    ReportStatus.PENDING      -> StatusGrayBg
    ReportStatus.VERIFYING    -> StatusAmberBg
    ReportStatus.CONFIRMED    -> StatusGreenBg
    ReportStatus.CRITICAL     -> StatusRedBg
    ReportStatus.IN_PROGRESS  -> Color(0xFFE3F2FD)
    ReportStatus.RESOLVED     -> StatusGreenBg
    ReportStatus.FALSE        -> StatusGrayBg
    ReportStatus.DUPLICATED   -> StatusPurpleBg
}

fun ReportStatus.toIcon(): ImageVector = when (this) {
    ReportStatus.PENDING      -> Icons.Default.HourglassEmpty
    ReportStatus.VERIFYING    -> Icons.Default.Search
    ReportStatus.CONFIRMED    -> Icons.Default.Verified
    ReportStatus.CRITICAL     -> Icons.Default.Emergency
    ReportStatus.IN_PROGRESS  -> Icons.Default.BuildCircle
    ReportStatus.RESOLVED     -> Icons.Default.TaskAlt
    ReportStatus.FALSE        -> Icons.Default.Cancel
    ReportStatus.DUPLICATED   -> Icons.Default.ContentCopy
}

fun ReportStatus.toRaviaIconKind(): RaviaIconKind = when (this) {
    ReportStatus.PENDING      -> RaviaIconKind.Clock
    ReportStatus.VERIFYING    -> RaviaIconKind.Radar
    ReportStatus.CONFIRMED    -> RaviaIconKind.Checkmark
    ReportStatus.CRITICAL     -> RaviaIconKind.Siren
    ReportStatus.IN_PROGRESS  -> RaviaIconKind.Tool
    ReportStatus.RESOLVED     -> RaviaIconKind.Checkmark
    ReportStatus.FALSE        -> RaviaIconKind.Ban
    ReportStatus.DUPLICATED   -> RaviaIconKind.File
}

fun ReportPriority.toColor(): Color = when (this) {
    ReportPriority.LOW      -> PriorityLow
    ReportPriority.MEDIUM   -> PriorityMedium
    ReportPriority.HIGH     -> PriorityHigh
    ReportPriority.CRITICAL -> PriorityCritical
}

fun ReportPriority.toIcon(): ImageVector = when (this) {
    ReportPriority.LOW      -> Icons.Default.Info
    ReportPriority.MEDIUM   -> Icons.Default.Warning
    ReportPriority.HIGH     -> Icons.Default.PriorityHigh
    ReportPriority.CRITICAL -> Icons.Default.Emergency
}

fun ReportPriority.toRaviaIconKind(): RaviaIconKind = when (this) {
    ReportPriority.LOW      -> RaviaIconKind.Eye
    ReportPriority.MEDIUM   -> RaviaIconKind.Alert
    ReportPriority.HIGH     -> RaviaIconKind.Siren
    ReportPriority.CRITICAL -> RaviaIconKind.Siren
}

fun AlertSeverity.toColor(): Color = when (this) {
    AlertSeverity.INFO     -> StatusBlue
    AlertSeverity.CAUTION  -> StatusAmber
    AlertSeverity.URGENT   -> StatusOrange
    AlertSeverity.CRITICAL -> StatusRed
}

fun AlertSeverity.toBgColor(): Color = when (this) {
    AlertSeverity.INFO     -> Color(0xFFE3F2FD)
    AlertSeverity.CAUTION  -> StatusAmberBg
    AlertSeverity.URGENT   -> StatusOrangeBg
    AlertSeverity.CRITICAL -> StatusRedBg
}

fun AlertSeverity.toIcon(): ImageVector = when (this) {
    AlertSeverity.INFO     -> Icons.Default.Info
    AlertSeverity.CAUTION  -> Icons.Default.Warning
    AlertSeverity.URGENT   -> Icons.Default.NotificationsActive
    AlertSeverity.CRITICAL -> Icons.Default.Emergency
}

fun AlertSeverity.toRaviaIconKind(): RaviaIconKind = when (this) {
    AlertSeverity.INFO     -> RaviaIconKind.Spark
    AlertSeverity.CAUTION  -> RaviaIconKind.Alert
    AlertSeverity.URGENT   -> RaviaIconKind.Siren
    AlertSeverity.CRITICAL -> RaviaIconKind.Siren
}

fun RiskLevel.toColor(): Color = when (this) {
    RiskLevel.LOW      -> StatusGreen
    RiskLevel.MEDIUM   -> StatusAmber
    RiskLevel.HIGH     -> StatusOrange
    RiskLevel.CRITICAL -> StatusRed
}

@Composable
fun StatusBadge(status: ReportStatus, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(status.toBackgroundColor(), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = status.toIcon(),
            contentDescription = null,
            tint = status.toColor(),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = status.displayName(),
            color = status.toColor(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: ReportPriority, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(priority.toColor().copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = priority.toIcon(),
            contentDescription = null,
            tint = priority.toColor(),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = priority.displayName(),
            color = priority.toColor(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(severity.toBgColor(), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = severity.toIcon(),
            contentDescription = null,
            tint = severity.toColor(),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = severity.displayName(),
            color = severity.toColor(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

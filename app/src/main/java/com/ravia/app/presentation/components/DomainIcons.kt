package com.ravia.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.ui.theme.Blue600
import com.ravia.app.ui.theme.Cyan600
import com.ravia.app.ui.theme.StatusAmberDark
import com.ravia.app.ui.theme.StatusBlue
import com.ravia.app.ui.theme.StatusGreenDark
import com.ravia.app.ui.theme.StatusOrange
import com.ravia.app.ui.theme.StatusPurple
import com.ravia.app.ui.theme.StatusRed

fun ReportCategory.toIcon(): ImageVector = when (this) {
    ReportCategory.SECURITY -> Icons.Outlined.LocalPolice
    ReportCategory.ACCIDENT -> Icons.Outlined.DirectionsCar
    ReportCategory.INJURED_PERSON -> Icons.Outlined.MedicalServices
    ReportCategory.FIRE -> Icons.Outlined.LocalFireDepartment
    ReportCategory.FLOOD -> Icons.Outlined.WaterDrop
    ReportCategory.DANGEROUS_ANIMAL -> Icons.Outlined.Pets
    ReportCategory.MISSING_PERSON -> Icons.Outlined.PersonSearch
    ReportCategory.VIOLENCE -> Icons.Outlined.ReportProblem
    ReportCategory.INFRASTRUCTURE -> Icons.Outlined.Construction
    ReportCategory.RISK_ZONE -> Icons.Outlined.Block
    ReportCategory.OTHER -> Icons.Outlined.MoreHoriz
}

fun ReportCategory.toAccentColor(): Color = when (this) {
    ReportCategory.SECURITY -> Blue600
    ReportCategory.ACCIDENT -> StatusOrange
    ReportCategory.INJURED_PERSON -> StatusRed
    ReportCategory.FIRE -> StatusRed
    ReportCategory.FLOOD -> Cyan600
    ReportCategory.DANGEROUS_ANIMAL -> StatusAmberDark
    ReportCategory.MISSING_PERSON -> StatusPurple
    ReportCategory.VIOLENCE -> StatusRed
    ReportCategory.INFRASTRUCTURE -> StatusAmberDark
    ReportCategory.RISK_ZONE -> StatusOrange
    ReportCategory.OTHER -> StatusBlue
}

fun ReportCategory.toRaviaIconKind(): RaviaIconKind = when (this) {
    ReportCategory.SECURITY -> RaviaIconKind.Shield
    ReportCategory.ACCIDENT -> RaviaIconKind.Car
    ReportCategory.INJURED_PERSON -> RaviaIconKind.Medical
    ReportCategory.FIRE -> RaviaIconKind.Fire
    ReportCategory.FLOOD -> RaviaIconKind.Water
    ReportCategory.DANGEROUS_ANIMAL -> RaviaIconKind.Paw
    ReportCategory.MISSING_PERSON -> RaviaIconKind.People
    ReportCategory.VIOLENCE -> RaviaIconKind.Siren
    ReportCategory.INFRASTRUCTURE -> RaviaIconKind.Tool
    ReportCategory.RISK_ZONE -> RaviaIconKind.Radar
    ReportCategory.OTHER -> RaviaIconKind.Spark
}

@Composable
fun CategoryIconBadge(
    category: ReportCategory,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    tint: Color = category.toAccentColor(),
    containerColor: Color = category.toAccentColor().copy(alpha = if (selected) 0.20f else 0.12f)
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "categoryIconScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        RaviaLineIcon(
            kind = category.toRaviaIconKind(),
            tint = tint,
            modifier = Modifier.size(iconSize),
            strokeWidth = if (selected) 2.3.dp else 1.9.dp
        )
    }
}

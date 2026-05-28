package com.ravia.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class RaviaIconKind {
    Alert,
    Ban,
    Bell,
    Camera,
    Car,
    Chat,
    Checkmark,
    Chevron,
    Clock,
    Compass,
    Edit,
    Eye,
    File,
    Fire,
    Gear,
    Location,
    Lock,
    Logout,
    Map,
    Medical,
    Moon,
    Paw,
    People,
    Phone,
    Radar,
    Radius,
    Report,
    Shield,
    Siren,
    Spark,
    Tool,
    Trophy,
    User,
    Vibrate,
    Water,
}

@Composable
fun RaviaLineIcon(
    kind: RaviaIconKind,
    tint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val w = size.width
        val h = size.height

        when (kind) {
            RaviaIconKind.Alert -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.14f)
                    lineTo(w * 0.88f, h * 0.82f)
                    lineTo(w * 0.12f, h * 0.82f)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.50f, h * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, radius = stroke.width * 0.55f, center = Offset(w * 0.50f, h * 0.69f))
            }
            RaviaIconKind.Ban -> {
                drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.28f, h * 0.72f), Offset(w * 0.72f, h * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Bell -> {
                val bell = Path().apply {
                    moveTo(w * 0.28f, h * 0.66f)
                    cubicTo(w * 0.34f, h * 0.56f, w * 0.32f, h * 0.42f, w * 0.38f, h * 0.32f)
                    cubicTo(w * 0.44f, h * 0.22f, w * 0.56f, h * 0.22f, w * 0.62f, h * 0.32f)
                    cubicTo(w * 0.68f, h * 0.42f, w * 0.66f, h * 0.56f, w * 0.72f, h * 0.66f)
                    lineTo(w * 0.28f, h * 0.66f)
                }
                drawPath(bell, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.40f, h * 0.74f), Offset(w * 0.60f, h * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, radius = w * 0.035f, center = Offset(w * 0.50f, h * 0.81f))
            }
            RaviaIconKind.Camera -> {
                drawRoundRect(tint, Offset(w * 0.18f, h * 0.30f), Size(w * 0.64f, h * 0.46f), CornerRadius(w * 0.08f, w * 0.08f), style = stroke)
                drawLine(tint, Offset(w * 0.34f, h * 0.30f), Offset(w * 0.40f, h * 0.20f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.40f, h * 0.20f), Offset(w * 0.60f, h * 0.20f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.60f, h * 0.20f), Offset(w * 0.66f, h * 0.30f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.54f), style = stroke)
            }
            RaviaIconKind.Car -> {
                val car = Path().apply {
                    moveTo(w * 0.18f, h * 0.60f)
                    lineTo(w * 0.25f, h * 0.42f)
                    lineTo(w * 0.38f, h * 0.32f)
                    lineTo(w * 0.62f, h * 0.32f)
                    lineTo(w * 0.75f, h * 0.42f)
                    lineTo(w * 0.82f, h * 0.60f)
                    lineTo(w * 0.18f, h * 0.60f)
                }
                drawPath(car, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.32f, h * 0.44f), Offset(w * 0.68f, h * 0.44f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.32f, h * 0.68f), style = stroke)
                drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.68f, h * 0.68f), style = stroke)
            }
            RaviaIconKind.Chat -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.14f, h * 0.18f),
                    size = Size(w * 0.72f, h * 0.54f),
                    cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
                    style = stroke
                )
                val tail = Path().apply {
                    moveTo(w * 0.36f, h * 0.72f)
                    lineTo(w * 0.28f, h * 0.86f)
                    lineTo(w * 0.50f, h * 0.72f)
                }
                drawPath(tail, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.34f, h * 0.43f), Offset(w * 0.66f, h * 0.43f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.34f, h * 0.56f), Offset(w * 0.56f, h * 0.56f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Checkmark -> {
                drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.34f, h * 0.52f), Offset(w * 0.46f, h * 0.64f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.46f, h * 0.64f), Offset(w * 0.68f, h * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Chevron -> {
                drawLine(tint, Offset(w * 0.38f, h * 0.24f), Offset(w * 0.62f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.62f, h * 0.50f), Offset(w * 0.38f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Clock -> {
                drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.30f), Offset(w * 0.50f, h * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.52f), Offset(w * 0.66f, h * 0.60f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Map -> {
                val path = Path().apply {
                    moveTo(w * 0.18f, h * 0.25f)
                    lineTo(w * 0.40f, h * 0.16f)
                    lineTo(w * 0.62f, h * 0.25f)
                    lineTo(w * 0.84f, h * 0.16f)
                    lineTo(w * 0.84f, h * 0.76f)
                    lineTo(w * 0.62f, h * 0.85f)
                    lineTo(w * 0.40f, h * 0.76f)
                    lineTo(w * 0.18f, h * 0.85f)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.40f, h * 0.16f), Offset(w * 0.40f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.62f, h * 0.25f), Offset(w * 0.62f, h * 0.85f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Compass -> {
                drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                val needle = Path().apply {
                    moveTo(w * 0.58f, h * 0.22f)
                    lineTo(w * 0.46f, h * 0.56f)
                    lineTo(w * 0.74f, h * 0.42f)
                    close()
                }
                drawPath(needle, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.28f, h * 0.76f), Offset(w * 0.44f, h * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Edit -> {
                drawRoundRect(tint, Offset(w * 0.20f, h * 0.20f), Size(w * 0.54f, h * 0.60f), CornerRadius(w * 0.06f, w * 0.06f), style = stroke)
                drawLine(tint, Offset(w * 0.40f, h * 0.62f), Offset(w * 0.78f, h * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.68f, h * 0.22f), Offset(w * 0.80f, h * 0.34f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Eye -> {
                val eye = Path().apply {
                    moveTo(w * 0.14f, h * 0.50f)
                    cubicTo(w * 0.28f, h * 0.30f, w * 0.72f, h * 0.30f, w * 0.86f, h * 0.50f)
                    cubicTo(w * 0.72f, h * 0.70f, w * 0.28f, h * 0.70f, w * 0.14f, h * 0.50f)
                }
                drawPath(eye, color = tint, style = stroke)
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
            }
            RaviaIconKind.File -> {
                val file = Path().apply {
                    moveTo(w * 0.28f, h * 0.14f)
                    lineTo(w * 0.58f, h * 0.14f)
                    lineTo(w * 0.74f, h * 0.30f)
                    lineTo(w * 0.74f, h * 0.86f)
                    lineTo(w * 0.28f, h * 0.86f)
                    close()
                }
                drawPath(file, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.58f, h * 0.14f), Offset(w * 0.58f, h * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.58f, h * 0.32f), Offset(w * 0.74f, h * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.38f, h * 0.52f), Offset(w * 0.64f, h * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.38f, h * 0.66f), Offset(w * 0.58f, h * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Fire -> {
                val flame = Path().apply {
                    moveTo(w * 0.52f, h * 0.88f)
                    cubicTo(w * 0.30f, h * 0.78f, w * 0.26f, h * 0.56f, w * 0.42f, h * 0.40f)
                    cubicTo(w * 0.52f, h * 0.30f, w * 0.50f, h * 0.22f, w * 0.48f, h * 0.12f)
                    cubicTo(w * 0.70f, h * 0.28f, w * 0.80f, h * 0.46f, w * 0.72f, h * 0.66f)
                    cubicTo(w * 0.68f, h * 0.78f, w * 0.60f, h * 0.84f, w * 0.52f, h * 0.88f)
                }
                drawPath(flame, color = tint, style = stroke)
            }
            RaviaIconKind.Gear -> {
                drawCircle(tint, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                listOf(
                    Offset(w * 0.50f, h * 0.10f) to Offset(w * 0.50f, h * 0.22f),
                    Offset(w * 0.50f, h * 0.78f) to Offset(w * 0.50f, h * 0.90f),
                    Offset(w * 0.10f, h * 0.50f) to Offset(w * 0.22f, h * 0.50f),
                    Offset(w * 0.78f, h * 0.50f) to Offset(w * 0.90f, h * 0.50f)
                ).forEach { (a, b) -> drawLine(tint, a, b, strokeWidth = stroke.width, cap = StrokeCap.Round) }
            }
            RaviaIconKind.Location -> {
                val pin = Path().apply {
                    moveTo(w * 0.50f, h * 0.88f)
                    cubicTo(w * 0.28f, h * 0.62f, w * 0.24f, h * 0.48f, w * 0.24f, h * 0.36f)
                    cubicTo(w * 0.24f, h * 0.18f, w * 0.38f, h * 0.10f, w * 0.50f, h * 0.10f)
                    cubicTo(w * 0.62f, h * 0.10f, w * 0.76f, h * 0.18f, w * 0.76f, h * 0.36f)
                    cubicTo(w * 0.76f, h * 0.48f, w * 0.72f, h * 0.62f, w * 0.50f, h * 0.88f)
                }
                drawPath(pin, color = tint, style = stroke)
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.36f), style = stroke)
            }
            RaviaIconKind.Lock -> {
                drawRoundRect(tint, Offset(w * 0.24f, h * 0.42f), Size(w * 0.52f, h * 0.40f), CornerRadius(w * 0.08f, w * 0.08f), style = stroke)
                drawArc(tint, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w * 0.34f, h * 0.18f), size = Size(w * 0.32f, h * 0.38f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.58f), Offset(w * 0.50f, h * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Logout -> {
                drawRoundRect(tint, Offset(w * 0.18f, h * 0.18f), Size(w * 0.36f, h * 0.64f), CornerRadius(w * 0.05f, w * 0.05f), style = stroke)
                drawLine(tint, Offset(w * 0.48f, h * 0.50f), Offset(w * 0.82f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.70f, h * 0.36f), Offset(w * 0.82f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.70f, h * 0.64f), Offset(w * 0.82f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.People -> {
                drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.38f, h * 0.34f), style = stroke)
                drawCircle(tint, radius = w * 0.10f, center = Offset(w * 0.66f, h * 0.38f), style = stroke)
                drawArc(tint, startAngle = 202f, sweepAngle = 136f, useCenter = false, topLeft = Offset(w * 0.18f, h * 0.50f), size = Size(w * 0.42f, h * 0.34f), style = stroke)
                drawArc(tint, startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = Offset(w * 0.52f, h * 0.55f), size = Size(w * 0.32f, h * 0.26f), style = stroke)
            }
            RaviaIconKind.Medical -> {
                drawRoundRect(tint, Offset(w * 0.18f, h * 0.28f), Size(w * 0.64f, h * 0.52f), CornerRadius(w * 0.08f, w * 0.08f), style = stroke)
                drawLine(tint, Offset(w * 0.40f, h * 0.28f), Offset(w * 0.40f, h * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.40f, h * 0.18f), Offset(w * 0.60f, h * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.60f, h * 0.18f), Offset(w * 0.60f, h * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.40f), Offset(w * 0.50f, h * 0.68f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.36f, h * 0.54f), Offset(w * 0.64f, h * 0.54f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Moon -> {
                val moon = Path().apply {
                    moveTo(w * 0.66f, h * 0.18f)
                    cubicTo(w * 0.44f, h * 0.22f, w * 0.30f, h * 0.40f, w * 0.34f, h * 0.60f)
                    cubicTo(w * 0.38f, h * 0.78f, w * 0.58f, h * 0.88f, w * 0.76f, h * 0.76f)
                    cubicTo(w * 0.62f, h * 0.74f, w * 0.50f, h * 0.62f, w * 0.50f, h * 0.48f)
                    cubicTo(w * 0.50f, h * 0.34f, w * 0.56f, h * 0.24f, w * 0.66f, h * 0.18f)
                }
                drawPath(moon, color = tint, style = stroke)
            }
            RaviaIconKind.Paw -> {
                drawCircle(tint, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.62f), style = stroke)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.30f, h * 0.38f), style = stroke)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.44f, h * 0.30f), style = stroke)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.58f, h * 0.30f), style = stroke)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.38f), style = stroke)
            }
            RaviaIconKind.Phone -> {
                drawRoundRect(tint, Offset(w * 0.30f, h * 0.12f), Size(w * 0.40f, h * 0.76f), CornerRadius(w * 0.08f, w * 0.08f), style = stroke)
                drawLine(tint, Offset(w * 0.44f, h * 0.76f), Offset(w * 0.56f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Radar -> {
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.58f), style = stroke)
                drawArc(tint, startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = Offset(w * 0.30f, h * 0.36f), size = Size(w * 0.40f, h * 0.40f), style = stroke)
                drawArc(tint, startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = Offset(w * 0.18f, h * 0.24f), size = Size(w * 0.64f, h * 0.64f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.58f), Offset(w * 0.78f, h * 0.30f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.36f, h * 0.86f), Offset(w * 0.64f, h * 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Radius -> {
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawCircle(tint, radius = w * 0.24f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawCircle(tint, radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.86f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Report -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.24f, h * 0.14f),
                    size = Size(w * 0.52f, h * 0.72f),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.36f, h * 0.39f), Offset(w * 0.64f, h * 0.39f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.36f, h * 0.54f), Offset(w * 0.64f, h * 0.54f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.36f, h * 0.69f), Offset(w * 0.54f, h * 0.69f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Shield -> {
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.10f)
                    lineTo(w * 0.20f, h * 0.24f)
                    lineTo(w * 0.20f, h * 0.52f)
                    cubicTo(w * 0.20f, h * 0.72f, w * 0.34f, h * 0.84f, w * 0.50f, h * 0.92f)
                    cubicTo(w * 0.66f, h * 0.84f, w * 0.80f, h * 0.72f, w * 0.80f, h * 0.52f)
                    lineTo(w * 0.80f, h * 0.24f)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.38f, h * 0.52f), Offset(w * 0.47f, h * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.47f, h * 0.62f), Offset(w * 0.64f, h * 0.40f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Siren -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.25f, h * 0.38f),
                    size = Size(w * 0.50f, h * 0.34f),
                    cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.18f, h * 0.82f), Offset(w * 0.82f, h * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.18f), Offset(w * 0.50f, h * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.24f, h * 0.25f), Offset(w * 0.32f, h * 0.33f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.76f, h * 0.25f), Offset(w * 0.68f, h * 0.33f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.38f, h * 0.72f), Offset(w * 0.38f, h * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.62f, h * 0.72f), Offset(w * 0.62f, h * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Spark -> {
                val spark = Path().apply {
                    moveTo(w * 0.50f, h * 0.10f)
                    lineTo(w * 0.60f, h * 0.40f)
                    lineTo(w * 0.90f, h * 0.50f)
                    lineTo(w * 0.60f, h * 0.60f)
                    lineTo(w * 0.50f, h * 0.90f)
                    lineTo(w * 0.40f, h * 0.60f)
                    lineTo(w * 0.10f, h * 0.50f)
                    lineTo(w * 0.40f, h * 0.40f)
                    close()
                }
                drawPath(spark, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.74f, h * 0.18f), Offset(w * 0.82f, h * 0.10f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.18f, h * 0.82f), Offset(w * 0.26f, h * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Tool -> {
                drawLine(tint, Offset(w * 0.26f, h * 0.74f), Offset(w * 0.72f, h * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawArc(tint, startAngle = 40f, sweepAngle = 250f, useCenter = false, topLeft = Offset(w * 0.56f, h * 0.12f), size = Size(w * 0.26f, h * 0.26f), style = stroke)
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.28f, h * 0.74f), style = stroke)
            }
            RaviaIconKind.Trophy -> {
                drawRoundRect(tint, Offset(w * 0.32f, h * 0.18f), Size(w * 0.36f, h * 0.36f), CornerRadius(w * 0.06f, w * 0.06f), style = stroke)
                drawArc(tint, startAngle = 90f, sweepAngle = 120f, useCenter = false, topLeft = Offset(w * 0.16f, h * 0.22f), size = Size(w * 0.28f, h * 0.30f), style = stroke)
                drawArc(tint, startAngle = -30f, sweepAngle = 120f, useCenter = false, topLeft = Offset(w * 0.56f, h * 0.22f), size = Size(w * 0.28f, h * 0.30f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.54f), Offset(w * 0.50f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.36f, h * 0.82f), Offset(w * 0.64f, h * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.58f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.User -> {
                drawCircle(tint, radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.34f), style = stroke)
                drawArc(tint, startAngle = 202f, sweepAngle = 136f, useCenter = false, topLeft = Offset(w * 0.24f, h * 0.52f), size = Size(w * 0.52f, h * 0.34f), style = stroke)
            }
            RaviaIconKind.Vibrate -> {
                drawRoundRect(tint, Offset(w * 0.34f, h * 0.18f), Size(w * 0.32f, h * 0.64f), CornerRadius(w * 0.07f, w * 0.07f), style = stroke)
                drawLine(tint, Offset(w * 0.20f, h * 0.34f), Offset(w * 0.26f, h * 0.40f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.20f, h * 0.58f), Offset(w * 0.26f, h * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.80f, h * 0.34f), Offset(w * 0.74f, h * 0.40f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.80f, h * 0.58f), Offset(w * 0.74f, h * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            RaviaIconKind.Water -> {
                val drop = Path().apply {
                    moveTo(w * 0.50f, h * 0.12f)
                    cubicTo(w * 0.30f, h * 0.38f, w * 0.24f, h * 0.50f, w * 0.24f, h * 0.64f)
                    cubicTo(w * 0.24f, h * 0.80f, w * 0.36f, h * 0.90f, w * 0.50f, h * 0.90f)
                    cubicTo(w * 0.64f, h * 0.90f, w * 0.76f, h * 0.80f, w * 0.76f, h * 0.64f)
                    cubicTo(w * 0.76f, h * 0.50f, w * 0.70f, h * 0.38f, w * 0.50f, h * 0.12f)
                }
                drawPath(drop, color = tint, style = stroke)
            }
        }
    }
}

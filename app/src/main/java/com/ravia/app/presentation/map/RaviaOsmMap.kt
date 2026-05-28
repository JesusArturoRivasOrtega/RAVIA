package com.ravia.app.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ravia.app.domain.model.LocationPoint
import com.ravia.app.domain.model.Report
import com.ravia.app.domain.model.ReportCategory
import com.ravia.app.domain.model.ReportPriority
import com.ravia.app.domain.model.RiskLevel
import com.ravia.app.domain.model.RiskZone
import com.ravia.app.domain.model.isVisibleOnMap
import com.ravia.app.presentation.components.toAccentColor
import com.ravia.app.presentation.components.toColor
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.concurrent.ConcurrentHashMap

@Composable
fun RaviaOsmMap(
    reports: List<Report>,
    riskZones: List<RiskZone>,
    currentLocation: LocationPoint?,
    showRiskZones: Boolean,
    onReportClick: (Report) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val visibleReports = remember(reports) { reports.filter { it.status.isVisibleOnMap() } }
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 3.0
            maxZoomLevel = 20.0
            controller.setZoom(14.0)
            setScrollableAreaLimitDouble(null)
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(currentLocation?.latitude, currentLocation?.longitude, visibleReports.size) {
        val center = currentLocation?.let { GeoPoint(it.latitude, it.longitude) }
            ?: visibleReports.firstOrNull()?.let { GeoPoint(it.latitude, it.longitude) }
        center?.let {
            mapView.controller.animateTo(it)
            if (mapView.zoomLevelDouble < 12.0) {
                mapView.controller.setZoom(14.0)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            map.overlays.clear()

            if (showRiskZones) {
                riskZones.forEach { zone ->
                    map.overlays.add(zone.toPolygon())
                }
            }

            currentLocation?.let { location ->
                map.overlays.add(location.toMarker(context, map))
            }

            visibleReports.forEach { report ->
                map.overlays.add(report.toMarker(context, map, onReportClick))
            }

            map.invalidate()
        },
    )
}

private fun RiskZone.toPolygon(): Polygon =
    Polygon().apply {
        points = Polygon.pointsAsCircle(GeoPoint(latitude, longitude), radius)
        fillColor = riskLevel.toColor().copy(alpha = 0.12f).toArgb()
        strokeColor = riskLevel.toColor().copy(alpha = 0.58f).toArgb()
        strokeWidth = 3f
        title = name
        snippet = "Riesgo ${riskLevel.displayName()}"
    }

private fun LocationPoint.toMarker(context: Context, map: MapView): Marker =
    Marker(map).apply {
        position = GeoPoint(latitude, longitude)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        icon = MapMarkerIconCache.locationPin(context)
        title = "Tu ubicacion"
    }

private fun Report.toMarker(
    context: Context,
    map: MapView,
    onReportClick: (Report) -> Unit,
): Marker =
    Marker(map).apply {
        position = GeoPoint(latitude, longitude)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        icon = MapMarkerIconCache.reportPin(context, category, priority)
        title = title
        snippet = category.displayName
        setOnMarkerClickListener { _, _ ->
            onReportClick(this@toMarker)
            true
        }
    }

private object MapMarkerIconCache {
    private val reportPins = ConcurrentHashMap<String, Drawable.ConstantState>()
    private val locationPins = ConcurrentHashMap<String, Drawable.ConstantState>()

    fun reportPin(
        context: Context,
        category: ReportCategory,
        priority: ReportPriority,
    ): Drawable {
        val density = context.resources.displayMetrics.densityDpi
        val key = "$density-${category.name}-${priority.name}"
        return reportPins[key]?.newDrawable(context.resources)
            ?: createReportPinDrawable(context, category, priority).also { drawable ->
                drawable.constantState?.let { reportPins[key] = it }
            }
    }

    fun locationPin(context: Context): Drawable {
        val density = context.resources.displayMetrics.densityDpi
        val key = "$density-location"
        return locationPins[key]?.newDrawable(context.resources)
            ?: createLocationDrawable(context).also { drawable ->
                drawable.constantState?.let { locationPins[key] = it }
            }
    }
}

private fun createReportPinDrawable(
    context: Context,
    category: ReportCategory,
    priority: ReportPriority,
): Drawable {
    val density = context.resources.displayMetrics.density
    val width = (58 * density).toInt()
    val height = (72 * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val color = category.toAccentColor().toArgb()
    val ringColor = priority.toMapRingColor()
    val centerX = width / 2f
    val circleRadius = width * 0.36f
    val circleY = height * 0.36f

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.argb(55, 0, 0, 0)
    }
    canvas.drawOval(
        RectF(width * 0.25f, height * 0.78f, width * 0.75f, height * 0.93f),
        shadowPaint,
    )

    val pinPath = Path().apply {
        addCircle(centerX, circleY, circleRadius, Path.Direction.CW)
        moveTo(centerX - circleRadius * 0.58f, circleY + circleRadius * 0.63f)
        lineTo(centerX, height * 0.88f)
        lineTo(centerX + circleRadius * 0.58f, circleY + circleRadius * 0.63f)
        close()
    }

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = ringColor
        style = Paint.Style.STROKE
        strokeWidth = 4.2f * density
    }
    canvas.drawPath(pinPath, fillPaint)
    canvas.drawPath(pinPath, ringPaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        alpha = 238
    }
    canvas.drawCircle(centerX, circleY, circleRadius * 0.62f, innerPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = 13.5f * density
    }
    val textBounds = Rect()
    val label = category.pinLabel()
    textPaint.getTextBounds(label, 0, label.length, textBounds)
    canvas.drawText(label, centerX, circleY + textBounds.height() / 2f, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun createLocationDrawable(context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val size = (34 * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f

    val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(48, 25, 118, 210)
    }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
    }
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(25, 118, 210)
    }

    canvas.drawCircle(center, center, center * 0.92f, outer)
    canvas.drawCircle(center, center, center * 0.45f, fill)
    canvas.drawCircle(center, center, center * 0.45f, stroke)

    return BitmapDrawable(context.resources, bitmap)
}

private fun ReportCategory.pinLabel(): String = when (this) {
    ReportCategory.SECURITY -> "SEG"
    ReportCategory.ACCIDENT -> "VIA"
    ReportCategory.INJURED_PERSON -> "MED"
    ReportCategory.FIRE -> "FUE"
    ReportCategory.FLOOD -> "AGU"
    ReportCategory.DANGEROUS_ANIMAL -> "PEL"
    ReportCategory.MISSING_PERSON -> "BUS"
    ReportCategory.VIOLENCE -> "SOS"
    ReportCategory.INFRASTRUCTURE -> "OBR"
    ReportCategory.RISK_ZONE -> "RZ"
    ReportCategory.OTHER -> "INF"
}

private fun ReportPriority.toMapRingColor(): Int = when (this) {
    ReportPriority.CRITICAL -> AndroidColor.rgb(93, 18, 31)
    ReportPriority.HIGH -> AndroidColor.rgb(230, 81, 0)
    ReportPriority.MEDIUM -> AndroidColor.rgb(255, 193, 7)
    ReportPriority.LOW -> AndroidColor.rgb(0, 137, 123)
}

package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.ui.theme.StatusIrregularPurple
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusScheduledRed
import kotlin.math.hypot

// Precomputed geometry for zero-allocation rendering at 60/120fps
private data class SectorPrecomputed(
    val sector: Sector,
    val centerNormX: Float,
    val centerNormY: Float,
    val shortName: String
)

@Composable
fun InteractiveBarinasMap(
    sectors: List<Sector>,
    selectedSector: Sector?,
    onSectorSelected: (Sector) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Geographic boundary of Barinas City Metropolitan Area
    val minLat = 8.520
    val maxLat = 8.685
    val minLng = -70.285
    val maxLng = -70.180

    val density = LocalDensity.current.density
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY
    val primaryColor = MaterialTheme.colorScheme.primary

    // Remove pulseAlpha to prevent 60fps continuous redrawing.
    // We will use a static highlight for the selected sector instead.

    // Precompute normalized positions outside the draw loop
    val precomputedSectors = remember(sectors) {
        sectors.map { sector ->
            val count = sector.coordinates.size.coerceAtLeast(1)
            var sumLat = 0.0
            var sumLng = 0.0

            for (coord in sector.coordinates) {
                sumLat += coord.first
                sumLng += coord.second
            }

            val cLat = sumLat / count
            val cLng = sumLng / count

            // Normalize within Barinas urban bounding box, clamping outliers gracefully to edges
            val rawNx = ((cLng - minLng) / (maxLng - minLng)).toFloat()
            val rawNy = (1f - ((cLat - minLat) / (maxLat - minLat)).toFloat())

            val cnx = rawNx.coerceIn(0.06f, 0.94f)
            val cny = rawNy.coerceIn(0.06f, 0.94f)

            SectorPrecomputed(
                sector = sector,
                centerNormX = cnx,
                centerNormY = cny,
                shortName = sector.name
                    .replace("Sector ", "")
                    .replace(" 34,5 kV", "")
                    .replace(" kV", "")
            )
        }
    }

    // Reusable Paints to avoid allocations in draw loop
    val textPaint = remember(textColor, density) {
        Paint().apply {
            color = textColor
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val labelBgPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(215, 20, 25, 35)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    val riverPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(100, 2, 136, 209)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
    }

    val mapBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
    val mapBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.60f)

    val riverPath = remember {
        Path().apply {
            moveTo(0.42f, 0.00f)
            cubicTo(0.55f, 0.22f, 0.68f, 0.45f, 0.78f, 0.72f)
            lineTo(0.88f, 1.00f)
        }
    }

    val troncal5Path = remember {
        Path().apply {
            moveTo(0.20f, 1.00f)
            cubicTo(0.35f, 0.75f, 0.52f, 0.50f, 0.85f, 0.15f)
        }
    }

    val riverGlowColor = remember { Color(0xFF0288D1).copy(alpha = 0.22f) }
    val riverBodyColor = remember { Color(0xFF29B6F6).copy(alpha = 0.65f) }
    val roadColor = remember(isDark) { 
        if (isDark) Color(0xFF546E7A).copy(alpha = 0.35f) else Color(0xFF90A4AE).copy(alpha = 0.45f)
    }
    
    val subColor = remember { Color(0xFFFFB300) }
    val subStations = remember {
        listOf(
            Pair(0.48f, 0.42f) to "S/E Barinas I",
            Pair(0.28f, 0.26f) to "S/E Alto Barinas",
            Pair(0.38f, 0.72f) to "S/E Mijagua"
        )
    }

    val selectedLabelSize = remember(density) { 12f * density }

    Box(
        modifier = modifier
            .background(mapBgColor, RoundedCornerShape(16.dp))
            .border(1.dp, mapBorderColor, RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("barinas_circuit_canvas_map")
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val oldScale = scale
                        val newScale = (scale * zoom).coerceIn(0.8f, 3.5f)
                        
                        // Smooth pinch-to-zoom mathematically centered on the fingers' centroid
                        offsetX = (offsetX + pan.x) - (centroid.x - offsetX) * (newScale / oldScale - 1f)
                        offsetY = (offsetY + pan.y) - (centroid.y - offsetY) * (newScale / oldScale - 1f)
                        scale = newScale
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                }
                .pointerInput(precomputedSectors) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()

                        var closestSector: Sector? = null
                        var minDistance = Float.MAX_VALUE

                        for (item in precomputedSectors) {
                            // GraphicsLayer automatically transforms pointer coordinates, so we compare directly!
                            val mapX = item.centerNormX * canvasW * 0.85f + canvasW * 0.075f
                            val mapY = item.centerNormY * canvasH * 0.85f + canvasH * 0.075f

                            val dist = hypot(tapOffset.x - mapX, tapOffset.y - mapY)
                            val threshold = 48f * density
                            if (dist < threshold && dist < minDistance) {
                                minDistance = dist
                                closestSector = item.sector
                            }
                        }

                        closestSector?.let { onSectorSelected(it) }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val drawScaleX = width * 0.85f
            val drawScaleY = height * 0.85f
            val baseOffsetX = width * 0.075f
            val baseOffsetY = height * 0.075f

            // =========================================================================
            // 1. DIBUJAR RÍO SANTO DOMINGO (Sello geográfico de Barinas)
            // =========================================================================
            withTransform({
                translate(baseOffsetX, baseOffsetY)
                scale(drawScaleX, drawScaleY)
            }) {
                val avgScale = (drawScaleX + drawScaleY) / 2f
                drawPath(
                    path = riverPath,
                    color = riverGlowColor,
                    style = Stroke(width = 16f / avgScale, cap = StrokeCap.Round)
                )
                drawPath(
                    path = riverPath,
                    color = riverBodyColor,
                    style = Stroke(width = 8f / avgScale, cap = StrokeCap.Round)
                )
            }

            // Etiqueta del Río
            riverPaint.textSize = 11f.coerceIn(9f, 16f) * density
            drawContext.canvas.nativeCanvas.drawText(
                "🌊 Río Santo Domingo",
                0.72f * drawScaleX + baseOffsetX,
                0.52f * drawScaleY + baseOffsetY,
                riverPaint
            )

            // =========================================================================
            // 2. EJES VIALES PRINCIPALES DE BARINAS (Arterias eléctricas)
            // =========================================================================
            val roadWidth = 3f

            // Troncal 5 (Sur -> Centro -> Noreste hacia Guanare)
            withTransform({
                translate(baseOffsetX, baseOffsetY)
                scale(drawScaleX, drawScaleY)
            }) {
                val avgScale = (drawScaleX + drawScaleY) / 2f
                drawPath(path = troncal5Path, color = roadColor, style = Stroke(width = (roadWidth * 1.4f) / avgScale, cap = StrokeCap.Round))
            }

            // Av. Cuatricentenaria (Oeste a Este)
            drawLine(
                color = roadColor,
                start = Offset(0.12f * drawScaleX + baseOffsetX, 0.62f * drawScaleY + baseOffsetY),
                end = Offset(0.70f * drawScaleX + baseOffsetX, 0.58f * drawScaleY + baseOffsetY),
                strokeWidth = roadWidth,
                cap = StrokeCap.Round
            )

            // Av. 23 de Enero (Centro a Cuatricentenaria)
            drawLine(
                color = roadColor,
                start = Offset(0.48f * drawScaleX + baseOffsetX, 0.38f * drawScaleY + baseOffsetY),
                end = Offset(0.42f * drawScaleX + baseOffsetX, 0.62f * drawScaleY + baseOffsetY),
                strokeWidth = roadWidth,
                cap = StrokeCap.Round
            )

            // Av. Los Próceres / Av. Alberto Arvelo Torrealba (Alto Barinas)
            drawLine(
                color = roadColor,
                start = Offset(0.18f * drawScaleX + baseOffsetX, 0.32f * drawScaleY + baseOffsetY),
                end = Offset(0.45f * drawScaleX + baseOffsetX, 0.22f * drawScaleY + baseOffsetY),
                strokeWidth = roadWidth,
                cap = StrokeCap.Round
            )

            // =========================================================================
            // 3. SUBESTACIONES PRINCIPALES (Hubs)
            // =========================================================================
            for ((coord, subName) in subStations) {
                val sx = coord.first * drawScaleX + baseOffsetX
                val sy = coord.second * drawScaleY + baseOffsetY

                drawCircle(color = subColor.copy(alpha = 0.20f), radius = 12f, center = Offset(sx, sy))
                drawCircle(color = subColor, radius = 5f, center = Offset(sx, sy))
            }

            // =========================================================================
            // 4. NODOS DE SECTORES (Limpio, Sin solapamientos masivos)
            // =========================================================================
            for (item in precomputedSectors) {
                val secX = item.centerNormX * drawScaleX + baseOffsetX
                val secY = item.centerNormY * drawScaleY + baseOffsetY
                val pinOffset = Offset(secX, secY)
                val isSelected = selectedSector?.id == item.sector.id

                val pinColor = when (item.sector.status) {
                    ServiceStatus.NORMAL -> StatusNormalGreen
                    ServiceStatus.SCHEDULED_OUTAGE -> StatusScheduledRed
                    ServiceStatus.IRREGULAR_OUTAGE -> StatusIrregularPurple
                }

                if (isSelected) {
                    // Highlight ring for selected sector
                    drawCircle(
                        color = pinColor.copy(alpha = 0.45f),
                        radius = 24f,
                        center = pinOffset
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 14f,
                        center = pinOffset,
                        style = Stroke(width = 3f)
                    )
                    drawCircle(
                        color = pinColor,
                        radius = 8f,
                        center = pinOffset
                    )
                } else {
                    // Subtle, crisp electrical node
                    drawCircle(
                        color = pinColor.copy(alpha = 0.25f),
                        radius = 8f,
                        center = pinOffset
                    )
                    drawCircle(
                        color = pinColor,
                        radius = 4.5f,
                        center = pinOffset
                    )
                }

                // Render label ONLY when selected to eliminate text rendering bottlenecks
                if (isSelected) {
                    val labelText = "📍 ${item.shortName}"
                    textPaint.textSize = selectedLabelSize
                    textPaint.color = android.graphics.Color.WHITE

                    val textWidth = textPaint.measureText(labelText)
                    val bgMargin = 8f * density
                    val badgeTop = pinOffset.y - 26f
                    val badgeLeft = pinOffset.x - (textWidth / 2f) - bgMargin
                    val badgeRight = pinOffset.x + (textWidth / 2f) + bgMargin
                    val badgeBottom = badgeTop + (16f * density)

                    labelBgPaint.color = android.graphics.Color.argb(235, 15, 23, 42)
                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        badgeLeft, badgeTop, badgeRight, badgeBottom,
                        12f, 12f, labelBgPaint
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        pinOffset.x,
                        badgeTop + (12f * density),
                        textPaint
                    )
                }
            }
        }

        // =========================================================================
        // 5. CONTROLES DE ZOOM FLOTANTES (+ / - / Centrar)
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column {
                    IconButton(
                        onClick = { scale = (scale * 1.3f).coerceAtMost(3.5f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Acercar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { scale = (scale / 1.3f).coerceAtLeast(0.8f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Alejar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            scale = 1.0f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.CenterFocusStrong, contentDescription = "Centrar Barinas", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // =========================================================================
        // 6. BARRA INFERIOR DE LEYENDA (Moderna y no intrusiva)
        // =========================================================================
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegendDot(StatusNormalGreen, "Con Luz")
                LegendDot(StatusScheduledRed, "Corte PAC")
                LegendDot(StatusIrregularPurple, "Avería")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

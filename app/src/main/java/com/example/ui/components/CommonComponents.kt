package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.local.BranchEntity
import com.example.data.local.RiskAlertEntity
import com.example.domain.AlertActionStatus
import com.example.domain.RiskLevel
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RiskElevated
import com.example.ui.theme.RiskElevatedContainer
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskNormalContainer
import com.example.ui.theme.RiskUrgent
import com.example.ui.theme.RiskUrgentContainer
import kotlin.math.roundToInt

@Composable
fun SimulatedDataBanner(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simulated_data_banner"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Science,
                contentDescription = "Simulated Demo Notice",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = strings.simulatedBanner,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RiskLevelBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val (bg, fg, label) = when (level) {
        RiskLevel.NORMAL -> Triple(RiskNormalContainer, RiskNormal, strings.riskNormal)
        RiskLevel.ELEVATED -> Triple(RiskElevatedContainer, RiskElevated, strings.riskElevated)
        RiskLevel.URGENT -> Triple(RiskUrgentContainer, RiskUrgent, strings.riskUrgent)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActionStatusChip(status: AlertActionStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        AlertActionStatus.OPEN -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        AlertActionStatus.ACKNOWLEDGED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        AlertActionStatus.PREVENTIVE_ACTION_TAKEN -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        AlertActionStatus.RESOLVED -> RiskNormalContainer to RiskNormal
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TelemetryKpiCard(
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RiskDistributionBarChart(
    normalCount: Int,
    elevatedCount: Int,
    urgentCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (normalCount + elevatedCount + urgentCount).coerceAtLeast(1)
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
        ) {
            val w = size.width
            val h = size.height
            val nW = (normalCount.toFloat() / total) * w
            val eW = (elevatedCount.toFloat() / total) * w
            val uW = (urgentCount.toFloat() / total) * w

            drawRoundRect(
                color = Color(0xFFE2E8F0),
                size = Size(w, h),
                cornerRadius = CornerRadius(9f, 9f)
            )
            if (nW > 0f) {
                drawRect(color = RiskNormal, topLeft = Offset.Zero, size = Size(nW, h))
            }
            if (eW > 0f) {
                drawRect(color = RiskElevated, topLeft = Offset(nW, 0f), size = Size(eW, h))
            }
            if (uW > 0f) {
                drawRect(color = RiskUrgent, topLeft = Offset(nW + eW, 0f), size = Size(uW, h))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendDot(color = RiskNormal, label = "${strings.riskNormal}: $normalCount")
            LegendDot(color = RiskElevated, label = "${strings.riskElevated}: $elevatedCount")
            LegendDot(color = RiskUrgent, label = "${strings.riskUrgent}: $urgentCount")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BranchRiskCanvasMap(
    branches: List<BranchEntity>,
    alerts: List<RiskAlertEntity>,
    selectedBranchId: String?,
    onSelectBranch: (BranchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val surfaceBg = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.08f),
                            surfaceBg.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            val mapWidthPx = constraints.maxWidth.toFloat()
            val mapHeightPx = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gridColor = primaryColor.copy(alpha = 0.14f)
                for (i in 1..4) {
                    val y = h * i / 5f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5f
                    )
                    val x = w * i / 5f
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.5f
                    )
                }

                // Connect regional nodes with subtle network telemetry arcs
                for (i in 0 until branches.size - 1) {
                    val a = branches[i]
                    val b = branches[i + 1]
                    drawLine(
                        color = primaryColor.copy(alpha = 0.25f),
                        start = Offset(a.mapXFraction * w, a.mapYFraction * h),
                        end = Offset(b.mapXFraction * w, b.mapYFraction * h),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                }

                branches.forEach { branch ->
                    val branchAlerts = alerts.filter { it.branchId == branch.id && it.actionStatus != "RESOLVED" }
                    val highest = branchAlerts.maxByOrNull { RiskLevel.fromName(it.riskLevel).priority }
                    val nodeColor = when (RiskLevel.fromName(highest?.riskLevel)) {
                        RiskLevel.URGENT -> RiskUrgent
                        RiskLevel.ELEVATED -> RiskElevated
                        RiskLevel.NORMAL -> RiskNormal
                    }
                    val cx = branch.mapXFraction * w
                    val cy = branch.mapYFraction * h

                    if (highest?.riskLevel == RiskLevel.URGENT.name || branch.id == selectedBranchId) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.22f),
                            radius = 18f * pulseRadius,
                            center = Offset(cx, cy)
                        )
                    }
                    if (branch.id == selectedBranchId) {
                        drawCircle(
                            color = primaryColor,
                            radius = 20f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3f)
                        )
                    }
                    drawCircle(
                        color = Color.White,
                        radius = 13f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = nodeColor,
                        radius = 9f,
                        center = Offset(cx, cy)
                    )
                }
            }

            // Accessible interactive pins positioned over Canvas coordinates
            branches.forEach { branch ->
                val xPx = (branch.mapXFraction * mapWidthPx).roundToInt()
                val yPx = (branch.mapYFraction * mapHeightPx).roundToInt()
                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPx - 44, yPx - 44) }
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onSelectBranch(branch) }
                        .testTag("map_node_${branch.id}"),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = branch.region,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Legend Pill top-start
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Deccan & Telangana Telemetry Grid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

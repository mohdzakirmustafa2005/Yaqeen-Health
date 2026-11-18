package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BranchEntity
import com.example.domain.RiskLevel
import com.example.domain.Symptom
import com.example.i18n.LocalAppStrings
import com.example.ui.MainUiState
import com.example.ui.components.RiskDistributionBarChart
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.SimulatedDataBanner
import com.example.ui.components.TelemetryKpiCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RiskElevated
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskUrgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: MainUiState,
    onSimulateReading: (BranchEntity, Double, Double, Double?, Int?, String) -> Unit,
    onSyncQueue: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onOpenOrgRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val org = uiState.activeOrganization
    val role = uiState.preferences.activeRole
    val branches = uiState.orgBranches
    var selectedBranch by remember(branches) { mutableStateOf(branches.firstOrNull()) }
    var showCustomTelemetryDialog by remember { mutableStateOf(false) }

    val totalWorkers = branches.sumOf { it.workerCount }
    val openAlerts = uiState.orgAlerts.filter { it.actionStatus != "RESOLVED" }
    val avgFatigue = if (uiState.orgCheckIns.isNotEmpty()) {
        String.format("%.1f", uiState.orgCheckIns.map { it.fatigueScore }.average())
    } else "—"

    val normalAlerts = uiState.orgAlerts.count { it.riskLevel == RiskLevel.NORMAL.name }
    val elevatedAlerts = uiState.orgAlerts.count { it.riskLevel == RiskLevel.ELEVATED.name }
    val urgentAlerts = uiState.orgAlerts.count { it.riskLevel == RiskLevel.URGENT.name }
    val latestReading = uiState.orgReadings.firstOrNull { it.branchId == selectedBranch?.id } ?: uiState.orgReadings.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SimulatedDataBanner()
        }

        // Hero Banner Card with generated editorial artwork
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_workforce_hero),
                        contentDescription = "Yaqeen Allah Workforce Health Network Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xBB0B4F4A),
                                        Color(0xEE07322E)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = org?.industry ?: "Workforce Safety",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = org?.city ?: "India",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = org?.name ?: strings.brandTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.brandTagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateToCheckIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("quick_checkin_button")
                            ) {
                                Text(strings.navCheckIn)
                            }
                            OutlinedButton(
                                onClick = onOpenOrgRegistration,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("hero_register_org_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddBusiness,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(strings.registerOrg)
                            }
                        }
                    }
                }
            }
        }

        // 2x2 KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryKpiCard(
                        label = strings.activeWorkers,
                        value = totalWorkers.toString(),
                        subtitle = "${branches.size} workplace branch(es)",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryKpiCard(
                        label = strings.openAlerts,
                        value = openAlerts.size.toString(),
                        subtitle = "${openAlerts.count { it.riskLevel == "URGENT" }} urgent attention",
                        accentColor = if (openAlerts.any { it.riskLevel == "URGENT" }) RiskUrgent else RiskNormal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryKpiCard(
                        label = strings.avgFatigue,
                        value = "$avgFatigue/10",
                        subtitle = "${uiState.orgCheckIns.size} logged check-ins",
                        accentColor = RiskElevated,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryKpiCard(
                        label = strings.pendingSync,
                        value = uiState.pendingSyncCount.toString(),
                        subtitle = if (uiState.pendingSyncCount > 0) "Tap below to sync" else "All records synced",
                        accentColor = if (uiState.pendingSyncCount > 0) RiskElevated else RiskNormal,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (uiState.pendingSyncCount > 0) {
                    FilledTonalButton(
                        onClick = onSyncQueue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("dashboard_sync_queue_button")
                    ) {
                        Text("${strings.syncNow} (${uiState.pendingSyncCount})")
                    }
                }
            }
        }

        // Structured Intelligence Models Snapshot (Worker, HeatMetric, FatigueLog)
        item {
            val peakHeatScore = uiState.heatMetrics.maxOfOrNull { it.heatRiskScore } ?: 0
            val peakFatigueRisk = uiState.fatigueLogs.maxOfOrNull { it.fatigueRiskScore } ?: 0
            val consentedWorkers = uiState.workers.count { it.consentGiven }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("structured_intelligence_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Captured Intelligence Models",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Worker", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text("${uiState.workers.size} profiled", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("$consentedWorkers DPDP consented", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("HeatMetric", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text("${uiState.heatMetrics.size} streams", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Peak $peakHeatScore/100", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("FatigueLog", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("${uiState.fatigueLogs.size} logs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Peak $peakFatigueRisk/100", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Risk Distribution Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Workforce Risk Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = onNavigateToAlerts,
                            modifier = Modifier.testTag("view_all_alerts_button")
                        ) {
                            Text(strings.navAlerts)
                        }
                    }
                    RiskDistributionBarChart(
                        normalCount = normalAlerts,
                        elevatedCount = elevatedAlerts,
                        urgentCount = urgentAlerts
                    )
                }
            }
        }

        // Environmental Telemetry & Quick Simulation Controls
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.environmentalTelemetry,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (branches.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBranch?.name ?: branches.first().name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.selectBranch) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text("${branch.name} (${branch.region})") },
                                        onClick = {
                                            selectedBranch = branch
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (latestReading != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricChip(
                                icon = Icons.Filled.Thermostat,
                                label = "Air Temp",
                                value = "${latestReading.temperatureC}°C",
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                icon = Icons.Filled.WaterDrop,
                                label = "Humidity",
                                value = "${latestReading.humidityPercent.toInt()}%",
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                icon = Icons.Filled.Warning,
                                label = "WBGT",
                                value = latestReading.wbgtC?.let { "${it}°C" } ?: "Missing",
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                icon = Icons.Filled.Air,
                                label = "AQI",
                                value = latestReading.airQualityIndex?.toString() ?: "Missing",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "Source: ${latestReading.sourceLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (role.canLogEnvironment && selectedBranch != null) {
                        Text(
                            text = "Demo Telemetry Presets (Triggers Early Warning Engine):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilledTonalButton(
                                    onClick = {
                                        selectedBranch?.let {
                                            onSimulateReading(it, 39.4, 68.0, 33.4, 155, "Simulated Heatwave Demo")
                                        }
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("preset_heatwave_button")
                                ) {
                                    Text(strings.presetHeatwave)
                                }
                            }
                            item {
                                FilledTonalButton(
                                    onClick = {
                                        selectedBranch?.let {
                                            onSimulateReading(it, 35.0, 56.0, null, 245, "Simulated AQI Dust Spike (No WBGT)")
                                        }
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("preset_aqi_button")
                                ) {
                                    Text(strings.presetAqiSpike)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = {
                                        selectedBranch?.let {
                                            onSimulateReading(it, 28.2, 46.0, 23.1, 72, "Simulated Normal Shift")
                                        }
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("preset_normal_button")
                                ) {
                                    Text(strings.presetNormal)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = { showCustomTelemetryDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("custom_telemetry_button")
                                ) {
                                    Text(strings.simulateReading)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Privacy-Scoped Workforce Check-In Feed
        item {
            Text(
                text = "Recent Workforce Check-Ins (Role-Scoped Privacy)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (!role.canViewIndividualSymptomsWithConsent && role != com.example.domain.UserRole.WORKER) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Privacy Protection Active",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = strings.privacyRedactedNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        items(uiState.orgCheckIns.take(6), key = { it.id }) { checkIn ->
            val canSeeDetails = role == com.example.domain.UserRole.WORKER ||
                (role.canViewIndividualSymptomsWithConsent && checkIn.consentGiven)
            val symptoms = Symptom.parseCsv(checkIn.symptomsCsv)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (canSeeDetails) checkIn.workerAlias else "${checkIn.department} · De-identified",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val risk = when {
                            checkIn.fatigueScore >= 8 || symptoms.size >= 2 -> RiskLevel.URGENT
                            checkIn.fatigueScore >= 6 || symptoms.isNotEmpty() -> RiskLevel.ELEVATED
                            else -> RiskLevel.NORMAL
                        }
                        RiskLevelBadge(level = risk)
                    }
                    Text(
                        text = "${checkIn.branchName} · Fatigue ${checkIn.fatigueScore}/10 · ${checkIn.shiftHours}h shift",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (canSeeDetails) {
                        val symptomText = if (symptoms.isEmpty()) "No symptoms reported" else symptoms.joinToString { it.key }
                        Text(
                            text = "Symptoms: $symptomText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = if (checkIn.consentGiven) {
                                "Individual medical details restricted to Occupational Health Pro."
                            } else {
                                "Worker has not granted clinical review consent (DPDP protected)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (checkIn.syncStatus == "SYNCED") RiskNormal else RiskElevated,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (checkIn.syncStatus == "SYNCED") "Synced" else "Queued Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCustomTelemetryDialog && selectedBranch != null) {
        CustomTelemetryDialog(
            branch = selectedBranch!!,
            onDismiss = { showCustomTelemetryDialog = false },
            onSubmit = { temp, rh, wbgt, aqi ->
                onSimulateReading(selectedBranch!!, temp, rh, wbgt, aqi, "Manual Safety Officer Entry (Simulated)")
                showCustomTelemetryDialog = false
            }
        )
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CustomTelemetryDialog(
    branch: BranchEntity,
    onDismiss: () -> Unit,
    onSubmit: (Double, Double, Double?, Int?) -> Unit
) {
    var tempText by remember { mutableStateOf("37.5") }
    var humidityText by remember { mutableStateOf("62") }
    var wbgtText by remember { mutableStateOf("31.2") }
    var aqiText by remember { mutableStateOf("145") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Environmental Telemetry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Branch: ${branch.name}", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    label = { Text("Air Temperature (°C)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = humidityText,
                    onValueChange = { humidityText = it },
                    label = { Text("Relative Humidity (%)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = wbgtText,
                    onValueChange = { wbgtText = it },
                    label = { Text("WBGT (°C) — leave blank to test missing check") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = aqiText,
                    onValueChange = { aqiText = it },
                    label = { Text("Air Quality Index (AQI)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = tempText.toDoubleOrNull() ?: 34.0
                    val h = humidityText.toDoubleOrNull() ?: 55.0
                    val w = wbgtText.toDoubleOrNull()
                    val a = aqiText.toIntOrNull()
                    onSubmit(t, h, w, a)
                }
            ) { Text("Evaluate Risk") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

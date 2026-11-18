package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.BranchEntity
import com.example.domain.RiskLevel
import com.example.i18n.LocalAppStrings
import com.example.ui.MainUiState
import com.example.ui.components.BranchRiskCanvasMap
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.SimulatedDataBanner
import com.example.ui.components.TelemetryKpiCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RiskElevated
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskUrgent

@Composable
fun PopulationScreen(
    uiState: MainUiState,
    onGenerateAiBriefing: () -> Unit,
    onBuildCsvReport: () -> String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var selectedBranch by remember(uiState.allBranches) {
        mutableStateOf<BranchEntity?>(uiState.allBranches.firstOrNull())
    }
    var reportPreviewCsv by remember { mutableStateOf<String?>(null) }

    val totalWorkforce = uiState.allBranches.sumOf { it.workerCount }
    val allUrgent = uiState.allAlerts.count { it.riskLevel == RiskLevel.URGENT.name && it.actionStatus != "RESOLVED" }
    val maxTemp = uiState.allReadings.maxOfOrNull { it.temperatureC }
    val maxAqi = uiState.allReadings.mapNotNull { it.airQualityIndex }.maxOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("population_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimulatedDataBanner()
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.populationTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.populationSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Aggregated Multi-Tenant Public Health KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryKpiCard(
                    label = "Covered Workers",
                    value = totalWorkforce.toString(),
                    subtitle = "${uiState.organizations.size} orgs · ${uiState.allBranches.size} sites",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TelemetryKpiCard(
                    label = "Peak Temp / AQI",
                    value = "${maxTemp ?: "—"}°C",
                    subtitle = "Max AQI: ${maxAqi ?: "—"} · $allUrgent urgent",
                    accentColor = if (allUrgent > 0) RiskUrgent else RiskNormal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Geographic Risk Map (Custom Compose Canvas)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.geographicRiskMap,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BranchRiskCanvasMap(
                    branches = uiState.allBranches,
                    alerts = uiState.allAlerts,
                    selectedBranchId = selectedBranch?.id,
                    onSelectBranch = { selectedBranch = it }
                )
            }
        }

        selectedBranch?.let { branch ->
            item {
                val env = uiState.allReadings.firstOrNull { it.branchId == branch.id }
                val branchAlerts = uiState.allAlerts.filter { it.branchId == branch.id && it.actionStatus != "RESOLVED" }
                val topLevel = RiskLevel.fromName(
                    branchAlerts.maxByOrNull { RiskLevel.fromName(it.riskLevel).priority }?.riskLevel
                )
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
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
                                text = "${branch.region} · ${branch.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            RiskLevelBadge(level = topLevel)
                        }
                        Text(
                            text = "${branch.location} · ${branch.workerCount} workers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (env != null) {
                            Text(
                                text = "Temp ${env.temperatureC}°C · RH ${env.humidityPercent.toInt()}% · WBGT ${env.wbgtC ?: "Missing"} · AQI ${env.airQualityIndex ?: "Missing"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // AI Preventive Shift Briefing Card (Clearly labeled offline placeholder when Firebase declined)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.aiBriefingTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = strings.aiBriefingOfflineNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    uiState.aiBriefing?.let { briefing ->
                        Text(
                            text = briefing.summary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    FilledTonalButton(
                        onClick = onGenerateAiBriefing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("generate_ai_briefing_button")
                    ) {
                        Text(strings.generateAiBriefing)
                    }
                }
            }
        }

        // Export Public Health CSV Report
        item {
            Button(
                onClick = { reportPreviewCsv = onBuildCsvReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("export_csv_report_button")
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.exportReport)
            }
        }

        // Regional Breakdown List (De-identified)
        item {
            Text(
                text = "De-Identified Regional Aggregates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val groupedByRegion = uiState.allBranches.groupBy { it.region }.entries.toList()
        items(groupedByRegion, key = { it.key }) { (region, regionBranches) ->
            val workers = regionBranches.sumOf { it.workerCount }
            val branchIds = regionBranches.map { it.id }.toSet()
            val regionAlerts = uiState.allAlerts.filter { it.branchId in branchIds && it.actionStatus != "RESOLVED" }
            val symptomatic = uiState.allCheckIns.count { it.branchId in branchIds && it.symptomsCsv.isNotBlank() }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(region, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${regionBranches.size} site(s) · $workers workers · $symptomatic symptomatic check-in(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${regionAlerts.size} alerts",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (regionAlerts.any { it.riskLevel == "URGENT" }) RiskUrgent else RiskElevated
                    )
                }
            }
        }
    }

    reportPreviewCsv?.let { csv ->
        AlertDialog(
            onDismissRequest = { reportPreviewCsv = null },
            title = { Text("De-Identified CSV Report") },
            text = {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = csv,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "Yaqeen Health — De-Identified Workforce Safety Report")
                            putExtra(Intent.EXTRA_TEXT, csv)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export CSV Report"))
                        reportPreviewCsv = null
                    },
                    modifier = Modifier.testTag("share_csv_intent_button")
                ) {
                    Text("Share / Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportPreviewCsv = null }) { Text("Close") }
            }
        )
    }
}

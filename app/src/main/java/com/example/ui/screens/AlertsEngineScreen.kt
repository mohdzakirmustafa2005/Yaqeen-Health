package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.RiskAlertEntity
import com.example.domain.AlertActionStatus
import com.example.domain.RiskLevel
import com.example.i18n.LocalAppStrings
import com.example.ui.MainUiState
import com.example.ui.components.ActionStatusChip
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.SimulatedDataBanner
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RiskElevated
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertsEngineScreen(
    uiState: MainUiState,
    onReviewAlert: (RiskAlertEntity, AlertActionStatus, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val role = uiState.preferences.activeRole
    var filterLevel by remember { mutableStateOf<RiskLevel?>(null) }
    var selectedAlertForReview by remember { mutableStateOf<RiskAlertEntity?>(null) }

    val filteredAlerts = remember(uiState.orgAlerts, filterLevel) {
        if (filterLevel == null) uiState.orgAlerts
        else uiState.orgAlerts.filter { it.riskLevel == filterLevel?.name }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("alerts_engine_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimulatedDataBanner()
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.alertsTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = strings.explainableEngineTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = strings.notMedicalDeviceNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter Chips
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterLevel == null,
                    onClick = { filterLevel = null },
                    label = { Text("All (${uiState.orgAlerts.size})") },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
                RiskLevel.entries.forEach { level ->
                    val count = uiState.orgAlerts.count { it.riskLevel == level.name }
                    FilterChip(
                        selected = filterLevel == level,
                        onClick = { filterLevel = if (filterLevel == level) null else level },
                        label = { Text("${level.label} ($count)") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }
        }

        items(filteredAlerts, key = { it.id }) { alert ->
            ExplainableAlertCard(
                alert = alert,
                canReview = role.canReviewAlerts,
                onOpenReview = { selectedAlertForReview = alert }
            )
        }
    }

    selectedAlertForReview?.let { alert ->
        AlertReviewDialog(
            alert = alert,
            onDismiss = { selectedAlertForReview = null },
            onConfirm = { status, note ->
                onReviewAlert(alert, status, note)
                selectedAlertForReview = null
            }
        )
    }
}

@Composable
private fun ExplainableAlertCard(
    alert: RiskAlertEntity,
    canReview: Boolean,
    onOpenReview: () -> Unit
) {
    val strings = LocalAppStrings.current
    val riskLevel = RiskLevel.fromName(alert.riskLevel)
    val actionStatus = AlertActionStatus.fromName(alert.actionStatus)
    val timeText = remember(alert.createdAt) {
        SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()).format(Date(alert.createdAt))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (riskLevel == RiskLevel.URGENT) 1.5.dp else 1.dp,
            color = if (riskLevel == RiskLevel.URGENT) RiskUrgent.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_card_${alert.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskLevelBadge(level = riskLevel)
                ActionStatusChip(status = actionStatus)
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = alert.riskCategory,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${alert.branchName} · $timeText · Overall Risk ${alert.overallScore}/100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 4 Sub-score bars for complete explainability
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SubScoreBar("Heat Exposure", alert.heatScore)
                SubScoreBar("Fatigue Load", alert.fatigueScore)
                SubScoreBar("Symptom Cluster", alert.symptomScore)
                SubScoreBar("Air Quality / Env", alert.envScore)
            }

            // Why generated
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.whyGenerated,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.explanation,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Missing Measurements transparency
            if (alert.missingMeasurements.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReportProblem,
                            contentDescription = strings.missingMeasurements,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = strings.missingMeasurements,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            alert.missingMeasurements.split("|").filter { it.isNotBlank() }.forEach { m ->
                                Text(
                                    text = "• $m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Recommended Actions
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.preventiveActions,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                alert.recommendedActions.split("|").filter { it.isNotBlank() }.forEach { act ->
                    Text(
                        text = "• $act",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!alert.actionNote.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${alert.reviewedByRole ?: "Reviewer"}: ${alert.actionNote}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (canReview) {
                OutlinedButton(
                    onClick = onOpenReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("review_alert_button_${alert.id}")
                ) {
                    Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.reviewAlert)
                }
            }
        }
    }
}

@Composable
private fun SubScoreBar(label: String, score: Int) {
    val barColor: Color = when {
        score >= 70 -> RiskUrgent
        score >= 50 -> RiskElevated
        else -> RiskNormal
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                "$score/100",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AlertReviewDialog(
    alert: RiskAlertEntity,
    onDismiss: () -> Unit,
    onConfirm: (AlertActionStatus, String) -> Unit
) {
    val strings = LocalAppStrings.current
    var note by remember { mutableStateOf(alert.actionNote ?: "") }
    var chosenStatus by remember { mutableStateOf(AlertActionStatus.PREVENTIVE_ACTION_TAKEN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.reviewAlert) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${alert.branchName} · ${alert.riskCategory}", style = MaterialTheme.typography.bodySmall)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        AlertActionStatus.ACKNOWLEDGED to strings.acknowledge,
                        AlertActionStatus.PREVENTIVE_ACTION_TAKEN to strings.markActionTaken,
                        AlertActionStatus.RESOLVED to strings.resolveAlert
                    ).forEach { (status, label) ->
                        FilterChip(
                            selected = chosenStatus == status,
                            onClick = { chosenStatus = status },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Preventive Action Log / Notes") },
                    placeholder = { Text("e.g., Shaded cooling tent & ORS water deployed") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_action_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(chosenStatus, note) },
                modifier = Modifier.testTag("confirm_alert_review_button")
            ) {
                Text("Save Audit Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

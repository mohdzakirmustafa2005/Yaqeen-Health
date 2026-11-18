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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.BranchEntity
import com.example.domain.Symptom
import com.example.domain.WorkIntensity
import com.example.i18n.LocalAppStrings
import com.example.ui.MainUiState
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.SimulatedDataBanner
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkerCheckInScreen(
    uiState: MainUiState,
    onToggleOfflineMode: (Boolean) -> Unit,
    onSyncQueue: () -> Unit,
    onSubmitCheckIn: (
        branch: BranchEntity,
        department: String,
        fatigueScore: Int,
        shiftHours: Int,
        workIntensity: WorkIntensity,
        symptoms: List<Symptom>,
        heatExposureScore: Int,
        hydrated: Boolean,
        consentGiven: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val branches = uiState.orgBranches
    var selectedBranch by remember(branches) { mutableStateOf(branches.firstOrNull()) }
    var department by remember { mutableStateOf("Furnace Line 2") }
    var fatigueFloat by remember { mutableFloatStateOf(5f) }
    var shiftFloat by remember { mutableFloatStateOf(8f) }
    var heatFloat by remember { mutableFloatStateOf(6f) }
    var intensity by remember { mutableStateOf(WorkIntensity.MODERATE) }
    val selectedSymptoms = remember { mutableStateListOf<Symptom>() }
    var hydrated by remember { mutableStateOf(true) }
    var consentGiven by remember(uiState.preferences.workerConsentGiven) {
        mutableStateOf(uiState.preferences.workerConsentGiven)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("worker_checkin_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimulatedDataBanner()
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.checkInTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.checkInSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Offline Mode & Queue Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.preferences.offlineFieldMode) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = strings.offlineModeLabel,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.offlineModeLabel,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Switch(
                            checked = uiState.preferences.offlineFieldMode,
                            onCheckedChange = onToggleOfflineMode,
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }
                    if (uiState.pendingSyncCount > 0) {
                        FilledTonalButton(
                            onClick = onSyncQueue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("checkin_sync_now_button")
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("${strings.syncNow} (${uiState.pendingSyncCount})")
                        }
                    }
                }
            }
        }

        // Check-In Form Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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
                                branches.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b.name) },
                                        onClick = {
                                            selectedBranch = b
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text(strings.departmentLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkin_department_input")
                    )

                    // Fatigue Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.fatigueScoreLabel, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${fatigueFloat.roundToInt()} / 10",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = fatigueFloat,
                            onValueChange = { fatigueFloat = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.testTag("fatigue_slider")
                        )
                    }

                    // Shift Duration Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.shiftHoursLabel, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${shiftFloat.roundToInt()} hrs",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = shiftFloat,
                            onValueChange = { shiftFloat = it },
                            valueRange = 4f..14f,
                            steps = 9,
                            modifier = Modifier.testTag("shift_hours_slider")
                        )
                    }

                    // Work Intensity Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.workIntensityLabel, style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WorkIntensity.entries.forEach { level ->
                                FilterChip(
                                    selected = intensity == level,
                                    onClick = { intensity = level },
                                    label = { Text(level.label) },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("intensity_chip_${level.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Perceived Heat Exposure
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Perceived Heat Exposure (1–10)", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${heatFloat.roundToInt()} / 10",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Slider(
                            value = heatFloat,
                            onValueChange = { heatFloat = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.testTag("heat_exposure_slider")
                        )
                    }

                    // Symptoms FlowRow
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.symptomsLabel, style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Symptom.entries.forEach { symptom ->
                                val label = when (symptom) {
                                    Symptom.DIZZINESS -> strings.symptomDizziness
                                    Symptom.HEADACHE -> strings.symptomHeadache
                                    Symptom.MUSCLE_CRAMPS -> strings.symptomCramps
                                    Symptom.EXCESSIVE_SWEATING -> strings.symptomSweating
                                    Symptom.SHORTNESS_OF_BREATH -> strings.symptomBreath
                                    Symptom.NAUSEA -> strings.symptomNausea
                                }
                                val isChecked = selectedSymptoms.contains(symptom)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = {
                                        if (isChecked) selectedSymptoms.remove(symptom)
                                        else selectedSymptoms.add(symptom)
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("symptom_chip_${symptom.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Hydration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.hydrationLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hydrated,
                            onCheckedChange = { hydrated = it },
                            modifier = Modifier.testTag("hydration_switch")
                        )
                    }

                    // Explicit DPDP Consent Checkbox
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = consentGiven,
                                onCheckedChange = { consentGiven = it },
                                modifier = Modifier.testTag("checkin_consent_checkbox")
                            )
                            Text(
                                text = strings.consentCheckboxLabel,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val targetBranch = selectedBranch ?: branches.firstOrNull() ?: return@Button
                            onSubmitCheckIn(
                                targetBranch,
                                department,
                                fatigueFloat.roundToInt(),
                                shiftFloat.roundToInt(),
                                intensity,
                                selectedSymptoms.toList(),
                                heatFloat.roundToInt(),
                                hydrated,
                                consentGiven
                            )
                        },
                        enabled = selectedBranch != null || branches.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("submit_checkin_button")
                    ) {
                        Icon(Icons.Filled.HealthAndSafety, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.submitCheckIn)
                    }
                }
            }
        }

        // Personal Immediate Risk Advisory Feedback Card
        uiState.lastCheckInAssessment?.let { assessment ->
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_alert_feedback_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Preventive Advisory",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            RiskLevelBadge(level = assessment.riskLevel)
                        }
                        Text(
                            text = assessment.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        assessment.recommendedActions.forEach { action ->
                            Text(
                                text = "• $action",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

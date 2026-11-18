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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OrganizationEntity
import com.example.domain.SubscriptionPlan
import com.example.domain.UserRole
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.MainUiState
import com.example.ui.components.SimulatedDataBanner
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsoleSettingsScreen(
    uiState: MainUiState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectOrg: (String) -> Unit,
    onUpdateConsent: (Boolean) -> Unit,
    onDeleteWorkerData: () -> Unit,
    onToggleTenantSuspension: (OrganizationEntity) -> Unit,
    onUpdateSubscriptionPlan: (String, SubscriptionPlan) -> Unit,
    onOpenOrgRegistration: () -> Unit,
    onAddBranch: (String, String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val activeOrg = uiState.activeOrganization
    val role = uiState.preferences.activeRole
    var showAddBranchDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("console_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SimulatedDataBanner()
        }

        item {
            Text(
                text = strings.consoleTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Language Switcher (EN, HI, TE, UR)
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
                        Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(strings.languageLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = uiState.preferences.language == lang,
                                onClick = { onSelectLanguage(lang) },
                                label = { Text(lang.nativeLabel) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        // Role-Based Access Switcher (All 8 SaaS Roles)
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
                    Text(strings.roleLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = role.scopeDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.entries.forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { onSelectRole(r) },
                                label = { Text(r.displayName) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("role_chip_${r.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Privacy, DPDP Consent & Data Deletion Rights
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
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
                        Icon(Icons.Filled.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(strings.privacyControlsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Aligned with India DPDP principles: explicit purpose-specific consent, employer medical data redaction, and worker erasure rights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.consentStatusLabel, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = uiState.preferences.workerConsentGiven,
                            onCheckedChange = onUpdateConsent,
                            modifier = Modifier.testTag("dpdp_consent_switch")
                        )
                    }
                    OutlinedButton(
                        onClick = onDeleteWorkerData,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RiskUrgent),
                        border = BorderStroke(1.dp, RiskUrgent.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("dpdp_delete_data_button")
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.deleteMyData)
                    }
                }
            }
        }

        // Organization & Branch Management + SaaS Plans
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
                    Text(strings.saasPlansTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenOrgRegistration,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("console_register_org_button")
                        ) {
                            Icon(Icons.Filled.AddBusiness, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(strings.registerOrg)
                        }
                        if (activeOrg != null && role.canManageOrg) {
                            OutlinedButton(
                                onClick = { showAddBranchDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("add_branch_button")
                            ) {
                                Text("Add Branch")
                            }
                        }
                    }

                    if (activeOrg != null) {
                        val currentPlan = SubscriptionPlan.fromName(activeOrg.subscriptionPlan)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubscriptionPlan.entries.forEach { plan ->
                                FilterChip(
                                    selected = currentPlan == plan,
                                    onClick = { onUpdateSubscriptionPlan(activeOrg.id, plan) },
                                    label = { Text("${plan.label} (${plan.priceInr})") },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Multi-Tenant Organizations (Super Admin Console & Switcher)
        item {
            Text(
                text = strings.superAdminTenants,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.organizations, key = { it.id }) { org ->
            val isSelected = org.id == activeOrg?.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                ),
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
                        Text(org.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (org.isSuspended) MaterialTheme.colorScheme.errorContainer else RiskNormal.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = if (org.isSuspended) "Suspended" else "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (org.isSuspended) RiskUrgent else RiskNormal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "${org.industry} · ${org.city} · ${SubscriptionPlan.fromName(org.subscriptionPlan).label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onSelectOrg(org.id) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(if (isSelected) "Active Workspace" else "Switch Tenant")
                        }
                        if (role == UserRole.SUPER_ADMIN) {
                            OutlinedButton(
                                onClick = { onToggleTenantSuspension(org) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(if (org.isSuspended) "Reactivate" else "Suspend")
                            }
                        }
                    }
                }
            }
        }

        // Audit Log Feed
        item {
            Text(
                text = strings.auditTrailTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.auditLogs.take(12), key = { it.id }) { log ->
            val fmt = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.action, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(fmt.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${log.actorRole}: ${log.details}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showAddBranchDialog) {
        AddBranchDialog(
            onDismiss = { showAddBranchDialog = false },
            onConfirm = { name, location, region, workers ->
                onAddBranch(name, location, region, workers)
                showAddBranchDialog = false
            }
        )
    }
}

@Composable
private fun AddBranchDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("Hyderabad") }
    var workersText by remember { mutableStateOf("45") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Workplace Branch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Branch / Unit Name") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Site Address") })
                OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("City / Region") })
                OutlinedTextField(value = workersText, onValueChange = { workersText = it }, label = { Text("Worker Count") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, location.ifBlank { region }, region, workersText.toIntOrNull() ?: 25)
                    }
                }
            ) { Text("Create Branch") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

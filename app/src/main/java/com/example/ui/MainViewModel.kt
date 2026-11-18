package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiBriefingRepository
import com.example.data.ai.AiBriefingResult
import com.example.data.local.AuditLogEntity
import com.example.data.local.BranchEntity
import com.example.data.local.EnvironmentalReadingEntity
import com.example.data.local.HealthCheckInEntity
import com.example.data.local.OrganizationEntity
import com.example.data.local.RiskAlertEntity
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.preferences.UserSessionPreferences
import com.example.data.repository.WorkforceHealthRepository
import com.example.domain.AlertActionStatus
import com.example.domain.DemoAuthCatalog
import com.example.domain.ExplainableRiskAssessment
import com.example.domain.FatigueLog
import com.example.domain.HeatMetric
import com.example.domain.SubscriptionPlan
import com.example.domain.Symptom
import com.example.domain.SyncStatus
import com.example.domain.UserRole
import com.example.domain.WorkIntensity
import com.example.domain.Worker
import com.example.domain.toFatigueLog
import com.example.domain.toHeatMetric
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val preferences: UserSessionPreferences = UserSessionPreferences(),
    val organizations: List<OrganizationEntity> = emptyList(),
    val activeOrganization: OrganizationEntity? = null,
    val allBranches: List<BranchEntity> = emptyList(),
    val orgBranches: List<BranchEntity> = emptyList(),
    val allCheckIns: List<HealthCheckInEntity> = emptyList(),
    val orgCheckIns: List<HealthCheckInEntity> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val heatMetrics: List<HeatMetric> = emptyList(),
    val fatigueLogs: List<FatigueLog> = emptyList(),
    val pendingSyncCount: Int = 0,
    val allReadings: List<EnvironmentalReadingEntity> = emptyList(),
    val orgReadings: List<EnvironmentalReadingEntity> = emptyList(),
    val allAlerts: List<RiskAlertEntity> = emptyList(),
    val orgAlerts: List<RiskAlertEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val lastCheckInAssessment: ExplainableRiskAssessment? = null,
    val aiBriefing: AiBriefingResult? = null,
    val statusBannerMessage: String? = null
)

class MainViewModel(
    private val repository: WorkforceHealthRepository,
    private val prefsRepo: AppPreferencesRepository,
    private val aiBriefingRepo: AiBriefingRepository
) : ViewModel() {

    private val transientAssessment = MutableStateFlow<ExplainableRiskAssessment?>(null)
    private val transientBriefing = MutableStateFlow<AiBriefingResult?>(null)
    private val transientMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        prefsRepo.preferencesFlow,
        repository.allOrganizations,
        repository.allBranches,
        repository.allCheckIns,
        repository.allReadings,
        repository.allAlerts,
        repository.allAuditLogs,
        transientAssessment,
        transientBriefing,
        transientMessage
    ) { values ->
        val prefs = values[0] as UserSessionPreferences
        @Suppress("UNCHECKED_CAST")
        val orgs = values[1] as List<OrganizationEntity>
        @Suppress("UNCHECKED_CAST")
        val branches = values[2] as List<BranchEntity>
        @Suppress("UNCHECKED_CAST")
        val checkIns = values[3] as List<HealthCheckInEntity>
        @Suppress("UNCHECKED_CAST")
        val readings = values[4] as List<EnvironmentalReadingEntity>
        @Suppress("UNCHECKED_CAST")
        val alerts = values[5] as List<RiskAlertEntity>
        @Suppress("UNCHECKED_CAST")
        val audits = values[6] as List<AuditLogEntity>
        val assessment = values[7] as ExplainableRiskAssessment?
        val briefing = values[8] as AiBriefingResult?
        val message = values[9] as String?

        val activeOrg = orgs.firstOrNull { it.id == prefs.activeOrgId } ?: orgs.firstOrNull()
        val orgId = activeOrg?.id ?: prefs.activeOrgId
        val orgBranches = branches.filter { it.organizationId == orgId }
        val orgCheckIns = checkIns.filter { it.organizationId == orgId }
        val orgReadings = readings.filter { it.organizationId == orgId }
        val orgAlerts = alerts.filter { it.organizationId == orgId }
        val pendingCount = orgCheckIns.count { it.syncStatus == SyncStatus.PENDING_SYNC.name }

        val fatigueLogs = orgCheckIns.map { it.toFatigueLog() }
        val heatMetrics = orgReadings.map { it.toHeatMetric() }
        val workers = orgCheckIns
            .distinctBy { it.workerAlias }
            .mapIndexed { idx, chk ->
                Worker(
                    id = "wrk_${idx + 1}",
                    organizationId = chk.organizationId,
                    branchId = chk.branchId,
                    workerCode = "YQN-${101 + idx}",
                    displayAlias = chk.workerAlias,
                    department = chk.department,
                    consentGiven = chk.consentGiven,
                    preferredLanguage = prefs.language.code,
                    isSimulated = chk.isSimulated
                )
            }

        MainUiState(
            preferences = prefs.copy(activeOrgId = orgId),
            organizations = orgs,
            activeOrganization = activeOrg,
            allBranches = branches,
            orgBranches = orgBranches,
            allCheckIns = checkIns,
            orgCheckIns = orgCheckIns,
            workers = workers,
            heatMetrics = heatMetrics,
            fatigueLogs = fatigueLogs,
            pendingSyncCount = pendingCount,
            allReadings = readings,
            orgReadings = orgReadings,
            allAlerts = alerts,
            orgAlerts = orgAlerts,
            auditLogs = audits.filter { prefs.activeRole == UserRole.SUPER_ADMIN || it.organizationId == orgId },
            lastCheckInAssessment = assessment,
            aiBriefing = briefing,
            statusBannerMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun signInWithDemoCredentials(username: String, password: String): String? {
        val account = DemoAuthCatalog.authenticate(username, password)
            ?: return "Invalid username or password. Use 'admin' / 'password123' or tap a role card below."
        viewModelScope.launch {
            prefsRepo.setSignedInSession(account.username, account.role)
            transientMessage.value = "Signed in as ${account.displayName} (${account.role.displayName})"
        }
        return null
    }

    fun signOut() {
        viewModelScope.launch {
            prefsRepo.signOut()
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setActiveOrganization(orgId: String) {
        viewModelScope.launch {
            prefsRepo.setActiveOrg(orgId)
            transientBriefing.value = null
        }
    }

    fun setActiveRole(role: UserRole) {
        viewModelScope.launch { prefsRepo.setActiveRole(role) }
    }

    fun setOfflineMode(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setOfflineFieldMode(enabled) }
    }

    fun updateWorkerConsent(consent: Boolean) {
        val state = uiState.value
        val orgId = state.activeOrganization?.id ?: return
        viewModelScope.launch {
            prefsRepo.setWorkerConsent(consent)
            repository.updateWorkerConsent(orgId, DEFAULT_WORKER_ALIAS, consent)
            transientMessage.value = if (consent) {
                "DPDP consent granted for authorized Occupational Health review."
            } else {
                "DPDP consent withdrawn; individual symptoms redacted from OHP view."
            }
        }
    }

    fun deleteWorkerCheckIns() {
        val orgId = uiState.value.activeOrganization?.id ?: return
        viewModelScope.launch {
            repository.deleteWorkerCheckInData(orgId, DEFAULT_WORKER_ALIAS)
            transientAssessment.value = null
            transientMessage.value = "Your check-in records for this site have been deleted."
        }
    }

    fun registerOrganization(
        orgName: String,
        industry: String,
        city: String,
        plan: SubscriptionPlan,
        initialBranchName: String,
        workerCount: Int
    ) {
        val role = uiState.value.preferences.activeRole
        viewModelScope.launch {
            val newOrgId = repository.registerOrganization(
                orgName = orgName,
                industry = industry,
                city = city,
                plan = plan,
                initialBranchName = initialBranchName,
                workerCount = workerCount,
                actorRole = role
            )
            prefsRepo.setActiveOrg(newOrgId)
            transientMessage.value = "Registered '$orgName' and switched active tenant."
        }
    }

    fun addBranch(branchName: String, location: String, region: String, workerCount: Int) {
        val state = uiState.value
        val orgId = state.activeOrganization?.id ?: return
        viewModelScope.launch {
            repository.addBranch(orgId, branchName, location, region, workerCount, state.preferences.activeRole)
            transientMessage.value = "Added branch '$branchName'."
        }
    }

    fun submitCheckIn(
        branch: BranchEntity,
        department: String,
        fatigueScore: Int,
        shiftHours: Int,
        workIntensity: WorkIntensity,
        symptoms: List<Symptom>,
        heatExposureScore: Int,
        hydrated: Boolean,
        consentGiven: Boolean
    ) {
        val state = uiState.value
        val orgId = state.activeOrganization?.id ?: return
        val offline = state.preferences.offlineFieldMode
        viewModelScope.launch {
            prefsRepo.setWorkerConsent(consentGiven)
            val assessment = repository.submitCheckIn(
                organizationId = orgId,
                branch = branch,
                workerAlias = DEFAULT_WORKER_ALIAS,
                department = department,
                fatigueScore = fatigueScore,
                shiftHours = shiftHours,
                workIntensity = workIntensity,
                symptoms = symptoms,
                heatExposureScore = heatExposureScore,
                hydrated = hydrated,
                consentGiven = consentGiven,
                offlineMode = offline
            )
            transientAssessment.value = assessment
            transientMessage.value = if (offline) {
                "Saved offline to local queue. Tap Sync Queue when connected."
            } else {
                "Check-in synced & analyzed by Early Warning Engine."
            }
        }
    }

    fun syncOfflineQueue() {
        val orgId = uiState.value.activeOrganization?.id ?: return
        viewModelScope.launch {
            val synced = repository.syncPendingCheckIns(orgId)
            transientMessage.value = if (synced > 0) {
                "Synced $synced offline check-in(s) and updated risk alerts."
            } else {
                "No pending offline check-ins to sync."
            }
        }
    }

    fun recordEnvironmentalReading(
        branch: BranchEntity,
        temperatureC: Double,
        humidityPercent: Double,
        wbgtC: Double?,
        airQualityIndex: Int?,
        sourceLabel: String,
        isSimulated: Boolean = true
    ) {
        val state = uiState.value
        val orgId = state.activeOrganization?.id ?: return
        viewModelScope.launch {
            val assessment = repository.recordEnvironmentalReading(
                organizationId = orgId,
                branch = branch,
                temperatureC = temperatureC,
                humidityPercent = humidityPercent,
                wbgtC = wbgtC,
                airQualityIndex = airQualityIndex,
                sourceLabel = sourceLabel,
                actorRole = state.preferences.activeRole,
                isSimulated = isSimulated
            )
            transientMessage.value = "Telemetry evaluated: ${assessment.riskLevel.label} (${assessment.riskCategory})."
        }
    }

    fun reviewAlert(alert: RiskAlertEntity, status: AlertActionStatus, note: String) {
        val role = uiState.value.preferences.activeRole
        viewModelScope.launch {
            repository.reviewAlert(alert, status, note, role)
            transientMessage.value = "Alert updated to ${status.label}."
        }
    }

    fun toggleTenantSuspension(org: OrganizationEntity) {
        val role = uiState.value.preferences.activeRole
        viewModelScope.launch {
            repository.toggleTenantSuspension(org, role)
        }
    }

    fun updateSubscriptionPlan(orgId: String, plan: SubscriptionPlan) {
        val role = uiState.value.preferences.activeRole
        viewModelScope.launch {
            repository.updateSubscriptionPlan(orgId, plan, role)
            transientMessage.value = "Updated plan to ${plan.label}."
        }
    }

    fun generateAiBriefing() {
        val state = uiState.value
        val orgName = state.activeOrganization?.name ?: "Workforce Network"
        val urgentCount = state.orgAlerts.count { it.riskLevel == "URGENT" && it.actionStatus != "RESOLVED" }
        val elevatedCount = state.orgAlerts.count { it.riskLevel == "ELEVATED" && it.actionStatus != "RESOLVED" }
        val peakTemp = state.orgReadings.maxOfOrNull { it.temperatureC }
        val peakAqi = state.orgReadings.mapNotNull { it.airQualityIndex }.maxOrNull()
        viewModelScope.launch {
            transientBriefing.value = aiBriefingRepo.generateShiftBriefing(
                organizationName = orgName,
                urgentCount = urgentCount,
                elevatedCount = elevatedCount,
                peakTemperatureC = peakTemp,
                peakAqi = peakAqi,
                language = state.preferences.language
            )
        }
    }

    fun buildDeIdentifiedCsvReport(): String {
        val state = uiState.value
        return repository.buildDeIdentifiedCsvReport(
            orgs = state.organizations,
            branches = state.allBranches,
            readings = state.allReadings,
            alerts = state.allAlerts,
            checkIns = state.allCheckIns
        )
    }

    fun clearBannerMessage() {
        transientMessage.value = null
    }

    companion object {
        const val DEFAULT_WORKER_ALIAS = "Worker #104 (Demo)"

        fun factory(
            repository: WorkforceHealthRepository,
            prefsRepo: AppPreferencesRepository,
            aiBriefingRepo: AiBriefingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, prefsRepo, aiBriefingRepo) as T
            }
        }
    }
}

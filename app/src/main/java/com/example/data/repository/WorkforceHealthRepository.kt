package com.example.data.repository

import com.example.data.local.AuditLogEntity
import com.example.data.local.BranchEntity
import com.example.data.local.DemoSeedData
import com.example.data.local.EnvironmentalReadingEntity
import com.example.data.local.HealthCheckInEntity
import com.example.data.local.OrganizationEntity
import com.example.data.local.RiskAlertEntity
import com.example.data.local.YaqeenDatabase
import com.example.domain.AlertActionStatus
import com.example.domain.EarlyWarningEngine
import com.example.domain.ExplainableRiskAssessment
import com.example.domain.RiskAssessmentInput
import com.example.domain.SubscriptionPlan
import com.example.domain.Symptom
import com.example.domain.SyncStatus
import com.example.domain.UserRole
import com.example.domain.WorkIntensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WorkforceHealthRepository(private val db: YaqeenDatabase) {

    val allOrganizations: Flow<List<OrganizationEntity>> = db.organizationDao().observeAll()
    val allBranches: Flow<List<BranchEntity>> = db.branchDao().observeAll()
    val allCheckIns: Flow<List<HealthCheckInEntity>> = db.healthCheckInDao().observeAll()
    val allReadings: Flow<List<EnvironmentalReadingEntity>> = db.environmentalReadingDao().observeAll()
    val allAlerts: Flow<List<RiskAlertEntity>> = db.riskAlertDao().observeAll()
    val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().observeAll()

    suspend fun ensureSeeded() {
        DemoSeedData.seedIfEmpty(db)
    }

    suspend fun registerOrganization(
        orgName: String,
        industry: String,
        city: String,
        plan: SubscriptionPlan,
        initialBranchName: String,
        workerCount: Int,
        actorRole: UserRole
    ): String {
        val now = System.currentTimeMillis()
        val orgId = "org_${UUID.randomUUID().toString().take(8)}"
        val branchId = "br_${UUID.randomUUID().toString().take(8)}"
        val org = OrganizationEntity(
            id = orgId,
            name = orgName.trim(),
            industry = industry.trim(),
            city = city.trim(),
            subscriptionPlan = plan.name,
            isSimulated = false,
            createdAt = now
        )
        val branch = BranchEntity(
            id = branchId,
            organizationId = orgId,
            name = initialBranchName.trim(),
            location = city.trim(),
            region = city.substringBefore(",").trim().ifEmpty { "India" },
            workerCount = workerCount.coerceAtLeast(1),
            mapXFraction = (0.22f + (now % 55) / 100f).coerceIn(0.18f, 0.85f),
            mapYFraction = (0.22f + ((now / 7) % 55) / 100f).coerceIn(0.18f, 0.85f),
            isSimulated = false
        )
        db.organizationDao().insert(org)
        db.branchDao().insert(branch)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = orgId,
                actorRole = actorRole.displayName,
                action = "ORG_REGISTERED",
                details = "Registered tenant '${org.name}' (${plan.label}) with branch '${branch.name}'.",
                timestamp = now
            )
        )
        return orgId
    }

    suspend fun addBranch(
        orgId: String,
        branchName: String,
        location: String,
        region: String,
        workerCount: Int,
        actorRole: UserRole
    ) {
        val now = System.currentTimeMillis()
        val branch = BranchEntity(
            id = "br_${UUID.randomUUID().toString().take(8)}",
            organizationId = orgId,
            name = branchName.trim(),
            location = location.trim(),
            region = region.trim().ifEmpty { "Telangana" },
            workerCount = workerCount.coerceAtLeast(1),
            mapXFraction = (0.20f + (now % 60) / 100f).coerceIn(0.18f, 0.85f),
            mapYFraction = (0.20f + ((now / 11) % 60) / 100f).coerceIn(0.18f, 0.85f),
            isSimulated = false
        )
        db.branchDao().insert(branch)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = orgId,
                actorRole = actorRole.displayName,
                action = "BRANCH_CREATED",
                details = "Added workplace branch '${branch.name}' ($workerCount workers).",
                timestamp = now
            )
        )
    }

    suspend fun submitCheckIn(
        organizationId: String,
        branch: BranchEntity,
        workerAlias: String,
        department: String,
        fatigueScore: Int,
        shiftHours: Int,
        workIntensity: WorkIntensity,
        symptoms: List<Symptom>,
        heatExposureScore: Int,
        hydrated: Boolean,
        consentGiven: Boolean,
        offlineMode: Boolean
    ): ExplainableRiskAssessment? {
        val now = System.currentTimeMillis()
        val syncStatus = if (offlineMode) SyncStatus.PENDING_SYNC else SyncStatus.SYNCED
        val checkIn = HealthCheckInEntity(
            id = "chk_${UUID.randomUUID().toString().take(8)}",
            organizationId = organizationId,
            branchId = branch.id,
            branchName = branch.name,
            workerAlias = workerAlias.ifBlank { "Worker #104 (Demo)" },
            department = department.ifBlank { "Operations" },
            fatigueScore = fatigueScore,
            shiftHours = shiftHours,
            workIntensity = workIntensity.name,
            symptomsCsv = symptoms.joinToString(",") { it.name },
            heatExposureScore = heatExposureScore,
            hydrated = hydrated,
            consentGiven = consentGiven,
            syncStatus = syncStatus.name,
            isSimulated = false,
            recordedAt = now
        )
        db.healthCheckInDao().insert(checkIn)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = organizationId,
                actorRole = UserRole.WORKER.displayName,
                action = if (offlineMode) "CHECKIN_QUEUED_OFFLINE" else "CHECKIN_SUBMITTED",
                details = "Worker check-in logged at ${branch.name} (consent=$consentGiven, sync=${syncStatus.name}).",
                timestamp = now
            )
        )
        return if (!offlineMode) {
            evaluateAndCreateAlertForCheckIn(checkIn)
        } else {
            null
        }
    }

    suspend fun syncPendingCheckIns(organizationId: String): Int {
        val pending = db.healthCheckInDao().getPendingSync(organizationId)
        if (pending.isEmpty()) return 0
        db.healthCheckInDao().markAllSynced(organizationId)
        pending.forEach { evaluateAndCreateAlertForCheckIn(it.copy(syncStatus = SyncStatus.SYNCED.name)) }
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = organizationId,
                actorRole = "Sync Engine",
                action = "OFFLINE_QUEUE_SYNCED",
                details = "Synchronized ${pending.size} offline worker check-in(s) and ran Early Warning Engine.",
                timestamp = System.currentTimeMillis()
            )
        )
        return pending.size
    }

    suspend fun recordEnvironmentalReading(
        organizationId: String,
        branch: BranchEntity,
        temperatureC: Double,
        humidityPercent: Double,
        wbgtC: Double?,
        airQualityIndex: Int?,
        sourceLabel: String,
        actorRole: UserRole,
        isSimulated: Boolean
    ): ExplainableRiskAssessment {
        val now = System.currentTimeMillis()
        val reading = EnvironmentalReadingEntity(
            id = "env_${UUID.randomUUID().toString().take(8)}",
            organizationId = organizationId,
            branchId = branch.id,
            branchName = branch.name,
            temperatureC = temperatureC,
            humidityPercent = humidityPercent,
            wbgtC = wbgtC,
            airQualityIndex = airQualityIndex,
            sourceLabel = sourceLabel,
            isSimulated = isSimulated,
            recordedAt = now
        )
        db.environmentalReadingDao().insert(reading)

        val checkIns = db.healthCheckInDao().observeByOrg(organizationId).first()
            .filter { it.branchId == branch.id && it.recordedAt >= now - 86_400_000L }
        val avgFatigue = if (checkIns.isNotEmpty()) checkIns.map { it.fatigueScore }.average().toInt() else null
        val symptomaticCount = checkIns.count { it.symptomsCsv.isNotBlank() }

        val assessment = EarlyWarningEngine.evaluate(
            RiskAssessmentInput(
                organizationId = organizationId,
                branchId = branch.id,
                branchName = branch.name,
                temperatureC = temperatureC,
                humidityPercent = humidityPercent,
                wbgtC = wbgtC,
                airQualityIndex = airQualityIndex,
                fatigueScore = avgFatigue,
                shiftHours = checkIns.maxOfOrNull { it.shiftHours },
                workIntensity = WorkIntensity.fromName(checkIns.firstOrNull()?.workIntensity),
                symptomCount = if (symptomaticCount > 0) 1 else 0,
                branchSymptomaticCountLast24h = symptomaticCount,
                hydrated = checkIns.none { !it.hydrated },
                dataSource = sourceLabel,
                timestampMillis = now
            )
        )

        db.riskAlertDao().insert(
            RiskAlertEntity(
                id = "alt_${UUID.randomUUID().toString().take(8)}",
                organizationId = organizationId,
                branchId = branch.id,
                branchName = branch.name,
                riskCategory = assessment.riskCategory,
                riskLevel = assessment.riskLevel.name,
                heatScore = assessment.heatScore,
                fatigueScore = assessment.fatigueScore,
                symptomScore = assessment.symptomScore,
                envScore = assessment.envScore,
                overallScore = assessment.overallScore,
                explanation = assessment.explanation,
                missingMeasurements = assessment.missingMeasurements.joinToString("|"),
                recommendedActions = assessment.recommendedActions.joinToString("|"),
                actionStatus = AlertActionStatus.OPEN.name,
                isSimulated = isSimulated,
                createdAt = now,
                updatedAt = now
            )
        )

        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = organizationId,
                actorRole = actorRole.displayName,
                action = "ENVIRONMENT_EVALUATED",
                details = "Processed ${reading.temperatureC}°C / ${reading.humidityPercent}% RH (WBGT=${wbgtC ?: "Missing"}, AQI=${airQualityIndex ?: "Missing"}) -> ${assessment.riskLevel.label} Alert.",
                timestamp = now
            )
        )
        return assessment
    }

    suspend fun reviewAlert(
        alert: RiskAlertEntity,
        newStatus: AlertActionStatus,
        note: String,
        actorRole: UserRole
    ) {
        val now = System.currentTimeMillis()
        db.riskAlertDao().updateAlertAction(
            alertId = alert.id,
            status = newStatus.name,
            role = actorRole.displayName,
            note = note.ifBlank { "Reviewed by ${actorRole.displayName}" },
            updatedAt = now
        )
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = alert.organizationId,
                actorRole = actorRole.displayName,
                action = "ALERT_${newStatus.name}",
                details = "${alert.branchName}: ${newStatus.label} — ${note.ifBlank { "Preventive protocol logged." }}",
                timestamp = now
            )
        )
    }

    suspend fun toggleTenantSuspension(org: OrganizationEntity, actorRole: UserRole) {
        val next = !org.isSuspended
        db.organizationDao().setSuspended(org.id, next)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = org.id,
                actorRole = actorRole.displayName,
                action = if (next) "TENANT_SUSPENDED" else "TENANT_REACTIVATED",
                details = "${org.name} status updated to ${if (next) "Suspended" else "Active"}.",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSubscriptionPlan(orgId: String, plan: SubscriptionPlan, actorRole: UserRole) {
        db.organizationDao().updatePlan(orgId, plan.name)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = orgId,
                actorRole = actorRole.displayName,
                action = "SUBSCRIPTION_UPDATED",
                details = "Subscription tier changed to ${plan.label} (${plan.priceInr}).",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateWorkerConsent(orgId: String, workerAlias: String, consent: Boolean) {
        db.healthCheckInDao().updateWorkerConsent(orgId, workerAlias, consent)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = orgId,
                actorRole = UserRole.WORKER.displayName,
                action = "DPDP_CONSENT_UPDATED",
                details = "Worker clinical review consent set to $consent (purpose limitation enforced).",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteWorkerCheckInData(orgId: String, workerAlias: String) {
        db.healthCheckInDao().deleteWorkerCheckIns(orgId, workerAlias)
        db.auditLogDao().insert(
            AuditLogEntity(
                id = "aud_${UUID.randomUUID().toString().take(8)}",
                organizationId = orgId,
                actorRole = UserRole.WORKER.displayName,
                action = "DPDP_DATA_DELETED",
                details = "Worker exercised right to erasure for '$workerAlias' check-in records.",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun evaluateAndCreateAlertForCheckIn(checkIn: HealthCheckInEntity): ExplainableRiskAssessment {
        val now = System.currentTimeMillis()
        val latestReading = db.environmentalReadingDao().observeByOrg(checkIn.organizationId).first()
            .firstOrNull { it.branchId == checkIn.branchId }
        val branchRecent = db.healthCheckInDao().observeByOrg(checkIn.organizationId).first()
            .filter { it.branchId == checkIn.branchId && it.recordedAt >= now - 86_400_000L }
        val symptoms = Symptom.parseCsv(checkIn.symptomsCsv)

        val assessment = EarlyWarningEngine.evaluate(
            RiskAssessmentInput(
                organizationId = checkIn.organizationId,
                branchId = checkIn.branchId,
                branchName = checkIn.branchName,
                temperatureC = latestReading?.temperatureC,
                humidityPercent = latestReading?.humidityPercent,
                wbgtC = latestReading?.wbgtC,
                airQualityIndex = latestReading?.airQualityIndex,
                fatigueScore = checkIn.fatigueScore,
                shiftHours = checkIn.shiftHours,
                workIntensity = WorkIntensity.fromName(checkIn.workIntensity),
                symptomCount = symptoms.size,
                branchSymptomaticCountLast24h = branchRecent.count { it.symptomsCsv.isNotBlank() },
                hydrated = checkIn.hydrated,
                dataSource = latestReading?.sourceLabel ?: "Worker Mobile Check-In",
                timestampMillis = now
            )
        )

        db.riskAlertDao().insert(
            RiskAlertEntity(
                id = "alt_${UUID.randomUUID().toString().take(8)}",
                organizationId = checkIn.organizationId,
                branchId = checkIn.branchId,
                branchName = checkIn.branchName,
                riskCategory = assessment.riskCategory,
                riskLevel = assessment.riskLevel.name,
                heatScore = assessment.heatScore,
                fatigueScore = assessment.fatigueScore,
                symptomScore = assessment.symptomScore,
                envScore = assessment.envScore,
                overallScore = assessment.overallScore,
                explanation = assessment.explanation,
                missingMeasurements = assessment.missingMeasurements.joinToString("|"),
                recommendedActions = assessment.recommendedActions.joinToString("|"),
                actionStatus = AlertActionStatus.OPEN.name,
                isSimulated = checkIn.isSimulated,
                createdAt = now,
                updatedAt = now
            )
        )
        return assessment
    }

    fun buildDeIdentifiedCsvReport(
        orgs: List<OrganizationEntity>,
        branches: List<BranchEntity>,
        readings: List<EnvironmentalReadingEntity>,
        alerts: List<RiskAlertEntity>,
        checkIns: List<HealthCheckInEntity>
    ): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("# YAQEEN ALLAH WORKFORCE HEALTH AI — DE-IDENTIFIED PUBLIC HEALTH & SAFETY REPORT")
        sb.appendLine("# Generated: ${fmt.format(Date())} | Notice: Simulated demo records labeled; zero individual worker PII exported.")
        sb.appendLine("Region,Organization,Branch,Workers,LatestTempC,LatestHumidityPct,LatestWbgtC,LatestAqi,CheckInCount,SymptomaticCount,UrgentAlerts,ElevatedAlerts,SimulatedDemo")
        branches.forEach { branch ->
            val org = orgs.firstOrNull { it.id == branch.organizationId }
            val latestEnv = readings.firstOrNull { it.branchId == branch.id }
            val branchAlerts = alerts.filter { it.branchId == branch.id }
            val branchCheckIns = checkIns.filter { it.branchId == branch.id }
            val urgent = branchAlerts.count { it.riskLevel == "URGENT" }
            val elevated = branchAlerts.count { it.riskLevel == "ELEVATED" }
            val symptomatic = branchCheckIns.count { it.symptomsCsv.isNotBlank() }
            sb.appendLine(
                listOf(
                    branch.region,
                    org?.name ?: branch.organizationId,
                    branch.name,
                    branch.workerCount,
                    latestEnv?.temperatureC ?: "N/A",
                    latestEnv?.humidityPercent ?: "N/A",
                    latestEnv?.wbgtC ?: "Missing",
                    latestEnv?.airQualityIndex ?: "Missing",
                    branchCheckIns.size,
                    symptomatic,
                    urgent,
                    elevated,
                    branch.isSimulated
                ).joinToString(",") { "\"${it.toString().replace("\"", "'")}\"" }
            )
        }
        return sb.toString()
    }
}

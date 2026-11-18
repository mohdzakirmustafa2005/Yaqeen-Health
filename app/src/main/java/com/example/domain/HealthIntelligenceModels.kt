package com.example.domain

import com.example.data.local.EnvironmentalReadingEntity
import com.example.data.local.HealthCheckInEntity

/**
 * Core health-intelligence domain models for YAQEEN ALLAH WORKFORCE HEALTH AI.
 */

data class Worker(
    val id: String,
    val organizationId: String,
    val branchId: String,
    val workerCode: String,
    val displayAlias: String,
    val department: String,
    val roleTitle: String = "Plant Operator",
    val shiftSchedule: String = "Day Shift (08:00–17:00)",
    val consentGiven: Boolean = false,
    val preferredLanguage: String = "en",
    val isSimulated: Boolean = true
)

data class HeatMetric(
    val id: String,
    val organizationId: String,
    val branchId: String,
    val branchName: String,
    val airTempC: Double,
    val relativeHumidityPct: Double,
    val wbgtC: Double? = null,
    val airQualityIndex: Int? = null,
    val heatRiskScore: Int,
    val missingSensorNote: String? = null,
    val sourceLabel: String,
    val isSimulated: Boolean = true,
    val recordedAtMillis: Long
) {
    val hasWbgtSensor: Boolean get() = wbgtC != null
}

data class FatigueLog(
    val id: String,
    val organizationId: String,
    val branchId: String,
    val workerId: String,
    val workerAlias: String,
    val department: String,
    val fatigueScore: Int,
    val shiftHours: Int,
    val workIntensity: WorkIntensity,
    val reportedSymptoms: List<Symptom>,
    val hydrated: Boolean,
    val fatigueRiskScore: Int,
    val syncStatus: SyncStatus,
    val consentGiven: Boolean,
    val isSimulated: Boolean = true,
    val recordedAtMillis: Long
)

data class DemoCredentialAccount(
    val username: String,
    val password: String,
    val role: UserRole,
    val displayName: String,
    val badgeNote: String
)

object DemoAuthCatalog {
    val accounts: List<DemoCredentialAccount> = listOf(
        DemoCredentialAccount(
            username = "admin",
            password = "password123",
            role = UserRole.ORG_ADMIN,
            displayName = "Admin · Organization Demo",
            badgeNote = "Primary demo login (branches, telemetry, alerts & population map)"
        ),
        DemoCredentialAccount(
            username = "safety@yaqeen.ai",
            password = "Yaqeen@2026",
            role = UserRole.SAFETY_OFFICER,
            displayName = "Arjun Rao · Safety Officer",
            badgeNote = "Recommended for 5-min judge demo (telemetry, alerts & actions)"
        ),
        DemoCredentialAccount(
            username = "worker@yaqeen.ai",
            password = "Worker@2026",
            role = UserRole.WORKER,
            displayName = "Imran Khan · Worker #104",
            badgeNote = "Daily check-in, offline queue & DPDP consent controls"
        ),
        DemoCredentialAccount(
            username = "ohp@yaqeen.ai",
            password = "Health@2026",
            role = UserRole.OCCUPATIONAL_HEALTH_PRO,
            displayName = "Dr. Meera Devi · Occupational Health",
            badgeNote = "Consented symptom review & preventive referrals"
        ),
        DemoCredentialAccount(
            username = "owner@yaqeen.ai",
            password = "Owner@2026",
            role = UserRole.ORG_OWNER,
            displayName = "Srinivas Reddy · Org Owner",
            badgeNote = "Organization registration, branches & SaaS plans"
        ),
        DemoCredentialAccount(
            username = "publichealth@yaqeen.ai",
            password = "Public@2026",
            role = UserRole.PUBLIC_HEALTH_AUTHORITY,
            displayName = "Telangana Health Desk",
            badgeNote = "De-identified regional risk map & CSV export"
        ),
        DemoCredentialAccount(
            username = "superadmin@yaqeen.ai",
            password = "Admin@2026",
            role = UserRole.SUPER_ADMIN,
            displayName = "Platform Super Admin",
            badgeNote = "Multi-tenant SaaS console & platform audit trail"
        )
    )

    fun authenticate(usernameInput: String, passwordInput: String): DemoCredentialAccount? {
        val cleanUser = usernameInput.trim().lowercase()
        val cleanPass = passwordInput.trim()
        return accounts.firstOrNull {
            it.username.lowercase() == cleanUser && it.password == cleanPass
        }
    }
}

fun EnvironmentalReadingEntity.toHeatMetric(): HeatMetric {
    val computedHeatScore = when {
        wbgtC != null && wbgtC >= 32.5 -> 90
        wbgtC != null && wbgtC >= 29.5 -> 66
        wbgtC != null -> 24
        else -> {
            val apparent = temperatureC + (0.12 * (humidityPercent - 40.0)).coerceAtLeast(0.0)
            when {
                apparent >= 40.0 -> 82
                apparent >= 35.0 -> 60
                else -> 22
            }
        }
    }
    val missingNote = if (wbgtC == null) {
        "WBGT sensor missing — conservative estimate from air temp & RH"
    } else null

    return HeatMetric(
        id = id,
        organizationId = organizationId,
        branchId = branchId,
        branchName = branchName,
        airTempC = temperatureC,
        relativeHumidityPct = humidityPercent,
        wbgtC = wbgtC,
        airQualityIndex = airQualityIndex,
        heatRiskScore = computedHeatScore,
        missingSensorNote = missingNote,
        sourceLabel = sourceLabel,
        isSimulated = isSimulated,
        recordedAtMillis = recordedAt
    )
}

fun HealthCheckInEntity.toFatigueLog(): FatigueLog {
    val intensity = WorkIntensity.fromName(workIntensity)
    val base = (fatigueScore * 8).coerceIn(8, 80)
    val shiftPenalty = if (shiftHours > 9) 14 else 0
    val hydrationPenalty = if (!hydrated) 10 else 0
    val computedFatigueScore = (base + shiftPenalty + hydrationPenalty + intensity.weight / 3).coerceIn(0, 100)

    return FatigueLog(
        id = id,
        organizationId = organizationId,
        branchId = branchId,
        workerId = workerAlias,
        workerAlias = workerAlias,
        department = department,
        fatigueScore = fatigueScore,
        shiftHours = shiftHours,
        workIntensity = intensity,
        reportedSymptoms = Symptom.parseCsv(symptomsCsv),
        hydrated = hydrated,
        fatigueRiskScore = computedFatigueScore,
        syncStatus = SyncStatus.fromName(syncStatus),
        consentGiven = consentGiven,
        isSimulated = isSimulated,
        recordedAtMillis = recordedAt
    )
}

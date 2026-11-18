package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.domain.AlertActionStatus
import com.example.domain.RiskLevel
import com.example.domain.SubscriptionPlan
import com.example.domain.SyncStatus
import com.example.domain.WorkIntensity

@Database(
    entities = [
        OrganizationEntity::class,
        BranchEntity::class,
        HealthCheckInEntity::class,
        EnvironmentalReadingEntity::class,
        RiskAlertEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YaqeenDatabase : RoomDatabase() {
    abstract fun organizationDao(): OrganizationDao
    abstract fun branchDao(): BranchDao
    abstract fun healthCheckInDao(): HealthCheckInDao
    abstract fun environmentalReadingDao(): EnvironmentalReadingDao
    abstract fun riskAlertDao(): RiskAlertDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: YaqeenDatabase? = null

        fun getInstance(context: Context): YaqeenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    YaqeenDatabase::class.java,
                    "yaqeen_workforce_health.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

object DemoSeedData {
    const val DEFAULT_ORG_ID = "org_deccan_mfg"

    suspend fun seedIfEmpty(db: YaqeenDatabase) {
        if (db.organizationDao().count() > 0) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L

        val orgs = listOf(
            OrganizationEntity(
                id = "org_deccan_mfg",
                name = "Deccan Precision Manufacturing",
                industry = "Factory & Foundry",
                city = "Mahbubnagar, TS",
                subscriptionPlan = SubscriptionPlan.STARTER.name,
                isSimulated = true,
                createdAt = now - 72 * hour
            ),
            OrganizationEntity(
                id = "org_telangana_infra",
                name = "Telangana Infra Constructors",
                industry = "Construction & Civil",
                city = "Hyderabad, TS",
                subscriptionPlan = SubscriptionPlan.PROFESSIONAL.name,
                isSimulated = true,
                createdAt = now - 48 * hour
            ),
            OrganizationEntity(
                id = "org_vindhya_agro",
                name = "Vindhya Agro Logistics",
                industry = "Warehouse & Processing",
                city = "Warangal, TS",
                subscriptionPlan = SubscriptionPlan.FREE_PILOT.name,
                isSimulated = true,
                createdAt = now - 24 * hour
            )
        )

        val branches = listOf(
            BranchEntity(
                id = "br_mbnr_foundry",
                organizationId = "org_deccan_mfg",
                name = "Unit A · Forging & Foundry",
                location = "Jadcherla Industrial Park, Mahbubnagar",
                region = "Mahbubnagar",
                workerCount = 112,
                mapXFraction = 0.28f,
                mapYFraction = 0.68f,
                isSimulated = true
            ),
            BranchEntity(
                id = "br_mbnr_assembly",
                organizationId = "org_deccan_mfg",
                name = "Unit B · CNC Assembly",
                location = "Bhootpur Corridor, Mahbubnagar",
                region = "Mahbubnagar",
                workerCount = 84,
                mapXFraction = 0.36f,
                mapYFraction = 0.78f,
                isSimulated = true
            ),
            BranchEntity(
                id = "br_hyd_metro",
                organizationId = "org_telangana_infra",
                name = "Site 04 · Elevated Viaduct",
                location = "Uppal East Corridor, Hyderabad",
                region = "Hyderabad",
                workerCount = 164,
                mapXFraction = 0.56f,
                mapYFraction = 0.42f,
                isSimulated = true
            ),
            BranchEntity(
                id = "br_hyd_casting",
                organizationId = "org_telangana_infra",
                name = "Casting Yard · Patancheru",
                location = "Patancheru IDA, Hyderabad",
                region = "Hyderabad",
                workerCount = 96,
                mapXFraction = 0.44f,
                mapYFraction = 0.36f,
                isSimulated = true
            ),
            BranchEntity(
                id = "br_wgl_grain",
                organizationId = "org_vindhya_agro",
                name = "Cold & Dry Storage Hub",
                location = "Kazipet Logistics Zone, Warangal",
                region = "Warangal",
                workerCount = 48,
                mapXFraction = 0.76f,
                mapYFraction = 0.28f,
                isSimulated = true
            )
        )

        val readings = listOf(
            EnvironmentalReadingEntity(
                id = "env_1",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_foundry",
                branchName = "Unit A · Forging & Foundry",
                temperatureC = 39.2,
                humidityPercent = 68.0,
                wbgtC = 33.1,
                airQualityIndex = 162,
                sourceLabel = "Simulated Site Sensor (Demo)",
                isSimulated = true,
                recordedAt = now - hour
            ),
            EnvironmentalReadingEntity(
                id = "env_2",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_assembly",
                branchName = "Unit B · CNC Assembly",
                temperatureC = 31.4,
                humidityPercent = 49.0,
                wbgtC = 25.8,
                airQualityIndex = 94,
                sourceLabel = "Simulated Site Sensor (Demo)",
                isSimulated = true,
                recordedAt = now - 2 * hour
            ),
            EnvironmentalReadingEntity(
                id = "env_3",
                organizationId = "org_telangana_infra",
                branchId = "br_hyd_metro",
                branchName = "Site 04 · Elevated Viaduct",
                temperatureC = 38.6,
                humidityPercent = 54.0,
                wbgtC = null,
                airQualityIndex = 218,
                sourceLabel = "Simulated Roadside Station (Demo)",
                isSimulated = true,
                recordedAt = now - hour
            ),
            EnvironmentalReadingEntity(
                id = "env_4",
                organizationId = "org_vindhya_agro",
                branchId = "br_wgl_grain",
                branchName = "Cold & Dry Storage Hub",
                temperatureC = 28.5,
                humidityPercent = 52.0,
                wbgtC = 23.4,
                airQualityIndex = 76,
                sourceLabel = "Simulated Warehouse Node (Demo)",
                isSimulated = true,
                recordedAt = now - 3 * hour
            )
        )

        val checkIns = listOf(
            HealthCheckInEntity(
                id = "chk_1",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_foundry",
                branchName = "Unit A · Forging & Foundry",
                workerAlias = "Worker #104 (Demo)",
                department = "Furnace Line 2",
                fatigueScore = 8,
                shiftHours = 10,
                workIntensity = WorkIntensity.HEAVY.name,
                symptomsCsv = "DIZZINESS,EXCESSIVE_SWEATING",
                heatExposureScore = 9,
                hydrated = false,
                consentGiven = true,
                syncStatus = SyncStatus.SYNCED.name,
                isSimulated = true,
                recordedAt = now - 45 * 60_000L
            ),
            HealthCheckInEntity(
                id = "chk_2",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_foundry",
                branchName = "Unit A · Forging & Foundry",
                workerAlias = "Worker #118 (Demo)",
                department = "Press Shop",
                fatigueScore = 7,
                shiftHours = 9,
                workIntensity = WorkIntensity.MODERATE.name,
                symptomsCsv = "HEADACHE",
                heatExposureScore = 7,
                hydrated = true,
                consentGiven = false,
                syncStatus = SyncStatus.SYNCED.name,
                isSimulated = true,
                recordedAt = now - 90 * 60_000L
            ),
            HealthCheckInEntity(
                id = "chk_3",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_assembly",
                branchName = "Unit B · CNC Assembly",
                workerAlias = "Worker #209 (Demo)",
                department = "Quality Bay",
                fatigueScore = 3,
                shiftHours = 8,
                workIntensity = WorkIntensity.LIGHT.name,
                symptomsCsv = "",
                heatExposureScore = 3,
                hydrated = true,
                consentGiven = true,
                syncStatus = SyncStatus.SYNCED.name,
                isSimulated = true,
                recordedAt = now - 3 * hour
            ),
            HealthCheckInEntity(
                id = "chk_4",
                organizationId = "org_telangana_infra",
                branchId = "br_hyd_metro",
                branchName = "Site 04 · Elevated Viaduct",
                workerAlias = "Worker #412 (Demo)",
                department = "Pier Rigging",
                fatigueScore = 8,
                shiftHours = 10,
                workIntensity = WorkIntensity.HEAVY.name,
                symptomsCsv = "SHORTNESS_OF_BREATH,HEADACHE",
                heatExposureScore = 8,
                hydrated = true,
                consentGiven = true,
                syncStatus = SyncStatus.SYNCED.name,
                isSimulated = true,
                recordedAt = now - 50 * 60_000L
            ),
            HealthCheckInEntity(
                id = "chk_5",
                organizationId = "org_vindhya_agro",
                branchId = "br_wgl_grain",
                branchName = "Cold & Dry Storage Hub",
                workerAlias = "Worker #503 (Demo)",
                department = "Dispatch",
                fatigueScore = 4,
                shiftHours = 8,
                workIntensity = WorkIntensity.MODERATE.name,
                symptomsCsv = "",
                heatExposureScore = 3,
                hydrated = true,
                consentGiven = true,
                syncStatus = SyncStatus.SYNCED.name,
                isSimulated = true,
                recordedAt = now - 4 * hour
            )
        )

        val alerts = listOf(
            RiskAlertEntity(
                id = "alt_1",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_foundry",
                branchName = "Unit A · Forging & Foundry",
                riskCategory = "Heat & Thermal Exposure",
                riskLevel = RiskLevel.URGENT.name,
                heatScore = 90,
                fatigueScore = 78,
                symptomScore = 68,
                envScore = 62,
                overallScore = 78,
                explanation = "WBGT 33.1°C with heavy work exceeds urgent occupational heat guidance. High reported fatigue (8/10) over 10h heavy shift. 2 heat/fatigue-related symptoms reported in check-in (Source: Simulated Site Sensor (Demo)).",
                missingMeasurements = "",
                recommendedActions = "Enforce mandatory 15-min shaded cooling & ORS/water hydration cycles every hour.|Rotate heavy physical tasks during peak afternoon thermal window.|Arrange voluntary occupational health professional check-in (with worker consent).|Advisory notice: Safety officer review required; never auto-diagnoses or declares a worker unfit.",
                actionStatus = AlertActionStatus.OPEN.name,
                isSimulated = true,
                createdAt = now - 40 * 60_000L,
                updatedAt = now - 40 * 60_000L
            ),
            RiskAlertEntity(
                id = "alt_2",
                organizationId = "org_telangana_infra",
                branchId = "br_hyd_metro",
                branchName = "Site 04 · Elevated Viaduct",
                riskCategory = "Environmental Air Quality",
                riskLevel = RiskLevel.URGENT.name,
                heatScore = 76,
                fatigueScore = 74,
                symptomScore = 58,
                envScore = 85,
                overallScore = 74,
                explanation = "Poor air quality (AQI 218) increases respiratory & heat-recovery burden. Air temp 38.6°C and 54% RH indicate elevated heat exposure (Source: Simulated Roadside Station (Demo)).",
                missingMeasurements = "WBGT sensor reading (estimated conservatively from air temp + relative humidity; do not equate air temp with WBGT)",
                recommendedActions = "Provide certified dust/particulate respirators and increase site ventilation.|Enforce mandatory 15-min shaded cooling & ORS/water hydration cycles every hour.|Arrange voluntary occupational health professional check-in (with worker consent).|Advisory notice: Safety officer review required; never auto-diagnoses or declares a worker unfit.",
                actionStatus = AlertActionStatus.ACKNOWLEDGED.name,
                reviewedByRole = "Safety Officer",
                actionNote = "Distributed N95 masks & shifted rigging breaks to shaded tent.",
                isSimulated = true,
                createdAt = now - 55 * 60_000L,
                updatedAt = now - 20 * 60_000L
            ),
            RiskAlertEntity(
                id = "alt_3",
                organizationId = "org_deccan_mfg",
                branchId = "br_mbnr_assembly",
                branchName = "Unit B · CNC Assembly",
                riskCategory = "Routine Safety Monitoring",
                riskLevel = RiskLevel.NORMAL.name,
                heatScore = 24,
                fatigueScore = 24,
                symptomScore = 10,
                envScore = 20,
                overallScore = 21,
                explanation = "All measured workplace indicators at Unit B · CNC Assembly are within routine monitoring range (Source: Simulated Site Sensor (Demo)).",
                missingMeasurements = "",
                recommendedActions = "Continue standard hydration reminders and routine shift check-ins.|Advisory notice: Safety officer review required; never auto-diagnoses or declares a worker unfit.",
                actionStatus = AlertActionStatus.RESOLVED.name,
                reviewedByRole = "Safety Officer",
                actionNote = "Verified chilled drinking water station operational.",
                isSimulated = true,
                createdAt = now - 3 * hour,
                updatedAt = now - 2 * hour
            )
        )

        val logs = listOf(
            AuditLogEntity(
                id = "aud_1",
                organizationId = "org_deccan_mfg",
                actorRole = "System Engine",
                action = "SIMULATED_ALERT_CREATED",
                details = "Generated Urgent Heat & Thermal Exposure alert for Unit A · Forging & Foundry (WBGT 33.1°C).",
                timestamp = now - 40 * 60_000L
            ),
            AuditLogEntity(
                id = "aud_2",
                organizationId = "org_telangana_infra",
                actorRole = "Safety Officer",
                action = "ALERT_ACKNOWLEDGED",
                details = "Site 04 AQI 218 alert acknowledged; respiratory PPE & shaded hydration breaks logged.",
                timestamp = now - 20 * 60_000L
            )
        )

        db.organizationDao().insertAll(orgs)
        db.branchDao().insertAll(branches)
        db.environmentalReadingDao().insertAll(readings)
        db.healthCheckInDao().insertAll(checkIns)
        db.riskAlertDao().insertAll(alerts)
        db.auditLogDao().insertAll(logs)
    }
}

package com.example

import com.example.domain.EarlyWarningEngine
import com.example.domain.RiskAssessmentInput
import com.example.domain.RiskLevel
import com.example.domain.WorkIntensity
import com.example.domain.toHeatMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EarlyWarningEngineTest {

    @Test
    fun urgentAlertWhenWbgtAndFatigueAreHigh() {
        val result = EarlyWarningEngine.evaluate(
            RiskAssessmentInput(
                organizationId = "org_1",
                branchId = "br_1",
                branchName = "Forging Unit A",
                temperatureC = 39.2,
                humidityPercent = 68.0,
                wbgtC = 33.1,
                airQualityIndex = 160,
                fatigueScore = 8,
                shiftHours = 10,
                workIntensity = WorkIntensity.HEAVY,
                symptomCount = 2,
                branchSymptomaticCountLast24h = 3,
                hydrated = false,
                dataSource = "Simulated Site Sensor",
                timestampMillis = 1_700_000_000_000L
            )
        )
        assertEquals(RiskLevel.URGENT, result.riskLevel)
        assertTrue(result.missingMeasurements.isEmpty())
        assertTrue(result.recommendedActions.any { it.contains("never auto-diagnoses") })
    }

    @Test
    fun flagsMissingWbgtAndMissingAqiExplicitly() {
        val result = EarlyWarningEngine.evaluate(
            RiskAssessmentInput(
                organizationId = "org_1",
                branchId = "br_2",
                branchName = "Site 04",
                temperatureC = 38.0,
                humidityPercent = 58.0,
                wbgtC = null,
                airQualityIndex = null,
                fatigueScore = 4,
                shiftHours = 8,
                workIntensity = WorkIntensity.MODERATE,
                symptomCount = 0,
                branchSymptomaticCountLast24h = 0,
                hydrated = true,
                dataSource = "Simulated Roadside",
                timestampMillis = 1_700_000_000_000L
            )
        )
        assertTrue(result.missingMeasurements.any { it.contains("WBGT") })
        assertTrue(result.missingMeasurements.any { it.contains("AQI") })
    }

    @Test
    fun mapsEnvironmentalReadingToHeatMetricWithMissingSensorNote() {
        val entity = com.example.data.local.EnvironmentalReadingEntity(
            id = "env_t",
            organizationId = "org_1",
            branchId = "br_1",
            branchName = "Site 04",
            temperatureC = 38.6,
            humidityPercent = 54.0,
            wbgtC = null,
            airQualityIndex = 218,
            sourceLabel = "Simulated",
            recordedAt = 1_700_000_000_000L
        )
        val metric = entity.toHeatMetric()
        assertEquals(false, metric.hasWbgtSensor)
        assertTrue(metric.missingSensorNote != null)
        assertTrue(metric.heatRiskScore >= 60)
    }

    @Test
    fun demoAuthCatalogAuthenticatesValidAndRejectsInvalidCredentials() {
        val admin = com.example.domain.DemoAuthCatalog.authenticate("admin", "password123")
        assertEquals(com.example.domain.UserRole.ORG_ADMIN, admin?.role)
        val valid = com.example.domain.DemoAuthCatalog.authenticate("safety@yaqeen.ai", "Yaqeen@2026")
        assertEquals(com.example.domain.UserRole.SAFETY_OFFICER, valid?.role)
        val invalid = com.example.domain.DemoAuthCatalog.authenticate("admin", "wrong")
        assertEquals(null, invalid)
    }
}

package com.example.domain

import kotlin.math.roundToInt

data class RiskAssessmentInput(
    val organizationId: String,
    val branchId: String,
    val branchName: String,
    val temperatureC: Double?,
    val humidityPercent: Double?,
    val wbgtC: Double?,
    val airQualityIndex: Int?,
    val fatigueScore: Int?,
    val shiftHours: Int?,
    val workIntensity: WorkIntensity,
    val symptomCount: Int,
    val branchSymptomaticCountLast24h: Int,
    val hydrated: Boolean,
    val dataSource: String,
    val timestampMillis: Long
)

data class ExplainableRiskAssessment(
    val riskCategory: String,
    val riskLevel: RiskLevel,
    val heatScore: Int,
    val fatigueScore: Int,
    val symptomScore: Int,
    val envScore: Int,
    val overallScore: Int,
    val explanation: String,
    val missingMeasurements: List<String>,
    val recommendedActions: List<String>
)

/**
 * Transparent, auditable Early Warning Intelligence Engine.
 * Never diagnoses medical conditions or declares any worker medically unfit.
 * Uses WBGT when available and explicitly flags missing sensor measurements.
 */
object EarlyWarningEngine {

    fun evaluate(input: RiskAssessmentInput): ExplainableRiskAssessment {
        val missing = mutableListOf<String>()
        val reasons = mutableListOf<String>()
        val actions = mutableListOf<String>()

        // 1. Heat Risk Scoring (prefers WBGT; never silently treats air temp as WBGT)
        val heatScore: Int = when {
            input.wbgtC != null -> {
                val intensityBias = when (input.workIntensity) {
                    WorkIntensity.HEAVY -> 2.0
                    WorkIntensity.MODERATE -> 0.8
                    WorkIntensity.LIGHT -> 0.0
                }
                val effectiveWbgt = input.wbgtC + intensityBias
                when {
                    effectiveWbgt >= 32.5 -> {
                        reasons.add(
                            "WBGT ${format1(input.wbgtC)}°C with ${input.workIntensity.label.lowercase()} work exceeds urgent occupational heat guidance."
                        )
                        90
                    }
                    effectiveWbgt >= 29.5 -> {
                        reasons.add(
                            "WBGT ${format1(input.wbgtC)}°C indicates elevated thermal strain risk."
                        )
                        66
                    }
                    else -> 24
                }
            }
            input.temperatureC != null && input.humidityPercent != null -> {
                missing.add("WBGT sensor reading (estimated conservatively from air temp + relative humidity; do not equate air temp with WBGT)")
                val apparentHeatIndex = input.temperatureC + (0.12 * (input.humidityPercent - 40.0)).coerceAtLeast(0.0)
                when {
                    apparentHeatIndex >= 40.0 -> {
                        reasons.add(
                            "Air temp ${format1(input.temperatureC)}°C at ${input.humidityPercent.roundToInt()}% RH yields high apparent heat stress."
                        )
                        82
                    }
                    apparentHeatIndex >= 35.0 -> {
                        reasons.add(
                            "Air temp ${format1(input.temperatureC)}°C and ${input.humidityPercent.roundToInt()}% RH indicate elevated heat exposure."
                        )
                        60
                    }
                    else -> 22
                }
            }
            else -> {
                missing.add("WBGT / Ambient Temperature & Humidity sensor telemetry")
                20
            }
        }

        // 2. Fatigue Risk Scoring
        val fatigueScore: Int = if (input.fatigueScore != null) {
            val base = (input.fatigueScore * 8).coerceIn(8, 80)
            val shiftPenalty = if ((input.shiftHours ?: 8) > 9) 14 else 0
            val hydrationPenalty = if (!input.hydrated) 10 else 0
            val combined = (base + shiftPenalty + hydrationPenalty + input.workIntensity.weight / 3).coerceIn(0, 100)
            if (combined >= 70) {
                reasons.add("High reported fatigue (${input.fatigueScore}/10) over ${input.shiftHours ?: 8}h ${input.workIntensity.label.lowercase()} shift.")
            } else if (combined >= 50) {
                reasons.add("Moderate-to-elevated fatigue score (${input.fatigueScore}/10).")
            }
            combined
        } else {
            missing.add("Worker fatigue check-in score")
            15
        }

        // 3. Symptom Trend Risk Scoring
        val symptomScore = ((input.symptomCount * 24) + (input.branchSymptomaticCountLast24h * 10)).coerceIn(0, 100)
        if (input.symptomCount >= 2) {
            reasons.add("${input.symptomCount} heat/fatigue-related symptoms reported in check-in.")
        }
        if (input.branchSymptomaticCountLast24h >= 3) {
            reasons.add("Cluster signal: ${input.branchSymptomaticCountLast24h} workers at ${input.branchName} reported symptoms in the last 24h.")
        }

        // 4. Environmental / AQI Scoring
        val envScore: Int = if (input.airQualityIndex != null) {
            when {
                input.airQualityIndex >= 200 -> {
                    reasons.add("Poor air quality (AQI ${input.airQualityIndex}) increases respiratory & heat-recovery burden.")
                    85
                }
                input.airQualityIndex >= 150 -> {
                    reasons.add("Elevated air quality index (AQI ${input.airQualityIndex}) detected on site.")
                    62
                }
                else -> 20
            }
        } else {
            missing.add("Site Air Quality Index (AQI) monitor")
            15
        }

        val overallScore = (
            heatScore * 0.35 +
                fatigueScore * 0.28 +
                symptomScore * 0.22 +
                envScore * 0.15
            ).roundToInt().coerceIn(0, 100)

        val peakSignal = maxOf(heatScore, fatigueScore, symptomScore, envScore)
        val riskLevel = when {
            overallScore >= 70 || peakSignal >= 85 -> RiskLevel.URGENT
            overallScore >= 46 || peakSignal >= 60 -> RiskLevel.ELEVATED
            else -> RiskLevel.NORMAL
        }

        val riskCategory = when {
            heatScore >= fatigueScore && heatScore >= envScore && heatScore >= 55 -> "Heat & Thermal Exposure"
            envScore >= heatScore && envScore >= fatigueScore && envScore >= 55 -> "Environmental Air Quality"
            symptomScore >= 55 -> "Symptom Cluster Trend"
            fatigueScore >= 55 -> "Shift Fatigue Load"
            else -> "Routine Safety Monitoring"
        }

        if (heatScore >= 58) {
            actions.add("Enforce mandatory 15-min shaded cooling & ORS/water hydration cycles every hour.")
            actions.add("Rotate heavy physical tasks during peak afternoon thermal window.")
        }
        if (fatigueScore >= 55) {
            actions.add("Review shift scheduling and provide scheduled recovery rest breaks.")
        }
        if (envScore >= 60) {
            actions.add("Provide certified dust/particulate respirators and increase site ventilation.")
        }
        if (symptomScore >= 48 || riskLevel == RiskLevel.URGENT) {
            actions.add("Arrange voluntary occupational health professional check-in (with worker consent).")
        }
        if (actions.isEmpty()) {
            actions.add("Continue standard hydration reminders and routine shift check-ins.")
        }
        actions.add("Advisory notice: Safety officer review required; never auto-diagnoses or declares a worker unfit.")

        val explanationText = if (reasons.isEmpty()) {
            "All measured workplace indicators at ${input.branchName} are within routine monitoring range (Source: ${input.dataSource})."
        } else {
            reasons.joinToString(" ") + " (Source: ${input.dataSource})."
        }

        return ExplainableRiskAssessment(
            riskCategory = riskCategory,
            riskLevel = riskLevel,
            heatScore = heatScore,
            fatigueScore = fatigueScore,
            symptomScore = symptomScore,
            envScore = envScore,
            overallScore = overallScore,
            explanation = explanationText,
            missingMeasurements = missing,
            recommendedActions = actions
        )
    }

    private fun format1(value: Double): String = ((value * 10.0).roundToInt() / 10.0).toString()
}

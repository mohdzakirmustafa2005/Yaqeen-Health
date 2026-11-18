package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val industry: String,
    val city: String,
    val country: String = "India",
    val subscriptionPlan: String,
    val isSuspended: Boolean = false,
    val isSimulated: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String,
    val location: String,
    val region: String,
    val workerCount: Int,
    val mapXFraction: Float,
    val mapYFraction: Float,
    val isSimulated: Boolean = true
)

@Entity(tableName = "health_checkins")
data class HealthCheckInEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val branchId: String,
    val branchName: String,
    val workerAlias: String,
    val department: String,
    val fatigueScore: Int,
    val shiftHours: Int,
    val workIntensity: String,
    val symptomsCsv: String,
    val heatExposureScore: Int,
    val hydrated: Boolean,
    val consentGiven: Boolean,
    val syncStatus: String,
    val isSimulated: Boolean = true,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "environmental_readings")
data class EnvironmentalReadingEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val branchId: String,
    val branchName: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val wbgtC: Double?,
    val airQualityIndex: Int?,
    val sourceLabel: String,
    val isSimulated: Boolean = true,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "risk_alerts")
data class RiskAlertEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val branchId: String,
    val branchName: String,
    val riskCategory: String,
    val riskLevel: String,
    val heatScore: Int,
    val fatigueScore: Int,
    val symptomScore: Int,
    val envScore: Int,
    val overallScore: Int,
    val explanation: String,
    val missingMeasurements: String,
    val recommendedActions: String,
    val actionStatus: String,
    val reviewedByRole: String? = null,
    val actionNote: String? = null,
    val isSimulated: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val actorRole: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

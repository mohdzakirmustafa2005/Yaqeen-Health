package com.example.domain

enum class UserRole(
    val displayName: String,
    val scopeDescription: String,
    val canViewIndividualSymptomsWithConsent: Boolean,
    val canReviewAlerts: Boolean,
    val canLogEnvironment: Boolean,
    val canManageOrg: Boolean,
    val canViewSuperAdmin: Boolean,
    val canViewPopulationIntelligence: Boolean
) {
    WORKER(
        displayName = "Worker",
        scopeDescription = "Own health check-ins, offline queue, personal alerts & DPDP consent",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = false,
        canLogEnvironment = false,
        canManageOrg = false,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = false
    ),
    SAFETY_OFFICER(
        displayName = "Safety Officer",
        scopeDescription = "Environmental monitoring, alert review & preventive action logging",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = true,
        canLogEnvironment = true,
        canManageOrg = false,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = true
    ),
    OCCUPATIONAL_HEALTH_PRO(
        displayName = "Occupational Health Pro",
        scopeDescription = "Authorized consented health review & preventive referrals",
        canViewIndividualSymptomsWithConsent = true,
        canReviewAlerts = true,
        canLogEnvironment = false,
        canManageOrg = false,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = true
    ),
    HR_MANAGER(
        displayName = "HR Manager",
        scopeDescription = "Workforce administration & aggregate branch trends (no medical PII)",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = false,
        canLogEnvironment = false,
        canManageOrg = true,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = false
    ),
    ORG_ADMIN(
        displayName = "Organization Admin",
        scopeDescription = "Branch management, telemetry & aggregate risk overview",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = true,
        canLogEnvironment = true,
        canManageOrg = true,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = true
    ),
    ORG_OWNER(
        displayName = "Organization Owner",
        scopeDescription = "Organization settings, subscription plan & aggregate safety",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = true,
        canLogEnvironment = true,
        canManageOrg = true,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = true
    ),
    SUPER_ADMIN(
        displayName = "Super Admin",
        scopeDescription = "Multi-tenant SaaS console, tenant lifecycle & platform audit",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = true,
        canLogEnvironment = true,
        canManageOrg = true,
        canViewSuperAdmin = true,
        canViewPopulationIntelligence = true
    ),
    PUBLIC_HEALTH_AUTHORITY(
        displayName = "Public Health Authority",
        scopeDescription = "De-identified regional trends, environmental maps & CSV exports",
        canViewIndividualSymptomsWithConsent = false,
        canReviewAlerts = false,
        canLogEnvironment = false,
        canManageOrg = false,
        canViewSuperAdmin = false,
        canViewPopulationIntelligence = true
    );

    companion object {
        fun fromName(name: String?): UserRole =
            entries.firstOrNull { it.name == name } ?: SAFETY_OFFICER
    }
}

enum class RiskLevel(val label: String, val priority: Int) {
    NORMAL("Normal", 0),
    ELEVATED("Elevated", 1),
    URGENT("Urgent", 2);

    companion object {
        fun fromName(name: String?): RiskLevel =
            entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

enum class AlertActionStatus(val label: String) {
    OPEN("Open"),
    ACKNOWLEDGED("Acknowledged"),
    PREVENTIVE_ACTION_TAKEN("Action Taken"),
    RESOLVED("Resolved");

    companion object {
        fun fromName(name: String?): AlertActionStatus =
            entries.firstOrNull { it.name == name } ?: OPEN
    }
}

enum class SubscriptionPlan(val label: String, val priceInr: String, val limitSummary: String) {
    FREE_PILOT("Free Pilot", "₹0", "Up to 25 workers · 1 branch"),
    STARTER("Starter", "₹999/mo", "Up to 150 workers · 3 branches"),
    PROFESSIONAL("Professional", "₹3,999/mo", "Up to 750 workers · 10 branches"),
    BUSINESS("Business", "₹9,999/mo", "Up to 2,500 workers · Multi-site"),
    ENTERPRISE("Enterprise", "Custom", "Unlimited sites · API & Govt SLA");

    companion object {
        fun fromName(name: String?): SubscriptionPlan =
            entries.firstOrNull { it.name == name } ?: FREE_PILOT
    }
}

enum class WorkIntensity(val label: String, val weight: Int) {
    LIGHT("Light", 0),
    MODERATE("Moderate", 10),
    HEAVY("Heavy", 22);

    companion object {
        fun fromName(name: String?): WorkIntensity =
            entries.firstOrNull { it.name == name } ?: MODERATE
    }
}

enum class SyncStatus(val label: String) {
    SYNCED("Synced"),
    PENDING_SYNC("Pending Offline Sync");

    companion object {
        fun fromName(name: String?): SyncStatus =
            entries.firstOrNull { it.name == name } ?: SYNCED
    }
}

enum class Symptom(val key: String) {
    DIZZINESS("Dizziness"),
    HEADACHE("Headache"),
    MUSCLE_CRAMPS("Muscle Cramps"),
    EXCESSIVE_SWEATING("Heavy Sweating"),
    SHORTNESS_OF_BREATH("Shortness of Breath"),
    NAUSEA("Nausea");

    companion object {
        fun parseCsv(csv: String): List<Symptom> =
            csv.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { token -> entries.firstOrNull { it.name == token || it.key == token } }
    }
}

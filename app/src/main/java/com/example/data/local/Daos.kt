package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<OrganizationEntity>>

    @Query("SELECT COUNT(*) FROM organizations")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OrganizationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OrganizationEntity)

    @Query("UPDATE organizations SET isSuspended = :suspended WHERE id = :orgId")
    suspend fun setSuspended(orgId: String, suspended: Boolean)

    @Query("UPDATE organizations SET subscriptionPlan = :plan WHERE id = :orgId")
    suspend fun updatePlan(orgId: String, plan: String)
}

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches ORDER BY name ASC")
    fun observeAll(): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches WHERE organizationId = :orgId ORDER BY name ASC")
    fun observeByOrg(orgId: String): Flow<List<BranchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BranchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BranchEntity)
}

@Dao
interface HealthCheckInDao {
    @Query("SELECT * FROM health_checkins ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<HealthCheckInEntity>>

    @Query("SELECT * FROM health_checkins WHERE organizationId = :orgId ORDER BY recordedAt DESC")
    fun observeByOrg(orgId: String): Flow<List<HealthCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HealthCheckInEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HealthCheckInEntity)

    @Query("SELECT * FROM health_checkins WHERE organizationId = :orgId AND syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSync(orgId: String): List<HealthCheckInEntity>

    @Query("UPDATE health_checkins SET syncStatus = 'SYNCED' WHERE organizationId = :orgId AND syncStatus = 'PENDING_SYNC'")
    suspend fun markAllSynced(orgId: String)

    @Query("UPDATE health_checkins SET consentGiven = :consent WHERE organizationId = :orgId AND workerAlias = :workerAlias")
    suspend fun updateWorkerConsent(orgId: String, workerAlias: String, consent: Boolean)

    @Query("DELETE FROM health_checkins WHERE organizationId = :orgId AND workerAlias = :workerAlias")
    suspend fun deleteWorkerCheckIns(orgId: String, workerAlias: String)
}

@Dao
interface EnvironmentalReadingDao {
    @Query("SELECT * FROM environmental_readings ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<EnvironmentalReadingEntity>>

    @Query("SELECT * FROM environmental_readings WHERE organizationId = :orgId ORDER BY recordedAt DESC")
    fun observeByOrg(orgId: String): Flow<List<EnvironmentalReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EnvironmentalReadingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: EnvironmentalReadingEntity)
}

@Dao
interface RiskAlertDao {
    @Query("SELECT * FROM risk_alerts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RiskAlertEntity>>

    @Query("SELECT * FROM risk_alerts WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun observeByOrg(orgId: String): Flow<List<RiskAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RiskAlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RiskAlertEntity)

    @Query(
        "UPDATE risk_alerts SET actionStatus = :status, reviewedByRole = :role, actionNote = :note, updatedAt = :updatedAt WHERE id = :alertId"
    )
    suspend fun updateAlertAction(
        alertId: String,
        status: String,
        role: String,
        note: String,
        updatedAt: Long
    )
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeAll(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE organizationId = :orgId ORDER BY timestamp DESC LIMIT 100")
    fun observeByOrg(orgId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AuditLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AuditLogEntity)
}

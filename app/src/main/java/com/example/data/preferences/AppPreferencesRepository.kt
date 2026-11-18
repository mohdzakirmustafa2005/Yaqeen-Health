package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.DemoSeedData
import com.example.domain.UserRole
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "yaqeen_preferences")

data class UserSessionPreferences(
    val language: AppLanguage = AppLanguage.EN,
    val activeOrgId: String = DemoSeedData.DEFAULT_ORG_ID,
    val activeRole: UserRole = UserRole.SAFETY_OFFICER,
    val workerConsentGiven: Boolean = true,
    val offlineFieldMode: Boolean = false,
    val isSignedIn: Boolean = false,
    val signedInUserEmail: String = ""
)

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val ACTIVE_ORG_ID = stringPreferencesKey("active_org_id")
        val ACTIVE_ROLE = stringPreferencesKey("active_role")
        val WORKER_CONSENT = booleanPreferencesKey("worker_consent_given")
        val OFFLINE_MODE = booleanPreferencesKey("offline_field_mode")
        val IS_SIGNED_IN = booleanPreferencesKey("is_signed_in")
        val SIGNED_IN_EMAIL = stringPreferencesKey("signed_in_email")
    }

    val preferencesFlow: Flow<UserSessionPreferences> = context.dataStore.data.map { prefs ->
        UserSessionPreferences(
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            activeOrgId = prefs[Keys.ACTIVE_ORG_ID] ?: DemoSeedData.DEFAULT_ORG_ID,
            activeRole = UserRole.fromName(prefs[Keys.ACTIVE_ROLE]),
            workerConsentGiven = prefs[Keys.WORKER_CONSENT] ?: true,
            offlineFieldMode = prefs[Keys.OFFLINE_MODE] ?: false,
            isSignedIn = prefs[Keys.IS_SIGNED_IN] ?: false,
            signedInUserEmail = prefs[Keys.SIGNED_IN_EMAIL] ?: ""
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setActiveOrg(orgId: String) {
        context.dataStore.edit { it[Keys.ACTIVE_ORG_ID] = orgId }
    }

    suspend fun setActiveRole(role: UserRole) {
        context.dataStore.edit { it[Keys.ACTIVE_ROLE] = role.name }
    }

    suspend fun setWorkerConsent(consent: Boolean) {
        context.dataStore.edit { it[Keys.WORKER_CONSENT] = consent }
    }

    suspend fun setOfflineFieldMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OFFLINE_MODE] = enabled }
    }

    suspend fun setSignedInSession(email: String, role: UserRole) {
        context.dataStore.edit {
            it[Keys.IS_SIGNED_IN] = true
            it[Keys.SIGNED_IN_EMAIL] = email
            it[Keys.ACTIVE_ROLE] = role.name
        }
    }

    suspend fun signOut() {
        context.dataStore.edit {
            it[Keys.IS_SIGNED_IN] = false
            it[Keys.SIGNED_IN_EMAIL] = ""
        }
    }
}

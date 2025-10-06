package com.virtualsandbox.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "virtual_sandbox_prefs"

val Context.sandboxDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_NAME
)

@Singleton
class SandboxPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val virtualizationEnabledKey = booleanPreferencesKey("virtualization_enabled")
    private val biometricUnlockKey = booleanPreferencesKey("biometric_unlock")
    private val defaultNetworkIsolationKey = booleanPreferencesKey("default_network_isolation")

    val preferences: Flow<SandboxPreferences> = context.sandboxDataStore.data.map { prefs ->
        SandboxPreferences(
            virtualizationEnabled = prefs[virtualizationEnabledKey] ?: true,
            biometricUnlockEnabled = prefs[biometricUnlockKey] ?: false,
            defaultNetworkIsolation = prefs[defaultNetworkIsolationKey] ?: true,
        )
    }

    suspend fun updateVirtualizationEnabled(enabled: Boolean) {
        context.sandboxDataStore.edit { prefs ->
            prefs[virtualizationEnabledKey] = enabled
        }
    }

    suspend fun updateBiometricUnlock(enabled: Boolean) {
        context.sandboxDataStore.edit { prefs ->
            prefs[biometricUnlockKey] = enabled
        }
    }

    suspend fun updateDefaultNetworkIsolation(enabled: Boolean) {
        context.sandboxDataStore.edit { prefs ->
            prefs[defaultNetworkIsolationKey] = enabled
        }
    }
}

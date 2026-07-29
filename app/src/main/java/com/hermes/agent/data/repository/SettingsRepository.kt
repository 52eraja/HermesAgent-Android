package com.hermes.agent.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.hermes.agent.network.ApiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore preference keys */
private object SettingsKeys {
    val SERVER_URL = stringPreferencesKey("server_url")
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")
    val TOKEN = stringPreferencesKey("token")
    val DEFAULT_MODEL = stringPreferencesKey("default_model")
    val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
}

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(
    name = "hermes_agent_settings"
)

/**
 * Persists and retrieves app settings using Jetpack DataStore.
 */
class SettingsRepository(private val context: Context) {

    /** Observable settings as ApiConfig */
    val settingsFlow: Flow<ApiConfig> = context.settingsStore.data.map { prefs ->
        ApiConfig(
            serverUrl = prefs[SettingsKeys.SERVER_URL] ?: ApiConfig.DEFAULT_SERVER_URL,
            username = prefs[SettingsKeys.USERNAME] ?: "",
            password = prefs[SettingsKeys.PASSWORD] ?: "",
            token = prefs[SettingsKeys.TOKEN] ?: "",
            defaultModel = prefs[SettingsKeys.DEFAULT_MODEL] ?: ApiConfig.DEFAULT_MODEL
        )
    }

    /** Dark mode preference: "system", "light", "dark" */
    val darkModeFlow: Flow<String> = context.settingsStore.data.map { prefs ->
        prefs[SettingsKeys.DARK_MODE] ?: "system"
    }

    /** Save the full API config */
    suspend fun saveConfig(config: ApiConfig) {
        context.settingsStore.edit { prefs ->
            prefs[SettingsKeys.SERVER_URL] = config.serverUrl
            prefs[SettingsKeys.USERNAME] = config.username
            prefs[SettingsKeys.PASSWORD] = config.password
            prefs[SettingsKeys.TOKEN] = config.token
            prefs[SettingsKeys.DEFAULT_MODEL] = config.defaultModel
        }
    }

    /** Save dark mode preference */
    suspend fun setDarkMode(mode: String) {
        context.settingsStore.edit { prefs ->
            prefs[SettingsKeys.DARK_MODE] = mode
        }
    }

    /** Clear all sensitive auth data */
    suspend fun clearAuth() {
        context.settingsStore.edit { prefs ->
            prefs.remove(SettingsKeys.USERNAME)
            prefs.remove(SettingsKeys.PASSWORD)
            prefs.remove(SettingsKeys.TOKEN)
        }
    }

    /** Clear all settings */
    suspend fun clearAll() {
        context.settingsStore.edit { it.clear() }
    }
}

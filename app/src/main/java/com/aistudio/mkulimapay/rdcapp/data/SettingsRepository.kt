package com.aistudio.mkulimapay.rdcapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.AppSettings
import com.aistudio.mkulimapay.rdcapp.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.mkulimaSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mkulima_pay_settings"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
        val LARGE_TEXT = booleanPreferencesKey("large_text")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settingsFlow: Flow<AppSettings> = context.mkulimaSettingsDataStore.data.map { prefs ->
        AppSettings(
            language = prefs[Keys.LANGUAGE]?.let { raw ->
                AppLanguage.entries.firstOrNull { it.name == raw }
            } ?: AppLanguage.FR,
            themePreference = prefs[Keys.THEME]?.let { raw ->
                ThemePreference.entries.firstOrNull { it.name == raw }
            } ?: ThemePreference.AUTO,
            largeText = prefs[Keys.LARGE_TEXT] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = settingsFlow.first()
        val updated = transform(current)
        context.mkulimaSettingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = updated.language.name
            prefs[Keys.THEME] = updated.themePreference.name
            prefs[Keys.LARGE_TEXT] = updated.largeText
            prefs[Keys.NOTIFICATIONS] = updated.notificationsEnabled
            prefs[Keys.ONBOARDING_COMPLETED] = updated.onboardingCompleted
        }
    }
}

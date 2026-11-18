package com.aistudio.mkulimapay.rdcapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.mkulimapay.rdcapp.data.SettingsRepository
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.AppSettings
import com.aistudio.mkulimapay.rdcapp.model.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun toggleLanguage() {
        viewModelScope.launch {
            settingsRepository.updateSettings {
                val next = if (it.language == AppLanguage.FR) AppLanguage.SW else AppLanguage.FR
                it.copy(language = next)
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(language = lang) }
        }
    }

    fun setThemePreference(pref: ThemePreference) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(themePreference = pref) }
        }
    }

    fun setLargeText(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(largeText = enabled) }
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(onboardingCompleted = true) }
        }
    }
}

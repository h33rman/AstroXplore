package com.example.astroxplore.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.database.SettingsRepository
import com.example.astroxplore.core.database.ThemeMode
import com.example.astroxplore.features.auth.data.AuthRepository
import com.example.astroxplore.features.profile.data.ProfileRepository
import com.example.astroxplore.features.profile.model.ProfileModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<ProfileModel?>(null)
    private val _userInterests = MutableStateFlow<List<String>>(emptyList())
    private val _availableKeywords = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(
            settingsRepository.themeMode,
            settingsRepository.dynamicColorEnabled
        ) { themeMode, dynamicColor ->
            Pair(themeMode, dynamicColor)
        },
        _userProfile,
        _userInterests,
        _availableKeywords
    ) { settings, profile, interests, keywords ->
        ProfileUiState(
            themeMode = settings.first,
            dynamicColorEnabled = settings.second,
            profile = profile,
            interests = interests,
            availableKeywords = keywords
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                // Reactive local profile observation
                launch {
                    profileRepository.getLocalProfile(user.id).collectLatest { profile ->
                        _userProfile.value = profile
                    }
                }
                
                // Reactive local interests observation
                launch {
                    profileRepository.getLocalUserPreferences().collectLatest { prefs ->
                        _userInterests.value = prefs
                    }
                }

                // Background sync
                launch {
                    profileRepository.syncProfile(user.id)
                    profileRepository.getUserPreferences(user.id)
                }
            }
            
            profileRepository.getLocalKeywords().collectLatest {
                if (it.isEmpty()) {
                    profileRepository.syncAvailableKeywords()
                } else {
                    _availableKeywords.value = it
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColor(enabled)
        }
    }

    fun updateInterests(interests: List<String>) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            _userInterests.value = interests
            profileRepository.syncUserPreferences(user.id, interests)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

data class ProfileUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val profile: ProfileModel? = null,
    val interests: List<String> = emptyList(),
    val availableKeywords: List<String> = emptyList()
)

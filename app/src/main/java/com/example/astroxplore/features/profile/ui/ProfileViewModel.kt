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
        settingsRepository.themeMode,
        settingsRepository.dynamicColorEnabled,
        settingsRepository.language,
        _userProfile,
        _userInterests,
        _availableKeywords
    ) { args ->
        ProfileUiState(
            themeMode = args[0] as ThemeMode,
            dynamicColorEnabled = args[1] as Boolean,
            language = args[2] as String,
            profile = args[3] as? ProfileModel,
            interests = args[4] as List<String>,
            availableKeywords = args[5] as List<String>
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
                _userProfile.value = profileRepository.getProfile(user.id)
                _userInterests.value = profileRepository.getUserPreferences(user.id)
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

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(languageCode)
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
    val language: String = "en",
    val profile: ProfileModel? = null,
    val interests: List<String> = emptyList(),
    val availableKeywords: List<String> = emptyList()
)

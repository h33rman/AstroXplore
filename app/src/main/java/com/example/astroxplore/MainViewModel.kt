package com.example.astroxplore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.database.SettingsRepository
import com.example.astroxplore.core.util.NavigationSignal
import com.example.astroxplore.features.auth.data.AuthRepository
import com.example.astroxplore.features.profile.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val navigationSignal: NavigationSignal
) : ViewModel() {
    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing
        )

    private val _isOnboarded = MutableStateFlow<Boolean?>(null)
    val isOnboarded: StateFlow<Boolean?> = _isOnboarded.asStateFlow()

    val scrollToTopEvent = navigationSignal.scrollToTop

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    // Fast path: check local settings first
                    val isLocallyOnboarded = settingsRepository.onboardingComplete.first()
                    if (isLocallyOnboarded) {
                        _isOnboarded.value = true
                    } else {
                        // Reliable path: fetch from DB
                        val profile = profileRepository.getProfile(authRepository.currentUser?.id ?: "")
                        _isOnboarded.value = profile?.isOnboarded ?: false
                        
                        // Sync back to local if true
                        if (_isOnboarded.value == true) {
                            settingsRepository.setOnboardingComplete(true)
                        }
                    }
                } else {
                    _isOnboarded.value = null
                }
            }
        }
    }

    fun triggerScrollToTop() {
        navigationSignal.onScrollToTop()
    }
}

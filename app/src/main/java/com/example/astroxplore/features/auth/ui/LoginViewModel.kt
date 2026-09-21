package com.example.astroxplore.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.R
import com.example.astroxplore.core.network.NetworkConnectivityObserver
import com.example.astroxplore.core.util.ErrorMapper
import com.example.astroxplore.features.auth.data.AuthRepository
import com.example.astroxplore.features.profile.data.ProfileRepository
import com.example.astroxplore.features.profile.model.ProfileModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkConnectivityObserver.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                authRepository.login(email, password)
                val user = authRepository.currentUser
                if (user != null) {
                    var profile = profileRepository.getProfile(user.id)
                    
                    // If profile doesn't exist (e.g., signup needed confirmation), create it now
                    if (profile == null) {
                        val newProfile = ProfileModel(
                            id = user.id,
                            email = user.email ?: email,
                            isOnboarded = false
                        )
                        profileRepository.updateProfile(newProfile)
                        profile = newProfile
                    }
                    
                    _uiState.value = LoginUiState.Success(isOnboarded = profile.isOnboarded)
                } else {
                    _uiState.value = LoginUiState.Error(R.string.error_unknown)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(ErrorMapper.mapToMessage(e))
            }
        }
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val isOnboarded: Boolean) : LoginUiState
    data class Error(val messageResId: Int) : LoginUiState
}

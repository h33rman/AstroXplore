package com.example.astroxplore.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.features.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun signup(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.value = SignupUiState.Loading
            try {
                // For now name is just passed, in real app we'd save it to profile
                authRepository.register(email, password)
                _uiState.value = SignupUiState.Success
            } catch (e: Exception) {
                _uiState.value = SignupUiState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

sealed interface SignupUiState {
    data object Idle : SignupUiState
    data object Loading : SignupUiState
    data object Success : SignupUiState
    data class Error(val message: String) : SignupUiState
}

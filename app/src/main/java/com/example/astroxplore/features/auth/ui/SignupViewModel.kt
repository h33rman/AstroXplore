package com.example.astroxplore.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.model.UserProfile
import com.example.astroxplore.features.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val countries = Locale.getISOCountries().map {
        val locale = Locale.Builder().setRegion(it).build()
        locale.displayCountry
    }.sorted()

    val educationLevels = listOf(
        "High School",
        "Undergraduate - 1st Year",
        "Undergraduate - 2nd Year",
        "Undergraduate - 3rd Year",
        "Undergraduate - 4th Year",
        "Master's Degree",
        "PhD Candidate",
        "Postdoctoral Researcher",
        "Professor / Professional Researcher"
    )

    val affiliationTypes = listOf(
        "University",
        "Organization",
        "Company",
        "Institution",
        "Individual"
    )

    val researchInterests = listOf(
        "Cosmology", "Exoplanets", "Black Holes", "JWST", "Dark Energy",
        "Gravitational Waves", "Galaxies", "Star Formation", "Neutron Stars",
        "Supernovae", "Planetary Science", "Astrobiology", "Radio Astronomy",
        "High Energy Astrophysics", "Solar Physics"
    )

    fun signup(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        affiliationType: String,
        affiliationName: String?,
        country: String,
        educationLevel: String,
        interests: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = SignupUiState.Loading
            try {
                val profile = UserProfile(
                    id = "", // Will be set in repository
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    affiliationType = affiliationType,
                    affiliationName = affiliationName,
                    country = country,
                    educationLevel = educationLevel,
                    researchInterests = interests
                )
                authRepository.register(email, password, profile)
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

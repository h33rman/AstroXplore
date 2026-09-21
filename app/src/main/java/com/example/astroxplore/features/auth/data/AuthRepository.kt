package com.example.astroxplore.features.auth.data

import com.example.astroxplore.features.profile.model.ProfileModel
import com.example.astroxplore.features.profile.model.UserPreference
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun login(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * @return Boolean - true if email confirmation is required
     */
    suspend fun register(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String,
        institution: String? = null,
        orcidId: String? = null
    ): Boolean {
        val user = supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        } ?: throw Exception("Signup failed: User is null")
        
        val session = supabaseClient.auth.currentSessionOrNull()
        
        if (session != null) {
            // Confirmation is OFF, we can create the profile record immediately
            val initialProfile = ProfileModel(
                id = user.id,
                email = email,
                firstName = firstName,
                lastName = lastName,
                fullName = "$firstName $lastName",
                institution = institution,
                orcidId = orcidId,
                isOnboarded = false
            )
            supabaseClient.postgrest["profiles"].insert(initialProfile)
            return false
        } else {
            // Confirmation is ON. We'll handle profile creation on first login.
            return true
        }
    }

    suspend fun logout() {
        supabaseClient.auth.signOut()
    }

    val sessionStatus: Flow<SessionStatus> = supabaseClient.auth.sessionStatus

    val currentUser get() = supabaseClient.auth.currentUserOrNull()
    
    fun isLoggedIn(): Boolean = supabaseClient.auth.currentSessionOrNull() != null
}

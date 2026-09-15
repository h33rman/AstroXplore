package com.example.astroxplore.features.auth.data

import com.example.astroxplore.core.model.UserProfile
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

    suspend fun register(email: String, password: String, profile: UserProfile) {
        val response = supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val user = response ?: throw Exception("Signup failed: User is null")
        
        // Save profile to Supabase 'profiles' table
        supabaseClient.postgrest["profiles"].insert(profile.copy(id = user.id))
    }

    suspend fun logout() {
        supabaseClient.auth.signOut()
    }

    val sessionStatus: Flow<SessionStatus> = supabaseClient.auth.sessionStatus

    val currentUser = supabaseClient.auth.currentSessionOrNull()?.user
    
    fun isLoggedIn(): Boolean = supabaseClient.auth.currentSessionOrNull() != null
}

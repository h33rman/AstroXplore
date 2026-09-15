package com.example.astroxplore.features.auth.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
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

    suspend fun register(email: String, password: String) {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        supabaseClient.auth.signOut()
    }

    val sessionStatus: Flow<SessionStatus> = supabaseClient.auth.sessionStatus

    val currentUser = supabaseClient.auth.currentSessionOrNull()?.user
    
    fun isLoggedIn(): Boolean = supabaseClient.auth.currentSessionOrNull() != null
}

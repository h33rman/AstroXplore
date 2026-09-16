package com.example.astroxplore.core.util

import android.util.Log
import com.example.astroxplore.R
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.auth.exception.AuthRestException
import java.io.IOException
import java.net.UnknownHostException

object ErrorMapper {
    private const val TAG = "ErrorMapper"

    fun mapToMessage(e: Throwable): Int {
        // Log the technical error to IDE console for developers
        Log.e(TAG, "Technical Error Caught: ${e.message}", e)

        return when (e) {
            is UnknownHostException, is IOException -> R.string.error_network
            is AuthRestException -> {
                // Supabase Auth specific error codes
                when (e.error) {
                    "invalid_credentials", "invalid_grant" -> R.string.error_invalid_credentials
                    "user_already_exists" -> R.string.error_user_exists
                    else -> R.string.error_unknown
                }
            }
            is RestException -> {
                // General Postgrest or other Supabase REST errors
                if (e.description?.contains("row-level security policy") == true) {
                    R.string.error_database
                } else {
                    R.string.error_unknown
                }
            }
            else -> R.string.error_unknown
        }
    }
}

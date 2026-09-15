package com.example.astroxplore.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val affiliationType: String, // University, Organization, Company, Institution, Individual
    val affiliationName: String? = null,
    val country: String,
    val educationLevel: String,
    val researchInterests: List<String> = emptyList()
)

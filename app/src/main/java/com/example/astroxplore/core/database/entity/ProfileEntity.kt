package com.example.astroxplore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.astroxplore.features.profile.model.ProfileModel

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String?,
    val firstName: String?,
    val lastName: String?,
    val institution: String?,
    val affiliationType: String?,
    val affiliationName: String?,
    val country: String?,
    val educationLevel: String?,
    val orcidId: String?,
    val isOnboarded: Boolean,
    val createdAt: String?,
    val isSynced: Boolean = true
) {
    fun toDomainModel() = ProfileModel(
        id = id,
        email = email,
        fullName = fullName,
        firstName = firstName,
        lastName = lastName,
        institution = institution,
        affiliationType = affiliationType,
        affiliationName = affiliationName,
        country = country,
        educationLevel = educationLevel,
        orcidId = orcidId,
        isOnboarded = isOnboarded,
        createdAt = createdAt
    )
}

fun ProfileModel.toEntity(isSynced: Boolean = true) = ProfileEntity(
    id = id,
    email = email,
    fullName = fullName,
    firstName = firstName,
    lastName = lastName,
    institution = institution,
    affiliationType = affiliationType,
    affiliationName = affiliationName,
    country = country,
    educationLevel = educationLevel,
    orcidId = orcidId,
    isOnboarded = isOnboarded,
    createdAt = createdAt,
    isSynced = isSynced
)

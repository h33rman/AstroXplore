package com.example.astroxplore.features.groups.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupModel(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("focus_area") val focusArea: String? = null,
    @SerialName("member_count") val memberCount: Int = 1,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class GroupPaperModel(
    @SerialName("id") val id: String? = null,
    @SerialName("group_id") val groupId: String,
    @SerialName("bibcode") val bibcode: String,
    @SerialName("added_by") val addedBy: String,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("added_at") val addedAt: String? = null
)

@Serializable
data class GroupMemberModel(
    @SerialName("id") val id: String? = null,
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("role") val role: String = "member", // admin, moderator, member
    @SerialName("joined_at") val joinedAt: String? = null
)

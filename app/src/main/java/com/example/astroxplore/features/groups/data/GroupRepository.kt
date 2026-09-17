package com.example.astroxplore.features.groups.data

import com.example.astroxplore.core.database.dao.GroupDao
import com.example.astroxplore.core.database.entity.GroupEntity
import com.example.astroxplore.features.groups.model.GroupModel
import com.example.astroxplore.features.groups.model.GroupPaperModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val groupDao: GroupDao
) {
    /**
     * Reactively observe groups from the local database (Offline-First)
     */
    fun getLocalGroups(): Flow<List<GroupModel>> = groupDao.getAllGroups().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun syncGroups() = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        try {
            val remoteGroups = supabaseClient.postgrest["groups"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("owner_id", userId)
                    }
                }
                .decodeList<GroupModel>()
            
            groupDao.insertGroups(remoteGroups.map { it.toEntity(isSynced = true) })
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun createGroup(name: String, description: String?, focusArea: String?) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        
        // 1. Create locally first
        val localId = UUID.randomUUID().toString()
        val localGroup = GroupEntity(
            id = localId,
            name = name,
            description = description,
            ownerId = userId,
            focusArea = focusArea,
            memberCount = 1,
            createdAt = LocalDateTime.now().toString(),
            isSynced = false
        )
        groupDao.insertGroup(localGroup)

        // 2. Push to Supabase
        try {
            val supabaseGroup = mapOf(
                "name" to name,
                "description" to description,
                "owner_id" to userId,
                "focus_area" to focusArea,
                "member_count" to 1
            )
            // Insert and get the record back to update the local ID if needed, 
            // or just rely on the sync on next refresh.
            supabaseClient.postgrest["groups"].insert(supabaseGroup)
            
            // Mark as synced locally
            groupDao.insertGroup(localGroup.copy(isSynced = true))
        } catch (e: Exception) {
            // Log error, it will remain isSynced = false for future retry
        }
    }

    suspend fun addPaperToGroup(groupId: String, bibcode: String) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        val groupPaper = GroupPaperModel(
            groupId = groupId,
            bibcode = bibcode,
            addedBy = userId
        )
        try {
            supabaseClient.postgrest["group_papers"].insert(groupPaper)
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun getGroupPapers(groupId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["group_papers"]
                .select(columns = Columns.list("bibcode")) {
                    filter {
                        eq("group_id", groupId)
                    }
                }
                .decodeList<Map<String, String>>()
                .map { it["bibcode"] ?: "" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Mappers
    private fun GroupEntity.toDomainModel() = GroupModel(
        id = id,
        name = name,
        description = description,
        ownerId = ownerId,
        focusArea = focusArea,
        memberCount = memberCount,
        createdAt = createdAt
    )

    private fun GroupModel.toEntity(isSynced: Boolean) = GroupEntity(
        id = id,
        name = name,
        description = description,
        ownerId = ownerId,
        focusArea = focusArea,
        memberCount = memberCount,
        createdAt = createdAt,
        isSynced = isSynced
    )
}

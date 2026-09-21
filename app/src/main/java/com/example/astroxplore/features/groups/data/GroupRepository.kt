package com.example.astroxplore.features.groups.data

import com.example.astroxplore.core.database.dao.GroupDao
import com.example.astroxplore.core.database.dao.GroupPaperDao
import com.example.astroxplore.core.database.entity.GroupEntity
import com.example.astroxplore.core.database.entity.toEntity
import com.example.astroxplore.features.groups.model.GroupMemberModel
import com.example.astroxplore.features.groups.model.GroupModel
import com.example.astroxplore.features.groups.model.GroupPaperModel
import com.example.astroxplore.features.groups.model.PresentationModel
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
    private val groupDao: GroupDao,
    private val groupPaperDao: GroupPaperDao
) {
    /**
     * Reactively observe groups from the local database (Offline-First)
     */
    fun getLocalGroups(): Flow<List<GroupModel>> = groupDao.getAllGroups().map { entities ->
        entities.map { it.toDomainModel() }
    }

    /**
     * Reactively observe papers in a group from local cache
     */
    fun getLocalGroupPapers(groupId: String): Flow<List<GroupPaperModel>> = 
        groupPaperDao.getGroupPapers(groupId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun syncGroups() = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        try {
            // 1. Fetch group IDs where the user is a member
            val memberships = supabaseClient.postgrest["group_members"]
                .select(columns = Columns.list("group_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Map<String, String>>()
            
            val groupIds = memberships.mapNotNull { it["group_id"] }
            
            if (groupIds.isNotEmpty()) {
                // 2. Fetch the actual group details for those IDs
                val remoteGroups = supabaseClient.postgrest["groups"]
                    .select(columns = Columns.ALL) {
                        filter { 
                            isIn("id", groupIds)
                        }
                    }
                    .decodeList<GroupModel>()
                
                // 3. Update local Room cache
                groupDao.insertGroups(remoteGroups.map { it.toEntity(isSynced = true) })

                // 4. Sync papers for each group
                remoteGroups.forEach { group ->
                    syncGroupPapers(group.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Simple logging to Logcat
        }
    }

    suspend fun syncGroupPapers(groupId: String) = withContext(Dispatchers.IO) {
        try {
            val remotePapers = supabaseClient.postgrest["group_papers"]
                .select(columns = Columns.ALL) {
                    filter { eq("group_id", groupId) }
                }
                .decodeList<GroupPaperModel>()
            
            groupPaperDao.insertGroupPapers(remotePapers.map { it.toEntity(isSynced = true) })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createGroup(name: String, description: String?, focusArea: String?) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        
        // Short human-friendly ID
        val displayId = UUID.randomUUID().toString().take(8).uppercase()
        val localId = UUID.randomUUID().toString()
        
        val localGroup = GroupEntity(
            id = localId,
            displayId = displayId,
            name = name,
            description = description,
            ownerId = userId,
            focusArea = focusArea,
            memberCount = 1,
            createdAt = LocalDateTime.now().toString(),
            isSynced = false
        )
        groupDao.insertGroup(localGroup)

        try {
            val supabaseGroup = mapOf(
                "display_id" to displayId,
                "name" to name,
                "description" to description,
                "owner_id" to userId,
                "focus_area" to focusArea,
                "member_count" to 1
            )
            val insertedGroup = supabaseClient.postgrest["groups"]
                .insert(supabaseGroup) { select() }
                .decodeSingle<GroupModel>()
            
            // Also create initial membership for the owner
            val initialMember = GroupMemberModel(
                groupId = insertedGroup.id,
                userId = userId,
                role = "admin"
            )
            supabaseClient.postgrest["group_members"].insert(initialMember)
            
            // Update local
            groupDao.insertGroup(insertedGroup.toEntity(isSynced = true))
        } catch (e: Exception) {
            // Remains unsynced
        }
    }

    suspend fun joinGroupByDisplayId(displayId: String): Boolean = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
        try {
            val group = supabaseClient.postgrest["groups"]
                .select(columns = Columns.ALL) {
                    filter { eq("display_id", displayId.uppercase()) }
                }
                .decodeSingle<GroupModel>()
            
            val membership = GroupMemberModel(
                groupId = group.id,
                userId = userId,
                role = "member"
            )
            supabaseClient.postgrest["group_members"].insert(membership)
            
            // Save locally
            groupDao.insertGroup(group.toEntity(isSynced = true))
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }

    suspend fun leaveGroup(groupId: String) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        try {
            supabaseClient.postgrest["group_members"].delete {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
            groupDao.clearAll()
            syncGroups()
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun deleteGroup(groupId: String) = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["groups"].delete {
                filter { eq("id", groupId) }
            }
            groupDao.clearAll()
            syncGroups()
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun updateGroup(groupId: String, name: String, description: String?, focusArea: String?) = withContext(Dispatchers.IO) {
        try {
            val update = mapOf(
                "name" to name,
                "description" to description,
                "focus_area" to focusArea
            )
            supabaseClient.postgrest["groups"].update(update) {
                filter { eq("id", groupId) }
            }
            syncGroups()
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun addPaperToGroup(groupId: String, bibcode: String) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        val localId = UUID.randomUUID().toString()
        val groupPaper = GroupPaperModel(
            id = localId,
            groupId = groupId,
            bibcode = bibcode,
            addedBy = userId,
            addedAt = LocalDateTime.now().toString()
        )
        
        // Save locally first
        groupPaperDao.insertGroupPaper(groupPaper.toEntity(isSynced = false))

        try {
            val supabaseGroupPaper = mapOf(
                "group_id" to groupId,
                "bibcode" to bibcode,
                "added_by" to userId
            )
            val inserted = supabaseClient.postgrest["group_papers"]
                .insert(supabaseGroupPaper) { select() }
                .decodeSingle<GroupPaperModel>()
            
            // Mark as synced and update with real ID from Supabase
            groupPaperDao.deleteGroupPaper(groupId, bibcode)
            groupPaperDao.insertGroupPaper(inserted.toEntity(isSynced = true))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun voteForPaper(groupPaperId: String) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        try {
            val vote = mapOf(
                "group_paper_id" to groupPaperId,
                "user_id" to userId
            )
            supabaseClient.postgrest["group_paper_votes"].insert(vote)
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun unvoteForPaper(groupPaperId: String) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        try {
            supabaseClient.postgrest["group_paper_votes"].delete {
                filter {
                    eq("group_paper_id", groupPaperId)
                    eq("user_id", userId)
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun getGroupPapers(groupId: String): List<GroupPaperModel> = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
        try {
            val papers = supabaseClient.postgrest["group_papers"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("group_id", groupId)
                    }
                }
                .decodeList<GroupPaperModel>()

            // If we have a user, check their votes
            if (userId.isNotEmpty() && papers.isNotEmpty()) {
                val userVotes = supabaseClient.postgrest["group_paper_votes"]
                    .select(columns = Columns.list("group_paper_id")) {
                        filter {
                            eq("user_id", userId)
                            // Ideally use 'in' filter for group paper ids
                        }
                    }
                    .decodeList<Map<String, String>>()
                    .map { it["group_paper_id"] }
                
                return@withContext papers.map { paper ->
                    paper.copy(isVotedByMe = userVotes.contains(paper.id))
                }
            }
            
            papers
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun schedulePresentation(groupId: String, bibcode: String, scheduledAt: LocalDateTime) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        val presentation = PresentationModel(
            groupId = groupId,
            bibcode = bibcode,
            presenterId = userId,
            scheduledAt = scheduledAt.toString()
        )
        try {
            supabaseClient.postgrest["group_presentations"].insert(presentation)
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun getGroupPresentations(groupId: String): List<PresentationModel> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["group_presentations"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("group_id", groupId)
                    }
                }
                .decodeList<PresentationModel>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Mappers
    private fun GroupEntity.toDomainModel() = GroupModel(
        id = id,
        displayId = displayId,
        name = name,
        description = description,
        ownerId = ownerId,
        focusArea = focusArea,
        memberCount = memberCount,
        createdAt = createdAt
    )

    private fun GroupModel.toEntity(isSynced: Boolean) = GroupEntity(
        id = id,
        displayId = displayId,
        name = name,
        description = description,
        ownerId = ownerId,
        focusArea = focusArea,
        memberCount = memberCount,
        createdAt = createdAt,
        isSynced = isSynced
    )
}

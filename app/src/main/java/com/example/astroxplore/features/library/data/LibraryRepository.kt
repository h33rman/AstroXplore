package com.example.astroxplore.features.library.data

import com.example.astroxplore.core.database.dao.SavedPaperDao
import com.example.astroxplore.core.database.entity.SavedPaperEntity
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.library.model.SavedPaperModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val savedPaperDao: SavedPaperDao
) {
    fun getSavedPapers(): Flow<List<PaperModel>> = savedPaperDao.getAllSavedPapers().map { entities ->
        entities.map { it.toDomainModel() }
    }

    fun isPaperSaved(bibcode: String): Flow<Boolean> = savedPaperDao.isPaperSaved(bibcode)

    suspend fun toggleSave(paper: PaperModel) = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        val isCurrentlySaved = savedPaperDao.isPaperSaved(paper.bibcode).first()
        
        if (isCurrentlySaved) {
            unsavePaper(userId, paper.bibcode)
        } else {
            savePaper(userId, paper)
        }
    }

    private suspend fun savePaper(userId: String, paper: PaperModel) {
        // Save locally
        savedPaperDao.savePaper(paper.toEntity())
        
        // Save to Supabase
        try {
            val model = paper.toSupabaseModel(userId)
            supabaseClient.postgrest["saved_papers"].insert(model)
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun unsavePaper(userId: String, bibcode: String) {
        // Remove locally
        savedPaperDao.deletePaper(bibcode)
        
        // Remove from Supabase
        try {
            supabaseClient.postgrest["saved_papers"].delete {
                filter {
                    eq("user_id", userId)
                    eq("bibcode", bibcode)
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun syncLibrary() = withContext(Dispatchers.IO) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        try {
            val remotePapers = supabaseClient.postgrest["saved_papers"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SavedPaperModel>()
            
            // Sync remote to local
            remotePapers.forEach { remote ->
                savedPaperDao.savePaper(remote.toEntity())
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    // Mappers
    private fun SavedPaperEntity.toDomainModel() = PaperModel(
        bibcode = bibcode,
        rawTitles = listOf(title),
        authors = authors.split(", "),
        abstractText = abstractText,
        keywords = listOf(category),
        rawPubDate = dateDisplay,
        citationCount = citationCount
    )

    private fun PaperModel.toEntity() = SavedPaperEntity(
        bibcode = bibcode,
        title = title,
        authors = authors.joinToString(", "),
        abstractText = abstractText,
        category = category,
        dateDisplay = dateDisplay,
        citationCount = citationCount
    )

    private fun SavedPaperModel.toEntity() = SavedPaperEntity(
        bibcode = bibcode,
        title = title ?: "",
        authors = authors ?: "",
        abstractText = abstract ?: "",
        category = category ?: "",
        dateDisplay = dateDisplay ?: "",
        citationCount = citationCount
    )

    private fun PaperModel.toSupabaseModel(userId: String) = SavedPaperModel(
        userId = userId,
        bibcode = bibcode,
        title = title,
        authors = authors.joinToString(", "),
        abstract = abstractText,
        category = category,
        dateDisplay = dateDisplay,
        citationCount = citationCount
    )
}

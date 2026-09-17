package com.example.astroxplore.features.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.network.NasaAdsQueryBuilder
import com.example.astroxplore.core.util.ErrorMapper
import com.example.astroxplore.core.util.NavigationSignal
import com.example.astroxplore.features.auth.data.AuthRepository
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.library.data.LibraryRepository
import com.example.astroxplore.features.profile.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val paperRepository: PaperRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val libraryRepository: LibraryRepository,
    private val navigationSignal: NavigationSignal
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<Int?>(null)
    val error = _error.asStateFlow()

    private val _selectedCategory = MutableStateFlow("For You")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val scrollToTopEvent = navigationSignal.scrollToTop

    private val _savedPaperIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPaperIds: StateFlow<Set<String>> = _savedPaperIds.asStateFlow()

    // Offline-First Feed: Reactively observe the database
    val feedPapers: StateFlow<List<PaperModel>> = paperRepository.getCachedFeed()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadSavedPapers()
        
        // Initial load: refresh if empty
        viewModelScope.launch {
            feedPapers.collect {
                if (it.isEmpty() && !_isRefreshing.value) {
                    refresh()
                }
            }
        }
    }

    private fun loadSavedPapers() {
        viewModelScope.launch {
            libraryRepository.getSavedPapers().collect { papers ->
                _savedPaperIds.value = papers.map { it.bibcode }.toSet()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                val query = buildQuery(_selectedCategory.value)
                paperRepository.refreshFeed(query)
            } catch (e: Exception) {
                _error.value = ErrorMapper.mapToMessage(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleSavePaper(paper: PaperModel) {
        viewModelScope.launch {
            libraryRepository.toggleSave(paper)
        }
    }

    private suspend fun buildQuery(category: String): String {
        return when (category) {
            "For You" -> {
                val userId = authRepository.currentUser?.id
                val prefs = userId?.let { profileRepository.getUserPreferences(it) } ?: emptyList()
                NasaAdsQueryBuilder.buildPreferenceQuery(prefs)
            }
            else -> "keyword:\"$category\" AND property:eprint"
        }
    }
}

package com.example.astroxplore.features.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.network.NasaAdsQueryBuilder
import com.example.astroxplore.core.util.ErrorMapper
import com.example.astroxplore.features.auth.data.AuthRepository
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.profile.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val paperRepository: PaperRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _categories = listOf("For You", "Astrophysics", "Galaxies", "Cosmology")
    val categories: List<String> = _categories

    private val _selectedCategory = MutableStateFlow(_categories[0])
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10
    private var isLastPage = false
    private val currentPapers = mutableListOf<PaperModel>()

    init {
        fetchPapers(_selectedCategory.value)
    }

    fun selectCategory(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        currentPage = 0
        isLastPage = false
        currentPapers.clear()
        fetchPapers(category)
    }

    fun loadMore() {
        val state = uiState.value
        if (state is FeedUiState.LoadingMore || isLastPage) return
        currentPage++
        fetchPapers(_selectedCategory.value, isLoadMore = true)
    }

    private fun fetchPapers(category: String, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isLoadMore) {
                _uiState.value = FeedUiState.LoadingMore(currentPapers.toList())
            } else if (currentPapers.isEmpty()) {
                _uiState.value = FeedUiState.Loading
            }

            try {
                val query = buildQuery(category)
                val newPapers = paperRepository.getPapersByQuery(
                    query = query,
                    page = currentPage,
                    pageSize = pageSize
                )

                if (newPapers.size < pageSize) {
                    isLastPage = true
                }

                currentPapers.addAll(newPapers)
                _uiState.value = FeedUiState.Success(currentPapers.toList(), isLastPage)
            } catch (e: Exception) {
                if (isLoadMore) {
                    _uiState.value = FeedUiState.Success(currentPapers.toList(), isLastPage)
                } else {
                    _uiState.value = FeedUiState.Error(ErrorMapper.mapToMessage(e))
                }
            }
        }
    }

    private suspend fun buildQuery(category: String): String {
        return when (category) {
            "For You" -> {
                val userId = authRepository.currentUser?.id
                val prefs = userId?.let { profileRepository.getUserPreferences(it) } ?: emptyList()
                NasaAdsQueryBuilder.buildPreferenceQuery(prefs)
            }
            "Astrophysics" -> "keyword:astrophysics AND property:eprint"
            "Galaxies" -> "keyword:galaxies AND property:eprint"
            "Cosmology" -> "keyword:cosmology AND property:eprint"
            else -> "keyword:\"$category\" AND property:eprint"
        }
    }
}

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class LoadingMore(val currentPapers: List<PaperModel>) : FeedUiState
    data class Success(val papers: List<PaperModel>, val isLastPage: Boolean) : FeedUiState
    data class Error(val messageResId: Int) : FeedUiState
}

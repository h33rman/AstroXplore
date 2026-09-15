package com.example.astroxplore.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.network.NasaAdsQueryBuilder
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val paperRepository: PaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = ExploreUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            try {
                val solrQuery = NasaAdsQueryBuilder.buildQuickSearchQuery(query)
                val results = paperRepository.getPapersByQuery(solrQuery)
                _uiState.value = ExploreUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(e.message ?: "Search failed")
            }
        }
    }
}

sealed interface ExploreUiState {
    data object Idle : ExploreUiState
    data object Loading : ExploreUiState
    data class Success(val results: List<PaperModel>) : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}

package com.example.astroxplore.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.network.NasaAdsQueryBuilder
import com.example.astroxplore.core.util.ErrorMapper
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class SearchFilter(
    val yearRange: IntRange? = null,
    val refereedOnly: Boolean = false,
    val bibstem: String? = null,
    val author: String? = null,
    val abstractOnly: String? = null,
    val titleOnly: String? = null,
    val orcid: String? = null,
    val firstAuthor: String? = null,
    val affiliation: String? = null,
    val objectName: String? = null,
    val arxivId: String? = null,
    val doi: String? = null,
    val citationCountMin: Int? = null,
    val isOpenAccess: Boolean = false,
    val hasData: Boolean = false,
    // Pro Fields
    val arxivClass: String? = null,
    val authorCountRange: IntRange? = null,
    val bibGroup: String? = null,
    val database: String? = null, // astronomy, physics, general
    val docType: String? = null // article, eprint, catalog, etc.
) {
    fun isActive(): Boolean = yearRange != null || refereedOnly || !bibstem.isNullOrBlank() || 
            !author.isNullOrBlank() || !abstractOnly.isNullOrBlank() || !titleOnly.isNullOrBlank() || 
            !orcid.isNullOrBlank() || !firstAuthor.isNullOrBlank() || !affiliation.isNullOrBlank() || 
            !objectName.isNullOrBlank() || !arxivId.isNullOrBlank() || !doi.isNullOrBlank() || 
            citationCountMin != null || isOpenAccess || hasData || !arxivClass.isNullOrBlank() ||
            authorCountRange != null || !bibGroup.isNullOrBlank() || !database.isNullOrBlank() ||
            !docType.isNullOrBlank()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val paperRepository: PaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter = _searchFilter.asStateFlow()

    init {
        _searchQuery
            .combine(_searchFilter) { query, filter -> query to filter }
            .debounce(500.milliseconds)
            .onEach { (query, filter) -> performSearch(query, filter) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun clearFilters() {
        _searchFilter.value = SearchFilter()
    }

    private suspend fun performSearch(query: String, filter: SearchFilter) {
        if (query.isBlank() && !filter.isActive()) {
            _uiState.value = ExploreUiState.Idle
            return
        }

        _uiState.value = ExploreUiState.Loading
        try {
            val solrQuery = NasaAdsQueryBuilder.buildAdvancedQuery(
                query = query,
                filter = filter
            )
            val results = paperRepository.getPapersByQuery(solrQuery)
            _uiState.value = ExploreUiState.Success(results)
        } catch (e: Exception) {
            _uiState.value = ExploreUiState.Error(ErrorMapper.mapToMessage(e))
        }
    }
}

sealed interface ExploreUiState {
    data object Idle : ExploreUiState
    data object Loading : ExploreUiState
    data class Success(val results: List<PaperModel>) : ExploreUiState
    data class Error(val messageResId: Int) : ExploreUiState
}

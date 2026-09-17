package com.example.astroxplore.features.feed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.core.util.ErrorMapper
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.library.data.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaperDetailsViewModel @Inject constructor(
    private val paperRepository: PaperRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaperDetailsUiState>(PaperDetailsUiState.Loading)
    val uiState: StateFlow<PaperDetailsUiState> = _uiState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun loadPaper(bibcode: String) {
        viewModelScope.launch {
            _uiState.value = PaperDetailsUiState.Loading
            try {
                val papers = paperRepository.getPapersByQuery(
                    query = "bibcode:$bibcode",
                    pageSize = 1
                )
                val paper = papers.firstOrNull()
                if (paper != null) {
                    _uiState.value = PaperDetailsUiState.Success(paper)
                    observeSavedState(bibcode)
                } else {
                    _uiState.value = PaperDetailsUiState.Error("Paper not found")
                }
            } catch (e: Exception) {
                _uiState.value = PaperDetailsUiState.Error("Failed to load paper")
            }
        }
    }

    private fun observeSavedState(bibcode: String) {
        libraryRepository.isPaperSaved(bibcode)
            .onEach { _isSaved.value = it }
            .launchIn(viewModelScope)
    }

    fun toggleSave(paper: PaperModel) {
        viewModelScope.launch {
            libraryRepository.toggleSave(paper)
        }
    }
}

sealed interface PaperDetailsUiState {
    data object Loading : PaperDetailsUiState
    data class Success(val paper: PaperModel) : PaperDetailsUiState
    data class Error(val message: String) : PaperDetailsUiState
}

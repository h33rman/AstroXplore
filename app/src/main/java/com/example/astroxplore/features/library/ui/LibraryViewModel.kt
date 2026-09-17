package com.example.astroxplore.features.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.library.data.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val savedPapers: StateFlow<List<PaperModel>> = libraryRepository.getSavedPapers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        syncLibrary()
    }

    fun syncLibrary() {
        viewModelScope.launch {
            _isRefreshing.value = true
            libraryRepository.syncLibrary()
            _isRefreshing.value = false
        }
    }

    fun toggleSave(paper: PaperModel) {
        viewModelScope.launch {
            libraryRepository.toggleSave(paper)
        }
    }
}

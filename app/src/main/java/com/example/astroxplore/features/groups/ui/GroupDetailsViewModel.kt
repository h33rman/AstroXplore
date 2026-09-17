package com.example.astroxplore.features.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.groups.data.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val paperRepository: PaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupDetailsUiState>(GroupDetailsUiState.Loading)
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()

    fun loadGroupPapers(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupDetailsUiState.Loading
            try {
                val bibcodes = groupRepository.getGroupPapers(groupId)
                if (bibcodes.isEmpty()) {
                    _uiState.value = GroupDetailsUiState.Success(emptyList())
                } else {
                    // Create an OR query for the bibcodes
                    val query = "bibcode:(" + bibcodes.joinToString(" OR ") + ")"
                    val papers = paperRepository.getPapersByQuery(query, pageSize = 50)
                    _uiState.value = GroupDetailsUiState.Success(papers)
                }
            } catch (e: Exception) {
                _uiState.value = GroupDetailsUiState.Error("Failed to load group papers")
            }
        }
    }
}

sealed interface GroupDetailsUiState {
    data object Loading : GroupDetailsUiState
    data class Success(val papers: List<PaperModel>) : GroupDetailsUiState
    data class Error(val message: String) : GroupDetailsUiState
}

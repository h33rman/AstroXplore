package com.example.astroxplore.features.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.features.auth.data.AuthRepository
import com.example.astroxplore.features.feed.data.PaperRepository
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.groups.data.GroupRepository
import com.example.astroxplore.features.groups.model.GroupModel
import com.example.astroxplore.features.groups.model.GroupPaperModel
import com.example.astroxplore.features.groups.model.PresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class GroupDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val paperRepository: PaperRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupDetailsUiState>(GroupDetailsUiState.Loading)
    val uiState: StateFlow<GroupDetailsUiState> = _uiState.asStateFlow()

    private val _currentGroup = MutableStateFlow<GroupModel?>(null)
    val currentGroup = _currentGroup.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.id

    fun loadGroupData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupDetailsUiState.Loading
            
            // Sync specific group info from local
            groupRepository.getLocalGroups().collectLatest { groups ->
                val group = groups.find { it.id == groupId }
                _currentGroup.value = group
            }
        }

        viewModelScope.launch {
            // Observe papers in this group reactively
            groupRepository.getLocalGroupPapers(groupId).collectLatest { groupPapers ->
                refreshGroupUi(groupId, groupPapers)
            }
        }
        
        // Background sync
        viewModelScope.launch {
            groupRepository.syncGroupPapers(groupId)
        }
    }

    private suspend fun refreshGroupUi(groupId: String, groupPapers: List<GroupPaperModel>) {
        try {
            val presentations = groupRepository.getGroupPresentations(groupId)
            
            if (groupPapers.isEmpty()) {
                _uiState.value = GroupDetailsUiState.Success(emptyList(), emptyList(), presentations)
            } else {
                val bibcodes = groupPapers.map { it.bibcode }
                val query = "bibcode:(" + bibcodes.joinToString(" OR ") + ")"
                val papers = paperRepository.getPapersByQuery(query, pageSize = 50)
                
                _uiState.value = GroupDetailsUiState.Success(
                    papers = papers,
                    groupPapers = groupPapers,
                    presentations = presentations
                )
            }
        } catch (e: Exception) {
            _uiState.value = GroupDetailsUiState.Error("Failed to load group content")
        }
    }

    private fun refreshGroupContent(groupId: String) {
        viewModelScope.launch {
            groupRepository.syncGroupPapers(groupId)
        }
    }

    fun voteForPaper(groupPaperId: String) {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            groupRepository.voteForPaper(groupPaperId)
            refreshGroupContent(groupId)
        }
    }

    fun unvoteForPaper(groupPaperId: String) {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            groupRepository.unvoteForPaper(groupPaperId)
            refreshGroupContent(groupId)
        }
    }

    fun schedulePresentation(bibcode: String, dateTime: LocalDateTime) {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            groupRepository.schedulePresentation(groupId, bibcode, dateTime)
            refreshGroupContent(groupId)
        }
    }

    fun updateGroup(name: String, description: String?, focusArea: String?) {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            groupRepository.updateGroup(groupId, name, description, focusArea)
        }
    }

    fun deleteGroup() {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }

    fun leaveGroup() {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            groupRepository.leaveGroup(groupId)
        }
    }
}

sealed interface GroupDetailsUiState {
    data object Loading : GroupDetailsUiState
    data class Success(
        val papers: List<PaperModel>,
        val groupPapers: List<GroupPaperModel>,
        val presentations: List<PresentationModel>
    ) : GroupDetailsUiState
    data class Error(val message: String) : GroupDetailsUiState
}

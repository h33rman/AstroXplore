package com.example.astroxplore.features.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astroxplore.features.groups.data.GroupRepository
import com.example.astroxplore.features.groups.model.GroupModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Offline-First: Reactively observe local database
    val groups: StateFlow<List<GroupModel>> = groupRepository.getLocalGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<GroupsUiState> = groups.map { 
        GroupsUiState.Success(it) 
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupsUiState.Loading
    )

    init {
        syncGroups()
    }

    fun syncGroups() {
        viewModelScope.launch {
            _isRefreshing.value = true
            groupRepository.syncGroups()
            _isRefreshing.value = false
        }
    }

    fun createGroup(name: String, description: String?, focusArea: String?) {
        viewModelScope.launch {
            groupRepository.createGroup(name, description, focusArea)
            // No need to reload manually, the Flow will emit the new local group immediately
        }
    }
}

sealed interface GroupsUiState {
    data object Loading : GroupsUiState
    data class Success(val groups: List<GroupModel>) : GroupsUiState
    data class Error(val message: String) : GroupsUiState
}

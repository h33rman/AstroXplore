package com.example.astroxplore.features.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.features.feed.ui.components.PaperCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onPaperClick: (String) -> Unit,
    viewModel: GroupDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Shelf", "Votes", "Calendar", "Admin")

    LaunchedEffect(groupId) {
        viewModel.loadGroupPapers(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Galaxy Evolution Group", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Active Discussion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share Group Link */ }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Invite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            ) 
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ShelfTab(uiState, onPaperClick)
                1 -> VotesTab()
                2 -> CalendarTab()
                3 -> AdminTab()
            }
        }
    }
}

@Composable
fun ShelfTab(uiState: GroupDetailsUiState, onPaperClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is GroupDetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is GroupDetailsUiState.Success -> {
                if (uiState.papers.isEmpty()) {
                    EmptyShelfState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(uiState.papers, key = { it.bibcode }) { paper ->
                            PaperCard(
                                paper = paper,
                                onTitleClick = { onPaperClick(paper.bibcode) },
                                onReadMoreClick = { onPaperClick(paper.bibcode) }
                            )
                        }
                    }
                }
            }
            is GroupDetailsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun VotesTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.HowToVote, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Consensus Voting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Members can vote on which papers to present in the next session.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CalendarTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Outlined.EventNote, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Group Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Schedule weekly presentations and set collaborative deadlines.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AdminTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        ListItem(
            headlineContent = { Text("Membership Requests") },
            supportingContent = { Text("3 researchers waiting to join") },
            leadingContent = { Icon(Icons.Default.PersonSearch, contentDescription = null) },
            trailingContent = { TextButton(onClick = {}) { Text("View") } }
        )
        
        ListItem(
            headlineContent = { Text("Focus Area Settings") },
            supportingContent = { Text("Update keywords and club description") },
            leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
        
        Button(
            onClick = { /* Delete/Leave Group */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Leave Journal Club")
        }
    }
}

@Composable
fun EmptyShelfState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No papers in this club yet.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

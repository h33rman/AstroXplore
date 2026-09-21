package com.example.astroxplore.features.groups.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.features.feed.ui.components.PaperCard
import com.example.astroxplore.core.util.QrCodeUtils
import com.example.astroxplore.features.groups.model.GroupModel
import com.example.astroxplore.features.groups.ui.components.ConsensusVoting
import com.example.astroxplore.features.groups.ui.components.GroupCalendar
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onPaperClick: (String) -> Unit,
    viewModel: GroupDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val group by viewModel.currentGroup.collectAsState()
    val currentUserId = viewModel.currentUserId
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Shelf", "Votes", "Calendar", "Admin")

    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showScheduleSheet by remember { mutableStateOf(false) }
    var selectedBibcodeForSchedule by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(groupId) {
        viewModel.loadGroupData(groupId)
    }

    if (group == null && uiState !is GroupDetailsUiState.Loading) {
        // Handled: Group might have been deleted
        LaunchedEffect(Unit) { onNavigateBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(group?.name ?: "Journal Club", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(group?.focusArea ?: "Active Discussion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code")
                    }
                    if (group?.ownerId == currentUserId) {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Club")
                        }
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
                1 -> {
                    if (uiState is GroupDetailsUiState.Success) {
                        val state = uiState as GroupDetailsUiState.Success
                        ConsensusVoting(
                            papers = state.groupPapers,
                            onVote = { viewModel.voteForPaper(it) },
                            onUnvote = { viewModel.unvoteForPaper(it) }
                        )
                    }
                }
                2 -> {
                    if (uiState is GroupDetailsUiState.Success) {
                        val state = uiState as GroupDetailsUiState.Success
                        GroupCalendar(
                            presentations = state.presentations,
                            onScheduleClick = { 
                                if (state.papers.isNotEmpty()) {
                                    selectedBibcodeForSchedule = state.papers.first().bibcode
                                    showScheduleSheet = true 
                                }
                            }
                        )
                    }
                }
                3 -> AdminTab(
                    group = group,
                    currentUserId = currentUserId,
                    onLeave = { viewModel.leaveGroup() },
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showQrDialog && group != null) {
        QrCodeDialog(
            displayId = group!!.displayId,
            name = group!!.name,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showEditSheet && group != null) {
        EditGroupSheet(
            group = group!!,
            onDismiss = { showEditSheet = false },
            onUpdate = { n, d, f -> 
                viewModel.updateGroup(n, d, f)
                showEditSheet = false
            }
        )
    }

    if (showScheduleSheet && selectedBibcodeForSchedule != null) {
        SchedulePresentationSheet(
            bibcode = selectedBibcodeForSchedule!!,
            onDismiss = { showScheduleSheet = false },
            onSchedule = { dateTime ->
                viewModel.schedulePresentation(selectedBibcodeForSchedule!!, dateTime)
                showScheduleSheet = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Dissolve Journal Club?") },
            text = { Text("This action cannot be undone. All shared papers and discussions will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.deleteGroup() 
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePresentationSheet(
    bibcode: String,
    onDismiss: () -> Unit,
    onSchedule: (LocalDateTime) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()
        ) {
            Text("Schedule Presentation", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("For paper: $bibcode", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Simplified: Just scheduling for 24 hours from now for demonstration
            // In a real app, we'd use a DatePicker/TimePicker
            Button(
                onClick = { onSchedule(LocalDateTime.now().plusDays(1)) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Schedule for Tomorrow")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AdminTab(
    group: GroupModel?,
    currentUserId: String?,
    onLeave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        ListItem(
            headlineContent = { Text("Membership Requests") },
            supportingContent = { Text("No pending researchers") },
            leadingContent = { Icon(Icons.Default.PersonSearch, contentDescription = null) },
            trailingContent = { TextButton(onClick = {}) { Text("View") } }
        )
        
        ListItem(
            headlineContent = { Text("Club Visibility") },
            supportingContent = { Text("Public - Anyone with ID can join") },
            leadingContent = { Icon(Icons.Default.Public, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.weight(1f))

        if (group?.ownerId == currentUserId) {
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dissolve Journal Club")
            }
        } else {
            Button(
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Leave Journal Club")
            }
        }
    }
}

@Composable
fun QrCodeDialog(
    displayId: String,
    name: String,
    onDismiss: () -> Unit
) {
    val qrBitmap = remember(displayId) { QrCodeUtils.generateQrCode(displayId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Club QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Club ID: $displayId", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Share this ID or QR code with colleagues to invite them.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupSheet(
    group: GroupModel,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var description by remember { mutableStateOf(group.description ?: "") }
    var focusArea by remember { mutableStateOf(group.focusArea ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()).navigationBarsPadding()
        ) {
            Text("Edit Club Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Club Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = focusArea, onValueChange = { focusArea = it }, label = { Text("Focus Area") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { onUpdate(name, description, focusArea) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Update Settings")
            }
            Spacer(modifier = Modifier.height(32.dp))
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

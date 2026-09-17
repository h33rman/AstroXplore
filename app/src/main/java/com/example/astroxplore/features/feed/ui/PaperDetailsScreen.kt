package com.example.astroxplore.features.feed.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.feed.ui.components.AstroAbstractView
import com.example.astroxplore.features.feed.ui.components.AstroPaperTitleText
import com.example.astroxplore.features.groups.model.GroupModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperDetailsScreen(
    bibcode: String,
    onNavigateBack: () -> Unit,
    viewModel: PaperDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val userGroups by viewModel.userGroups.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showGroupPicker by remember { mutableStateOf(false) }

    LaunchedEffect(bibcode) {
        viewModel.loadPaper(bibcode)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Publication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out this paper: ${bibcode}\nhttps://ui.adsabs.harvard.edu/abs/${bibcode}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (uiState is PaperDetailsUiState.Success) {
                val paper = (uiState as PaperDetailsUiState.Success).paper
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                val url = paper.pdfUrl ?: "https://ui.adsabs.harvard.edu/abs/${paper.bibcode}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Launch, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Read Full Paper", fontWeight = FontWeight.Bold)
                        }
                        
                        FilledTonalIconButton(
                            onClick = { viewModel.toggleSave(paper) },
                            modifier = Modifier.size(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is PaperDetailsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PaperDetailsUiState.Success -> {
                    PaperDetailsContent(
                        paper = state.paper,
                        onCiteClick = {
                            clipboardManager.setText(AnnotatedString(state.paper.bibcode))
                            scope.launch {
                                snackbarHostState.showSnackbar("Bibcode copied to clipboard")
                            }
                        },
                        onAddToGroupClick = {
                            showGroupPicker = true
                        }
                    )
                }
                is PaperDetailsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (showGroupPicker) {
            GroupPickerSheet(
                groups = userGroups,
                onDismiss = { showGroupPicker = false },
                onGroupSelected = { groupId ->
                    viewModel.addPaperToGroup(groupId, bibcode)
                    showGroupPicker = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Added to Journal Club")
                    }
                }
            )
        }
    }
}

@Composable
fun PaperDetailsContent(
    paper: PaperModel,
    onCiteClick: () -> Unit,
    onAddToGroupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Category Badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = paper.category.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        AstroPaperTitleText(
            title = paper.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
                letterSpacing = (-0.5).sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCiteClick,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Outlined.FormatQuote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cite")
            }
            OutlinedButton(
                onClick = onAddToGroupClick,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Group")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Authors
        Text(
            text = "AUTHORS",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = paper.authors.joinToString(", "),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Abstract
        Text(
            text = "ABSTRACT",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        AstroAbstractView(
            rawAbstract = paper.abstractText,
            isExpanded = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Metadata
        Text(
            text = "METADATA",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                MetadataRow("Published", paper.dateDisplay)
                MetadataRow("Bibcode", paper.bibcode)
                MetadataRow("Citations", paper.citationCount.toString())
                if (paper.arxivId != null) {
                    MetadataRow("arXiv ID", paper.arxivId!!)
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp)) // Space for bottom bar
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPickerSheet(
    groups: List<GroupModel>,
    onDismiss: () -> Unit,
    onGroupSelected: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Add to Journal Club",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (groups.isEmpty()) {
                Text(
                    "You haven't joined any groups yet.",
                    modifier = Modifier.padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                groups.forEach { group ->
                    Surface(
                        onClick = { onGroupSelected(group.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(group.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

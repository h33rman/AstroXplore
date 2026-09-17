package com.example.astroxplore.features.feed.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import com.example.astroxplore.features.feed.model.PaperModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperDetailsScreen(
    bibcode: String,
    onNavigateBack: () -> Unit,
    viewModel: PaperDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    LaunchedEffect(bibcode) {
        viewModel.loadPaper(bibcode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
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
                            onClick = { /* Open PDF */ },
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
                    PaperDetailsContent(state.paper)
                }
                is PaperDetailsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun PaperDetailsContent(paper: PaperModel) {
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
        Text(
            text = paper.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

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
        Text(
            text = paper.abstractText,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
                textAlign = TextAlign.Justify
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Metadata
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

        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
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

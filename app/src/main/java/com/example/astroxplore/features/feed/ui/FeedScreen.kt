package com.example.astroxplore.features.feed.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.MainViewModel
import com.example.astroxplore.R
import com.example.astroxplore.core.util.findActivity
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.feed.ui.components.PaperCard
import com.example.astroxplore.features.feed.ui.components.PaperCardSkeleton
import com.example.astroxplore.features.feed.ui.components.PaperDetailsBottomSheet
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(LocalContext.current.findActivity()!!),
    onSearchClick: () -> Unit = {},
    onPaperClick: (String) -> Unit = {}
) {
    val papers by viewModel.feedPapers.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val savedPaperIds by viewModel.savedPaperIds.collectAsState()
    
    val listState = rememberLazyListState()
    
    // Bottom Sheet State
    var selectedPaperForDetails by remember { mutableStateOf<PaperModel?>(null) }
    var showAuthorsOnly by remember { mutableStateOf(false) }

    // Listen for Scroll to Top Event
    LaunchedEffect(Unit) {
        mainViewModel.scrollToTopEvent.collectLatest {
            listState.animateScrollToItem(0)
        }
    }
    
    // Modern Collapsing Header Logic
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val firstItemIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val isScrolled = remember { derivedStateOf { firstItemIndex.value > 0 || scrollOffset.value > 20 } }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (isScrolled.value) 0f else 1f,
        animationSpec = tween(300),
        label = "headerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Feed with Pull to Refresh
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 250.dp, bottom = 16.dp)
            ) {
                if (papers.isEmpty() && isRefreshing) {
                    items(5) { PaperCardSkeleton() }
                } else if (papers.isEmpty() && error != null) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(error!!), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    items(papers, key = { it.bibcode }) { paper ->
                        PaperCard(
                            paper = paper,
                            isSaved = savedPaperIds.contains(paper.bibcode),
                            onSaveClick = { viewModel.toggleSavePaper(paper) },
                            onTitleClick = { onPaperClick(paper.bibcode) },
                            onReadMoreClick = {
                                selectedPaperForDetails = paper
                                showAuthorsOnly = false
                            },
                            onAuthorsClick = {
                                selectedPaperForDetails = paper
                                showAuthorsOnly = true
                            }
                        )
                    }
                    if (papers.isNotEmpty()) {
                        item { EndOfFeedMessage() }
                    }
                }
            }
        }

        // Immersive Header Overlay (Sticky Bar)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isScrolled.value) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else Color.Transparent,
            tonalElevation = if (isScrolled.value) 6.dp else 0.dp,
            shadowElevation = if (isScrolled.value) 12.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                // Greeting & Discovery Title
                Box(
                    modifier = Modifier
                        .graphicsLayer { 
                            alpha = headerAlpha
                            translationY = - (1f - headerAlpha) * 30f
                        }
                        .animateContentSize()
                ) {
                    if (headerAlpha > 0.05f) {
                        DiscoveryHeader()
                    }
                }

                // Sticky Search Bar
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    SearchBar(
                        onSearchClick = onSearchClick,
                        isSticky = isScrolled.value
                    )
                }
            }
        }

        // Paper Details Bottom Sheet
        if (selectedPaperForDetails != null) {
            PaperDetailsBottomSheet(
                paper = selectedPaperForDetails!!,
                showAuthorsOnly = showAuthorsOnly,
                onDismiss = { selectedPaperForDetails = null },
                onNavigateToDetails = onPaperClick
            )
        }
    }
}

@Composable
fun DiscoveryHeader() {
    val currentDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        sdf.format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good morning, Researcher",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = stringResource(R.string.discovery),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                lineHeight = 44.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
fun SearchBar(onSearchClick: () -> Unit, isSticky: Boolean = false) {
    val barHeight by animateDpAsState(
        targetValue = if (isSticky) 46.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "barHeight"
    )
    
    val containerColor = if (isSticky) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    Surface(
        onClick = onSearchClick,
        color = containerColor,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight),
        border = if (isSticky) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        },
        tonalElevation = if (isSticky) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isSticky) 18.dp else 22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search 10M+ astrophysics papers...",
                style = if (isSticky) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = if (isSticky) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EndOfFeedMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You're all caught up!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    FeedScreen()
}

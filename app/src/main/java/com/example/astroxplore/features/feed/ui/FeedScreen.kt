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
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.astroxplore.features.groups.ui.components.GroupPickerSheet
import com.example.astroxplore.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    onSearchClick: () -> Unit = {},
    onPaperClick: (String) -> Unit = {},
    onLibraryClick: () -> Unit = {}
) {
    val papers by viewModel.feedPapers.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val savedPaperIds by viewModel.savedPaperIds.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val userGroups by viewModel.userGroups.collectAsState()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Bottom Sheet State
    var selectedPaperForDetails by remember { mutableStateOf<PaperModel?>(null) }
    var selectedPaperForGroup by remember { mutableStateOf<PaperModel?>(null) }
    var showAuthorsOnly by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

    // Listen for Scroll to Top Event (Always reset when Feed tab is clicked)
    LaunchedEffect(Unit) {
        // Force scroll to top on initial entry to ensure header visibility
        listState.scrollToItem(0)
        
        viewModel.scrollToTopEvent.collectLatest {
            listState.animateScrollToItem(0)
        }
    }
    
    // Modern Collapsing Header Logic
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val firstItemIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    // Force header visibility when refreshing, offline, or at the very top
    val isScrolled = remember { 
        derivedStateOf { 
            if (isRefreshing || (!isOnline && papers.isEmpty())) {
                false 
            } else {
                firstItemIndex.value > 0 || scrollOffset.value > 20
            }
        } 
    }
    
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            // Main Feed
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 220.dp, bottom = 16.dp)
                ) {
                    if (!isOnline && papers.isEmpty()) {
                        item {
                            OfflineFeedState(onLibraryClick = onLibraryClick)
                        }
                    } else if (papers.isEmpty() && isRefreshing) {
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
                                onReadMoreClick = { onPaperClick(paper.bibcode) },
                                onAuthorsClick = {
                                    selectedPaperForDetails = paper
                                    showAuthorsOnly = true
                                },
                                onMoreClick = {
                                    selectedPaperForGroup = paper
                                    showGroupPicker = true
                                }
                            )
                        }
                        if (papers.isNotEmpty()) {
                            item { EndOfFeedMessage() }
                        }
                    }
                }
            }
        }

        // Header Overlay
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isScrolled.value) MaterialTheme.colorScheme.surface.copy(alpha = 0.98f) else Color.Transparent,
            tonalElevation = if (isScrolled.value) 4.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                // Greeting & Title
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

                // Search Bar
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

        // Details Sheet
        if (selectedPaperForDetails != null) {
            PaperDetailsBottomSheet(
                paper = selectedPaperForDetails!!,
                showAuthorsOnly = showAuthorsOnly,
                onDismiss = { selectedPaperForDetails = null },
                onNavigateToDetails = onPaperClick
            )
        }

        if (showGroupPicker && selectedPaperForGroup != null) {
            GroupPickerSheet(
                groups = userGroups,
                onDismiss = { showGroupPicker = false },
                onGroupSelected = { groupId ->
                    viewModel.addPaperToGroup(groupId, selectedPaperForGroup!!.bibcode)
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
            
            IconButton(
                onClick = {},
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.discovery),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                lineHeight = 44.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
fun SearchBar(onSearchClick: () -> Unit, isSticky: Boolean = false) {
    val barHeight by animateDpAsState(
        targetValue = if (isSticky) 48.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "barHeight"
    )
    
    val containerColor = if (isSticky) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    Surface(
        onClick = onSearchClick,
        color = containerColor,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
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
                modifier = Modifier.size(if (isSticky) 20.dp else 24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search 10M+ astrophysics papers...",
                style = if (isSticky) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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

@Composable
fun OfflineFeedState(onLibraryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You're offline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Discovery requires an internet connection. Visit your Library to read saved papers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLibraryClick,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Go to Library")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    FeedScreen()
}

package com.example.astroxplore.features.feed.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.R
import com.example.astroxplore.features.feed.ui.components.PaperCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    onSearchClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // Social media scroll detection
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
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
    
    val searchBarTranslation by animateDpAsState(
        targetValue = if (isScrolled.value) (-110).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "searchBarTranslation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Main List (Underneath Header)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 220.dp, bottom = 16.dp)
        ) {
            when (val state = uiState) {
                is FeedUiState.Loading -> {
                    items(5) { PaperCardSkeleton() }
                }
                is FeedUiState.Success -> {
                    items(state.papers, key = { it.bibcode }) { paper ->
                        PaperCard(paper = paper)
                    }
                    if (state.isLastPage) {
                        item { EndOfFeedMessage() }
                    }
                }
                is FeedUiState.LoadingMore -> {
                    items(state.currentPapers, key = { it.bibcode }) { paper ->
                        PaperCard(paper = paper)
                    }
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
                is FeedUiState.Error -> {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(state.messageResId), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Immersive Header Overlay
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isScrolled.value) MaterialTheme.colorScheme.background else Color.Transparent,
            tonalElevation = if (isScrolled.value) 4.dp else 0.dp,
            shadowElevation = if (isScrolled.value) 8.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                // Greeting & Discovery Title (Fades Out)
                Box(
                    modifier = Modifier
                        .graphicsLayer { 
                            alpha = headerAlpha
                            translationY = - (1f - headerAlpha) * 30f
                        }
                        .height(if (isScrolled.value) 0.dp else 110.dp)
                ) {
                    if (headerAlpha > 0.05f) {
                        DiscoveryHeader()
                    }
                }

                // Sticky Search Bar (Stays)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    SearchBar(onSearchClick = onSearchClick)
                }
            }
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
            .padding(horizontal = 20.dp, vertical = 8.dp)
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
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.discovery),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SearchBar(onSearchClick: () -> Unit) {
    Surface(
        onClick = onSearchClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search 10M+ papers...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PaperCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(modifier = Modifier.size(80.dp, 16.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha), MaterialTheme.shapes.small))
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(28.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha), MaterialTheme.shapes.small))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(28.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha), MaterialTheme.shapes.small))
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha), MaterialTheme.shapes.small))
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.size(120.dp, 32.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha), MaterialTheme.shapes.small))
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

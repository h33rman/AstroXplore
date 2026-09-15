package com.example.astroxplore.features.feed.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.astroxplore.R
import com.example.astroxplore.features.feed.model.PaperModel
import com.example.astroxplore.features.feed.ui.components.PaperCard

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val categories = listOf("For You", "Astrophysics", "Galaxies", "Cosmology")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val dummyPapers = remember {
        listOf(
            PaperModel(
                bibcode = "2027arXiv270112345G",
                rawTitles = listOf("Reconstruction of a dark energy model for the Dirac-Born-Infeld scalar field with the Hubble and DESI data via Gaussian process"),
                abstractText = "In this study, we reconstruct the dark energy (DE) as a Dirac-Born-Infeld (DBI) scalar field from the Hubble dataset (32 CC + 26 BAO) and the DESI dataset using...",
                authors = listOf("Ghosh, Sayantan", "Gadbail, Gaurav N.", "Sahoo, P. K.", "Bamba"),
                keywords = listOf("Dark Energy"),
                rawPubDate = "2027-01-01"
            ),
            PaperModel(
                bibcode = "2027arXiv270154321S",
                rawTitles = listOf("Statistical treatment of searches for counterparts of positionally-uncertain astrophysical sources: From flux upper limit..."),
                abstractText = "Rapid growth of the multimessenger and multiwavelength astrophysics had led to an increasing number of observations of the same events with inst...",
                authors = listOf("Sitarek, Julian", "Moralejo, Abelardo", "Jiménez Quiles, Juan"),
                keywords = listOf("Statistical Methods"),
                rawPubDate = "2027-01-01"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        FeedHeader()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CategoryChips(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(dummyPapers) { paper ->
                PaperCard(paper = paper)
            }
        }
    }
}

@Composable
fun FeedHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Hello, ${stringResource(R.string.scholar)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Monday, Sep 14",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.discovery),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = Color.Transparent
                ),
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    FeedScreen()
}

package com.example.astroxplore.features.search.ui

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.R
import com.example.astroxplore.features.feed.ui.components.PaperCard

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
    autofocus: Boolean = false
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autofocus) {
        if (autofocus) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onQueryChange(it) },
            onFilterClick = { showFilters = true },
            modifier = Modifier.focusRequester(focusRequester)
        )

        FilterChips(
            filter = searchFilter,
            onRemoveFilter = { viewModel.updateFilter(it) },
            onClearAll = { viewModel.clearFilters() }
        )

        when (val state = uiState) {
            is ExploreUiState.Idle -> {
                EmptyExploreState()
            }
            is ExploreUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ExploreUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.results) { paper ->
                        PaperCard(paper = paper)
                    }
                }
            }
            is ExploreUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(state.messageResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showFilters) {
        FilterBottomSheet(
            filter = searchFilter,
            onFilterChange = { viewModel.updateFilter(it) },
            onDismiss = { showFilters = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        tonalElevation = 4.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { 
                Text(
                    stringResource(R.string.search_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { 
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) 
            },
            trailingIcon = {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        Icons.Outlined.Tune, 
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    filter: SearchFilter,
    onRemoveFilter: (SearchFilter) -> Unit,
    onClearAll: () -> Unit
) {
    if (!filter.isActive()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            TextButton(onClick = onClearAll) {
                Text("Clear All", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (filter.refereedOnly) {
            filterChipItem("Refereed") { onRemoveFilter(filter.copy(refereedOnly = false)) }
        }
        if (filter.isOpenAccess) {
            filterChipItem("Open Access") { onRemoveFilter(filter.copy(isOpenAccess = false)) }
        }
        if (filter.hasData) {
            filterChipItem("Has Data") { onRemoveFilter(filter.copy(hasData = false)) }
        }

        filter.yearRange?.let { range ->
            filterChipItem("Year: ${range.first}-${range.last}") { onRemoveFilter(filter.copy(yearRange = null)) }
        }

        filter.bibstem?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Journal: $it") { onRemoveFilter(filter.copy(bibstem = null)) }
        }

        filter.author?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Author: $it") { onRemoveFilter(filter.copy(author = null)) }
        }

        filter.firstAuthor?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("1st Author: $it") { onRemoveFilter(filter.copy(firstAuthor = null)) }
        }

        filter.orcid?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("ORCiD: $it") { onRemoveFilter(filter.copy(orcid = null)) }
        }

        filter.titleOnly?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Title: $it") { onRemoveFilter(filter.copy(titleOnly = null)) }
        }

        filter.abstractOnly?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Abs: $it") { onRemoveFilter(filter.copy(abstractOnly = null)) }
        }

        filter.affiliation?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Aff: $it") { onRemoveFilter(filter.copy(affiliation = null)) }
        }

        filter.objectName?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Obj: $it") { onRemoveFilter(filter.copy(objectName = null)) }
        }

        filter.arxivId?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("arXiv: $it") { onRemoveFilter(filter.copy(arxivId = null)) }
        }

        filter.doi?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("DOI: $it") { onRemoveFilter(filter.copy(doi = null)) }
        }

        filter.citationCountMin?.let {
            filterChipItem("Citations > $it") { onRemoveFilter(filter.copy(citationCountMin = null)) }
        }
        
        // Pro Chips
        filter.arxivClass?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Class: $it") { onRemoveFilter(filter.copy(arxivClass = null)) }
        }
        filter.authorCountRange?.let { range ->
            filterChipItem("Authors: ${range.first}-${range.last}") { onRemoveFilter(filter.copy(authorCountRange = null)) }
        }
        filter.bibGroup?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Group: $it") { onRemoveFilter(filter.copy(bibGroup = null)) }
        }
        filter.database?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("DB: $it") { onRemoveFilter(filter.copy(database = null)) }
        }
        filter.docType?.takeIf { it.isNotBlank() }?.let {
            filterChipItem("Type: $it") { onRemoveFilter(filter.copy(docType = null)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.filterChipItem(label: String, onRemove: () -> Unit) {
    item {
        FilterChip(
            selected = true,
            onClick = onRemove,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Text(
                text = "Advanced Search Filters",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Standard Filters
            FilterSectionTitle("Identifiers")
            FilterTextField("arXiv ID", filter.arxivId ?: "", onValueChange = { onFilterChange(filter.copy(arxivId = it)) })
            FilterTextField("DOI", filter.doi ?: "", onValueChange = { onFilterChange(filter.copy(doi = it)) })

            Spacer(modifier = Modifier.height(16.dp))

            FilterSectionTitle("Refine Text Search")
            FilterTextField("Title Only", filter.titleOnly ?: "", onValueChange = { onFilterChange(filter.copy(titleOnly = it)) })
            FilterTextField("Abstract Only", filter.abstractOnly ?: "", onValueChange = { onFilterChange(filter.copy(abstractOnly = it)) })

            Spacer(modifier = Modifier.height(16.dp))

            FilterSectionTitle("Authors")
            FilterTextField("Author (Last, F)", filter.author ?: "", onValueChange = { onFilterChange(filter.copy(author = it)) })
            FilterTextField("First Author Only", filter.firstAuthor ?: "", onValueChange = { onFilterChange(filter.copy(firstAuthor = it)) })
            FilterTextField("ORCiD", filter.orcid ?: "", onValueChange = { onFilterChange(filter.copy(orcid = it)) })

            Spacer(modifier = Modifier.height(16.dp))

            FilterSectionTitle("Date & Publication")
            YearRangeInput(filter, onFilterChange)
            FilterTextField("Journal (bibstem, e.g. ApJ)", filter.bibstem ?: "", onValueChange = { onFilterChange(filter.copy(bibstem = it)) })

            Spacer(modifier = Modifier.height(24.dp))

            // Pro Section Accordion
            var proExpanded by remember { mutableStateOf(false) }
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { proExpanded = !proExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Tune, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "PRO SEARCH FILTERS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Icon(
                            imageVector = if (proExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    AnimatedVisibility(visible = proExpanded) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                            
                            FilterTextField("arXiv Class (e.g. astro-ph.CO)", filter.arxivClass ?: "", onValueChange = {
                                onFilterChange(filter.copy(arxivClass = it))
                            })
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Author Count Range", style = MaterialTheme.typography.labelMedium)
                            AuthorCountRangeInput(filter, onFilterChange)
                            
                            FilterTextField("Bib Group (e.g. HST, JWST)", filter.bibGroup ?: "", onValueChange = {
                                onFilterChange(filter.copy(bibGroup = it))
                            })
                            
                            FilterTextField("Database (astronomy, physics)", filter.database ?: "", onValueChange = {
                                onFilterChange(filter.copy(database = it))
                            })
                            
                            FilterTextField("Doc Type (article, catalog)", filter.docType ?: "", onValueChange = {
                                onFilterChange(filter.copy(docType = it))
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            FilterSectionTitle("Properties")
            PropertyToggle("Refereed Only", "Show only peer-reviewed papers", filter.refereedOnly) {
                onFilterChange(filter.copy(refereedOnly = it))
            }
            PropertyToggle("Open Access", "Show only open access papers", filter.isOpenAccess) {
                onFilterChange(filter.copy(isOpenAccess = it))
            }
            PropertyToggle("Has Data", "Papers with associated data links", filter.hasData) {
                onFilterChange(filter.copy(hasData = it))
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Search with Professional Filters", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(
        text = title, 
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun FilterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    kbType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(keyboardType = kbType),
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun YearRangeInput(filter: SearchFilter, onFilterChange: (SearchFilter) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        var yearStart by remember { mutableStateOf(filter.yearRange?.first?.toString() ?: "") }
        var yearEnd by remember { mutableStateOf(filter.yearRange?.last?.toString() ?: "") }

        OutlinedTextField(
            value = yearStart,
            onValueChange = { 
                yearStart = it
                val start = it.toIntOrNull()
                val end = yearEnd.toIntOrNull() ?: start
                if (start != null && end != null) onFilterChange(filter.copy(yearRange = start..end))
            },
            label = { Text("Start Year") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )
        OutlinedTextField(
            value = yearEnd,
            onValueChange = { 
                yearEnd = it
                val end = it.toIntOrNull()
                val start = yearStart.toIntOrNull() ?: end
                if (start != null && end != null) onFilterChange(filter.copy(yearRange = start..end))
            },
            label = { Text("End Year") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun AuthorCountRangeInput(filter: SearchFilter, onFilterChange: (SearchFilter) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        var countStart by remember { mutableStateOf(filter.authorCountRange?.first?.toString() ?: "") }
        var countEnd by remember { mutableStateOf(filter.authorCountRange?.last?.toString() ?: "") }

        OutlinedTextField(
            value = countStart,
            onValueChange = { 
                countStart = it
                val start = it.toIntOrNull()
                val end = countEnd.toIntOrNull() ?: 1000
                if (start != null) onFilterChange(filter.copy(authorCountRange = start..end))
            },
            label = { Text("Min") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )
        OutlinedTextField(
            value = countEnd,
            onValueChange = { 
                countEnd = it
                val end = it.toIntOrNull()
                val start = countStart.toIntOrNull() ?: 1
                if (end != null) onFilterChange(filter.copy(authorCountRange = start..end))
            },
            label = { Text("Max") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun PropertyToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun EmptyExploreState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Explore,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.ready_to_explore),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(R.string.explore_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.advanced_search_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

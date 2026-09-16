package com.example.astroxplore.features.onboarding.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Step Language
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Step Professional
    var affiliationType by remember { mutableStateOf("") }
    var affiliationName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var educationLevel by remember { mutableStateOf("") }
    var affiliationExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }

    // Step Interests
    val selectedInterests by viewModel.selectedInterests.collectAsState()

    if (showSuccess) {
        SuccessImmersive(onOnboardingComplete)
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Immersive Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Column {
                        if (isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        TopAppBar(
                            title = { 
                                Text(
                                    when(currentStep) {
                                        1 -> "Language"
                                        2 -> "Professional Background"
                                        else -> "Research Interests"
                                    }, 
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ) 
                            },
                            navigationIcon = {
                                if (currentStep > 1) {
                                    IconButton(onClick = { currentStep-- }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack, 
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                        
                        val progress by animateFloatAsState(
                            targetValue = currentStep / 3f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "progress"
                        )
                        
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(MaterialTheme.shapes.small),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                bottomBar = {
                    Box(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                        when (currentStep) {
                            1 -> {
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Text("Next: Background", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            2 -> {
                                Button(
                                    onClick = { currentStep = 3 },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    enabled = affiliationType.isNotEmpty() && country.isNotEmpty() && educationLevel.isNotEmpty(),
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Text("Next: Select Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            3 -> {
                                Button(
                                    onClick = { 
                                        viewModel.completeOnboarding(
                                            affiliationType = affiliationType,
                                            affiliationName = affiliationName,
                                            country = country,
                                            educationLevel = educationLevel,
                                            onSuccess = { showSuccess = true }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    enabled = selectedInterests.size in 3..6 && !isLoading,
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Text(stringResource(R.string.get_started), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "stepTransition"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (error != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(error!!),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        when (step) {
                            1 -> {
                                LanguageStep(
                                    selectedLanguage = selectedLanguage,
                                    onLanguageSelected = { viewModel.selectLanguage(it) },
                                    languages = viewModel.languages
                                )
                            }
                            2 -> {
                                ProfessionalStep(
                                    affiliationType = affiliationType,
                                    onAffiliationTypeChange = { affiliationType = it },
                                    affiliationName = affiliationName,
                                    onAffiliationNameChange = { affiliationName = it },
                                    country = country,
                                    onCountryChange = { country = it },
                                    educationLevel = educationLevel,
                                    onEducationLevelChange = { educationLevel = it },
                                    affiliationExpanded = affiliationExpanded,
                                    onAffiliationExpandedChange = { affiliationExpanded = it },
                                    countryExpanded = countryExpanded,
                                    onCountryExpandedChange = { countryExpanded = it },
                                    educationExpanded = educationExpanded,
                                    onEducationExpandedChange = { educationExpanded = it },
                                    viewModel = viewModel
                                )
                            }
                            3 -> {
                                InterestsStep(
                                    selectedInterests = selectedInterests,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    languages: List<Pair<String, String>>
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "Welcome! Choose your preferred language",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        languages.forEach { (code, name) ->
            val isSelected = selectedLanguage == code
            Surface(
                onClick = { onLanguageSelected(code) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.large,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onLanguageSelected(code) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalStep(
    affiliationType: String,
    onAffiliationTypeChange: (String) -> Unit,
    affiliationName: String,
    onAffiliationNameChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    educationLevel: String,
    onEducationLevelChange: (String) -> Unit,
    affiliationExpanded: Boolean,
    onAffiliationExpandedChange: (Boolean) -> Unit,
    countryExpanded: Boolean,
    onCountryExpandedChange: (Boolean) -> Unit,
    educationExpanded: Boolean,
    onEducationExpandedChange: (Boolean) -> Unit,
    viewModel: OnboardingViewModel
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "Tell us about your research background",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Affiliation
        ExposedDropdownMenuBox(
            expanded = affiliationExpanded,
            onExpandedChange = onAffiliationExpandedChange
        ) {
            OutlinedTextField(
                value = affiliationType,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.affiliation_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = affiliationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.large
            )
            ExposedDropdownMenu(expanded = affiliationExpanded, onDismissRequest = { onAffiliationExpandedChange(false) }) {
                viewModel.affiliationTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) }, 
                        onClick = { 
                            onAffiliationTypeChange(type)
                            onAffiliationExpandedChange(false) 
                        }
                    )
                }
            }
        }

        if (affiliationType != "Individual" && affiliationType.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = affiliationName,
                onValueChange = onAffiliationNameChange,
                label = { Text(stringResource(R.string.affiliation_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Country
        ExposedDropdownMenuBox(
            expanded = countryExpanded,
            onExpandedChange = onCountryExpandedChange
        ) {
            OutlinedTextField(
                value = country,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.country)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.large
            )
            ExposedDropdownMenu(expanded = countryExpanded, onDismissRequest = { onCountryExpandedChange(false) }) {
                viewModel.countries.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) }, 
                        onClick = { 
                            onCountryChange(item)
                            onCountryExpandedChange(false) 
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Education
        ExposedDropdownMenuBox(
            expanded = educationExpanded,
            onExpandedChange = onEducationExpandedChange
        ) {
            OutlinedTextField(
                value = educationLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.education_level)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.large
            )
            ExposedDropdownMenu(expanded = educationExpanded, onDismissRequest = { onEducationExpandedChange(false) }) {
                viewModel.educationLevels.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level) }, 
                        onClick = { 
                            onEducationLevelChange(level)
                            onEducationExpandedChange(false) 
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsStep(
    selectedInterests: List<String>,
    viewModel: OnboardingViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredKeywords by viewModel.filteredKeywords.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select 3-6 topics to personalize your academic feed.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Immersive Search Bar matching InterestsScreen
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search 100+ keywords...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredKeywords.forEach { topic ->
                val isSelected = selectedInterests.contains(topic)
                
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleInterest(topic) },
                    label = { 
                        Text(
                            text = topic, 
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        ) 
                    },
                    shape = MaterialTheme.shapes.large,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "${selectedInterests.size} / 6 selected",
            style = MaterialTheme.typography.labelLarge,
            color = if (selectedInterests.size in 3..6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SuccessImmersive(onComplete: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
        launch {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(600))
        }
        delay(2000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to the Crew!",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(alpha.value)
            )
            Text(
                text = "Your personalized feed is ready.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}

package com.example.astroxplore.features.auth.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.astroxplore.R
import com.example.astroxplore.core.ui.components.DynamicIslandError
import com.example.astroxplore.core.ui.components.LottieLoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var orcidId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val darkTheme = isSystemInDarkTheme()

    var showForm by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showForm = true
    }

    LaunchedEffect(uiState) {
        if (uiState is SignupUiState.Success) {
            if (!(uiState as SignupUiState.Success).needsEmailConfirmation) {
                onSignupSuccess()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High-end Mesh Gradient Background
        val gradientColor = MaterialTheme.colorScheme.secondary.copy(alpha = if (darkTheme) 0.3f else 0.1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(gradientColor, Color.Transparent),
                        center = Offset(1000f, 1000f),
                        radius = 2000f
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    if (uiState is SignupUiState.Loading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, 
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (uiState is SignupUiState.Success && (uiState as SignupUiState.Success).needsEmailConfirmation) {
                    EmailConfirmationView(
                        email = email,
                        onNavigateToLogin = onNavigateToLogin
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        AnimatedVisibility(
                            visible = showForm,
                            enter = fadeIn(tween(1000)) + slideInVertically { it / 2 }
                        ) {
                            Column {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_splash_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = stringResource(R.string.signup),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                Text(
                                    text = "Begin your academic career in the stars.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(48.dp))

                                // Immersive Form
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedTextField(
                                        value = firstName,
                                        onValueChange = { firstName = it },
                                        label = { Text(stringResource(R.string.first_name), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                        modifier = Modifier.weight(1f),
                                        shape = MaterialTheme.shapes.extraLarge,
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                        )
                                    )
                                    OutlinedTextField(
                                        value = lastName,
                                        onValueChange = { lastName = it },
                                        label = { Text(stringResource(R.string.last_name), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                        modifier = Modifier.weight(1f),
                                        shape = MaterialTheme.shapes.extraLarge,
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = institution,
                                    onValueChange = { institution = it },
                                    label = { Text("Institution / University", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = orcidId,
                                    onValueChange = { orcidId = it },
                                    label = { Text("ORCID iD (Optional)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                    shape = MaterialTheme.shapes.extraLarge,
                                    singleLine = true,
                                    placeholder = { Text("0000-0000-0000-0000") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text(stringResource(R.string.email), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text(stringResource(R.string.password), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password",
                                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                Button(
                                    onClick = {
                                        viewModel.signup(
                                            email = email,
                                            password = password,
                                            firstName = firstName,
                                            lastName = lastName,
                                            institution = institution,
                                            orcidId = orcidId
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    enabled = uiState !is SignupUiState.Loading &&
                                            email.isNotEmpty() && password.isNotEmpty() &&
                                            firstName.isNotEmpty() && lastName.isNotEmpty(),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    AnimatedContent(targetState = uiState is SignupUiState.Loading, label = "loading") { isLoading ->
                                        if (isLoading) {
                                            LottieLoadingView(
                                                size = 40,
                                                resId = R.raw.book_loader
                                            )
                                        } else {
                                            Text(text = stringResource(R.string.signup), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(stringResource(R.string.already_have_account_prefix), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                    TextButton(onClick = onNavigateToLogin) {
                                        Text(stringResource(R.string.login), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }

                DynamicIslandError(
                    isVisible = uiState is SignupUiState.Error,
                    message = if (uiState is SignupUiState.Error) stringResource((uiState as SignupUiState.Error).messageResId) else ""
                )
            }
        }
    }
}

@Composable
fun EmailConfirmationView(
    email: String,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Check Your Email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We've sent a verification link to:\n$email",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text("Back to Login", fontWeight = FontWeight.Bold)
        }
    }
}

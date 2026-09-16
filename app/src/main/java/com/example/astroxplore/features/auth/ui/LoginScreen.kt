package com.example.astroxplore.features.auth.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
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

@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
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
        if (uiState is LoginUiState.Success) {
            onLoginSuccess((uiState as LoginUiState.Success).isOnboarded)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High-end Mesh Gradient Background - Sync with theme
        val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.4f else 0.1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(gradientColor, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 1500f
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (uiState is LoginUiState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Branding Logo
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedVisibility(
                        visible = showForm,
                        enter = fadeIn(tween(1000)) + slideInVertically { it / 2 }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.welcome_back),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Continue your research journey",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            // Modern Immersive TextFields
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
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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

                            // Expressive Button
                            Button(
                                onClick = { viewModel.login(email, password) },
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                enabled = uiState !is LoginUiState.Loading && email.isNotEmpty() && password.isNotEmpty(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                )
                            ) {
                                AnimatedContent(targetState = uiState is LoginUiState.Loading, label = "loading") { isLoading ->
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 3.dp)
                                    } else {
                                        Text(text = stringResource(R.string.login), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            TextButton(onClick = onNavigateToSignup) {
                                Row {
                                    Text(stringResource(R.string.dont_have_account_prefix), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.signup), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }

                DynamicIslandError(
                    isVisible = uiState is LoginUiState.Error,
                    message = if (uiState is LoginUiState.Error) stringResource((uiState as LoginUiState.Error).messageResId) else ""
                )
            }
        }
    }
}

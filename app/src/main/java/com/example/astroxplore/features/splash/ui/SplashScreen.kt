package com.example.astroxplore.features.splash.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    val rocketScale = remember { Animatable(0f) }
    val rocketAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    
    // Warp speed effect
    val warpFactor = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            rocketScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        }
        launch {
            rocketAlpha.animateTo(1f, tween(1000))
        }
        delay(500)
        launch {
            textAlpha.animateTo(1f, tween(1000))
        }
        
        delay(1500)
        // Warp Drive Exit
        launch {
            warpFactor.animateTo(50f, tween(1000, easing = ExpoEaseIn))
        }
        launch {
            rocketScale.animateTo(0.2f, tween(800, easing = ExpoEaseIn))
            rocketAlpha.animateTo(0f, tween(600))
        }
        delay(800)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Generative Star Field - Adapt to theme
        val starColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        val stars = remember { List(100) { Offset(Random.nextFloat(), Random.nextFloat()) } }
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { star ->
                drawCircle(
                    color = starColor,
                    radius = (1.dp.toPx() + (warpFactor.value * star.x)),
                    center = Offset(star.x * size.width, star.y * size.height),
                    alpha = starAlpha
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .scale(rocketScale.value)
                    .alpha(rocketAlpha.value),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "ASTROXPLORE",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

private val ExpoEaseIn = Easing { x ->
    if (x == 0f) 0f else Math.pow(2.0, 10.0 * (x - 1.0)).toFloat()
}

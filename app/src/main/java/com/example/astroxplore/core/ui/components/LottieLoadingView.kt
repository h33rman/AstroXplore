package com.example.astroxplore.core.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.*
import com.example.astroxplore.R

@Composable
fun LottieLoadingView(
    modifier: Modifier = Modifier,
    size: Int = 200,
    @RawRes resId: Int = R.raw.book_loader
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(resId)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // Dynamic coloring using Lottie dynamic properties
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(MaterialTheme.colorScheme.primary.toArgb()),
            keyPath = arrayOf("**") // Tints every layer
        )
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress }, // Pass as lambda
            modifier = Modifier.size(size.dp),
            dynamicProperties = dynamicProperties
        )
    }
}

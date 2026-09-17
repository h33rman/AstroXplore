package com.example.astroxplore.features.feed.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astroxplore.core.ui.components.AstroNativeMathView
import com.example.astroxplore.core.util.AstroTextParser

@Composable
fun AstroPaperTitleText(
    title: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    style: TextStyle = MaterialTheme.typography.titleLarge
) {
    // Optimization: Academic titles are parsed into AnnotatedString for high-fidelity native rendering.
    val annotatedTitle = AstroTextParser.parse(title)
    
    Text(
        text = annotatedTitle,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Visible else TextOverflow.Ellipsis
    )
}

@Composable
fun AstroAbstractView(
    rawAbstract: String,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    // Decision: Only use full LaTeX rendering for expanded blocks that actually have 
    // block math ($$) or environments. Standard paragraphs are better rendered as native text.
    val needsLibraryRendering = rawAbstract.contains("$$") || rawAbstract.contains("\\begin{")
    
    if (isExpanded && needsLibraryRendering) {
        AstroNativeMathView(
            latex = rawAbstract,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            fontSize = 16.sp
        )
    } else {
        // High-fidelity native AnnotatedString.
        // FIX: Switched to TextAlign.Start because scientific papers have extremely long words 
        // (chemical names) that break justification and create huge gaps.
        val annotatedAbstract = AstroTextParser.parse(rawAbstract)
        
        Text(
            text = annotatedAbstract,
            modifier = modifier,
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Start, 
                lineHeight = 24.sp
            ),
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

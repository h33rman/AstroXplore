package com.example.astroxplore.features.feed.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astroxplore.features.feed.model.PaperModel

@Composable
fun PaperCard(
    paper: PaperModel,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onCiteClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    onReadMoreClick: () -> Unit = {},
    onAuthorsClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Row: Category (Start) and Date (End)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = paper.category.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                Text(
                    text = paper.dateDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title - Clickable
            AstroPaperTitleText(
                title = paper.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.5).sp
                ),
                modifier = Modifier.clickable { onTitleClick() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Abstract with Justify
            AstroAbstractView(
                rawAbstract = paper.abstractText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReadMoreClick() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Author Badge and "and others"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.medium,
                    onClick = onAuthorsClick
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = paper.authors.firstOrNull()?.split(",")?.firstOrNull() ?: "Researcher",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }
                if (paper.authors.size > 1) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "and others",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .clickable { onAuthorsClick() }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            // Action Bar: Citation, Save, Share | More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Citation
                    InteractionButton(
                        icon = Icons.Outlined.FormatQuote,
                        activeColor = MaterialTheme.colorScheme.secondary,
                        isActive = false,
                        label = paper.citationCount.toString(),
                        onClick = onCiteClick
                    )
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // Save
                    InteractionButton(
                        icon = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        activeColor = MaterialTheme.colorScheme.primary,
                        isActive = isSaved,
                        label = "Save",
                        onClick = onSaveClick
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    // Share
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // More Action
                IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
fun InteractionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaperCardPreview() {
    PaperCard(
        paper = PaperModel(
            bibcode = "2027arXiv270112345G",
            rawTitles = listOf("Very High Precision Astrometry for Exoplanets and Dark Matter with the Habitable Worlds Observatory"),
            abstractText = "Astrometry, one of the oldest branches of astronomy, has been revolutionized by missions like Hipparcos and especially Gaia, which mapped billions of stars with extraordinary precision...",
            authors = listOf("Malbet", "Labadie", "Leger", "Shao", "Gould"),
            keywords = listOf("INSTRUMENTATION AND METHODS FOR ASTROPHYSICS"),
            rawPubDate = "2027-01-01"
        ),
        isSaved = false
    )
}

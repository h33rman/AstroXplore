package com.example.astroxplore.features.feed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astroxplore.features.feed.model.PaperModel

@Composable
fun PaperCard(
    paper: PaperModel,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit = {},
    onGroupClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Row: Category and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = paper.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(3.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        CircleShape
                    ))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = paper.dateDisplay,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            Text(
                text = paper.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Abstract
            Text(
                text = paper.abstractText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Author Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.medium
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
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (paper.authors.size > 1) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "& ${paper.authors.size - 1} colleagues",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InteractionButton(
                        icon = Icons.Outlined.FavoriteBorder,
                        label = paper.citationCount.toString(),
                        onClick = onLikeClick
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    InteractionButton(
                        icon = Icons.Outlined.BookmarkBorder,
                        label = "Save",
                        onClick = onSaveClick
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InteractionButton(
                        icon = Icons.Outlined.Groups,
                        label = "Group",
                        onClick = onGroupClick
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.IosShare,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
            rawTitles = listOf("Reconstruction of a dark energy model for the Dirac-Born-Infeld scalar field with the Hubble and DESI data via Gaussian process"),
            abstractText = "In this study, we reconstruct the dark energy (DE) as a Dirac-Born-Infeld (DBI) scalar field from the Hubble dataset (32 CC + 26 BAO) and the DESI dataset using...",
            authors = listOf("Ghosh, Sayantan", "Gadbail, Gaurav N.", "Sahoo, P. K.", "Bamba"),
            keywords = listOf("Dark Energy"),
            rawPubDate = "2027-01-01"
        )
    )
}

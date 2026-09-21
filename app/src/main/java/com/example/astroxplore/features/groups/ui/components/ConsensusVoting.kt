package com.example.astroxplore.features.groups.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.astroxplore.features.groups.model.GroupPaperModel

@Composable
fun ConsensusVoting(
    papers: List<GroupPaperModel>,
    onVote: (String) -> Unit,
    onUnvote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (papers.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.HowToVote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Text(
                    "No papers to vote on",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(papers.sortedByDescending { it.voteCount }) { paper ->
                VoteItem(
                    paper = paper,
                    onVoteClick = {
                        if (paper.isVotedByMe) onUnvote(paper.id!!)
                        else onVote(paper.id!!)
                    }
                )
            }
        }
    }
}

@Composable
fun VoteItem(
    paper: GroupPaperModel,
    onVoteClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (paper.isVotedByMe) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "color"
    )

    Card(
        onClick = onVoteClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paper.bibcode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Paper in Discussion", // We'd ideally have title here too
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${paper.voteCount} Votes",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onVoteClick) {
                Icon(
                    imageVector = if (paper.isVotedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Vote",
                    tint = if (paper.isVotedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

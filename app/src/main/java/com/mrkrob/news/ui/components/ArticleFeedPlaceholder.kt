package com.mrkrob.news.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ArticleFeedPlaceholder(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "placeholder")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "placeholderAlpha",
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Bone(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                cornerRadius = 16.dp,
            )
            Spacer(Modifier.height(16.dp))
            Line(widthFraction = 0.4f)
            Spacer(Modifier.height(12.dp))
            Line(widthFraction = 1f, height = 20.dp)
            Spacer(Modifier.height(8.dp))
            Line(widthFraction = 0.7f, height = 20.dp)
        }
        repeat(4) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Line(widthFraction = 0.5f)
                    Spacer(Modifier.height(12.dp))
                    Line(widthFraction = 1f, height = 16.dp)
                    Spacer(Modifier.height(8.dp))
                    Line(widthFraction = 0.8f, height = 16.dp)
                }
                Bone(Modifier.size(ThumbnailSize), cornerRadius = 12.dp)
            }
        }
    }
}

@Composable
private fun Line(widthFraction: Float, height: Dp = 12.dp) {
    Bone(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        cornerRadius = height / 2,
    )
}

@Composable
private fun Bone(modifier: Modifier, cornerRadius: Dp) {
    Box(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}

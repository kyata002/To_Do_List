// File: ui/components/StarRotationAnimation.kt
package com.kyata.todolist.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HardCoreAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Animation xoay tròn liên tục khi active
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )

    // Animation scale nhấp nháy
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "starScale"
    )

    // Animation độ trong suốt
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.5f,
        animationSpec = tween(durationMillis = 500),
        label = "starAlpha"
    )

    Icon(
        imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = "Hard Core Mode",
        tint = if (isActive) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .rotate(if (isActive) rotation else 0f)
            .scale(scale)
            .alpha(alpha)
    )
}

@Composable
fun DisabledHardCoreAnimation(
    modifier: Modifier = Modifier
) {
    // Animation nhấp nháy nhẹ khi disabled
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disabledAlphaPulse"
    )

    Icon(
        imageVector = Icons.Outlined.Star,
        contentDescription = "Hard Core Mode Unavailable",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.alpha(alpha)
    )
}
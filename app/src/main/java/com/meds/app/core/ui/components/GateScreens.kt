package com.meds.app.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.core.ui.theme.calma.CalmaMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EntranceEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val TrackWidth = 124.dp
private val TrackHeight = 4.dp
private val ShimmerWidth = TrackWidth * 0.35f

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LoadingContent()
    }
}

@Composable
private fun LoadingContent() {
    val colors = LocalMedsColors.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val plantEntrance = remember { Animatable(0f) }
    val wordmarkEntrance = remember { Animatable(0f) }
    val phraseEntrance = remember { Animatable(0f) }
    val trackEntrance = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            delay(CalmaMotion.SPLASH_STAGGER_MILLIS.toLong())
            plantEntrance.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = CalmaMotion.SPLASH_ENTRANCE_MILLIS,
                    easing = EntranceEasing
                )
            )
        }
        launch {
            delay(180L)
            wordmarkEntrance.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = EntranceEasing)
            )
        }
        launch {
            delay(260L)
            phraseEntrance.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = EntranceEasing)
            )
        }
        launch {
            delay(330L)
            trackEntrance.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360, easing = EntranceEasing)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loadingLoop")
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = CalmaMotion.SPLASH_BREATH_MILLIS,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathPhase"
    )
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(196.dp)
                .graphicsLayer {
                    val entrance = plantEntrance.value
                    val entranceScale = lerp(0.86f, 1f, entrance)
                    alpha = entrance
                    scaleX = entranceScale
                    scaleY = entranceScale
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val haloScale = lerp(0.94f, 1.04f, breathPhase)
                        scaleX = haloScale
                        scaleY = haloScale
                        alpha = lerp(0.10f, 0.26f, breathPhase)
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colors.heroEnd,
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            PlantArt(
                stage = 3,
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer {
                        val loopScale = lerp(0.98f, 1.03f, breathPhase)
                        scaleX = loopScale
                        scaleY = loopScale
                        rotationZ = lerp(-1.2f, 1.2f, breathPhase)
                    }
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.graphicsLayer {
                val progress = wordmarkEntrance.value
                alpha = progress
                translationY = lerp(10.dp.toPx(), 0f, progress)
            }
        ) {
            MedsWordmark()
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.graphicsLayer {
                val progress = phraseEntrance.value
                alpha = progress
                translationY = lerp(8.dp.toPx(), 0f, progress)
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Preparando tu espacio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(6.dp))
            BreathingDots(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .width(TrackWidth)
                .height(TrackHeight)
                .graphicsLayer {
                    val progress = trackEntrance.value
                    alpha = progress
                    scaleX = lerp(0.9f, 1f, progress)
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(ShimmerWidth)
                    .graphicsLayer {
                        val bandWidthPx = ShimmerWidth.toPx()
                        val trackWidthPx = TrackWidth.toPx()
                        translationX = lerp(-bandWidthPx, trackWidthPx, shimmerProgress)
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                primaryColor.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun SetupErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No pudimos iniciar sesión",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Revisa tu conexión y que la autenticación anónima esté activa en Firebase.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        PrimaryActionButton(
            text = "Reintentar",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

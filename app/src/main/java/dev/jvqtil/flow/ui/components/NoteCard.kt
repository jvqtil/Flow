package dev.jvqtil.flow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.ui.NoteUiModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NoteCard(
    note: NoteUiModel,
    shouldAnimate: Boolean,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onAnimationFinished: () -> Unit
) {
    val visibleState = remember(note.id) {
        MutableTransitionState(
            !(shouldAnimate && !isDeleting)
        )
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val actionWidthPx = with(density) {
        64.dp.toPx()
    }

    val offsetX = remember(note.id) {
        Animatable(0f)
    }

    LaunchedEffect(
        shouldAnimate,
        isDeleting
    ) {
        visibleState.targetState = !isDeleting
    }

    LaunchedEffect(
        visibleState.isIdle,
        visibleState.currentState,
        shouldAnimate,
        isDeleting
    ) {
        if (!shouldAnimate || !visibleState.isIdle) {
            return@LaunchedEffect
        }

        if (isDeleting && !visibleState.currentState) {
            onAnimationFinished()
        }

        if (!isDeleting && visibleState.currentState) {
            onAnimationFinished()
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400)
        ) + fadeIn(
            animationSpec = tween(280)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(400)
        ) + fadeOut(
            animationSpec = tween(280)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        IntOffset(
                            x = offsetX.value.roundToInt(),
                            y = 0
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .pointerInput(note.id) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount)
                                            .coerceIn(
                                                -actionWidthPx,
                                                0f
                                            )
                                    )
                                }
                            },

                            onDragEnd = {
                                scope.launch {
                                    val targetValue =
                                        if (offsetX.value <= -actionWidthPx / 2f) {
                                            -actionWidthPx
                                        } else {
                                            0f
                                        }

                                    offsetX.animateTo(
                                        targetValue = targetValue,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        )
                    }
                    .clickable {
                        if (offsetX.value < -1f) {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )

                                onClick()
                            }
                        } else {
                            onClick()
                        }
                    }
                    .padding(18.dp)
            ) {
                Text(
                    text = note.text,
                    maxLines = 3,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
package dev.jvqtil.flow.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.ui.NoteUiModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NoteCard(
    note: NoteUiModel,
    previewLines: Int,
    shouldAnimate: Boolean,
    isDeleting: Boolean,
    isDragging: Boolean,
    closeActionsToken: Int,
    @SuppressLint("ModifierParameter") dragHandleModifier: Modifier = Modifier,
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

    var textLayoutResult by remember(note.id, previewLines) {
        mutableStateOf<TextLayoutResult?>(null)
    }

    val actionWidthPx =
        with(density) {
            64.dp.toPx()
        }

    val offsetX =
        remember(note.id) {
            Animatable(0f)
        }

    var lastCloseActionsToken by remember(note.id) {
        mutableFloatStateOf(0f)
    }

    val surfaceColor =
        MaterialTheme.colorScheme.surfaceContainer

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
        if (
            !shouldAnimate ||
            !visibleState.isIdle
        ) {
            return@LaunchedEffect
        }

        if (
            isDeleting &&
            !visibleState.currentState
        ) {
            onAnimationFinished()
        }

        if (
            !isDeleting &&
            visibleState.currentState
        ) {
            onAnimationFinished()
        }
    }

    LaunchedEffect(closeActionsToken) {
        if (closeActionsToken == 0) {
            return@LaunchedEffect
        }

        if (
            lastCloseActionsToken ==
            closeActionsToken.toFloat()
        ) {
            return@LaunchedEffect
        }

        lastCloseActionsToken =
            closeActionsToken.toFloat()

        offsetX.snapTo(0f)
    }

    val cornerRadius by animateDpAsState(
        targetValue =
            if (isDragging) {
                24.dp
            } else {
                20.dp
            },
        animationSpec = tween(180),
        label = "cornerRadius"
    )

    AnimatedVisibility(
        visibleState = visibleState,
        enter =
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(360)
            ) + fadeIn(
                animationSpec = tween(240)
            ),
        exit =
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(360)
            ) + fadeOut(
                animationSpec = tween(220)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (offsetX.value < -0.5f) {
                IconButton(
                    onClick = {
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(160)
                            )

                            onDelete()
                        }
                    },
                    modifier = Modifier.align(
                        Alignment.CenterEnd
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
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
                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,
                        shape =
                            RoundedCornerShape(
                                cornerRadius
                            )
                    )
                    .then(dragHandleModifier)
                    .pointerInput(note.id) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _,
                                                 dragAmount ->
                                if (isDeleting || isDragging) {
                                    return@detectHorizontalDragGestures
                                }

                                scope.launch {
                                    offsetX.snapTo(
                                        (
                                                offsetX.value +
                                                        dragAmount
                                                ).coerceIn(
                                                -actionWidthPx,
                                                0f
                                            )
                                    )
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val target =
                                        if (
                                            offsetX.value <=
                                            -actionWidthPx / 2f
                                        ) {
                                            -actionWidthPx
                                        } else {
                                            0f
                                        }

                                    offsetX.animateTo(
                                        targetValue = target,
                                        animationSpec = tween(220)
                                    )
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(220)
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
                                    animationSpec = tween(220)
                                )
                            }
                        } else {
                            onClick()
                        }
                    }
                    .padding(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            drawContent()

                            if (textLayoutResult?.hasVisualOverflow == true) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            surfaceColor.copy(alpha = 0f),
                                            surfaceColor.copy(alpha = 0.75f),
                                            surfaceColor
                                        ),
                                        startY = size.height * 0.90f,
                                        endY = size.height
                                    )
                                )
                            }
                        }
                ) {
                    Text(
                        text = note.text,
                        maxLines = previewLines,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        onTextLayout = {
                            textLayoutResult = it
                        }
                    )
                }
            }
        }
    }
}
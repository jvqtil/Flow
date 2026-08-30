package dev.jvqtil.flow.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.ui.EntryUiModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EntryCard(
    foldersEnabled: Boolean,
    swipeGesturesEnabled: Boolean,
    entry: EntryUiModel,
    previewLines: Int,
    shouldAnimate: Boolean,
    isDeleting: Boolean,
    isDragging: Boolean,
    closeActionsToken: Int,
    @SuppressLint("ModifierParameter") dragHandleModifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    onToggleTaskNote: () -> Unit,
    onSwitchFolder: () -> Unit,
    onAnimationFinished: () -> Unit
) {
    val visibleState = remember(entry.id) {
        MutableTransitionState(
            !(shouldAnimate && !isDeleting)
        )
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var textLayoutResult by remember(entry.id, previewLines) {
        mutableStateOf<TextLayoutResult?>(null)
    }

    val taskToolbarWidth = 96.dp
    val actionToolbarWidth = if (foldersEnabled) {
        128.dp
    } else {
        56.dp
    }

    val taskToolbarWidthPx = with(density) {
        taskToolbarWidth.toPx()
    }

    val actionToolbarWidthPx = with(density) {
        actionToolbarWidth.toPx()
    }

    val offsetX = remember(entry.id) {
        Animatable(0f)
    }

    var lastCloseActionsToken by remember(entry.id) {
        mutableFloatStateOf(0f)
    }

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer

    val textStartPadding by animateDpAsState(
        targetValue =
            if (entry.type == ENTRY_TYPE_TASK) {
                34.dp
            } else {
                0.dp
            },
        animationSpec = tween(220),
        label = "textStartPadding"
    )

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

        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(180)
        )
    }

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
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                if (offsetX.value > 0.5f) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                onToggleTaskNote()

                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(220)
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(taskToolbarWidth)
                    ) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription =
                                    if (
                                        entry.type ==
                                        ENTRY_TYPE_TASK
                                    ) {
                                        stringResource(
                                            R.string.convert_to_task_label
                                        )
                                    } else {
                                        stringResource(
                                            R.string.convert_to_note_label
                                        )
                                    },
                                tint =
                                    MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Text(
                                text =
                                    if (
                                        entry.type ==
                                        ENTRY_TYPE_TASK
                                    ) {
                                        stringResource(
                                            R.string.note_label
                                        )
                                    } else {
                                        stringResource(
                                            R.string.task_label
                                        )
                                    },
                                color =
                                    MaterialTheme.colorScheme.primary,
                                style =
                                    MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                if (offsetX.value < -0.5f) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(actionToolbarWidth),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (foldersEnabled) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(180)
                                        )

                                        onSwitchFolder()
                                    }
                                },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription =
                                        stringResource(
                                            R.string.switch_folder_label
                                        ),
                                    tint =
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(180)
                                    )

                                    onDelete()
                                }
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription =
                                    stringResource(
                                        R.string.delete_label
                                    ),
                                tint =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
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
                        color = surfaceColor,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .then(dragHandleModifier)
                    .then(
                        if (swipeGesturesEnabled) {
                            Modifier.pointerInput(entry.id) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->

                                        if (
                                            isDeleting ||
                                            isDragging
                                        ) {
                                            return@detectHorizontalDragGestures
                                        }

                                        scope.launch {
                                            val newOffset =
                                                (
                                                        offsetX.value +
                                                                dragAmount
                                                        ).coerceIn(
                                                        -actionToolbarWidthPx,
                                                        taskToolbarWidthPx
                                                    )

                                            offsetX.snapTo(newOffset)
                                        }
                                    },
                                    onDragEnd = {
                                        scope.launch {
                                            val taskToolbarThreshold =
                                                taskToolbarWidthPx / 2f

                                            val actionToolbarThreshold =
                                                actionToolbarWidthPx / 2f

                                            val target =
                                                when {
                                                    offsetX.value >=
                                                            taskToolbarThreshold -> {
                                                        taskToolbarWidthPx
                                                    }

                                                    offsetX.value <=
                                                            -actionToolbarThreshold -> {
                                                        -actionToolbarWidthPx
                                                    }

                                                    else -> {
                                                        0f
                                                    }
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
                        } else {
                            Modifier
                        })
                    .clickable {
                        if (
                            offsetX.value < -1f ||
                            offsetX.value > 1f
                        ) {
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

                            if (
                                textLayoutResult
                                    ?.hasVisualOverflow == true
                            ) {
                                drawRect(
                                    brush =
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                surfaceColor.copy(
                                                    alpha = 0f
                                                ),
                                                surfaceColor.copy(
                                                    alpha = 0.75f
                                                ),
                                                surfaceColor
                                            ),
                                            startY =
                                                size.height * 0.90f,
                                            endY = size.height
                                        )
                                )
                            }
                        }
                ) {
                    AnimatedVisibility(
                        visible = entry.type == ENTRY_TYPE_TASK,
                        enter =
                            fadeIn(
                                animationSpec = tween(280)
                            ) + scaleIn(
                                initialScale = 0.80f,
                                animationSpec = tween(320)
                            ),
                        exit =
                            fadeOut(
                                animationSpec = tween(240)
                            ) + scaleOut(
                                targetScale = 0.80f,
                                animationSpec = tween(280)
                            ),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        TaskCheckbox(
                            checked = entry.completed,
                            onClick = onToggleCompleted
                        )
                    }

                    if (entry.hasAttachments) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                        )
                    }

                    Text(
                        text = entry.text,
                        maxLines = previewLines,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = textStartPadding,
                                end = if (entry.hasAttachments) {
                                    24.dp
                                } else {
                                    0.dp
                                }
                            ),
                        onTextLayout = {
                            textLayoutResult = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 90 else 140
        ),
        label = "pressScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue =
            if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        animationSpec = tween(160),
        label = "backgroundColor"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(
                if (!checked) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(6.dp)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter =
                fadeIn(tween(100)) +
                        scaleIn(
                            initialScale = 0.5f,
                            animationSpec = tween(140)
                        ),
            exit =
                fadeOut(tween(80)) +
                        scaleOut(
                            targetScale = 0.5f,
                            animationSpec = tween(100)
                        )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.completed_label),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
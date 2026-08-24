package dev.jvqtil.flow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.ui.NoteUiModel

@Composable
fun NoteCard(
    note: NoteUiModel,
    shouldAnimate: Boolean,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onAnimationFinished: () -> Unit
) {
    val visibleState = remember(note.id) {
        MutableTransitionState(
            if (shouldAnimate && !isDeleting) {
                false
            } else {
                true
            }
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    onClick = onClick
                )
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
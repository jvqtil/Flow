package dev.jvqtil.flow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun UndoPopup(
    id: String,
    onUndo: () -> Unit,
    onTimeout: () -> Unit
) {
    var visible by remember(id) {
        mutableStateOf(false)
    }

    var clicked by remember(id) {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        visible = true

        delay(3.seconds)

        if (!clicked) {
            visible = false

            delay(250.milliseconds)

            onTimeout()
        }
    }

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) +
                        fadeIn(
                            animationSpec = tween(200)
                        ),
            exit =
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(250)
                ) +
                        fadeOut(
                            animationSpec = tween(180)
                        )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (clicked) return@clickable

                        clicked = true

                        scope.launch {
                            visible = false

                            delay(250.milliseconds)

                            onUndo()
                        }
                    }
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = stringResource(
                            R.string.undo_label
                        ),
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )
                }
            }
        }
    }
}
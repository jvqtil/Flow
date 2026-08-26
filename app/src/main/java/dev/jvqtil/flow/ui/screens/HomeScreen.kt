package dev.jvqtil.flow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.jvqtil.flow.data.Entry
import dev.jvqtil.flow.ui.EntryUiModel
import dev.jvqtil.flow.ui.components.AddButton
import dev.jvqtil.flow.ui.components.EntryCard
import dev.jvqtil.flow.ui.components.UndoPopup
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    entries: List<EntryUiModel>,
    previewLines: Int,
    shouldScrollToTop: Boolean,
    onScrollToTopHandled: () -> Unit,
    pendingDeletedEntries: Map<String, Entry>,
    undoEntry: Entry?,
    restoringEntryId: String?,
    deletingEntriesIds: Set<String>,
    onUndo: () -> Unit,
    onUndoTimeout: () -> Unit,
    onAnimationFinished: (String) -> Unit,
    onNewEntry: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onReorderEntries: (List<String>) -> Unit,
    onToggleCompleted: (String) -> Unit,
    onToggleTaskNote: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(shouldScrollToTop) {
        if (!shouldScrollToTop) return@LaunchedEffect

        delay(100.milliseconds)

        if (listState.layoutInfo.totalItemsCount > 0) {
            listState.animateScrollToItem(0)
        }

        onScrollToTopHandled()
    }

    var localEntries by remember {
        mutableStateOf(entries)
    }

    var closeActionsToken by remember {
        mutableIntStateOf(0)
    }

    var deletedEntriesPositions by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    LaunchedEffect(entries) {
        localEntries = entries
    }

    fun closeActions() {
        closeActionsToken++
    }

    val reorderableState =
        rememberReorderableLazyListState(
            lazyListState = listState
        ) { from, to ->
            val fromId = from.key as String
            val toId = to.key as String

            val fromIndex =
                localEntries.indexOfFirst {
                    it.id == fromId
                }

            val toIndex =
                localEntries.indexOfFirst {
                    it.id == toId
                }

            if (
                fromIndex >= 0 &&
                toIndex >= 0 &&
                fromIndex != toIndex
            ) {
                localEntries =
                    localEntries.toMutableList().apply {
                        add(
                            toIndex,
                            removeAt(fromIndex)
                        )
                    }
            }
        }

    val visibleEntries =
        buildList {
            addAll(localEntries)

            pendingDeletedEntries.values.forEach { deleted ->
                if (none { it.id == deleted.id }) {
                    val position =
                        deletedEntriesPositions[deleted.id]
                            ?: size

                    add(
                        position.coerceIn(0, size),
                        EntryUiModel(
                            id = deleted.id,
                            text = deleted.text
                        )
                    )
                }
            }
        }

    LaunchedEffect(
        localEntries,
        entries,
        deletingEntriesIds
    ) {
        if (deletingEntriesIds.isNotEmpty()) {
            return@LaunchedEffect
        }

        if (localEntries.map { it.id } != entries.map { it.id }) {
            delay(350.milliseconds)

            if (
                deletingEntriesIds.isEmpty() &&
                localEntries.map { it.id } != entries.map { it.id }
            ) {
                onReorderEntries(
                    localEntries.map { it.id }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Text(
            text = "Flow",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 20.dp,
                    top = 18.dp
                )
        )

        IconButton(
            onClick = {
                closeActions()
                onOpenSettings()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        if (visibleEntries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 70.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = visibleEntries,
                        key = { it.id }
                    ) { entry ->

                        val isDeleting =
                            entry.id in deletingEntriesIds

                        val isRestoring =
                            entry.id == restoringEntryId

                        val canReorder =
                            !isDeleting &&
                                    !isRestoring

                        ReorderableItem(
                            state = reorderableState,
                            key = entry.id,
                            enabled = canReorder
                        ) { isDragging ->
                            val scale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue =
                                    if (isDragging) {
                                        1.025f
                                    } else {
                                        1f
                                    },
                                animationSpec = tween(180),
                                label = "dragScale"
                            )

                            Box(
                                modifier = Modifier
                                    .zIndex(
                                        if (isDragging) {
                                            10f
                                        } else {
                                            0f
                                        }
                                    )
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            ) {
                                EntryCard(
                                    entry = entry,
                                    previewLines = previewLines,
                                    shouldAnimate =
                                        isDeleting || isRestoring,
                                    isDeleting = isDeleting,
                                    isDragging = isDragging,
                                    closeActionsToken = closeActionsToken,
                                    dragHandleModifier = with(this) {
                                        Modifier.longPressDraggableHandle()
                                    },
                                    onClick = {
                                        if (
                                            !isDeleting &&
                                            !isRestoring &&
                                            !isDragging
                                        ) {
                                            closeActions()
                                            onOpenEntry(entry.id)
                                        }
                                    },
                                    onDelete = {
                                        if (
                                            !isDeleting &&
                                            !isRestoring &&
                                            !isDragging
                                        ) {
                                            deletedEntriesPositions =
                                                deletedEntriesPositions + (
                                                        entry.id to
                                                                localEntries.indexOfFirst {
                                                                    it.id == entry.id
                                                                }
                                                        )

                                            closeActions()
                                            onDeleteEntry(entry.id)
                                        }
                                    },
                                    onToggleCompleted = {
                                        if (
                                            !isDeleting &&
                                            !isRestoring &&
                                            !isDragging
                                        ) {
                                            onToggleCompleted(entry.id)
                                        }
                                    },
                                    onToggleTaskNote = {
                                        if (
                                            !isDeleting &&
                                            !isRestoring &&
                                            !isDragging
                                        ) {
                                            onToggleTaskNote(entry.id)
                                        }
                                    },
                                    onAnimationFinished = {
                                        deletedEntriesPositions =
                                            deletedEntriesPositions - entry.id

                                        onAnimationFinished(entry.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            AddButton(
                onClick = {
                    closeActions()
                    onNewEntry()
                }
            )
        }

        AnimatedVisibility(
            visible = undoEntry != null,
            modifier = Modifier.fillMaxSize(),
            enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(200)
                ),
            exit =
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(140)
                ) + fadeOut(
                    animationSpec = tween(100)
                )
        ) {
            undoEntry?.let { entry ->
                UndoPopup(
                    entryId = entry.id,
                    onUndo = {
                        closeActions()
                        onUndo()
                    },
                    onTimeout = onUndoTimeout
                )
            }
        }
    }
}
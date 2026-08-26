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
import dev.jvqtil.flow.data.Note
import dev.jvqtil.flow.ui.NoteUiModel
import dev.jvqtil.flow.ui.components.AddButton
import dev.jvqtil.flow.ui.components.NoteCard
import dev.jvqtil.flow.ui.components.UndoPopup
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    notes: List<NoteUiModel>,
    previewLines: Int,
    shouldScrollToTop: Boolean,
    onScrollToTopHandled: () -> Unit,
    pendingDeletedNotes: Map<String, Note>,
    undoNote: Note?,
    restoringNoteId: String?,
    deletingNoteIds: Set<String>,
    onUndo: () -> Unit,
    onUndoTimeout: () -> Unit,
    onAnimationFinished: (String) -> Unit,
    onAddNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onReorderNotes: (List<String>) -> Unit
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

    var localNotes by remember {
        mutableStateOf(notes)
    }

    var closeActionsToken by remember {
        mutableIntStateOf(0)
    }

    var deletedNotePositions by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    LaunchedEffect(notes) {
        localNotes = notes
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
                localNotes.indexOfFirst {
                    it.id == fromId
                }

            val toIndex =
                localNotes.indexOfFirst {
                    it.id == toId
                }

            if (
                fromIndex >= 0 &&
                toIndex >= 0 &&
                fromIndex != toIndex
            ) {
                localNotes =
                    localNotes.toMutableList().apply {
                        add(
                            toIndex,
                            removeAt(fromIndex)
                        )
                    }
            }
        }

    val visibleNotes =
        buildList {
            addAll(localNotes)

            pendingDeletedNotes.values.forEach { deleted ->
                if (none { it.id == deleted.id }) {
                    val position =
                        deletedNotePositions[deleted.id]
                            ?: size

                    add(
                        position.coerceIn(0, size),
                        NoteUiModel(
                            id = deleted.id,
                            text = deleted.text
                        )
                    )
                }
            }
        }

    LaunchedEffect(
        localNotes,
        notes,
        deletingNoteIds
    ) {
        if (deletingNoteIds.isNotEmpty()) {
            return@LaunchedEffect
        }

        if (localNotes.map { it.id } != notes.map { it.id }) {
            delay(350.milliseconds)

            if (
                deletingNoteIds.isEmpty() &&
                localNotes.map { it.id } != notes.map { it.id }
            ) {
                onReorderNotes(
                    localNotes.map { it.id }
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

        if (visibleNotes.isEmpty()) {
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
                        items = visibleNotes,
                        key = { it.id }
                    ) { note ->

                        val isDeleting =
                            note.id in deletingNoteIds

                        val isRestoring =
                            note.id == restoringNoteId

                        val canReorder =
                            !isDeleting &&
                                    !isRestoring

                        ReorderableItem(
                            state = reorderableState,
                            key = note.id,
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
                                NoteCard(
                                    note = note,
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
                                            onOpenNote(note.id)
                                        }
                                    },
                                    onDelete = {
                                        if (
                                            !isDeleting &&
                                            !isRestoring &&
                                            !isDragging
                                        ) {
                                            deletedNotePositions =
                                                deletedNotePositions + (
                                                        note.id to localNotes.indexOfFirst {
                                                            it.id == note.id
                                                        }
                                                        )

                                            closeActions()
                                            onDeleteNote(note.id)
                                        }
                                    },
                                    onAnimationFinished = {
                                        deletedNotePositions =
                                            deletedNotePositions - note.id

                                        onAnimationFinished(note.id)
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
                    onAddNote()
                }
            )
        }

        AnimatedVisibility(
            visible = undoNote != null,
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
            undoNote?.let { note ->
                UndoPopup(
                    noteId = note.id,
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
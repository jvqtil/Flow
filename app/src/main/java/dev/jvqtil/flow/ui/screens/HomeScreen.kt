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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.data.Note
import dev.jvqtil.flow.ui.NoteUiModel
import dev.jvqtil.flow.ui.components.AddButton
import dev.jvqtil.flow.ui.components.NoteCard
import dev.jvqtil.flow.ui.components.UndoPopup

@Composable
fun HomeScreen(
    notes: List<NoteUiModel>,
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
    onOpenSettings: () -> Unit
) {
    val visibleNotes = buildList {
        addAll(notes)

        pendingDeletedNotes.values.forEach { deleted ->
            if (none { it.id == deleted.id }) {
                add(
                    NoteUiModel(
                        id = deleted.id,
                        text = deleted.text
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Text(
            text = "Flow",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 20.dp,
                    top = 18.dp
                )
        )

        IconButton(
            onClick = onOpenSettings,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 70.dp,
                        bottom = 96.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = visibleNotes,
                    key = { it.id }
                ) { note ->
                    val isDeleting = note.id in deletingNoteIds
                    val isRestoring = note.id == restoringNoteId

                    NoteCard(
                        note = note,
                        shouldAnimate = isDeleting || isRestoring,
                        isDeleting = isDeleting,
                        onAnimationFinished = {
                            onAnimationFinished(note.id)
                        },
                        onDelete = {
                            if (!isDeleting && !isRestoring) {
                                onDeleteNote(note.id)
                            }
                        },
                        onClick = {
                            if (!isDeleting && !isRestoring) {
                                onOpenNote(note.id)
                            }
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            AddButton(
                onClick = onAddNote
            )
        }

        AnimatedVisibility(
            visible = undoNote != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(
                animationSpec = tween(200)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(
                animationSpec = tween(200)
            )
        ) {
            undoNote?.let { note ->
                UndoPopup(
                    noteId = note.id,
                    onUndo = onUndo,
                    onTimeout = onUndoTimeout
                )
            }
        }
    }
}
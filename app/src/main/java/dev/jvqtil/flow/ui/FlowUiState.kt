package dev.jvqtil.flow.ui

import dev.jvqtil.flow.data.Note

data class FlowUiState(
    val notes: List<NoteUiModel> = emptyList(),
    val pendingDeletedNotes: Map<String, Note> = emptyMap(),
    val undoNote: Note? = null,
    val restoringNoteId: String? = null,
    val deletingNoteIds: Set<String> = emptySet()
)
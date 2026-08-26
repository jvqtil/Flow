package dev.jvqtil.flow.ui

import dev.jvqtil.flow.data.Entry

data class FlowUiState(
    val entries: List<EntryUiModel> = emptyList(),
    val pendingDeletedEntries: Map<String, Entry> = emptyMap(),
    val undoEntry: Entry? = null,
    val restoringEntryId: String? = null,
    val deletingEntriesIds: Set<String> = emptySet()
)
package dev.jvqtil.flow.ui

import dev.jvqtil.flow.data.ENTRY_TYPE_NOTE

data class EntryUiModel(
    val id: String,
    val text: String,
    val type: String = ENTRY_TYPE_NOTE,
    val completed: Boolean = false
)
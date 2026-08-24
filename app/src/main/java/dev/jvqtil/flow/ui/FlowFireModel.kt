package dev.jvqtil.flow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.jvqtil.flow.data.Note
import dev.jvqtil.flow.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class FlowFireModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlowUiState())
    val uiState: StateFlow<FlowUiState> = _uiState.asStateFlow()

    private val databaseMutex = Mutex()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            repository.observeNotes().collect { notes ->
                _uiState.update {
                    it.copy(
                        notes = notes.map(::toUiModel)
                    )
                }
            }
        }
    }

    suspend fun getNote(id: String): NoteUiModel? {
        return databaseMutex.withLock {
            repository
                .getNote(id)
                ?.let(::toUiModel)
        }
    }

    fun createNote(): NoteUiModel {
        return NoteUiModel(
            id = UUID.randomUUID().toString(),
            text = ""
        )
    }

    fun saveNewNote(note: NoteUiModel) {
        if (note.text.isBlank()) {
            return
        }

        viewModelScope.launch {
            databaseMutex.withLock {
                repository.insertNote(
                    note.toDataModel()
                )
            }
        }
    }

    fun updateNote(note: NoteUiModel) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val existing = repository.getNote(note.id)
                    ?: return@withLock

                if (note.text.isBlank()) {
                    deleteExistingNote(existing)
                    return@withLock
                }

                repository.updateNote(
                    note.toDataModel(
                        createdAt = existing.createdAt
                    )
                )
            }
        }
    }

    fun deleteNote(note: NoteUiModel) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val existing = repository.getNote(note.id)
                    ?: return@withLock

                deleteExistingNote(existing)
            }
        }
    }

    private suspend fun deleteExistingNote(note: Note) {
        repository.deleteNote(note)

        _uiState.update {
            it.copy(
                pendingDeletedNotes =
                    it.pendingDeletedNotes + (note.id to note),
                undoNote = note,
                deletingNoteIds =
                    it.deletingNoteIds + note.id,
                restoringNoteId = null
            )
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            databaseMutex.withLock {
                val note = _uiState.value.undoNote
                    ?: return@withLock

                repository.restoreNote(note)

                _uiState.update {
                    it.copy(
                        pendingDeletedNotes =
                            it.pendingDeletedNotes - note.id,
                        undoNote = null,
                        deletingNoteIds =
                            it.deletingNoteIds - note.id,
                        restoringNoteId = note.id
                    )
                }
            }
        }
    }

    fun clearDeletedNote() {
        _uiState.update {
            it.copy(
                undoNote = null
            )
        }
    }

    fun clearDeletedAnimation(id: String) {
        _uiState.update {
            it.copy(
                pendingDeletedNotes =
                    it.pendingDeletedNotes - id,
                deletingNoteIds =
                    it.deletingNoteIds - id
            )
        }
    }

    fun clearRestoringNote() {
        _uiState.update {
            it.copy(
                restoringNoteId = null
            )
        }
    }

    private fun toUiModel(note: Note): NoteUiModel {
        return NoteUiModel(
            id = note.id,
            text = note.text
        )
    }

    private fun NoteUiModel.toDataModel(
        createdAt: Long = System.currentTimeMillis()
    ): Note {
        return Note(
            id = id,
            text = text,
            createdAt = createdAt
        )
    }
}

class FlowFireModelFactory(
    private val repository: NoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FlowFireModel::class.java)) {
            return FlowFireModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
package dev.jvqtil.flow.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.jvqtil.flow.data.Attachment
import dev.jvqtil.flow.data.ENTRY_TYPE_NOTE
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.data.Entry
import dev.jvqtil.flow.data.FlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class FlowFireModel(
    private val repository: FlowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlowUiState())
    val uiState: StateFlow<FlowUiState> = _uiState.asStateFlow()

    private val databaseMutex = Mutex()

    init {
        observeEntries()
    }

    private fun observeEntries() {
        viewModelScope.launch {
            repository.observeEntries().collect { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries.map(::toUiModel)
                    )
                }
            }
        }
    }

    suspend fun getEntry(
        id: String
    ): EntryUiModel? {
        return databaseMutex.withLock {
            repository
                .findById(id)
                ?.let(::toUiModel)
        }
    }

    fun createEntry(): EntryUiModel {
        return EntryUiModel(
            id = UUID.randomUUID().toString(),
            text = ""
        )
    }

    suspend fun ensureEntryExists(
        entry: EntryUiModel
    ) {
        databaseMutex.withLock {
            if (repository.findById(entry.id) != null) {
                return@withLock
            }

            repository.insertAtTop(
                entry.copy(
                    text = trimEmptyLines(entry.text)
                ).toDataModel()
            )
        }
    }

    fun saveNewEntry(
        entry: EntryUiModel
    ) {
        val normalizedEntry = entry.copy(
            text = trimEmptyLines(entry.text)
        )

        if (normalizedEntry.text.isBlank()) {
            return
        }

        viewModelScope.launch {
            databaseMutex.withLock {
                repository.insertAtTop(
                    normalizedEntry.toDataModel()
                )
            }
        }
    }

    fun updateEntry(
        entry: EntryUiModel,
        hasAttachments: Boolean
    ) {
        val normalizedEntry = entry.copy(
            text = trimEmptyLines(entry.text)
        )

        viewModelScope.launch {
            databaseMutex.withLock {
                val existing =
                    repository.findById(normalizedEntry.id)
                        ?: return@withLock

                if (
                    normalizedEntry.text.isBlank() &&
                    !hasAttachments
                ) {
                    deleteExistingEntry(existing)
                    return@withLock
                }

                repository.update(
                    normalizedEntry.toDataModel(
                        createdAt = existing.createdAt,
                        position = existing.position
                    )
                )
            }
        }
    }

    fun updateEntriesPositions(
        entriesIds: List<String>
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                repository.updateEntriesPositions(entriesIds)
            }
        }
    }

    fun deleteEntry(
        entry: EntryUiModel
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val existing =
                    repository.findById(entry.id)
                        ?: return@withLock

                deleteExistingEntry(existing)
            }
        }
    }

    private suspend fun deleteExistingEntry(
        entry: Entry
    ) {
        repository.delete(entry)

        _uiState.update {
            it.copy(
                pendingDeletedEntries =
                    it.pendingDeletedEntries +
                            (entry.id to entry),
                undoEntry = entry,
                deletingEntriesIds =
                    it.deletingEntriesIds + entry.id,
                restoringEntryId = null
            )
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            databaseMutex.withLock {
                val entry =
                    _uiState.value.undoEntry
                        ?: return@withLock

                repository.restore(entry)

                _uiState.update {
                    it.copy(
                        pendingDeletedEntries =
                            it.pendingDeletedEntries - entry.id,
                        undoEntry = null,
                        deletingEntriesIds =
                            it.deletingEntriesIds - entry.id,
                        restoringEntryId = entry.id
                    )
                }
            }
        }
    }

    fun clearDeletedEntry() {
        viewModelScope.launch {
            databaseMutex.withLock {
                val entry =
                    _uiState.value.undoEntry
                        ?: return@withLock

                repository.purgeAttachments(entry.id)

                _uiState.update {
                    it.copy(
                        undoEntry = null
                    )
                }
            }
        }
    }

    fun clearDeletedAnimation(
        id: String
    ) {
        _uiState.update {
            it.copy(
                pendingDeletedEntries =
                    it.pendingDeletedEntries - id,
                deletingEntriesIds =
                    it.deletingEntriesIds - id
            )
        }
    }

    fun clearRestoringEntry() {
        _uiState.update {
            it.copy(
                restoringEntryId = null
            )
        }
    }

    fun toggleCompleted(
        entryId: String
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val existing =
                    repository.findById(entryId)
                        ?: return@withLock

                if (existing.type != ENTRY_TYPE_TASK) {
                    return@withLock
                }

                repository.update(
                    existing.copy(
                        completed = !existing.completed
                    )
                )
            }
        }
    }

    fun toggleTaskNote(
        entryId: String
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val existing =
                    repository.findById(entryId)
                        ?: return@withLock

                val newType =
                    if (existing.type == ENTRY_TYPE_TASK) {
                        ENTRY_TYPE_NOTE
                    } else {
                        ENTRY_TYPE_TASK
                    }

                repository.update(
                    existing.copy(
                        type = newType,
                        completed =
                            if (newType == ENTRY_TYPE_TASK) {
                                existing.completed
                            } else {
                                false
                            }
                    )
                )
            }
        }
    }

    suspend fun addAttachments(
        entryId: String,
        uris: List<Uri>
    ) {
        databaseMutex.withLock {
            if (repository.findById(entryId) == null) {
                return@withLock
            }

            uris.forEach { uri ->
                repository.insertAttachment(
                    entryId = entryId,
                    uri = uri
                )
            }
        }
    }

    fun deleteAttachment(
        attachment: Attachment
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                repository.deleteAttachment(
                    attachment
                )
            }
        }
    }

    private fun toUiModel(
        entry: Entry
    ): EntryUiModel {
        return EntryUiModel(
            id = entry.id,
            text = entry.text,
            type = entry.type,
            completed = entry.completed
        )
    }

    private fun EntryUiModel.toDataModel(
        createdAt: Long = System.currentTimeMillis(),
        position: Long = 0L
    ): Entry {
        return Entry(
            id = id,
            text = text,
            createdAt = createdAt,
            position = position,
            type = type,
            completed = completed
        )
    }
}

class FlowFireModelFactory(
    private val repository: FlowRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FlowFireModel::class.java)) {
            return FlowFireModel(
                repository = repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

private fun trimEmptyLines(text: String): String {
    return text
        .replace(Regex("""\A(?:[ \t]*\r?\n)+"""), "")
        .replace(Regex("""(?:\r?\n[ \t]*)+\z"""), "")
}
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
import dev.jvqtil.flow.data.Folder
import dev.jvqtil.flow.data.MASTER_FOLDER_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class EntryUiModel(
    val id: String,
    val text: String,
    val type: String = ENTRY_TYPE_NOTE,
    val completed: Boolean = false,
    val folderId: String,
    val hasAttachments: Boolean = false
)

data class FolderUiModel(
    val id: String, val name: String
)

sealed interface UndoOperation {
    data class EntryDeleted(
        val entry: Entry
    ) : UndoOperation

    data class FolderDeleted(
        val snapshot: dev.jvqtil.flow.data.DeletedFolderSnapshot
    ) : UndoOperation
}

data class FlowUiState(
    val entries: List<EntryUiModel> = emptyList(),
    val folders: List<FolderUiModel> = emptyList(),
    val selectedFolderId: String? = null,
    val pendingDeletedEntries: Map<String, Entry> = emptyMap(),
    val undoOperation: UndoOperation? = null,
    val restoringEntryId: String? = null,
    val deletingEntriesIds: Set<String> = emptySet()
)

class FlowFireModel(
    private val repository: FlowRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            FlowUiState()
        )

    val uiState: StateFlow<FlowUiState> =
        _uiState.asStateFlow()

    private val databaseMutex =
        Mutex()

    init {
        observeFolders()
        observeEntries()

        viewModelScope.launch {
            databaseMutex.withLock {
                if (repository.getAllFolders().isEmpty()) {
                    repository.insertFolder(
                        Folder(
                            id = MASTER_FOLDER_ID,
                            name = "",
                            position = 0L
                        )
                    )
                }
            }
        }
    }

    private fun observeFolders() {
        viewModelScope.launch {
            repository.observeFolders().collect { folders ->

                _uiState.update { state ->

                    val selectedFolderId = state.selectedFolderId

                    val selectedExists = selectedFolderId != null && folders.any {
                        it.id == selectedFolderId
                    }

                    state.copy(
                        folders = folders.map(
                            ::toUiModel
                        ),

                        selectedFolderId = if (selectedExists) {
                            selectedFolderId
                        } else {
                            folders.firstOrNull()?.id
                        }
                    )
                }
            }
        }
    }

    private fun observeEntries() {
        viewModelScope.launch {
            combine(
                repository.observeEntries(),
                repository.observeEntryIdsWithAttachments()
            ) { entries, attachmentEntryIds ->

                val idsWithAttachments =
                    attachmentEntryIds.toSet()

                entries.map { entry ->
                    EntryUiModel(
                        id = entry.id,
                        text = entry.text,
                        type = entry.type,
                        completed = entry.completed,
                        folderId = entry.folderId,
                        hasAttachments =
                            entry.id in idsWithAttachments
                    )
                }
            }.collect { entries ->

                _uiState.update {
                    it.copy(
                        entries = entries
                    )
                }
            }
        }
    }

    suspend fun createFolder(
        name: String
    ): String? {

        val normalizedName = name.trim()

        if (normalizedName.isBlank()) {
            return null
        }

        return databaseMutex.withLock {

            val folders = repository.getAllFolders()

            val nextPosition = (folders.maxOfOrNull {
                it.position
            } ?: -1L) + 1L

            val folder = Folder(
                name = normalizedName, position = nextPosition
            )

            repository.insertFolder(
                folder
            )

            folder.id
        }
    }

    fun selectFolder(
        folderId: String
    ) {
        _uiState.update {
            it.copy(
                selectedFolderId = folderId
            )
        }
    }

    fun renameFolder(
        folderId: String, name: String
    ) {

        val normalizedName = name.trim()

        if (folderId != MASTER_FOLDER_ID && normalizedName.isBlank()) {
            return
        }

        viewModelScope.launch {

            databaseMutex.withLock {

                val folder = repository.findFolderById(
                    folderId
                ) ?: return@withLock

                repository.updateFolder(
                    folder.copy(
                        name = normalizedName
                    )
                )
            }
        }
    }

    fun deleteFolder(
        folderId: String
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val folder = repository.findFolderById(
                    folderId
                ) ?: return@withLock

                val snapshot = repository.deleteFolderWithEntries(
                    folder
                )

                _uiState.update { state ->
                    state.copy(
                        selectedFolderId = if (state.selectedFolderId == folderId) {
                            null
                        } else {
                            state.selectedFolderId
                        },

                        undoOperation = UndoOperation.FolderDeleted(
                            snapshot
                        )
                    )
                }
            }
        }
    }

    suspend fun getEntry(
        id: String
    ): EntryUiModel? {
        return databaseMutex.withLock {
            repository.findById(id)?.let(::toUiModel)
        }
    }

    fun createEntry(): EntryUiModel {
        return EntryUiModel(
            id = UUID.randomUUID().toString(),
            text = "",
            folderId = _uiState.value.selectedFolderId ?: ""
        )
    }

    suspend fun ensureEntryExists(
        entry: EntryUiModel
    ) {
        if (entry.folderId.isBlank()) {
            return
        }

        databaseMutex.withLock {
            if (repository.findById(
                    entry.id
                ) != null
            ) {
                return@withLock
            }

            repository.insertAtTop(
                entry.copy(
                    text = trimEmptyLines(
                        entry.text
                    )
                ).toDataModel()
            )
        }
    }

    fun saveNewEntry(
        entry: EntryUiModel
    ) {
        val normalizedEntry = entry.copy(
            text = trimEmptyLines(
                entry.text
            )
        )

        if (normalizedEntry.text.isBlank() || normalizedEntry.folderId.isBlank()) {
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
        entry: EntryUiModel, hasAttachments: Boolean
    ) {
        val normalizedEntry = entry.copy(
            text = trimEmptyLines(
                entry.text
            )
        )

        viewModelScope.launch {
            databaseMutex.withLock {
                val existing = repository.findById(
                    normalizedEntry.id
                ) ?: return@withLock

                if (normalizedEntry.text.isBlank() && !hasAttachments) {
                    deleteExistingEntry(
                        existing
                    )

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
        folderId: String, entryIds: List<String>
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                repository.updateEntriesPositions(
                    folderId = folderId, entryIds = entryIds
                )
            }
        }
    }

    fun moveEntryToFolder(
        entryId: String, targetFolderId: String
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val entry = repository.findById(
                    entryId
                ) ?: return@withLock

                repository.findFolderById(
                    targetFolderId
                ) ?: return@withLock

                if (entry.folderId == targetFolderId) {
                    return@withLock
                }

                repository.moveEntryToFolder(
                    entry = entry,

                    targetFolderId = targetFolderId
                )
            }
        }
    }

    fun deleteEntry(
        entryId: String
    ) {
        viewModelScope.launch {
            databaseMutex.withLock {
                val entry = repository.findById(
                    entryId
                ) ?: return@withLock

                deleteExistingEntry(
                    entry
                )
            }
        }
    }

    private suspend fun deleteExistingEntry(
        entry: Entry
    ) {
        repository.delete(
            entry
        )

        _uiState.update {
            it.copy(
                pendingDeletedEntries = it.pendingDeletedEntries + (entry.id to entry),
                undoOperation = UndoOperation.EntryDeleted(
                    entry
                ),
                deletingEntriesIds = it.deletingEntriesIds + entry.id,
                restoringEntryId = null
            )
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            databaseMutex.withLock {
                when (val operation = _uiState.value.undoOperation) {
                    is UndoOperation.EntryDeleted -> {
                        repository.restore(
                            operation.entry
                        )

                        _uiState.update {
                            it.copy(

                                pendingDeletedEntries = it.pendingDeletedEntries - operation.entry.id,
                                undoOperation = null,
                                deletingEntriesIds = it.deletingEntriesIds - operation.entry.id,
                                restoringEntryId = operation.entry.id
                            )
                        }
                    }

                    is UndoOperation.FolderDeleted -> {
                        repository.restoreFolderWithEntries(
                            operation.snapshot
                        )

                        _uiState.update {
                            it.copy(
                                undoOperation = null
                            )
                        }
                    }
                    null -> Unit
                }
            }
        }
    }

    fun clearUndo() {
        viewModelScope.launch {
            databaseMutex.withLock {
                when (val operation = _uiState.value.undoOperation) {
                    is UndoOperation.EntryDeleted -> {
                        repository.purgeAttachments(
                            operation.entry.id
                        )
                    }

                    is UndoOperation.FolderDeleted -> {
                        repository.permanentlyDeleteFolderSnapshot(
                            operation.snapshot
                        )
                    }
                    null -> Unit
                }

                _uiState.update {
                    it.copy(
                        undoOperation = null
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
                pendingDeletedEntries = it.pendingDeletedEntries - id,
                deletingEntriesIds = it.deletingEntriesIds - id
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
                val entry = repository.findById(
                    entryId
                ) ?: return@withLock

                if (entry.type != ENTRY_TYPE_TASK) {
                    return@withLock
                }

                repository.update(
                    entry.copy(
                        completed = !entry.completed
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
                val entry = repository.findById(
                    entryId
                ) ?: return@withLock

                val type = if (entry.type == ENTRY_TYPE_TASK) {
                    ENTRY_TYPE_NOTE
                } else {
                    ENTRY_TYPE_TASK
                }

                repository.update(
                    entry.copy(
                        type = type,
                        completed = if (type == ENTRY_TYPE_TASK) {
                            entry.completed
                        } else {
                            false
                        }
                    )
                )
            }
        }
    }

    suspend fun addAttachments(
        entryId: String, uris: List<Uri>
    ) {
        databaseMutex.withLock {
            if (repository.findById(
                    entryId
                ) == null
            ) {
                return@withLock
            }

            uris.forEach { uri ->
                repository.insertAttachment(
                    entryId = entryId, uri = uri
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

    fun observeAttachments(
        entryId: String
    ) = repository.observeAttachments(
        entryId
    )

    private fun toUiModel(
        entry: Entry
    ): EntryUiModel {
        return EntryUiModel(
            id = entry.id,
            text = entry.text,
            type = entry.type,
            completed = entry.completed,
            folderId = entry.folderId
        )
    }

    private fun toUiModel(
        folder: Folder
    ): FolderUiModel {

        return FolderUiModel(
            id = folder.id, name = folder.name
        )
    }

    private fun EntryUiModel.toDataModel(
        createdAt: Long =
            System.currentTimeMillis(),
        position: Long = 0L
    ): Entry {
        return Entry(
            id = id,
            text = text,
            createdAt = createdAt,
            position = position,
            type = type,
            completed = completed,
            folderId = folderId
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
        if (modelClass.isAssignableFrom(FlowFireModel::class.java)
        ) {
            return FlowFireModel(
                repository = repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

private fun trimEmptyLines(
    text: String
): String {

    return text.replace(
            Regex(
                """\A(?:[ \t]*\r?\n)+"""
            ), ""
    ).replace(
            Regex(
                """(?:\r?\n[ \t]*)+\z"""
            ), ""
        )
}
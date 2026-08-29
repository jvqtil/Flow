package dev.jvqtil.flow.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.jvqtil.flow.data.ALL_LIST_ID
import dev.jvqtil.flow.data.Attachment
import dev.jvqtil.flow.data.DeletedListSnapshot
import dev.jvqtil.flow.data.ENTRY_TYPE_NOTE
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.data.Entry
import dev.jvqtil.flow.data.EntryList
import dev.jvqtil.flow.data.FlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val listId: String = ALL_LIST_ID
)

data class EntryListUiModel(
    val id: String,
    val name: String
)

sealed interface UndoOperation {
    data class EntryDeleted(
        val entry: Entry
    ) : UndoOperation

    data class ListDeleted(
        val snapshot: DeletedListSnapshot
    ) : UndoOperation
}

data class FlowUiState(
    val entries: List<EntryUiModel> = emptyList(),
    val lists: List<EntryListUiModel> = emptyList(),
    val selectedListId: String = ALL_LIST_ID,
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
        observeLists()
        observeEntries()
    }

    private fun observeLists() {
        viewModelScope.launch {
            repository.observeLists().collect { lists ->

                val sortedLists =
                    lists.sortedBy {
                        it.position
                    }

                val normalizedLists =
                    listOf(
                        EntryList(
                            id = ALL_LIST_ID,
                            name = "All",
                            position = Long.MIN_VALUE
                        )
                    ) + sortedLists.filter {
                        it.id != ALL_LIST_ID
                    }

                _uiState.update { state ->

                    val selectedStillExists =
                        normalizedLists.any {
                            it.id ==
                                    state.selectedListId
                        }

                    state.copy(
                        lists =
                            normalizedLists.map(
                                ::toListUiModel
                            ),
                        selectedListId =
                            if (selectedStillExists) {
                                state.selectedListId
                            } else {
                                ALL_LIST_ID
                            }
                    )
                }
            }
        }
    }

    suspend fun createList(
        name: String
    ): String? {
        val normalizedName =
            name.trim()

        if (normalizedName.isBlank()) {
            return null
        }

        return databaseMutex.withLock {
            val lists =
                repository.getAllLists()

            val nextPosition =
                (
                        lists
                            .filter {
                                it.id != ALL_LIST_ID
                            }
                            .maxOfOrNull {
                                it.position
                            }
                            ?: 0L
                        ) + 1L

            val list =
                EntryList(
                    name = normalizedName,
                    position = nextPosition
                )

            repository.insertList(
                list
            )

            list.id
        }
    }

    fun selectList(
        listId: String
    ) {
        _uiState.update {
            it.copy(
                selectedListId = listId
            )
        }
    }

    fun renameList(
        listId: String,
        name: String
    ) {
        val normalizedName = name.trim()

        if (
            listId == ALL_LIST_ID ||
            normalizedName.isBlank()
        ) {
            return
        }

        viewModelScope.launch {
            databaseMutex.withLock {
                val list =
                    repository.findListById(listId)
                        ?: return@withLock

                repository.updateList(
                    list.copy(
                        name = normalizedName
                    )
                )
            }
        }
    }

    fun deleteList(
        listId: String
    ) {
        if (listId == ALL_LIST_ID) {
            return
        }

        viewModelScope.launch {
            databaseMutex.withLock {

                val list =
                    repository.findListById(listId)
                        ?: return@withLock

                val entries =
                    repository.getEntries(listId)

                val snapshot =
                    DeletedListSnapshot(
                        list = list,
                        entries = entries
                    )

                repository.deleteListWithEntries(
                    list
                )

                _uiState.update { state ->
                    state.copy(
                        selectedListId =
                            if (
                                state.selectedListId == listId
                            ) {
                                ALL_LIST_ID
                            } else {
                                state.selectedListId
                            },

                        undoOperation =
                            UndoOperation.ListDeleted(
                                snapshot = snapshot
                            ),

                        pendingDeletedEntries =
                            state.pendingDeletedEntries -
                                    entries.map {
                                        it.id
                                    }.toSet()
                    )
                }
            }
        }
    }

    private fun observeEntries() {
        viewModelScope.launch {
            repository.observeEntries().collect { entries ->

                _uiState.update {
                    it.copy(
                        entries =
                            entries.map(
                                ::toUiModel
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
            repository
                .findById(id)
                ?.let(::toUiModel)
        }
    }

    fun createEntry(): EntryUiModel {
        val selectedListId =
            _uiState.value.selectedListId

        return EntryUiModel(
            id =
                UUID.randomUUID()
                    .toString(),
            text = "",
            listId =
                selectedListId
        )
    }

    suspend fun ensureEntryExists(
        entry: EntryUiModel
    ) {
        databaseMutex.withLock {

            if (
                repository.findById(
                    entry.id
                ) != null
            ) {
                return@withLock
            }

            repository.insertAtTop(
                entry
                    .copy(
                        text =
                            trimEmptyLines(
                                entry.text
                            )
                    )
                    .toDataModel()
            )
        }
    }

    fun saveNewEntry(
        entry: EntryUiModel
    ) {
        val normalizedEntry =
            entry.copy(
                text =
                    trimEmptyLines(
                        entry.text
                    )
            )

        if (
            normalizedEntry.text.isBlank()
        ) {
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
        val normalizedEntry =
            entry.copy(
                text =
                    trimEmptyLines(
                        entry.text
                    )
            )

        viewModelScope.launch {

            databaseMutex.withLock {

                val existing =
                    repository.findById(
                        normalizedEntry.id
                    )
                        ?: return@withLock

                if (
                    normalizedEntry.text.isBlank() &&
                    !hasAttachments
                ) {
                    deleteExistingEntry(
                        existing
                    )

                    return@withLock
                }

                repository.update(
                    normalizedEntry.toDataModel(
                        createdAt =
                            existing.createdAt,
                        position =
                            existing.position
                    )
                )
            }
        }
    }

    fun updateEntriesPositions(
        entriesIds: List<String>
    ) {
        val selectedListId =
            _uiState.value.selectedListId

        viewModelScope.launch {

            databaseMutex.withLock {

                repository.updateEntriesPositions(
                    listId =
                        selectedListId,
                    entryIds =
                        entriesIds
                )
            }
        }
    }

    fun moveEntryToList(
        entryId: String,
        targetListId: String
    ) {
        if (targetListId == ALL_LIST_ID) {
            return
        }

        viewModelScope.launch {
            databaseMutex.withLock {
                val entry =
                    repository.findById(entryId)
                        ?: return@withLock

                val targetList =
                    repository.findListById(targetListId)
                        ?: return@withLock

                if (entry.listId == targetList.id) {
                    return@withLock
                }

                repository.moveEntryToList(
                    entry = entry,
                    targetListId = targetList.id
                )
            }
        }
    }

    fun deleteEntry(
        entry: EntryUiModel
    ) {
        viewModelScope.launch {

            databaseMutex.withLock {

                val existing =
                    repository.findById(
                        entry.id
                    )
                        ?: return@withLock

                deleteExistingEntry(
                    existing
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
                pendingDeletedEntries =
                    it.pendingDeletedEntries +
                            (
                                    entry.id to entry
                                    ),

                undoOperation =
                    UndoOperation.EntryDeleted(
                        entry = entry
                    ),

                deletingEntriesIds =
                    it.deletingEntriesIds +
                            entry.id,

                restoringEntryId = null
            )
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            databaseMutex.withLock {

                when (
                    val operation =
                        _uiState.value.undoOperation
                ) {

                    is UndoOperation.EntryDeleted -> {

                        val entry =
                            operation.entry

                        repository.restore(
                            entry
                        )

                        _uiState.update {
                            it.copy(
                                pendingDeletedEntries =
                                    it.pendingDeletedEntries -
                                            entry.id,

                                undoOperation = null,

                                deletingEntriesIds =
                                    it.deletingEntriesIds -
                                            entry.id,

                                restoringEntryId =
                                    entry.id
                            )
                        }
                    }

                    is UndoOperation.ListDeleted -> {

                        repository.restoreListWithEntries(
                            operation.snapshot
                        )

                        _uiState.update {
                            it.copy(
                                undoOperation = null
                            )
                        }
                    }

                    null -> {
                        return@withLock
                    }
                }
            }
        }
    }

    fun clearUndo() {
        viewModelScope.launch {
            databaseMutex.withLock {

                when (
                    val operation =
                        _uiState.value.undoOperation
                ) {

                    is UndoOperation.EntryDeleted -> {

                        repository.purgeAttachments(
                            operation.entry.id
                        )
                    }

                    is UndoOperation.ListDeleted -> {

                        repository.purgeListAttachments(
                            operation.snapshot.entries
                        )
                    }

                    null -> {
                        return@withLock
                    }
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
                pendingDeletedEntries =
                    it.pendingDeletedEntries -
                            id,
                deletingEntriesIds =
                    it.deletingEntriesIds -
                            id
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
                    repository.findById(
                        entryId
                    )
                        ?: return@withLock

                if (
                    existing.type !=
                    ENTRY_TYPE_TASK
                ) {
                    return@withLock
                }

                repository.update(
                    existing.copy(
                        completed =
                            !existing.completed
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
                    repository.findById(
                        entryId
                    )
                        ?: return@withLock

                val newType =
                    if (
                        existing.type ==
                        ENTRY_TYPE_TASK
                    ) {
                        ENTRY_TYPE_NOTE
                    } else {
                        ENTRY_TYPE_TASK
                    }

                repository.update(
                    existing.copy(
                        type = newType,
                        completed =
                            if (
                                newType ==
                                ENTRY_TYPE_TASK
                            ) {
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

            if (
                repository.findById(
                    entryId
                ) == null
            ) {
                return@withLock
            }

            uris.forEach { uri ->

                repository.insertAttachment(
                    entryId =
                        entryId,
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
            completed = entry.completed,
            listId = entry.listId
        )
    }

    private fun toListUiModel(
        list: EntryList
    ): EntryListUiModel {
        return EntryListUiModel(
            id = list.id,
            name = list.name
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
            listId = listId
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

        if (
            modelClass.isAssignableFrom(
                FlowFireModel::class.java
            )
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
    return text
        .replace(
            Regex(
                """\A(?:[ \t]*\r?\n)+"""
            ),
            ""
        )
        .replace(
            Regex(
                """(?:\r?\n[ \t]*)+\z"""
            ),
            ""
        )
}
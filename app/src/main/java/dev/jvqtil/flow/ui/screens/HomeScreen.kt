package dev.jvqtil.flow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.Entry
import dev.jvqtil.flow.data.MASTER_FOLDER_ID
import dev.jvqtil.flow.ui.EntryUiModel
import dev.jvqtil.flow.ui.FolderUiModel
import dev.jvqtil.flow.ui.UndoOperation
import dev.jvqtil.flow.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    foldersEnabled: Boolean,
    swipeGesturesEnabled: Boolean,
    entries: List<EntryUiModel>,
    folders: List<FolderUiModel>,
    selectedFolderId: String,
    previewLines: Int,
    shouldScrollToTop: Boolean,
    onScrollToTopHandled: () -> Unit,
    pendingDeletedEntries: Map<String, Entry>,
    undoOperation: UndoOperation?,
    restoringEntryId: String?,
    deletingEntriesIds: Set<String>,
    onSelectFolder: (String) -> Unit,
    onCreateFolder: suspend (String) -> String?,
    onRenameFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onUndo: () -> Unit,
    onUndoTimeout: () -> Unit,
    onAnimationFinished: (String) -> Unit,
    onNewEntry: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onReorderEntries: (List<String>) -> Unit,
    onReorderFolders: (List<String>) -> Unit,
    onToggleCompleted: (String) -> Unit,
    onToggleTaskNote: (String) -> Unit,
    onMoveEntryToFolder: (String, String) -> Unit
) {
    val listStates = remember {
        mutableMapOf<String, LazyListState>()
    }
    val folderListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var localEntries by remember(entries) {
        mutableStateOf(entries)
    }

    var localFolders by remember(folders) {
        mutableStateOf(folders)
    }

    var closeActionsToken by remember {
        mutableIntStateOf(0)
    }

    var deletedEntriesPositions by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    var showNewFolderSheet by remember {
        mutableStateOf(false)
    }

    var newFolderName by remember {
        mutableStateOf("")
    }

    var folderActionTarget by remember {
        mutableStateOf<FolderUiModel?>(null)
    }

    var folderSwitchDirection by remember {
        mutableIntStateOf(1)
    }

    var renameTarget by remember {
        mutableStateOf<FolderUiModel?>(null)
    }

    var renameText by remember {
        mutableStateOf("")
    }

    var entryToMove by remember {
        mutableStateOf<EntryUiModel?>(null)
    }

    var showFolderPicker by remember {
        mutableStateOf(false)
    }

    var createFolderForMove by remember {
        mutableStateOf(false)
    }

    fun closeActions() {
        closeActionsToken++
    }

    fun reorderEntries(fromId: String, toId: String) {
        val fromIndex = localEntries.indexOfFirst {
            it.id == fromId
        }

        val toIndex = localEntries.indexOfFirst {
            it.id == toId
        }

        if (
            fromIndex >= 0 &&
            toIndex >= 0 &&
            fromIndex != toIndex
        ) {
            localEntries = localEntries.toMutableList().apply {
                add(
                    toIndex,
                    removeAt(fromIndex)
                )
            }
        }
    }

    val visibleEntries = remember(
        localEntries,
        pendingDeletedEntries,
        deletedEntriesPositions
    ) {
        buildList {
            addAll(localEntries)

            pendingDeletedEntries.values.forEach { deleted ->
                if (none { it.id == deleted.id }) {
                    val position =
                        deletedEntriesPositions[deleted.id] ?: size

                    add(
                        position.coerceIn(0, size),
                        EntryUiModel(
                            id = deleted.id,
                            text = deleted.text,
                            type = deleted.type,
                            completed = deleted.completed,
                            folderId = deleted.folderId
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(
        localEntries,
        entries,
        deletingEntriesIds,
        selectedFolderId
    ) {
        if (deletingEntriesIds.isNotEmpty()) {
            return@LaunchedEffect
        }

        if (localEntries.map { it.id } == entries.map { it.id }) {
            return@LaunchedEffect
        }

        delay(350.milliseconds)

        if (
            deletingEntriesIds.isEmpty() &&
            localEntries.map { it.id } != entries.map { it.id }
        ) {
            onReorderEntries(
                localEntries
                    .asSequence()
                    .filter { it.folderId == selectedFolderId }
                    .map { it.id }
                    .toList()
            )
        }
    }

    LaunchedEffect(shouldScrollToTop, selectedFolderId) {
        if (!shouldScrollToTop) {
            return@LaunchedEffect
        }

        delay(100.milliseconds)

        listStates[selectedFolderId]?.let { listState ->
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(0)
            }
        }

        onScrollToTopHandled()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.app_name),
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
                contentDescription = stringResource(R.string.settings_label),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        if (foldersEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 64.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            ) {
                FoldersBar(
                    folders = localFolders,
                    selectedFolderId = selectedFolderId,
                    folderListState = folderListState,
                    onSelectFolder = { folderId ->
                        folderSwitchDirection =
                            if (
                                localFolders.indexOfFirst {
                                    it.id == folderId
                                } >
                                localFolders.indexOfFirst {
                                    it.id == selectedFolderId
                                }
                            ) {
                                1
                            } else {
                                -1
                            }

                        closeActions()
                        onSelectFolder(folderId)
                    },
                    onFolderLongPress = { folder ->
                        closeActions()
                        folderActionTarget = folder
                        renameTarget = null
                        renameText = ""
                    },
                    onNewFolder = {
                        closeActions()
                        newFolderName = ""
                        createFolderForMove = false
                        showNewFolderSheet = true
                    },
                    onLocalFoldersChange = { newFolders ->
                        localFolders = newFolders
                    },
                    onDragStarted = {
                        folderActionTarget = null
                        renameTarget = null
                        renameText = ""
                        closeActions()
                    },
                    onDragStopped = {
                        onReorderFolders(
                            localFolders.map { it.id }
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (foldersEnabled) {
                        116.dp
                    } else {
                        64.dp
                    },
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            AnimatedContent(
                targetState = selectedFolderId,
                transitionSpec = {
                    if (initialState == targetState) {
                        fadeIn(
                            animationSpec = tween(0)
                        ).togetherWith(
                            fadeOut(
                                animationSpec = tween(0)
                            )
                        )
                    } else {
                        val direction = folderSwitchDirection

                        (
                                slideInHorizontally(
                                    initialOffsetX = {
                                        direction * it / 6
                                    },
                                    animationSpec = tween(280)
                                ) +
                                        fadeIn(
                                            animationSpec = tween(220)
                                        )
                                ).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = {
                                        -direction * it / 6
                                    },
                                    animationSpec = tween(220)
                                ) +
                                        fadeOut(
                                            animationSpec = tween(170)
                                        )
                            )
                    }
                },
                label = "folderContent"
            ) { folderId ->
                val listState = remember(folderId) {
                    listStates.getOrPut(folderId) {
                        LazyListState()
                    }
                }

                val reorderableState = rememberReorderableLazyListState(
                    lazyListState = listState
                ) { from, to ->
                    val fromId = from.key as String
                    val toId = to.key as String

                    reorderEntries(fromId, toId)
                }

                val folderEntries = remember(
                    visibleEntries,
                    folderId
                ) {
                    visibleEntries.filter {
                        it.folderId == folderId
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = folderEntries,
                        key = { it.id }
                    ) { entry ->
                        ReorderableItem(
                            state = reorderableState,
                            key = entry.id
                        ) { isDragging ->
                            EntryCard(
                                foldersEnabled = foldersEnabled,
                                swipeGesturesEnabled = swipeGesturesEnabled,
                                entry = entry,
                                previewLines = previewLines,
                                shouldAnimate =
                                    entry.id in deletingEntriesIds ||
                                            entry.id == restoringEntryId,
                                isDeleting =
                                    entry.id in deletingEntriesIds,
                                isDragging = isDragging,
                                closeActionsToken = closeActionsToken,
                                dragHandleModifier =
                                    Modifier.longPressDraggableHandle(),
                                onClick = {
                                    if (
                                        entry.id !in deletingEntriesIds &&
                                        entry.id != restoringEntryId
                                    ) {
                                        closeActions()
                                        onOpenEntry(entry.id)
                                    }
                                },
                                onDelete = {
                                    if (
                                        entry.id !in deletingEntriesIds &&
                                        entry.id != restoringEntryId
                                    ) {
                                        val index =
                                            localEntries.indexOfFirst {
                                                it.id == entry.id
                                            }

                                        deletedEntriesPositions =
                                            deletedEntriesPositions +
                                                    (entry.id to index)

                                        closeActions()
                                        onDeleteEntry(entry.id)
                                    }
                                },
                                onToggleCompleted = {
                                    if (
                                        entry.id !in deletingEntriesIds &&
                                        entry.id != restoringEntryId
                                    ) {
                                        onToggleCompleted(entry.id)
                                    }
                                },
                                onToggleTaskNote = {
                                    if (
                                        entry.id !in deletingEntriesIds &&
                                        entry.id != restoringEntryId
                                    ) {
                                        onToggleTaskNote(entry.id)
                                    }
                                },
                                onSwitchFolder = {
                                    closeActions()
                                    entryToMove = entry
                                    showFolderPicker = true
                                }
                            ) {
                                deletedEntriesPositions =
                                    deletedEntriesPositions - entry.id

                                onAnimationFinished(entry.id)
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            AddButton(
                onClick = {
                    closeActions()
                    onNewEntry()
                }
            )
        }

        AnimatedVisibility(
            visible = undoOperation != null,
            modifier = Modifier.fillMaxSize(),
            enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) +
                        fadeIn(
                            animationSpec = tween(200)
                        ),
            exit =
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(140)
                ) +
                        fadeOut(
                            animationSpec = tween(100)
                        )
        ) {
            undoOperation?.let { operation ->
                val undoId = when (operation) {
                    is UndoOperation.EntryDeleted ->
                        operation.entry.id

                    is UndoOperation.FolderDeleted ->
                        operation.snapshot.folder.id
                }

                UndoPopup(
                    id = undoId,
                    onUndo = {
                        closeActions()
                        onUndo()
                    },
                    onTimeout = onUndoTimeout
                )
            }
        }
    }

    folderActionTarget?.let { folder ->
        val displayName =
            if (
                folder.id == MASTER_FOLDER_ID &&
                folder.name.isBlank()
            ) {
                stringResource(R.string.master_folder_label)
            } else {
                folder.name
            }

        FolderActionsBottomSheet(
            folder = folder,
            onRename = {
                renameTarget = folder
                renameText = displayName
                folderActionTarget = null
            },
            onDelete = {
                folderActionTarget = null
                onDeleteFolder(folder.id)
            },
            onDismiss = {
                folderActionTarget = null
            }
        )
    }

    renameTarget?.let { folder ->
        RenameFolderBottomSheet(
            value = renameText,
            onValueChange = {
                renameText = it
            },
            onSave = {
                onRenameFolder(
                    folder.id,
                    renameText.trim()
                )

                renameTarget = null
                renameText = ""
            },
            onReset = {
                onRenameFolder(
                    folder.id,
                    ""
                )

                renameTarget = null
                renameText = ""
            },
            onDismiss = {
                renameTarget = null
                renameText = ""
            },
            showReset = folder.id == MASTER_FOLDER_ID
        )
    }

    if (showNewFolderSheet) {
        NewFolderBottomSheet(
            value = newFolderName,
            onValueChange = {
                newFolderName = it
            },
            onCreate = {
                val name = newFolderName.trim()

                if (name.isNotBlank()) {
                    scope.launch {
                        val folderId = onCreateFolder(name)

                        if (folderId != null) {
                            if (createFolderForMove) {
                                entryToMove?.let { entry ->
                                    onMoveEntryToFolder(
                                        entry.id,
                                        folderId
                                    )
                                }
                            } else {
                                onSelectFolder(folderId)
                            }
                        }

                        entryToMove = null
                        createFolderForMove = false
                        newFolderName = ""
                        showNewFolderSheet = false
                    }
                }
            },
            onDismiss = {
                newFolderName = ""
                showNewFolderSheet = false
                entryToMove = null
                createFolderForMove = false
            }
        )
    }

    if (
        showFolderPicker &&
        entryToMove != null
    ) {
        FolderChoiceBottomSheet(
            folders = localFolders,
            selectedFolderId = entryToMove!!.folderId,
            onSelect = { folderId ->
                entryToMove?.let { entry ->
                    if (folderId != entry.folderId) {
                        localEntries =
                            localEntries.map { current ->
                                if (current.id == entry.id) {
                                    current.copy(
                                        folderId = folderId
                                    )
                                } else {
                                    current
                                }
                            }

                        onMoveEntryToFolder(
                            entry.id,
                            folderId
                        )
                    }
                }

                showFolderPicker = false
                entryToMove = null
                createFolderForMove = false
            },
            onCreateFolder = {
                showFolderPicker = false
                createFolderForMove = true
                newFolderName = ""
                showNewFolderSheet = true
            },
            onDismiss = {
                showFolderPicker = false
                entryToMove = null
                createFolderForMove = false
            }
        )
    }
}

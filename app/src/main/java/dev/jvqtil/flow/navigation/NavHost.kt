package dev.jvqtil.flow.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.jvqtil.flow.data.AppPreferences
import dev.jvqtil.flow.data.Attachment
import dev.jvqtil.flow.data.AttachmentStorage
import dev.jvqtil.flow.data.BackupManager
import dev.jvqtil.flow.data.ENTRY_TYPE_NOTE
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.data.FlowRepository
import dev.jvqtil.flow.ui.EntryUiModel
import dev.jvqtil.flow.ui.FlowFireModel
import dev.jvqtil.flow.ui.FlowFireModelFactory
import dev.jvqtil.flow.ui.models.EditorFont
import dev.jvqtil.flow.ui.models.KeyboardMode
import dev.jvqtil.flow.ui.models.UiFont
import dev.jvqtil.flow.ui.screens.EditorScreen
import dev.jvqtil.flow.ui.screens.HomeScreen
import dev.jvqtil.flow.ui.screens.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FlowNavHost(
    repository: FlowRepository,
    attachmentStorage: AttachmentStorage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.CreateDocument(
                    "application/json"
                )
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    runCatching {
                        val entries =
                            repository.getAllEntries()

                        val folders =
                            repository.getAllFolders()

                        BackupManager.exportNotes(
                            context = context,
                            uri = uri,
                            entries = entries,
                            folders = folders
                        )
                    }.onSuccess {
                        Toast.makeText(
                            context,
                            "Notes exported",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure {
                        Toast.makeText(
                            context,
                            "Export failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    runCatching {
                        val backup =
                            BackupManager.importNotes(
                                context = context,
                                uri = uri
                            )

                        backup.folders.forEach { folder ->
                            repository.restoreFolder(
                                folder
                            )
                        }

                        repository.restore(
                            backup.entries
                        )
                    }.onSuccess {
                        Toast.makeText(
                            context,
                            "Notes imported",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure {
                        Toast.makeText(
                            context,
                            "Import failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    val flowFireModel: FlowFireModel =
        viewModel(
            factory =
                FlowFireModelFactory(
                    repository = repository
                )
        )

    val uiState by
    flowFireModel
        .uiState
        .collectAsStateWithLifecycle()

    var shouldScrollHomeToTop by remember {
        mutableStateOf(false)
    }

    val amoled by
    AppPreferences
        .observeAmoled(context)
        .collectAsStateWithLifecycle(
            initialValue = false
        )

    val uiFont by
    AppPreferences
        .observeUiFont(context)
        .collectAsStateWithLifecycle(
            initialValue = UiFont.DEFAULT
        )

    val editorFont by
    AppPreferences
        .observeEditorFont(context)
        .collectAsStateWithLifecycle(
            initialValue = EditorFont.UI_FONT
        )

    val previewLines by
    AppPreferences
        .observePreviewLines(context)
        .collectAsStateWithLifecycle(
            initialValue = 4
        )

    val keyboardMode by
    AppPreferences
        .observeKeyboardMode(context)
        .collectAsStateWithLifecycle(
            initialValue = KeyboardMode.NORMAL
        )

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                ),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 8 },
                animationSpec = tween(220)
            ) + fadeIn(
                animationSpec = tween(180)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 8 },
                animationSpec = tween(220)
            ) + fadeOut(
                animationSpec = tween(180)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 8 },
                animationSpec = tween(220)
            ) + fadeIn(
                animationSpec = tween(180)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 8 },
                animationSpec = tween(330)
            ) + fadeOut(
                animationSpec = tween(270)
            )
        }
    ) {
        composable(HOME_ROUTE) {
            val selectedFolderId =
                uiState.selectedFolderId
                    ?: uiState.folders.firstOrNull()?.id

            HomeScreen(
                entries = if (selectedFolderId != null) {
                    uiState.entries.filter {
                        it.folderId == selectedFolderId
                    }
                } else {
                    emptyList()
                },
                folders = uiState.folders,
                selectedFolderId = selectedFolderId ?: "",
                previewLines = previewLines,
                shouldScrollToTop =
                    shouldScrollHomeToTop,
                onScrollToTopHandled = {
                    shouldScrollHomeToTop = false
                },
                pendingDeletedEntries =
                    uiState.pendingDeletedEntries,
                undoOperation =
                    uiState.undoOperation,
                restoringEntryId =
                    uiState.restoringEntryId,
                deletingEntriesIds =
                    uiState.deletingEntriesIds,
                onSelectFolder = { folderId ->
                    flowFireModel.selectFolder(
                        folderId
                    )
                },
                onCreateFolder = { name ->
                    flowFireModel.createFolder(
                        name
                    )
                },
                onRenameFolder = { id, name ->
                    flowFireModel.renameFolder(
                        folderId = id,
                        name = name
                    )
                },
                onDeleteFolder = { id ->
                    flowFireModel.deleteFolder(
                        id
                    )
                },
                onUndo = {
                    flowFireModel.undoDelete()
                },
                onUndoTimeout = {
                    flowFireModel.clearUndo()
                },
                onAnimationFinished = { id ->
                    if (
                        uiState.deletingEntriesIds
                            .contains(id)
                    ) {
                        flowFireModel.clearDeletedAnimation(
                            id
                        )
                    }

                    if (
                        uiState.restoringEntryId ==
                        id
                    ) {
                        flowFireModel.clearRestoringEntry()
                    }
                },
                onNewEntry = {
                    navController.navigate(
                        "$EDITOR_ROUTE/new?new=true"
                    )
                },
                onOpenEntry = { id ->
                    navController.navigate(
                        "$EDITOR_ROUTE/$id?new=false"
                    )
                },
                onDeleteEntry = { id ->
                    uiState.entries
                        .firstOrNull {
                            it.id == id
                        }
                        ?.let { entry ->
                            flowFireModel.deleteEntry(
                                entry.id
                            )
                        }
                },
                onOpenSettings = {
                    navController.navigate(
                        SETTINGS_ROUTE
                    )
                },
                onReorderEntries = { entryIds ->
                    val folderId =
                        uiState.selectedFolderId
                            ?: return@HomeScreen

                    flowFireModel.updateEntriesPositions(
                        folderId = folderId,
                        entryIds = entryIds
                    )
                },
                onToggleCompleted =
                    flowFireModel::toggleCompleted,
                onToggleTaskNote =
                    flowFireModel::toggleTaskNote,
                onMoveEntryToFolder =
                    flowFireModel::moveEntryToFolder
            )
        }

        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                amoled = amoled,
                onAmoledChanged = { enabled ->
                    scope.launch {
                        AppPreferences.setAmoled(
                            context = context,
                            enabled = enabled
                        )
                    }
                },
                uiFont = uiFont,
                onUiFontChanged = { font ->
                    scope.launch {
                        AppPreferences.setUiFont(
                            context = context,
                            font = font
                        )
                    }
                },
                editorFont = editorFont,
                onEditorFontChanged = { font ->
                    scope.launch {
                        AppPreferences.setEditorFont(
                            context = context,
                            font = font
                        )
                    }
                },
                previewLines = previewLines,
                onPreviewLinesChanged = { lines ->
                    scope.launch {
                        AppPreferences.setPreviewLines(
                            context = context,
                            lines = lines
                        )
                    }
                },
                keyboardMode = keyboardMode,
                onKeyboardModeChanged = { mode ->
                    scope.launch {
                        AppPreferences.setKeyboardMode(
                            context = context,
                            mode = mode
                        )
                    }
                },
                onExport = {
                    exportLauncher.launch(
                        "flow-backup.json"
                    )
                },
                onImport = {
                    importLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/json",
                            "text/plain"
                        )
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                "$EDITOR_ROUTE/{$ENTRY_ID}?new={new}",
            arguments =
                listOf(
                    navArgument(ENTRY_ID) {
                        type = NavType.StringType
                    },
                    navArgument("new") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
        ) { entryBackStackEntry ->

            val routeEntryId =
                entryBackStackEntry
                    .arguments
                    ?.getString(ENTRY_ID)

            val isNew =
                entryBackStackEntry
                    .arguments
                    ?.getBoolean("new")
                    ?: false

            var attachments by remember(
                routeEntryId,
                isNew
            ) {
                mutableStateOf<List<Attachment>>(
                    emptyList()
                )
            }

            var currentEntry by remember(
                routeEntryId,
                isNew
            ) {
                mutableStateOf<EntryUiModel?>(
                    null
                )
            }

            var entryPersisted by remember(
                routeEntryId,
                isNew
            ) {
                mutableStateOf(!isNew)
            }

            var skipSaveOnDispose by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(
                routeEntryId,
                isNew
            ) {
                currentEntry =
                    when {
                        isNew -> {
                            flowFireModel.createEntry()
                        }

                        !routeEntryId.isNullOrBlank() -> {
                            flowFireModel.getEntry(
                                routeEntryId
                            )
                        }

                        else -> {
                            null
                        }
                    }
            }

            LaunchedEffect(
                currentEntry?.id
            ) {
                val entryId =
                    currentEntry?.id
                        ?: return@LaunchedEffect

                repository
                    .observeAttachments(
                        entryId
                    )
                    .collect { currentAttachments ->
                        attachments =
                            currentAttachments
                    }
            }

            val latestEntry by
            rememberUpdatedState(
                currentEntry
            )

            val latestSkipSaveOnDispose by
            rememberUpdatedState(
                skipSaveOnDispose
            )

            val latestAttachments by
            rememberUpdatedState(
                attachments
            )

            val latestEntryPersisted by
            rememberUpdatedState(
                entryPersisted
            )

            LaunchedEffect(
                currentEntry?.id,
                currentEntry?.text,
                currentEntry?.folderId,
                currentEntry?.type,
                currentEntry?.completed
            ) {
                val entryToSave =
                    currentEntry
                        ?: return@LaunchedEffect

                delay(700.milliseconds)

                if (
                    isNew &&
                    !entryPersisted
                ) {
                    if (
                        entryToSave.text.isBlank() &&
                        attachments.isEmpty()
                    ) {
                        return@LaunchedEffect
                    }

                    flowFireModel.saveNewEntry(
                        entryToSave
                    )

                    entryPersisted = true
                    shouldScrollHomeToTop = true

                    return@LaunchedEffect
                }

                flowFireModel.updateEntry(
                    entry = entryToSave,
                    hasAttachments = attachments.isNotEmpty()
                )
            }

            DisposableEffect(Unit) {
                onDispose {
                    if (
                        latestSkipSaveOnDispose
                    ) {
                        return@onDispose
                    }

                    val entryToSave =
                        latestEntry
                            ?: return@onDispose

                    if (
                        isNew &&
                        !latestEntryPersisted
                    ) {
                        if (
                            entryToSave.text
                                .isNotBlank() ||
                            latestAttachments
                                .isNotEmpty()
                        ) {
                            flowFireModel.saveNewEntry(
                                entryToSave
                            )

                            shouldScrollHomeToTop = true
                        }

                        return@onDispose
                    }

                    if (
                        entryToSave.text.isBlank() &&
                        latestAttachments.isEmpty()
                    ) {
                        flowFireModel.deleteEntry(
                            entryToSave.id
                        )

                        return@onDispose
                    }

                    flowFireModel.updateEntry(
                        entry = entryToSave,
                        hasAttachments =
                            latestAttachments
                                .isNotEmpty()
                    )
                }
            }

            currentEntry?.let { entry ->
                EditorScreen(
                    entry = entry,
                    attachments = attachments,
                    attachmentStorage = attachmentStorage,
                    folders = uiState.folders,
                    selectedFolderId =
                        entry.folderId,
                    autoFocus = isNew,
                    uiFont = uiFont,
                    editorFont = editorFont,
                    keyboardMode = keyboardMode,
                    onBack = {
                        navController.popBackStack()
                    },
                    onTextChange = { text ->
                        currentEntry =
                            currentEntry?.copy(
                                text = text
                            )
                    },
                    onDelete = {
                        val entryToDelete =
                            currentEntry

                        if (entryToDelete == null) {
                            navController.popBackStack()
                            return@EditorScreen
                        }

                        skipSaveOnDispose = true

                        if (isNew) {
                            if (entryPersisted) {
                                flowFireModel.deleteEntry(
                                    entryToDelete.id
                                )
                            }
                        } else {
                            flowFireModel.deleteEntry(
                                entryToDelete.id
                            )
                        }

                        navController.popBackStack()
                    },
                    onSelectFolder = { folderId ->
                        currentEntry =
                            currentEntry?.copy(
                                folderId = folderId
                            )
                    },
                    onCreateFolder =
                        flowFireModel::createFolder,
                    onToggleTaskNote = {
                        val current =
                            currentEntry
                                ?: return@EditorScreen

                        flowFireModel.toggleTaskNote(
                            current.id
                        )

                        currentEntry =
                            current.copy(
                                type =
                                    if (
                                        current.type ==
                                        ENTRY_TYPE_TASK
                                    ) {
                                        ENTRY_TYPE_NOTE
                                    } else {
                                        ENTRY_TYPE_TASK
                                    },
                                completed =
                                    if (
                                        current.type ==
                                        ENTRY_TYPE_TASK
                                    ) {
                                        false
                                    } else {
                                        current.completed
                                    }
                            )
                    },
                    onAddAttachment = { uriStrings ->
                        scope.launch {
                            val entryToSave =
                                currentEntry
                                    ?: return@launch

                            if (
                                entryToSave.text.isBlank()
                            ) {
                                return@launch
                            }

                            if (
                                isNew &&
                                !entryPersisted
                            ) {
                                flowFireModel.ensureEntryExists(
                                    entryToSave
                                )

                                entryPersisted = true
                            }

                            flowFireModel.addAttachments(
                                entryId =
                                    entryToSave.id,
                                uris =
                                    uriStrings.map(
                                        Uri::parse
                                    )
                            )
                        }
                    },
                    onDeleteAttachment = { attachment ->
                        flowFireModel.deleteAttachment(
                            attachment
                        )
                    },
                    onOpenAttachment = { attachment ->
                        runCatching {
                            val file =
                                attachmentStorage.getFile(
                                    attachment.path
                                )

                            val uri =
                                androidx.core.content
                                    .FileProvider
                                    .getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )

                            val intent =
                                Intent(
                                    Intent.ACTION_VIEW
                                ).apply {
                                    setDataAndType(
                                        uri,
                                        attachment.mimeType
                                            ?: "*/*"
                                    )

                                    addFlags(
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }

                            context.startActivity(
                                intent
                            )
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Unable to open file",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}
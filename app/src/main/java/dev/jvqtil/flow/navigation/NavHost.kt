package dev.jvqtil.flow.navigation

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
import dev.jvqtil.flow.data.BackupManager
import dev.jvqtil.flow.data.FlowRepository
import dev.jvqtil.flow.ui.EntryUiModel
import dev.jvqtil.flow.ui.FlowFireModel
import dev.jvqtil.flow.ui.FlowFireModelFactory
import dev.jvqtil.flow.ui.components.EditorFont
import dev.jvqtil.flow.ui.components.UiFont
import dev.jvqtil.flow.ui.screens.EditorScreen
import dev.jvqtil.flow.ui.screens.HomeScreen
import dev.jvqtil.flow.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun FlowNavHost(
    repository: FlowRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/json"
        )
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val notes = repository.getAllEntries()

                    BackupManager.exportNotes(
                        context = context,
                        uri = uri,
                        entries = notes
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

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val notes = BackupManager.importNotes(
                        context = context,
                        uri = uri
                    )

                    repository.restoreEntry(notes)
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

    val flowFireModel: FlowFireModel = viewModel(
        factory = FlowFireModelFactory(repository)
    )

    val uiState by flowFireModel.uiState.collectAsStateWithLifecycle()

    var shouldScrollHomeToTop by remember {
        mutableStateOf(false)
    }

    val amoled by AppPreferences
        .observeAmoled(context)
        .collectAsStateWithLifecycle(
            initialValue = false
        )

    val uiFont by AppPreferences
        .observeUiFont(context)
        .collectAsStateWithLifecycle(
            initialValue = UiFont.DEFAULT
        )

    val editorFont by AppPreferences
        .observeEditorFont(context)
        .collectAsStateWithLifecycle(
            initialValue = EditorFont.UI_FONT
        )

    val previewLines by AppPreferences
        .observePreviewLines(context)
        .collectAsStateWithLifecycle(
            initialValue = 4
        )

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = Modifier
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
            HomeScreen(
                entries = uiState.entries,
                previewLines = previewLines,
                shouldScrollToTop = shouldScrollHomeToTop,
                onScrollToTopHandled = {
                    shouldScrollHomeToTop = false
                },
                pendingDeletedEntries =
                    uiState.pendingDeletedEntries,
                undoEntry = uiState.undoEntry,
                restoringEntryId = uiState.restoringEntryId,
                deletingEntriesIds = uiState.deletingEntriesIds,

                onUndo = {
                    flowFireModel.undoDelete()
                },

                onUndoTimeout = {
                    flowFireModel.clearDeletedEntry()
                },

                onAnimationFinished = { id ->
                    if (uiState.deletingEntriesIds.contains(id)) {
                        flowFireModel.clearDeletedAnimation(id)
                    }

                    if (uiState.restoringEntryId == id) {
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
                        .firstOrNull { it.id == id }
                        ?.let { note ->
                            flowFireModel.deleteEntry(note)
                        }
                },

                onOpenSettings = {
                    navController.navigate(SETTINGS_ROUTE)
                },

                onReorderEntries = { noteIds ->
                    flowFireModel.updateEntriesPositions(noteIds)
                },

                onToggleCompleted =
                    flowFireModel::toggleCompleted,

                onToggleTaskNote =
                    flowFireModel::toggleTaskNote
            )
        }

        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                amoled = amoled,
                uiFont = uiFont,
                editorFont = editorFont,
                previewLines = previewLines,
                onAmoledChanged = { enabled ->
                    scope.launch {
                        AppPreferences.setAmoled(
                            context = context,
                            enabled = enabled
                        )
                    }
                },
                onUiFontChanged = { font ->
                    scope.launch {
                        AppPreferences.setUiFont(
                            context = context,
                            font = font
                        )
                    }
                },
                onEditorFontChanged = { font ->
                    scope.launch {
                        AppPreferences.setEditorFont(
                            context = context,
                            font = font
                        )
                    }
                },
                onPreviewLinesChanged = { lines ->
                    scope.launch {
                        AppPreferences.setPreviewLines(
                            context = context,
                            lines = lines
                        )
                    }
                },
                onExport = {
                    exportLauncher.launch("flow-backup.json")
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
            route = "$EDITOR_ROUTE/{$ENTRY_ID}?new={new}",
            arguments = listOf(
                navArgument(ENTRY_ID) {
                    type = NavType.StringType
                },
                navArgument("new") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->

            val routeEntryId =
                entry.arguments?.getString(ENTRY_ID)

            val isNew =
                entry.arguments?.getBoolean("new") ?: false

            var currentEntry by remember(
                routeEntryId,
                isNew
            ) {
                mutableStateOf<EntryUiModel?>(null)
            }

            var skipSaveOnDispose by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(
                routeEntryId,
                isNew
            ) {
                currentEntry = when {
                    isNew -> {
                        flowFireModel.createEntry()
                    }

                    !routeEntryId.isNullOrBlank() -> {
                        flowFireModel.getEntry(routeEntryId)
                    }

                    else -> {
                        null
                    }
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    if (skipSaveOnDispose) {
                        return@onDispose
                    }

                    val entryToSave =
                        currentEntry ?: return@onDispose

                    if (isNew) {
                        if (entryToSave.text.isNotBlank()) {
                            flowFireModel.saveNewEntry(entryToSave)
                            shouldScrollHomeToTop = true
                        }
                    } else {
                        flowFireModel.updateEntry(entryToSave)
                    }
                }
            }

            currentEntry?.let { entry ->
                EditorScreen(
                    entry = entry,
                    autoFocus = isNew,
                    uiFont = uiFont,
                    editorFont = editorFont,

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
                            navController.popBackStack()
                        } else {
                            flowFireModel.deleteEntry(entryToDelete)
                            navController.popBackStack()
                        }
                    },

                    onToggleTaskNote = {
                        flowFireModel.toggleTaskNote(
                            entry.id
                        )

                        currentEntry =
                            currentEntry?.copy(
                                type =
                                    if (
                                        entry.type ==
                                        dev.jvqtil.flow.data.ENTRY_TYPE_TASK
                                    ) {
                                        dev.jvqtil.flow.data.ENTRY_TYPE_NOTE
                                    } else {
                                        dev.jvqtil.flow.data.ENTRY_TYPE_TASK
                                    },
                                completed =
                                    if (
                                        entry.type ==
                                        dev.jvqtil.flow.data.ENTRY_TYPE_TASK
                                    ) {
                                        false
                                    } else {
                                        entry.completed
                                    }
                            )
                    }
                )
            }
        }
    }
}
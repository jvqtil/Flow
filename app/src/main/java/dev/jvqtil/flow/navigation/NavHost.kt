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
import dev.jvqtil.flow.data.NoteRepository
import dev.jvqtil.flow.ui.FlowFireModel
import dev.jvqtil.flow.ui.FlowFireModelFactory
import dev.jvqtil.flow.ui.NoteUiModel
import dev.jvqtil.flow.ui.components.EditorFont
import dev.jvqtil.flow.ui.components.UiFont
import dev.jvqtil.flow.ui.screens.HomeScreen
import dev.jvqtil.flow.ui.screens.NoteScreen
import dev.jvqtil.flow.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun FlowNavHost(
    repository: NoteRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val notes = repository.getAllNotes()

                    BackupManager.exportNotes(
                        context = context,
                        uri = uri,
                        notes = notes
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

                    repository.restoreNotes(notes)
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
            .background(MaterialTheme.colorScheme.background),
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
                notes = uiState.notes,
                previewLines = previewLines,
                shouldScrollToTop = shouldScrollHomeToTop,
                onScrollToTopHandled = {
                    shouldScrollHomeToTop = false
                },
                pendingDeletedNotes = uiState.pendingDeletedNotes,
                undoNote = uiState.undoNote,
                restoringNoteId = uiState.restoringNoteId,
                deletingNoteIds = uiState.deletingNoteIds,

                onUndo = {
                    flowFireModel.undoDelete()
                },

                onUndoTimeout = {
                    flowFireModel.clearDeletedNote()
                },

                onAnimationFinished = { id ->
                    if (uiState.deletingNoteIds.contains(id)) {
                        flowFireModel.clearDeletedAnimation(id)
                    }

                    if (uiState.restoringNoteId == id) {
                        flowFireModel.clearRestoringNote()
                    }
                },

                onAddNote = {
                    navController.navigate(
                        "$NOTE_ROUTE/new?new=true"
                    )
                },

                onOpenNote = { id ->
                    navController.navigate(
                        "$NOTE_ROUTE/$id?new=false"
                    )
                },

                onDeleteNote = { id ->
                    uiState.notes
                        .firstOrNull { it.id == id }
                        ?.let { note ->
                            flowFireModel.deleteNote(note)
                        }
                },

                onReorderNotes = { noteIds ->
                    flowFireModel.updateNotePositions(noteIds)
                },

                onOpenSettings = {
                    navController.navigate(SETTINGS_ROUTE)
                }
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
            route = "$NOTE_ROUTE/{$NOTE_ID}?new={new}",
            arguments = listOf(
                navArgument(NOTE_ID) {
                    type = NavType.StringType
                },
                navArgument("new") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->

            val routeNoteId =
                entry.arguments?.getString(NOTE_ID)

            val isNew =
                entry.arguments?.getBoolean("new") ?: false

            var note by remember(
                routeNoteId,
                isNew
            ) {
                mutableStateOf<NoteUiModel?>(null)
            }

            LaunchedEffect(
                routeNoteId,
                isNew
            ) {
                note = when {
                    isNew -> {
                        flowFireModel.createNote()
                    }

                    !routeNoteId.isNullOrBlank() -> {
                        flowFireModel.getNote(routeNoteId)
                    }

                    else -> {
                        null
                    }
                }
            }

            fun saveAndBack() {
                val currentNote = note

                if (currentNote != null) {
                    if (isNew) {
                        flowFireModel.saveNewNote(currentNote)
                        shouldScrollHomeToTop = true
                    } else {
                        flowFireModel.updateNote(currentNote)
                    }
                }

                navController.popBackStack()
            }

            NoteScreen(
                text = note?.text ?: "",
                autoFocus = isNew,
                uiFont = uiFont,
                editorFont = editorFont,

                onBack = {
                    saveAndBack()
                },

                onTextChange = { text ->
                    note = note?.copy(text = text)
                },

                onDelete = {
                    val currentNote = note

                    if (currentNote == null) {
                        navController.popBackStack()
                        return@NoteScreen
                    }

                    if (isNew) {
                        navController.popBackStack()
                    } else {
                        flowFireModel.deleteNote(currentNote)
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
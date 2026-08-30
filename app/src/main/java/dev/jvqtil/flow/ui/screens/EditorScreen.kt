package dev.jvqtil.flow.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.Attachment
import dev.jvqtil.flow.data.AttachmentStorage
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.data.MASTER_FOLDER_ID
import dev.jvqtil.flow.ui.EntryUiModel
import dev.jvqtil.flow.ui.FolderUiModel
import dev.jvqtil.flow.ui.components.FolderChoiceBottomSheet
import dev.jvqtil.flow.ui.components.NewFolderBottomSheet
import dev.jvqtil.flow.ui.components.UndoPopup
import dev.jvqtil.flow.ui.models.EditorFont
import dev.jvqtil.flow.ui.models.KeyboardMode
import dev.jvqtil.flow.ui.models.UiFont
import dev.jvqtil.flow.ui.models.fontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EditorScreen(
    entry: EntryUiModel,
    attachments: List<Attachment>,
    attachmentStorage: AttachmentStorage,
    folders: List<FolderUiModel>,
    selectedFolderId: String,
    autoFocus: Boolean,
    uiFont: UiFont,
    editorFont: EditorFont,
    keyboardMode: KeyboardMode,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onSelectFolder: (String) -> Unit,
    onCreateFolder: suspend (String) -> String?,
    onToggleTaskNote: () -> Unit,
    onAddAttachment: (List<String>) -> Unit,
    onOpenAttachment: (Attachment) -> Unit,
    onDeleteAttachment: (Attachment) -> Unit,
    onRestoreAttachment: (Attachment) -> Unit,
    onPermanentlyDeleteAttachment: (Attachment) -> Unit
) {
    val placeholders = stringArrayResource(
        R.array.editor_placeholders
    )

    val chosenPlaceholder = remember {
        placeholders.random()
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val focusManager = LocalFocusManager.current

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val keyboardVisible =
        WindowInsets.ime.getBottom(density) > 0

    var hasFocus by remember {
        mutableStateOf(false)
    }

    var showFolderPicker by remember {
        mutableStateOf(false)
    }

    var showNewFolderSheet by remember {
        mutableStateOf(false)
    }

    var newFolderName by remember {
        mutableStateOf("")
    }

    var deletedAttachment by remember {
        mutableStateOf<Attachment?>(null)
    }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            delay(200.milliseconds)
            focusRequester.requestFocus()
        }
    }

    val cursorVisible =
        keyboardVisible && hasFocus

    val editorFontFamily =
        when (editorFont) {
            EditorFont.UI_FONT ->
                uiFont.fontFamily()

            else ->
                editorFont.fontFamily(uiFont)
        }

    val attachmentLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            if (uris.isNotEmpty()) {
                onAddAttachment(
                    uris.map(Uri::toString)
                )
            }
        }

    val selectedFolder =
        folders.firstOrNull {
            it.id == selectedFolderId
        }

    BasicTextField(
        value = entry.text,
        onValueChange = onTextChange,
        keyboardOptions =
            when (keyboardMode) {
                KeyboardMode.NORMAL ->
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization =
                            KeyboardCapitalization.Sentences,
                        autoCorrectEnabled = true
                    )

                KeyboardMode.CODE ->
                    KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization =
                            KeyboardCapitalization.None,
                        autoCorrectEnabled = false
                    )
            },
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .focusRequester(focusRequester)
            .onFocusChanged {
                hasFocus = it.isFocused
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        textStyle = TextStyle(
            fontFamily = editorFontFamily,
            color =
                MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
            lineHeight = 26.sp
        ),
        cursorBrush = SolidColor(
            if (cursorVisible) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            }
        ),
        onTextLayout = {},
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    onClick = {
                        focusManager.clearFocus(
                            force = true
                        )

                        hasFocus = false

                        onBack()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription =
                            stringResource(
                                R.string.back_label
                            ),
                        tint =
                            MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = 8.dp,
                            end = 8.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .widthIn(
                                min = 80.dp,
                                max = 130.dp
                            )
                            .background(
                                MaterialTheme.colorScheme
                                    .surfaceContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                showFolderPicker = true
                            }
                            .padding(
                                horizontal = 12.dp
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 8.dp
                            ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Folder,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme
                                        .onBackground,
                                modifier =
                                    Modifier.size(20.dp)
                            )

                            Text(
                                text =
                                    if (
                                        selectedFolder?.id ==
                                        MASTER_FOLDER_ID &&
                                        selectedFolder.name
                                            .isBlank()
                                    ) {
                                        stringResource(
                                            R.string
                                                .master_folder_label
                                        )
                                    } else {
                                        selectedFolder
                                            ?.name
                                            .orEmpty()
                                    },
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis,
                                style =
                                    MaterialTheme.typography
                                        .labelLarge,
                                color =
                                    MaterialTheme.colorScheme
                                        .onBackground
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(92.dp)
                            .height(42.dp)
                            .background(
                                color =
                                    MaterialTheme.colorScheme
                                        .surfaceContainer,
                                shape =
                                    RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                onClick =
                                    onToggleTaskNote
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.SwapHoriz,
                                contentDescription =
                                    if (
                                        entry.type ==
                                        ENTRY_TYPE_TASK
                                    ) {
                                        stringResource(
                                            R.string
                                                .convert_to_note_label
                                        )
                                    } else {
                                        stringResource(
                                            R.string
                                                .convert_to_task_label
                                        )
                                    },
                                tint =
                                    MaterialTheme.colorScheme
                                        .onBackground,
                                modifier =
                                    Modifier.size(20.dp)
                            )

                            Text(
                                text =
                                    if (
                                        entry.type ==
                                        ENTRY_TYPE_TASK
                                    ) {
                                        stringResource(
                                            R.string.task_label
                                        )
                                    } else {
                                        stringResource(
                                            R.string.note_label
                                        )
                                    },
                                color =
                                    MaterialTheme.colorScheme
                                        .onBackground,
                                style =
                                    MaterialTheme.typography
                                        .labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            attachmentLauncher.launch(
                                arrayOf("*/*")
                            )
                        },
                        enabled =
                            entry.text.isNotBlank(),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.AttachFile,
                            contentDescription =
                                stringResource(
                                    R.string
                                        .attach_file_label
                                ),
                            tint =
                                MaterialTheme.colorScheme
                                    .onBackground
                        )
                    }

                    IconButton(
                        onClick = {
                            focusManager.clearFocus(
                                force = true
                            )

                            hasFocus = false

                            onDelete()
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.DeleteOutline,
                            contentDescription =
                                stringResource(
                                    R.string.delete_label
                                ),
                            tint =
                                MaterialTheme.colorScheme
                                    .onBackground,
                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 76.dp,
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 12.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (entry.text.isEmpty()) {
                            Text(
                                text =
                                    chosenPlaceholder,
                                style =
                                    MaterialTheme.typography
                                        .bodyLarge
                                        .copy(
                                            fontFamily =
                                                editorFontFamily
                                        ),
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }

                        innerTextField()
                    }

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 4.dp
                            )
                    ) {
                        items(
                            items = attachments,
                            key = { it.id }
                        ) { attachment ->
                            AnimatedVisibility(
                                visible =
                                    deletedAttachment?.id !=
                                            attachment.id,
                                enter =
                                    slideInVertically(
                                        initialOffsetY = {
                                            it
                                        },
                                        animationSpec =
                                            tween(250)
                                    ),
                                exit =
                                    slideOutVertically(
                                        targetOffsetY = {
                                            it * 2
                                        },
                                        animationSpec =
                                            tween(300)
                                    )
                            ) {
                                AttachmentPreview(
                                    attachment = attachment,
                                    attachmentStorage =
                                        attachmentStorage,
                                    onOpen = {
                                        if (
                                            deletedAttachment?.id !=
                                            attachment.id
                                        ) {
                                            onOpenAttachment(
                                                attachment
                                            )
                                        }
                                    },
                                    onDelete = {
                                        if (
                                            deletedAttachment ==
                                            null
                                        ) {
                                            deletedAttachment =
                                                attachment

                                            onDeleteAttachment(
                                                attachment
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )

    if (showFolderPicker) {
        FolderChoiceBottomSheet(
            folders = folders,
            selectedFolderId =
                selectedFolderId,
            onSelect = {
                onSelectFolder(it)
                showFolderPicker = false
            },
            onCreateFolder = {
                showFolderPicker = false
                newFolderName = ""
                showNewFolderSheet = true
            },
            onDismiss = {
                showFolderPicker = false
            }
        )
    }

    if (showNewFolderSheet) {
        NewFolderBottomSheet(
            value = newFolderName,
            onValueChange = {
                newFolderName = it
            },
            onCreate = {
                val name =
                    newFolderName.trim()

                if (name.isNotBlank()) {
                    scope.launch {
                        val folderId =
                            onCreateFolder(name)

                        if (folderId != null) {
                            onSelectFolder(
                                folderId
                            )
                        }

                        newFolderName = ""
                        showNewFolderSheet = false
                    }
                }
            },
            onDismiss = {
                newFolderName = ""
                showNewFolderSheet = false
            }
        )
    }

    deletedAttachment?.let { attachment ->
        UndoPopup(
            id = attachment.id,
            onUndo = {
                onRestoreAttachment(
                    attachment
                )

                deletedAttachment = null
            },
            onTimeout = {
                onPermanentlyDeleteAttachment(
                    attachment
                )

                deletedAttachment = null
            }
        )
    }

    val currentDeletedAttachment by rememberUpdatedState(
        deletedAttachment
    )

    DisposableEffect(Unit) {
        onDispose {
            currentDeletedAttachment?.let { attachment ->
                onPermanentlyDeleteAttachment(attachment)
            }
        }
    }
}

@Composable
private fun AttachmentPreview(
    attachment: Attachment,
    attachmentStorage: AttachmentStorage,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val isImage =
        attachment.mimeType?.startsWith("image/") == true

    val file = remember(attachment.path) {
        attachmentStorage.getFile(
            attachment.path
        )
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(96.dp)
            .clickable(
                onClick = onOpen
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isImage) {
                AsyncImage(
                    model = file.toUri(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 14.dp,
                            end = 48.dp,
                            top = 14.dp,
                            bottom = 14.dp
                        ),
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = attachment.fileName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.85f
                )
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(
                            R.string.delete_label
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
package dev.jvqtil.flow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.MASTER_FOLDER_ID
import dev.jvqtil.flow.ui.FolderUiModel

private val SheetShape = RoundedCornerShape(18.dp)
private val FieldShape = RoundedCornerShape(14.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    top = 4.dp,
                    end = 20.dp,
                    bottom = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SheetTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            start = 4.dp,
            top = 4.dp,
            bottom = 8.dp
        )
    )
}

@Composable
private fun SheetRow(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = SheetShape
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        )

        trailingContent?.invoke()
    }
}

@Composable
fun FolderChoiceBottomSheet(
    folders: List<FolderUiModel>,
    selectedFolderId: String,
    onSelect: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    FolderBottomSheet(
        onDismiss = onDismiss
    ) {
        SheetTitle(
            text = stringResource(R.string.switch_folder_label)
        )
        folders.forEach { folder ->
            val selected = folder.id == selectedFolderId

            SheetRow(
                onClick = {
                    onSelect(folder.id)
                    onDismiss()
                },
                title = if (folder.id == MASTER_FOLDER_ID && folder.name.isBlank())
                    stringResource(R.string.master_folder_label)
                else folder.name,
                backgroundColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint =
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingContent = if (selected) {
                    @Composable {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    null
                }
            )
        }

        SheetRow(
            onClick = onCreateFolder,
            title = stringResource(R.string.new_folder_label),
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        )
    }
}

@Composable
fun FolderActionsBottomSheet(
    folder: FolderUiModel,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    FolderBottomSheet(
        onDismiss = onDismiss
    ) {
        SheetTitle(
            text = if (folder.id == MASTER_FOLDER_ID && folder.name.isBlank())
                stringResource(R.string.master_folder_label)
            else folder.name
        )

        SheetRow(
            onClick = onRename,
            title = stringResource(R.string.rename_label),
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        )

        if (folder.id != MASTER_FOLDER_ID) {
            SheetRow(
                onClick = onDelete,
                title = stringResource(R.string.delete_label),
                titleColor = MaterialTheme.colorScheme.error,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun FolderEditorBottomSheet(
    title: String,
    value: String,
    actionText: String,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    val focusRequester = remember {
        FocusRequester()
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    FolderBottomSheet(
        onDismiss = onDismiss
    ) {
        SheetTitle(
            text = title
        )

        extraContent?.invoke()

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            label = {
                Text(
                    stringResource(R.string.placeholder_name)
                )
            },
            shape = FieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Button(
            onClick = onAction,
            enabled = value.trim().isNotBlank(),
            shape = FieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = actionText
            )
        }
    }
}

@Composable
fun RenameFolderBottomSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    FolderEditorBottomSheet(
        title = stringResource(R.string.rename_folder_label),
        value = value,
        actionText = stringResource(R.string.rename_label),
        onValueChange = onValueChange,
        onAction = onSave,
        onDismiss = onDismiss,
        extraContent = {
            SheetRow(
                onClick = onReset,
                title = stringResource(R.string.reset_folder_name_label),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }
    )
}

@Composable
fun NewFolderBottomSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    FolderEditorBottomSheet(
        title = stringResource(R.string.new_folder_label),
        value = value,
        actionText = stringResource(R.string.create_label),
        onValueChange = onValueChange,
        onAction = onCreate,
        onDismiss = onDismiss
    )
}
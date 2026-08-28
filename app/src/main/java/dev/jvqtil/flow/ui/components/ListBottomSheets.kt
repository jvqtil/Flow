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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.ALL_LIST_ID
import dev.jvqtil.flow.ui.EntryListUiModel

private val SheetShape = RoundedCornerShape(18.dp)
private val FieldShape = RoundedCornerShape(14.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
    titleColor: Color =
        MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color =
        MaterialTheme.colorScheme.surfaceContainer,
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
fun ListChoiceBottomSheet(
    lists: List<EntryListUiModel>,
    selectedListId: String,
    onSelect: (String) -> Unit,
    onCreateList: () -> Unit,
    onDismiss: () -> Unit
) {
    ListBottomSheet(
        onDismiss = onDismiss
    ) {
        SheetTitle(
            text = stringResource(R.string.switch_list_label)
        )

        lists.forEach { list ->
            val selected = list.id == selectedListId

            val listName =
                if (list.id == ALL_LIST_ID) {
                    stringResource(R.string.all_list_label)
                } else {
                    list.name
                }

            SheetRow(
                onClick = {
                    onSelect(list.id)
                    onDismiss()
                },
                title = listName,
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
                trailingContent =
                    if (selected) {
                        {
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
            onClick = onCreateList,
            title = stringResource(R.string.new_list_label),
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            },
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

@Composable
fun ListActionsBottomSheet(
    list: EntryListUiModel,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ListBottomSheet(
        onDismiss = onDismiss
    ) {
        val listName =
            if (list.id == ALL_LIST_ID) {
                stringResource(R.string.all_list_label)
            } else {
                list.name
            }

        SheetTitle(
            text = listName
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

        SheetRow(
            onClick = onDelete,
            title = stringResource(R.string.delete_label),
            titleColor = MaterialTheme.colorScheme.error,
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
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

@Composable
private fun ListEditorBottomSheet(
    title: String,
    value: String,
    actionText: String,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    ListBottomSheet(
        onDismiss = onDismiss
    ) {
        SheetTitle(
            text = title
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            label = { stringResource(R.string.placeholder_name) },
            shape = FieldShape,
            modifier = Modifier.fillMaxWidth()
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
fun RenameListBottomSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ListEditorBottomSheet(
        title = stringResource(R.string.rename_list_label),
        value = value,
        actionText = stringResource(R.string.rename_label),
        onValueChange = onValueChange,
        onAction = onSave,
        onDismiss = onDismiss
    )
}

@Composable
fun NewListBottomSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    ListEditorBottomSheet(
        title = stringResource(R.string.new_list_label),
        value = value,
        actionText = stringResource(R.string.create_label),
        onValueChange = onValueChange,
        onAction = onCreate,
        onDismiss = onDismiss
    )
}
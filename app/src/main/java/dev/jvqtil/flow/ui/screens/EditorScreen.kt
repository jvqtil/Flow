package dev.jvqtil.flow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.ui.EntryUiModel
import dev.jvqtil.flow.ui.models.EditorFont
import dev.jvqtil.flow.ui.models.KeyboardMode
import dev.jvqtil.flow.ui.models.UiFont
import dev.jvqtil.flow.ui.models.fontFamily
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EditorScreen(
    entry: EntryUiModel,
    autoFocus: Boolean,
    uiFont: UiFont,
    editorFont: EditorFont,
    keyboardMode: KeyboardMode,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onToggleTaskNote: () -> Unit
) {
    val placeholders = stringArrayResource(
        R.array.editor_placeholders
    )

    val focusRequester = remember {
        FocusRequester()
    }

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val keyboardVisible =
        WindowInsets.ime.getBottom(density) > 0

    var hasFocus by remember {
        mutableStateOf(false)
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
            color = MaterialTheme.colorScheme.onBackground,
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
                        focusManager.clearFocus(force = true)
                        hasFocus = false
                        onBack()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint =
                            MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = 11.dp,
                            end = 52.dp
                        )
                        .size(width = 84.dp, height = 42.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            onClick = onToggleTaskNote
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription =
                                if (entry.type == ENTRY_TYPE_TASK) {
                                    stringResource(R.string.convert_to_note_label)
                                } else {
                                    stringResource(R.string.convert_to_task_label)
                                },
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text =
                                if (entry.type == ENTRY_TYPE_TASK) {
                                    stringResource(R.string.task_label)
                                } else {
                                    stringResource(R.string.note_label)
                                },
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        hasFocus = false
                        onDelete()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_label),
                        tint =
                            MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 68.dp,
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 12.dp
                        )
                ) {
                    if (entry.text.isEmpty()) {
                        Text(
                            text = placeholders.random(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = editorFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    innerTextField()
                }
            }
        }
    )
}
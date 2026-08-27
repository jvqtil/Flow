package dev.jvqtil.flow.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.jvqtil.flow.BuildConfig
import dev.jvqtil.flow.ui.models.EditorFont
import dev.jvqtil.flow.ui.models.KeyboardMode
import dev.jvqtil.flow.ui.models.UiFont
import dev.jvqtil.flow.ui.models.fontFamily
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    amoled: Boolean,
    onAmoledChanged: (Boolean) -> Unit,
    uiFont: UiFont,
    onUiFontChanged: (UiFont) -> Unit,
    editorFont: EditorFont,
    onEditorFontChanged: (EditorFont) -> Unit,
    previewLines: Int,
    onPreviewLinesChanged: (Int) -> Unit,
    keyboardMode: KeyboardMode,
    onKeyboardModeChanged: (KeyboardMode) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = "Pure black",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = "For AMOLED displays · Dark mode only",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = amoled,
                enabled = isDark,
                onCheckedChange = onAmoledChanged
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Fonts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        SettingCard(
            title = "UI",
            options = listOf(
                SettingOption(
                    title = "Default",
                    fontFamily = UiFont.DEFAULT.fontFamily(),
                    selected = uiFont == UiFont.DEFAULT,
                    onClick = {
                        onUiFontChanged(UiFont.DEFAULT)
                    }
                ),
                SettingOption(
                    title = "Serif",
                    fontFamily = UiFont.SERIF.fontFamily(),
                    selected = uiFont == UiFont.SERIF,
                    onClick = {
                        onUiFontChanged(UiFont.SERIF)
                    }
                ),
                SettingOption(
                    title = "Monospace",
                    fontFamily = UiFont.MONOSPACE.fontFamily(),
                    selected = uiFont == UiFont.MONOSPACE,
                    onClick = {
                        onUiFontChanged(UiFont.MONOSPACE)
                    }
                ),
                SettingOption(
                    title = "JetBrains Mono",
                    fontFamily = UiFont.JETBRAINS_MONO.fontFamily(),
                    selected = uiFont == UiFont.JETBRAINS_MONO,
                    onClick = {
                        onUiFontChanged(UiFont.JETBRAINS_MONO)
                    }
                )
            )
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        SettingCard(
            title = "Editor",
            options = listOf(
                SettingOption(
                    title = "UI Font",
                    fontFamily = uiFont.fontFamily(),
                    selected = editorFont == EditorFont.UI_FONT,
                    onClick = {
                        onEditorFontChanged(EditorFont.UI_FONT)
                    }
                ),
                SettingOption(
                    title = "Serif",
                    fontFamily = FontFamily.Serif,
                    selected = editorFont == EditorFont.SERIF,
                    onClick = {
                        onEditorFontChanged(EditorFont.SERIF)
                    }
                ),
                SettingOption(
                    title = "Monospace",
                    fontFamily = FontFamily.Monospace,
                    selected = editorFont == EditorFont.MONOSPACE,
                    onClick = {
                        onEditorFontChanged(EditorFont.MONOSPACE)
                    }
                ),
                SettingOption(
                    title = "JetBrains Mono",
                    fontFamily = EditorFont.JETBRAINS_MONO.fontFamily(uiFont),
                    selected = editorFont == EditorFont.JETBRAINS_MONO,
                    onClick = {
                        onEditorFontChanged(EditorFont.JETBRAINS_MONO)
                    }
                )
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "$previewLines lines",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Slider(
            value = previewLines.toFloat(),
            onValueChange = { value ->
                onPreviewLinesChanged(value.roundToInt())
            },
            valueRange = 3f..12f,
            steps = 8
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Keyboard",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Changes how keyboard behaves",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            KeyboardMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = keyboardMode == mode,
                    onClick = {
                        onKeyboardModeChanged(mode)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = KeyboardMode.entries.size
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (mode) {
                            KeyboardMode.NORMAL -> "Normal"
                            KeyboardMode.CODE -> "Code"
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Backup & Restore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onExport,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Export",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                onClick = onImport,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Import",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 32.dp,
                    bottom = 12.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Flow ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/jvqtil/Flow".toUri()
                        )

                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Source Code",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.NorthEast,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private data class SettingOption(
    val title: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val fontFamily: FontFamily? = null
)

@Composable
private fun SettingCard(
    title: String,
    options: List<SettingOption>,
    columns: Int = 2
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = 4.dp,
                    vertical = 2.dp
                )
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            options.chunked(columns).forEachIndexed { index, rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowOptions.forEach { option ->
                        SettingOptionButton(
                            option = option,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat(columns - rowOptions.size) {
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (index < (options.size - 1) / columns) {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingOptionButton(
    option: SettingOption,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = option.onClick,
        color = if (option.selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = option.fontFamily
                ),
                fontWeight = if (option.selected) {
                    FontWeight.Medium
                } else {
                    FontWeight.Normal
                },
                color = if (option.selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
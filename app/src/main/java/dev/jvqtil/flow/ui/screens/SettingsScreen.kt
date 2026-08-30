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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.jvqtil.flow.BuildConfig
import dev.jvqtil.flow.R
import dev.jvqtil.flow.data.ENTRY_TYPE_NOTE
import dev.jvqtil.flow.data.ENTRY_TYPE_TASK
import dev.jvqtil.flow.data.Feature
import dev.jvqtil.flow.ui.models.EditorFont
import dev.jvqtil.flow.ui.models.KeyboardMode
import dev.jvqtil.flow.ui.models.UiFont
import dev.jvqtil.flow.ui.models.fontFamily
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    enabledFeatures: Set<Feature>,
    onFeatureChanged: (Feature, Boolean) -> Unit,
    defaultEntryType: String,
    onDefaultEntryTypeChanged: (String) -> Unit,
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                )
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_label),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = stringResource(R.string.settings_label),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = stringResource(R.string.default_entry_type_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                val entryTypes = listOf(
                    ENTRY_TYPE_NOTE to R.string.note_label,
                    ENTRY_TYPE_TASK to R.string.task_label
                )

                entryTypes.forEachIndexed { index, (type, labelRes) ->
                    SegmentedButton(
                        selected = defaultEntryType == type,
                        onClick = {
                            onDefaultEntryTypeChanged(type)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = entryTypes.size
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(labelRes)
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
                text = stringResource(R.string.features_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            FeatureSwitch(
                title = stringResource(R.string.folders_label),
                description = stringResource(R.string.folders_description),
                checked = Feature.FOLDERS in enabledFeatures,
                onCheckedChange = { enabled ->
                    onFeatureChanged(
                        Feature.FOLDERS,
                        enabled
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            FeatureSwitch(
                title = stringResource(R.string.swipe_gestures_label),
                description = stringResource(R.string.swipe_gestures_description),
                checked = Feature.SWIPE_GESTURES in enabledFeatures,
                onCheckedChange = { enabled ->
                    onFeatureChanged(
                        Feature.SWIPE_GESTURES,
                        enabled
                    )
                }
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
                text = stringResource(R.string.appearance_label),
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
                val pureBlackEnabled =
                    Feature.PURE_BLACK in enabledFeatures

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pure_black_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = stringResource(R.string.pure_black_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = stringResource(R.string.dark_mode_only_label),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = pureBlackEnabled,
                    enabled = isDark,
                    onCheckedChange = { enabled ->
                        onFeatureChanged(
                            Feature.PURE_BLACK,
                            enabled
                        )
                    }
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
                text = stringResource(R.string.fonts_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SettingCard(
                title = stringResource(R.string.ui_label),
                options = listOf(
                    SettingOption(
                        title = stringResource(R.string.default_font),
                        fontFamily = UiFont.DEFAULT.fontFamily(),
                        selected = uiFont == UiFont.DEFAULT,
                        onClick = {
                            onUiFontChanged(UiFont.DEFAULT)
                        }
                    ),
                    SettingOption(
                        title = stringResource(R.string.serif_font),
                        fontFamily = UiFont.SERIF.fontFamily(),
                        selected = uiFont == UiFont.SERIF,
                        onClick = {
                            onUiFontChanged(UiFont.SERIF)
                        }
                    ),
                    SettingOption(
                        title = stringResource(R.string.monospace_font),
                        fontFamily = UiFont.MONOSPACE.fontFamily(),
                        selected = uiFont == UiFont.MONOSPACE,
                        onClick = {
                            onUiFontChanged(UiFont.MONOSPACE)
                        }
                    ),
                    SettingOption(
                        title = stringResource(R.string.jetbrains_mono_font),
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
                title = stringResource(R.string.editor_label),
                options = listOf(
                    SettingOption(
                        title = stringResource(R.string.ui_font_font),
                        fontFamily = uiFont.fontFamily(),
                        selected = editorFont == EditorFont.UI_FONT,
                        onClick = {
                            onEditorFontChanged(EditorFont.UI_FONT)
                        }
                    ),
                    SettingOption(
                        title = stringResource(R.string.serif_font),
                        fontFamily = FontFamily.Serif,
                        selected = editorFont == EditorFont.SERIF,
                        onClick = {
                            onEditorFontChanged(EditorFont.SERIF)
                        }
                    ),
                    SettingOption(
                        title = stringResource(R.string.monospace_font),
                        fontFamily = FontFamily.Monospace,
                        selected = editorFont == EditorFont.MONOSPACE,
                        onClick = {
                            onEditorFontChanged(EditorFont.MONOSPACE)
                        }
                    ),
                    SettingOption(
                        title = stringResource(R.string.jetbrains_mono_font),
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
                text = stringResource(R.string.preview_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                pluralStringResource(
                    R.plurals.preview_lines,
                    previewLines,
                    previewLines
                ),
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
                text = stringResource(R.string.keyboard_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
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
                                KeyboardMode.NORMAL ->
                                    stringResource(R.string.normal_label)

                                KeyboardMode.CODE ->
                                    stringResource(R.string.code_label)
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
                text = stringResource(R.string.backup_restore_label),
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
                            text = stringResource(R.string.export_label),
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
                            text = stringResource(R.string.import_label),
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
                        text = "${stringResource(R.string.app_name)} ${BuildConfig.VERSION_NAME}",
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
                                text = stringResource(R.string.source_code_label),
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

@Composable
private fun FeatureSwitch(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
package dev.jvqtil.flow.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import dev.jvqtil.flow.ui.components.EditorFont
import dev.jvqtil.flow.ui.components.UiFont
import dev.jvqtil.flow.ui.components.fontFamily

@SuppressLint("UseKtx")
@Composable
fun SettingsScreen(
    amoled: Boolean,
    uiFont: UiFont,
    editorFont: EditorFont,
    onAmoledChanged: (Boolean) -> Unit,
    onUiFontChanged: (UiFont) -> Unit,
    onEditorFontChanged: (EditorFont) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
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
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AMOLED",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Pure black background",
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

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Fonts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        FontSettingCard(
            title = "UI",
            options = listOf(
                FontOption(
                    title = "Default",
                    fontFamily = UiFont.DEFAULT.fontFamily(),
                    selected = uiFont == UiFont.DEFAULT,
                    onClick = {
                        onUiFontChanged(UiFont.DEFAULT)
                    }
                ),
                FontOption(
                    title = "Serif",
                    fontFamily = UiFont.SERIF.fontFamily(),
                    selected = uiFont == UiFont.SERIF,
                    onClick = {
                        onUiFontChanged(UiFont.SERIF)
                    }
                ),
                FontOption(
                    title = "Monospace",
                    fontFamily = UiFont.MONOSPACE.fontFamily(),
                    selected = uiFont == UiFont.MONOSPACE,
                    onClick = {
                        onUiFontChanged(UiFont.MONOSPACE)
                    }
                ),
                FontOption(
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
            modifier = Modifier.height(10.dp)
        )

        FontSettingCard(
            title = "Editor",
            options = listOf(
                FontOption(
                    title = "UI Font",
                    fontFamily = uiFont.fontFamily(),
                    selected = editorFont == EditorFont.UI_FONT,
                    onClick = {
                        onEditorFontChanged(EditorFont.UI_FONT)
                    }
                ),
                FontOption(
                    title = "Serif",
                    fontFamily = FontFamily.Serif,
                    selected = editorFont == EditorFont.SERIF,
                    onClick = {
                        onEditorFontChanged(EditorFont.SERIF)
                    }
                ),
                FontOption(
                    title = "Monospace",
                    fontFamily = FontFamily.Monospace,
                    selected = editorFont == EditorFont.MONOSPACE,
                    onClick = {
                        onEditorFontChanged(EditorFont.MONOSPACE)
                    }
                ),
                FontOption(
                    title = "JetBrains Mono",
                    fontFamily = EditorFont.JETBRAINS_MONO.fontFamily(uiFont),
                    selected = editorFont == EditorFont.JETBRAINS_MONO,
                    onClick = {
                        onEditorFontChanged(EditorFont.JETBRAINS_MONO)
                    }
                )
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentAlignment = Alignment.BottomCenter
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
                            text = "GitHub",
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

private data class FontOption(
    val title: String,
    val fontFamily: FontFamily,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun FontSettingCard(
    title: String,
    options: List<FontOption>
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
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

            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowOptions.forEach { option ->
                        FontOptionButton(
                            option = option,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowOptions.size == 1) {
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (rowOptions !== options.chunked(2).last()) {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FontOptionButton(
    option: FontOption,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = option.onClick,
        color = if (option.selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp,
                    vertical = 10.dp
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
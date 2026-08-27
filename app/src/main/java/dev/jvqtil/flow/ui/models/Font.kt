package dev.jvqtil.flow.ui.models

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.jvqtil.flow.R

private val JetBrainsMono = FontFamily(
    Font(
        resId = R.font.jetbrains_mono_regular,
        weight = FontWeight.Normal
    )
)

enum class UiFont {
    DEFAULT,
    SERIF,
    MONOSPACE,
    JETBRAINS_MONO
}

enum class EditorFont {
    UI_FONT,
    SERIF,
    MONOSPACE,
    JETBRAINS_MONO
}

fun UiFont.fontFamily(): FontFamily {
    return when (this) {
        UiFont.DEFAULT -> FontFamily.Default
        UiFont.SERIF -> FontFamily.Serif
        UiFont.MONOSPACE -> FontFamily.Monospace
        UiFont.JETBRAINS_MONO -> JetBrainsMono
    }
}

fun EditorFont.fontFamily(
    uiFont: UiFont
): FontFamily {
    return when (this) {
        EditorFont.UI_FONT -> {
            uiFont.fontFamily()
        }

        EditorFont.SERIF -> {
            FontFamily.Serif
        }

        EditorFont.MONOSPACE -> {
            FontFamily.Monospace
        }

        EditorFont.JETBRAINS_MONO -> {
            JetBrainsMono
        }
    }
}
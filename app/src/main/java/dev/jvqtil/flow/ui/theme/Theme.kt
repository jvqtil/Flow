package dev.jvqtil.flow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.jvqtil.flow.data.AppPreferences

private val FallbackDarkColors = darkColorScheme(
    background = FlowBackground,
    onBackground = Color(0xFFE6E1E9),
    surface = FlowSurface,
    onSurface = Color(0xFFE6E1E9)
)

private val FallbackAmoledColors = darkColorScheme(
    background = FlowAmoledBackground,
    onBackground = Color(0xFFE6E1E9),
    surface = FlowAmoledSurface,
    onSurface = Color(0xFFE6E1E9)
)

private val FallbackLightColors = lightColorScheme(
    background = FlowLightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = FlowLightSurface,
    onSurface = Color(0xFF1A1A1A)
)

@Composable
fun FlowTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val amoled by AppPreferences
        .observeAmoled(context)
        .collectAsState(initial = false)

    val colors = when {
        isDark && amoled -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context).copy(
                    background = FlowAmoledBackground,
                    surface = FlowAmoledSurface
                )
            } else {
                FallbackAmoledColors
            }
        }

        isDark -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context).copy(
                    background = FlowBackground,
                    surface = FlowSurface
                )
            } else {
                FallbackDarkColors
            }
        }

        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context).copy(
                    background = FlowLightBackground,
                    surface = FlowLightSurface
                )
            } else {
                FallbackLightColors
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
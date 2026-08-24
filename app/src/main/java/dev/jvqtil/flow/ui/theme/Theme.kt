package dev.jvqtil.flow.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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

    val baseColors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDark -> {
            dynamicDarkColorScheme(context)
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context)
        }

        isDark && amoled -> {
            FallbackAmoledColors
        }

        isDark -> {
            FallbackDarkColors
        }

        else -> {
            FallbackLightColors
        }
    }

    val targetBackground = when {
        !isDark -> FlowLightBackground
        amoled -> FlowAmoledBackground
        else -> FlowBackground
    }

    val targetSurface = when {
        !isDark -> FlowLightSurface
        amoled -> FlowAmoledSurface
        else -> FlowSurface
    }

    val background by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(
            durationMillis = 650,
            easing = LinearEasing
        ),
        label = "themeBackground"
    )

    val surface by animateColorAsState(
        targetValue = targetSurface,
        animationSpec = tween(
            durationMillis = 650,
            easing = LinearEasing
        ),
        label = "themeSurface"
    )

    val colors = baseColors.copy(
        background = background,
        surface = surface
    )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
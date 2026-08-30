package dev.jvqtil.flow.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import dev.jvqtil.flow.data.AppPreferences
import dev.jvqtil.flow.data.Feature
import dev.jvqtil.flow.ui.models.UiFont
import dev.jvqtil.flow.ui.models.fontFamily

private val FallbackDarkColors = darkColorScheme(
    background = FlowBackground,
    onBackground = Color(0xFFE6E1E9),
    surface = FlowSurface,
    onSurface = Color(0xFFE6E1E9)
)

private val FallbackAmoledColors = darkColorScheme(
    background = FlowPureBlackBackground,
    onBackground = Color(0xFFE6E1E9),
    surface = FlowPureBlackSurface,
    onSurface = Color(0xFFE6E1E9)
)

private val FallbackLightColors = lightColorScheme(
    background = FlowLightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = FlowLightSurface,
    onSurface = Color(0xFF1A1A1A)
)

val LocalAppFont = staticCompositionLocalOf<FontFamily> {
    FontFamily.Default
}

@Composable
fun FlowTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val enabledFeatures by AppPreferences
        .observeFeatures(context)
        .collectAsState(
            initial = setOf(
                Feature.SWIPE_GESTURES
            )
        )

    val pureBlackEnabled =
        Feature.PURE_BLACK in enabledFeatures

    val uiFont by AppPreferences
        .observeUiFont(context)
        .collectAsState(
            initial = UiFont.DEFAULT
        )

    val baseColors = when {
        isDark && pureBlackEnabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
            FallbackAmoledColors
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDark -> {
            dynamicDarkColorScheme(context)
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context)
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
        pureBlackEnabled -> FlowPureBlackBackground
        else -> FlowBackground
    }

    val targetSurface = when {
        !isDark -> FlowLightSurface
        pureBlackEnabled -> FlowPureBlackSurface
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

    val typography = Typography().run {
        copy(
            displayLarge = displayLarge.copy(
                fontFamily = uiFont.fontFamily()
            ),
            displayMedium = displayMedium.copy(
                fontFamily = uiFont.fontFamily()
            ),
            displaySmall = displaySmall.copy(
                fontFamily = uiFont.fontFamily()
            ),
            headlineLarge = headlineLarge.copy(
                fontFamily = uiFont.fontFamily()
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = uiFont.fontFamily()
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = uiFont.fontFamily()
            ),
            titleLarge = titleLarge.copy(
                fontFamily = uiFont.fontFamily()
            ),
            titleMedium = titleMedium.copy(
                fontFamily = uiFont.fontFamily()
            ),
            titleSmall = titleSmall.copy(
                fontFamily = uiFont.fontFamily()
            ),
            bodyLarge = bodyLarge.copy(
                fontFamily = uiFont.fontFamily()
            ),
            bodyMedium = bodyMedium.copy(
                fontFamily = uiFont.fontFamily()
            ),
            bodySmall = bodySmall.copy(
                fontFamily = uiFont.fontFamily()
            ),
            labelLarge = labelLarge.copy(
                fontFamily = uiFont.fontFamily()
            ),
            labelMedium = labelMedium.copy(
                fontFamily = uiFont.fontFamily()
            ),
            labelSmall = labelSmall.copy(
                fontFamily = uiFont.fontFamily()
            )
        )
    }

    CompositionLocalProvider(
        LocalAppFont provides uiFont.fontFamily()
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = content
        )
    }
}
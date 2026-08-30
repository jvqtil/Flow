package dev.jvqtil.flow.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.jvqtil.flow.ui.models.EditorFont
import dev.jvqtil.flow.ui.models.KeyboardMode
import dev.jvqtil.flow.ui.models.UiFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "flow_preferences"
)

object AppPreferences {
    private val LAST_UPDATE_CHECK =
        longPreferencesKey("last_update_check")

    private val AMOLED_KEY =
        booleanPreferencesKey("AMOLED")

    private val UI_FONT_KEY =
        stringPreferencesKey("ui_font")

    private val EDITOR_FONT_KEY =
        stringPreferencesKey("editor_font")

    private val PREVIEW_LINES_KEY =
        intPreferencesKey("preview_lines")

    private val KEYBOARD_MODE =
        stringPreferencesKey("keyboard_mode")

    suspend fun getLastUpdateCheck(
        context: Context
    ): Long {
        return context.dataStore.data
            .map { preferences ->
                preferences[LAST_UPDATE_CHECK] ?: 0L
            }
            .first()
    }

    suspend fun setLastUpdateCheck(
        context: Context,
        timestamp: Long
    ) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UPDATE_CHECK] = timestamp
        }
    }

    fun observeAmoled(
        context: Context
    ): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[AMOLED_KEY] ?: false
        }
    }

    suspend fun setAmoled(
        context: Context,
        enabled: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[AMOLED_KEY] = enabled
        }
    }

    fun observeUiFont(
        context: Context
    ): Flow<UiFont> {
        return context.dataStore.data.map { preferences ->
            preferences[UI_FONT_KEY]
                ?.let {
                    runCatching {
                        UiFont.valueOf(it)
                    }.getOrNull()
                }
                ?: UiFont.DEFAULT
        }
    }

    suspend fun setUiFont(
        context: Context,
        font: UiFont
    ) {
        context.dataStore.edit { preferences ->
            preferences[UI_FONT_KEY] = font.name
        }
    }

    fun observeEditorFont(
        context: Context
    ): Flow<EditorFont> {
        return context.dataStore.data.map { preferences ->
            preferences[EDITOR_FONT_KEY]
                ?.let {
                    runCatching {
                        EditorFont.valueOf(it)
                    }.getOrNull()
                }
                ?: EditorFont.UI_FONT
        }
    }

    suspend fun setEditorFont(
        context: Context,
        font: EditorFont
    ) {
        context.dataStore.edit { preferences ->
            preferences[EDITOR_FONT_KEY] = font.name
        }
    }

    fun observePreviewLines(
        context: Context
    ): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PREVIEW_LINES_KEY] ?: 4
        }
    }

    suspend fun setPreviewLines(
        context: Context,
        lines: Int
    ) {
        context.dataStore.edit { preferences ->
            preferences[PREVIEW_LINES_KEY] = lines
        }
    }

    fun observeKeyboardMode(
        context: Context
    ): Flow<KeyboardMode> =
        context.dataStore.data.map { preferences ->
            when (preferences[KEYBOARD_MODE]) {
                KeyboardMode.CODE.name ->
                    KeyboardMode.CODE

                else ->
                    KeyboardMode.NORMAL
            }
        }

    suspend fun setKeyboardMode(
        context: Context,
        mode: KeyboardMode
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEYBOARD_MODE] = mode.name
        }
    }
}
package dev.jvqtil.flow.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

enum class Feature(
    val defaultEnabled: Boolean
) {
    FOLDERS(
        defaultEnabled = false
    ),

    SWIPE_GESTURES(
        defaultEnabled = true
    ),

    PURE_BLACK(
        defaultEnabled = false
    )
}

object AppPreferences {

    private val ENABLED_FEATURES_KEY =
        stringSetPreferencesKey("enabled_features")

    private val DEFAULT_ENABLED_FEATURES =
        Feature.entries
            .filter { it.defaultEnabled }
            .toSet()

    fun observeFeatures(
        context: Context
    ): Flow<Set<Feature>> =
        context.dataStore.data.map { preferences ->
            preferences[ENABLED_FEATURES_KEY]
                ?.mapNotNull { value ->
                    runCatching {
                        Feature.valueOf(value)
                    }.getOrNull()
                }
                ?.toSet()
                ?: DEFAULT_ENABLED_FEATURES
        }

    suspend fun setFeature(
        context: Context,
        feature: Feature,
        enabled: Boolean
    ) {
        context.dataStore.edit { preferences ->
            val features =
                preferences[ENABLED_FEATURES_KEY]
                    ?.mapNotNull { value ->
                        runCatching {
                            Feature.valueOf(value)
                        }.getOrNull()
                    }
                    ?.toMutableSet()
                    ?: DEFAULT_ENABLED_FEATURES.toMutableSet()

            if (enabled) {
                features += feature
            } else {
                features -= feature
            }

            preferences[ENABLED_FEATURES_KEY] =
                features.map { it.name }.toSet()
        }
    }

    private val LAST_UPDATE_CHECK =
        longPreferencesKey("last_update_check")

    private val DEFAULT_ENTRY_TYPE_KEY =
        stringPreferencesKey("default_entry_type")

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
    ): Long =
        context.dataStore.data
            .map { preferences ->
                preferences[LAST_UPDATE_CHECK] ?: 0L
            }
            .first()

    suspend fun setLastUpdateCheck(
        context: Context,
        timestamp: Long
    ) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UPDATE_CHECK] = timestamp
        }
    }

    fun observeDefaultEntryType(
        context: Context
    ): Flow<String> =
        context.dataStore.data.map { preferences ->
            when (preferences[DEFAULT_ENTRY_TYPE_KEY]) {
                ENTRY_TYPE_TASK ->
                    ENTRY_TYPE_TASK

                else ->
                    ENTRY_TYPE_NOTE
            }
        }

    suspend fun setDefaultEntryType(
        context: Context,
        type: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_ENTRY_TYPE_KEY] = type
        }
    }

    fun observeUiFont(
        context: Context
    ): Flow<UiFont> =
        context.dataStore.data.map { preferences ->
            preferences[UI_FONT_KEY]
                ?.let {
                    runCatching {
                        UiFont.valueOf(it)
                    }.getOrNull()
                }
                ?: UiFont.DEFAULT
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
    ): Flow<EditorFont> =
        context.dataStore.data.map { preferences ->
            preferences[EDITOR_FONT_KEY]
                ?.let {
                    runCatching {
                        EditorFont.valueOf(it)
                    }.getOrNull()
                }
                ?: EditorFont.UI_FONT
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
    ): Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[PREVIEW_LINES_KEY] ?: 4
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
package dev.jvqtil.flow.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "flow_preferences"
)

object AppPreferences {

    private val AMOLED_KEY = booleanPreferencesKey("amoled")

    fun observeAmoled(context: Context): Flow<Boolean> {
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
}
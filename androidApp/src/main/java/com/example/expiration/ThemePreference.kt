package com.example.expiration

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val THEME_DATASTORE_NAME = "theme_prefs"
private val Context.dataStore by preferencesDataStore(name = THEME_DATASTORE_NAME)

class ThemePreference(private val context: Context) {
    private val KEY_DARK = booleanPreferencesKey("is_dark")

    val isDarkFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[KEY_DARK] ?: false }

    suspend fun setDark(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK] = isDark
        }
    }
}


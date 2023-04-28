package com.example.ai_submission.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    private val TOKEN_KEY = stringPreferencesKey(token_key)

    fun getToken(): Flow<String> {
        return dataStore.data.map { p ->
            p[TOKEN_KEY] ?: ""
        }
    }

    suspend fun setToken(value: String) {
        dataStore.edit { p ->
            p[TOKEN_KEY] = value
        }
    }

    companion object {
        private var INSTANCE: AppPreferences? = null
        const val token_key = "token"

        fun getInstance(dataStore: DataStore<Preferences>): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AppPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
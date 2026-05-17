package com.multiply.kids.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val CHILD_NAME = stringPreferencesKey("child_name")
        private val DEVICE_ID = stringPreferencesKey("device_id")
    }

    val childName: Flow<String> = context.dataStore.data.map { it[CHILD_NAME] ?: "" }

    suspend fun setChildName(name: String) {
        context.dataStore.edit { it[CHILD_NAME] = name.trim() }
    }

    suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data.first()[DEVICE_ID]
        if (existing != null) return existing
        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { it[DEVICE_ID] = newId }
        return newId
    }
}

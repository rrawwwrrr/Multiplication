package ru.rrawww.multiply.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AnswerMode(val key: String) {
    INPUT("input"),
    CHOICE("choice");

    companion object {
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: INPUT
    }
}

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val CHILD_NAME = stringPreferencesKey("child_name")
        private val ANSWER_MODE = stringPreferencesKey("answer_mode")
    }

    val childName: Flow<String> = context.dataStore.data.map { it[CHILD_NAME] ?: "" }

    val answerMode: Flow<AnswerMode> = context.dataStore.data.map {
        AnswerMode.fromKey(it[ANSWER_MODE])
    }

    suspend fun setAnswerMode(mode: AnswerMode) {
        context.dataStore.edit { it[ANSWER_MODE] = mode.key }
    }

    suspend fun setChildName(name: String) {
        context.dataStore.edit { it[CHILD_NAME] = name.trim() }
    }

}

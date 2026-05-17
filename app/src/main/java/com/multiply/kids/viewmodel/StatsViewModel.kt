package com.multiply.kids.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multiply.kids.data.local.AppDatabase
import com.multiply.kids.data.local.SessionEntity
import com.multiply.kids.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    val prefs = UserPreferences(app)

    val sessions: StateFlow<List<SessionEntity>> = db.sessionDao()
        .getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun bestPercentForTable(table: Int, sessions: List<SessionEntity>): Float {
        val forTable = sessions.filter { it.tableNumber == table }
        if (forTable.isEmpty()) return 0f
        return forTable.maxOf { it.score.toFloat() / it.total }
    }

    fun starsForPercent(percent: Float): Int = when {
        percent >= 1f -> 3
        percent >= 0.7f -> 2
        percent >= 0.4f -> 1
        else -> 0
    }

    fun updateName(name: String) {
        viewModelScope.launch { prefs.setChildName(name) }
    }
}

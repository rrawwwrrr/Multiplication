package com.multiply.kids.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multiply.kids.data.local.AppDatabase
import com.multiply.kids.data.local.SessionEntity
import com.multiply.kids.data.preferences.UserPreferences
import com.multiply.kids.data.remote.ApiClient
import com.multiply.kids.data.remote.SessionRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AnswerState { NONE, CORRECT, WRONG }

data class GameState(
    val tableNumber: Int = 2,
    val questions: List<Int> = emptyList(),
    val currentIndex: Int = 0,
    val input: String = "",
    val score: Int = 0,
    val answerState: AnswerState = AnswerState.NONE,
    val isFinished: Boolean = false,
    val startTime: Long = 0L,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val prefs = UserPreferences(app)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun startGame(tableNumber: Int) {
        _state.value = GameState(
            tableNumber = tableNumber,
            questions = (2..9).shuffled(),
            startTime = System.currentTimeMillis(),
        )
    }

    fun appendDigit(digit: Int) {
        val s = _state.value
        if (s.answerState != AnswerState.NONE || s.isFinished) return
        if (s.input.length >= 3) return
        _state.value = s.copy(input = s.input + digit.toString())
    }

    fun deleteLastDigit() {
        val s = _state.value
        if (s.answerState != AnswerState.NONE || s.input.isEmpty()) return
        _state.value = s.copy(input = s.input.dropLast(1))
    }

    fun submitAnswer() {
        val s = _state.value
        if (s.answerState != AnswerState.NONE || s.input.isEmpty()) return

        val correct = s.tableNumber * s.questions[s.currentIndex]
        val isCorrect = s.input.toIntOrNull() == correct

        _state.value = s.copy(
            answerState = if (isCorrect) AnswerState.CORRECT else AnswerState.WRONG,
            score = if (isCorrect) s.score + 1 else s.score,
        )

        viewModelScope.launch {
            delay(900)
            val next = _state.value.currentIndex + 1
            if (next >= _state.value.questions.size) {
                finishGame()
            } else {
                _state.value = _state.value.copy(
                    currentIndex = next,
                    input = "",
                    answerState = AnswerState.NONE,
                )
            }
        }
    }

    private fun finishGame() {
        _state.value = _state.value.copy(isFinished = true)
        viewModelScope.launch {
            val s = _state.value
            val duration = (System.currentTimeMillis() - s.startTime) / 1000f
            val childName = prefs.childName.first()
            val deviceId = prefs.getOrCreateDeviceId()

            val entity = SessionEntity(
                deviceId = deviceId,
                childName = childName,
                tableNumber = s.tableNumber,
                score = s.score,
                total = s.questions.size,
                durationSec = duration,
            )
            val insertedId = db.sessionDao().insert(entity)

            try {
                ApiClient.api.postSession(
                    SessionRequest(
                        device_id = deviceId,
                        child_name = childName,
                        table_number = s.tableNumber,
                        score = s.score,
                        total = s.questions.size,
                        duration_sec = duration,
                    )
                )
                db.sessionDao().markSynced(insertedId.toInt())
            } catch (_: Exception) {
                // Данные сохранены локально, синхронизируем позже
            }
        }
    }

    fun starsForScore(score: Int, total: Int): Int = when {
        total == 0 -> 0
        score.toFloat() / total >= 1f -> 3
        score.toFloat() / total >= 0.7f -> 2
        score.toFloat() / total >= 0.4f -> 1
        else -> 0
    }
}

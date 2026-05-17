package ru.rrawww.multiply.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ru.rrawww.multiply.data.local.AppDatabase
import ru.rrawww.multiply.data.local.SessionEntity
import ru.rrawww.multiply.data.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AnswerState { NONE, CORRECT, WRONG }

data class GameState(
    val tableNumber: Int = 2,
    val tableNumbers: List<Int> = emptyList(), // для смешанного режима (tableNumber == 0)
    val questions: List<Int> = emptyList(),
    val currentIndex: Int = 0,
    val input: String = "",
    val score: Int = 0,
    val answerState: AnswerState = AnswerState.NONE,
    val isFinished: Boolean = false,
    val startTime: Long = 0L,
) {
    fun currentTable() = tableNumbers.getOrNull(currentIndex) ?: tableNumber
}

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val prefs = UserPreferences(app)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun startGame(tableNumber: Int) {
        if (tableNumber == 0) {
            val pairs = (2..9).flatMap { t -> (2..9).map { m -> t to m } }.shuffled().take(16)
            _state.value = GameState(
                tableNumber = 0,
                tableNumbers = pairs.map { it.first },
                questions = pairs.map { it.second },
                startTime = System.currentTimeMillis(),
            )
        } else {
            _state.value = GameState(
                tableNumber = tableNumber,
                questions = (2..9).shuffled(),
                startTime = System.currentTimeMillis(),
            )
        }
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

    fun submitChoice(chosen: Int) {
        val s = _state.value
        if (s.answerState != AnswerState.NONE) return
        val correct = s.currentTable() * s.questions[s.currentIndex]
        val isCorrect = chosen == correct
        _state.value = s.copy(
            answerState = if (isCorrect) AnswerState.CORRECT else AnswerState.WRONG,
            score = if (isCorrect) s.score + 1 else s.score,
        )
        viewModelScope.launch {
            delay(900)
            val next = _state.value.currentIndex + 1
            if (next >= _state.value.questions.size) finishGame()
            else _state.value = _state.value.copy(
                currentIndex = next,
                input = "",
                answerState = AnswerState.NONE,
            )
        }
    }

    fun submitAnswer() {
        val s = _state.value
        if (s.answerState != AnswerState.NONE || s.input.isEmpty()) return

        val correct = s.currentTable() * s.questions[s.currentIndex]
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

            db.sessionDao().insert(
                SessionEntity(
                    childName = childName,
                    tableNumber = s.tableNumber,
                    score = s.score,
                    total = s.questions.size,
                    durationSec = duration,
                )
            )
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

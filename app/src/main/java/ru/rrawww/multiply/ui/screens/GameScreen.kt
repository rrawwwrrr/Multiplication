package ru.rrawww.multiply.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.rrawww.multiply.data.preferences.AnswerMode
import ru.rrawww.multiply.ui.theme.Correct
import ru.rrawww.multiply.ui.theme.Wrong
import ru.rrawww.multiply.viewmodel.AnswerState
import ru.rrawww.multiply.viewmodel.GameViewModel

@Composable
fun GameScreen(
    tableNumber: Int,
    answerMode: AnswerMode = AnswerMode.INPUT,
    viewModel: GameViewModel,
    onFinished: (score: Int, total: Int, duration: Int) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(tableNumber) {
        viewModel.startGame(tableNumber)
    }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            val duration = ((System.currentTimeMillis() - state.startTime) / 1000).toInt()
            onFinished(state.score, state.questions.size, duration)
        }
    }

    if (state.questions.isEmpty()) return

    val bgColor = when (state.answerState) {
        AnswerState.CORRECT -> Correct.copy(alpha = 0.15f)
        AnswerState.WRONG -> Wrong.copy(alpha = 0.15f)
        AnswerState.NONE -> MaterialTheme.colorScheme.background
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Прогресс
        Spacer(Modifier.height(32.dp))
        LinearProgressIndicator(
            progress = { (state.currentIndex.toFloat() / state.questions.size) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${state.currentIndex + 1} / ${state.questions.size}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )

        Spacer(Modifier.weight(1f))

        // Вопрос
        val multiplier = if (state.questions.isNotEmpty()) state.questions[state.currentIndex] else 1
        val currentTable = state.currentTable()
        Text(
            text = "$currentTable × $multiplier = ?",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(24.dp))

        // Поле ответа
        val answerBg = when (state.answerState) {
            AnswerState.CORRECT -> Correct
            AnswerState.WRONG -> Wrong
            AnswerState.NONE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        }
        val answerText = when {
            state.answerState == AnswerState.CORRECT -> "Верно!"
            state.answerState == AnswerState.WRONG ->
                "${currentTable * multiplier}"
            state.input.isEmpty() -> "_"
            else -> state.input
        }
        val textColor = if (state.answerState != AnswerState.NONE) Color.White
        else MaterialTheme.colorScheme.onBackground

        Box(
            modifier = Modifier
                .size(width = 180.dp, height = 80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(answerBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = answerText,
                style = MaterialTheme.typography.displayMedium,
                color = textColor,
            )
        }

        Spacer(Modifier.weight(1f))

        if (answerMode == AnswerMode.CHOICE) {
            val correct = currentTable * multiplier
            val options = remember(correct) { generateOptions(correct) }
            ChoicePad(
                options = options,
                correctAnswer = correct,
                answerState = state.answerState,
                onChoice = { viewModel.submitChoice(it) },
            )
        } else {
            NumPad(
                onDigit = { viewModel.appendDigit(it) },
                onDelete = { viewModel.deleteLastDigit() },
                onSubmit = { viewModel.submitAnswer() },
                enabled = state.answerState == AnswerState.NONE,
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun NumPad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
) {
    val rows = listOf(
        listOf(7, 8, 9),
        listOf(4, 5, 6),
        listOf(1, 2, 3),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { digit ->
                    NumButton(label = digit.toString(), onClick = { onDigit(digit) }, enabled = enabled)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconNumButton(
                icon = { Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.onBackground) },
                onClick = onDelete,
                enabled = enabled,
                containerColor = MaterialTheme.colorScheme.surface,
            )
            NumButton(label = "0", onClick = { onDigit(0) }, enabled = enabled)
            IconNumButton(
                icon = { Icon(Icons.Default.Check, contentDescription = "Проверить", tint = Color.White) },
                onClick = onSubmit,
                enabled = enabled,
                containerColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun generateOptions(correct: Int): List<Int> {
    val candidates = mutableSetOf<Int>()
    var delta = 1
    while (candidates.size < 3) {
        if (correct - delta > 0) candidates.add(correct - delta)
        if (candidates.size < 3) candidates.add(correct + delta)
        delta++
    }
    return (candidates.take(3) + correct).shuffled()
}

@Composable
private fun ChoicePad(
    options: List<Int>,
    correctAnswer: Int,
    answerState: AnswerState,
    onChoice: (Int) -> Unit,
) {
    val rows = options.chunked(2)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { option ->
                    val bgColor = when {
                        answerState == AnswerState.NONE -> MaterialTheme.colorScheme.surface
                        option == correctAnswer -> Correct
                        else -> Wrong
                    }
                    val textColor = if (answerState != AnswerState.NONE) Color.White
                    else MaterialTheme.colorScheme.onSurface
                    Button(
                        onClick = { onChoice(option) },
                        enabled = answerState == AnswerState.NONE,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(width = 160.dp, height = 80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor = textColor,
                            disabledContainerColor = bgColor,
                            disabledContentColor = textColor,
                        ),
                        elevation = ButtonDefaults.buttonElevation(4.dp),
                    ) {
                        Text(text = option.toString(), fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NumButton(label: String, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = ButtonDefaults.buttonElevation(4.dp),
    ) {
        Text(text = label, fontSize = 28.sp)
    }
}

@Composable
private fun IconNumButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        elevation = ButtonDefaults.buttonElevation(4.dp),
    ) {
        icon()
    }
}

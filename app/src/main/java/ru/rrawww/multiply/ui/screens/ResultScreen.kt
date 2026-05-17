package ru.rrawww.multiply.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.rrawww.multiply.ui.theme.Correct
import ru.rrawww.multiply.ui.theme.Orange
import ru.rrawww.multiply.ui.theme.StarColor
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float, val startY: Float, val speed: Float,
    val color: Color, val size: Float, val sway: Float,
)

@Composable
fun ResultScreen(
    tableNumber: Int,
    score: Int,
    total: Int,
    durationSec: Int,
    onHome: () -> Unit,
    onPlayAgain: (Int) -> Unit,
) {
    val stars = when {
        total == 0 -> 0
        score.toFloat() / total >= 1f -> 3
        score.toFloat() / total >= 0.7f -> 2
        score.toFloat() / total >= 0.4f -> 1
        else -> 0
    }

    val title = when (stars) {
        3 -> "Отлично!"
        2 -> "Молодец!"
        1 -> "Неплохо!"
        else -> "Попробуй ещё!"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "fall",
    )

    val particles = remember {
        if (stars >= 2) List(60) {
            Particle(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.5f,
                speed = Random.nextFloat() * 0.4f + 0.2f,
                color = listOf(StarColor, Correct, Orange, Color(0xFF2196F3), Color(0xFFE91E63)).random(),
                size = Random.nextFloat() * 10f + 6f,
                sway = Random.nextFloat() * 0.04f - 0.02f,
            )
        } else emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (particles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    val y = ((p.startY + progress * p.speed) % 1.2f) * size.height
                    val x = (p.x + sin(progress * 6f + p.sway * 100) * p.sway) * size.width
                    drawCircle(color = p.color, radius = p.size, center = Offset(x, y))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Text(
                        text = if (i < stars) "★" else "☆",
                        fontSize = 56.sp,
                        color = if (i < stars) StarColor else Color(0xFFE0E0E0),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Таблица × $tableNumber",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "$score из $total правильно",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = "за ${durationSec} сек",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { onPlayAgain(tableNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Ещё раз!", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("На главную", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

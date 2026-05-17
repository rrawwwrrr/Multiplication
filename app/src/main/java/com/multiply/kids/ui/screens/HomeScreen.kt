package com.multiply.kids.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.multiply.kids.data.local.SessionEntity
import com.multiply.kids.ui.theme.StarColor
import com.multiply.kids.ui.theme.StarEmpty
import com.multiply.kids.ui.theme.TableColors
import com.multiply.kids.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTableClick: (Int) -> Unit,
    onStatsClick: () -> Unit,
    viewModel: StatsViewModel,
) {
    val childName by viewModel.prefs.childName.collectAsState(initial = "")
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (childName.isNotEmpty()) "Привет, $childName!" else "Таблица умножения",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "Статистика")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Выбери таблицу",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items((2..9).toList()) { table ->
                    val colorIndex = table - 2
                    val color = TableColors[colorIndex % TableColors.size]
                    val bestPct = viewModel.bestPercentForTable(table, sessions)
                    val stars = viewModel.starsForPercent(bestPct)
                    TableCard(
                        table = table,
                        color = color,
                        stars = stars,
                        onClick = { onTableClick(table) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCard(
    table: Int,
    color: Color,
    stars: Int,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "× $table",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            StarsRow(stars = stars, maxStars = 3, size = 22)
        }
    }
}

@Composable
fun StarsRow(stars: Int, maxStars: Int = 3, size: Int = 20) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(maxStars) { i ->
            Text(
                text = if (i < stars) "★" else "☆",
                fontSize = size.sp,
                color = if (i < stars) StarColor else StarEmpty,
            )
        }
    }
}

package ru.rrawww.multiply.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.rrawww.multiply.data.local.SessionEntity
import ru.rrawww.multiply.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel,
) {
    val sessions by viewModel.sessions.collectAsState()
    val childName by viewModel.prefs.childName.collectAsState(initial = "")
    var editName by remember { mutableStateOf(childName) }

    LaunchedEffect(childName) { editName = childName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                // Изменить имя
                OutlinedTextField(
                    value = editName,
                    onValueChange = { if (it.length <= 20) editName = it },
                    label = { Text("Имя ребёнка") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (editName.isNotBlank()) viewModel.updateName(editName)
                    }),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            if (editName.isNotBlank()) viewModel.updateName(editName)
                        }) { Text("Сохранить") }
                    }
                )
            }

            item {
                SummaryCard(sessions = sessions)
            }

            item {
                Text(
                    text = "Лучший результат по таблицам",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            item {
                BestPerTableGrid(sessions = sessions, viewModel = viewModel)
            }

            item {
                Text(
                    text = "История сессий",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = "Нет сыгранных игр",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            items(sessions) { session ->
                SessionCard(session = session)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(sessions: List<SessionEntity>) {
    val totalCorrect = sessions.sumOf { it.score }
    val totalQ = sessions.sumOf { it.total }
    val pct = if (totalQ > 0) totalCorrect * 100 / totalQ else 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatChip(label = "Игр", value = "${sessions.size}")
            StatChip(label = "Правильно", value = "$totalCorrect / $totalQ")
            StatChip(label = "Точность", value = "$pct%")
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun BestPerTableGrid(sessions: List<SessionEntity>, viewModel: StatsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (2..9).chunked(4).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { table ->
                    val pct = viewModel.bestPercentForTable(table, sessions)
                    val stars = viewModel.starsForPercent(pct)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("× $table", style = MaterialTheme.typography.labelLarge)
                            StarsRow(stars = stars, maxStars = 3, size = 14)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: SessionEntity) {
    val fmt = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }
    val date = fmt.format(Date(session.createdAt))
    val pct = if (session.total > 0) session.score * 100 / session.total else 0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "× ${session.tableNumber}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(60.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("${session.score} / ${session.total}", style = MaterialTheme.typography.bodyLarge)
                Text(date, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.headlineMedium,
                color = if (pct >= 70) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
            )
        }
    }
}


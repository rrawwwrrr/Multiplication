package ru.rrawww.multiply.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.rrawww.multiply.data.preferences.AnswerMode
import ru.rrawww.multiply.data.preferences.UserPreferences
import ru.rrawww.multiply.ui.screens.GameScreen
import ru.rrawww.multiply.ui.screens.HomeScreen
import ru.rrawww.multiply.ui.screens.NameScreen
import ru.rrawww.multiply.ui.screens.ResultScreen
import ru.rrawww.multiply.ui.screens.StatsScreen
import ru.rrawww.multiply.viewmodel.GameViewModel
import ru.rrawww.multiply.viewmodel.StatsViewModel

sealed class Screen(val route: String) {
    object Name : Screen("name")
    object Home : Screen("home")
    object Game : Screen("game/{tableNumber}") {
        fun go(tableNumber: Int) = "game/$tableNumber"
    }
    object Result : Screen("result/{tableNumber}/{score}/{total}/{duration}") {
        fun go(t: Int, score: Int, total: Int, duration: Int) =
            "result/$t/$score/$total/$duration"
    }
    object Stats : Screen("stats")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = UserPreferences(context)

    val childName: String? by remember { prefs.childName.map<String, String?> { it } }
        .collectAsState(initial = null)
    val answerMode by prefs.answerMode.collectAsState(initial = AnswerMode.INPUT)

    if (childName == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (childName!!.isEmpty()) Screen.Name.route else Screen.Home.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Name.route) {
            val statsVm: StatsViewModel = viewModel()
            NameScreen(
                onNameSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Name.route) { inclusive = true }
                    }
                },
                viewModel = statsVm,
            )
        }

        composable(Screen.Home.route) {
            val statsVm: StatsViewModel = viewModel()
            HomeScreen(
                onTableClick = { tableNumber ->
                    navController.navigate(Screen.Game.go(tableNumber))
                },
                onStatsClick = { navController.navigate(Screen.Stats.route) },
                viewModel = statsVm,
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("tableNumber") { type = NavType.IntType })
        ) { backStack ->
            val tableNumber = backStack.arguments!!.getInt("tableNumber")
            val gameVm: GameViewModel = viewModel()
            GameScreen(
                tableNumber = tableNumber,
                answerMode = answerMode,
                viewModel = gameVm,
                onFinished = { score, total, duration ->
                    navController.navigate(Screen.Result.go(tableNumber, score, total, duration)) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("tableNumber") { type = NavType.IntType },
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("duration") { type = NavType.IntType },
            )
        ) { backStack ->
            val args = backStack.arguments!!
            ResultScreen(
                tableNumber = args.getInt("tableNumber"),
                score = args.getInt("score"),
                total = args.getInt("total"),
                durationSec = args.getInt("duration"),
                onHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onPlayAgain = { tableNumber ->
                    navController.navigate(Screen.Game.go(tableNumber)) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }

        composable(Screen.Stats.route) {
            val statsVm: StatsViewModel = viewModel()
            StatsScreen(
                onBack = { navController.popBackStack() },
                viewModel = statsVm,
            )
        }
    }
}

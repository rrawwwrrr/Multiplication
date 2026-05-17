package ru.rrawww.multiply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.rrawww.multiply.navigation.AppNavGraph
import ru.rrawww.multiply.ui.theme.MultiplyKidsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiplyKidsTheme {
                AppNavGraph()
            }
        }
    }
}

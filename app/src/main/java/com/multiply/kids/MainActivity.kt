package com.multiply.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.multiply.kids.navigation.AppNavGraph
import com.multiply.kids.ui.theme.MultiplyKidsTheme

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

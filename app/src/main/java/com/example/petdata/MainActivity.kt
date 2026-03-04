package com.example.petdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.petdata.data.local.TokenManager
import com.example.petdata.navigation.NavGraph
import com.example.petdata.ui.theme.RescateAnimalTheme

class MainActivity : ComponentActivity() {
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(applicationContext)

        setContent {
            setContent {
                RescateAnimalTheme {
                    var rolId by remember { mutableStateOf(1) }
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        tokenManager = tokenManager,
                        rolId = rolId,
                        onRolIdUpdated = { newRolId -> rolId = newRolId }
                    )
                }
            }
        }
    }
}
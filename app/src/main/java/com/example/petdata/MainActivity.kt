package com.example.petdata

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.auth0.android.jwt.JWT
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.UserData
import com.example.petdata.navigation.NavGraph
import com.example.petdata.ui.theme.RescateAnimalTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var tokenManager: TokenManager

    companion object {
        // Canal para avisarle al NavGraph que Google auth completó
        val googleAuthResult = MutableStateFlow<Int?>(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(applicationContext)

        // Manejar si la app se abrió DESDE CERO con el deep link
        handleIntent(intent)

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

    // Se dispara cuando la app YA ESTABA ABIERTA y llega el deep link
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme != "petdata" || data.host != "auth") return

        val token = data.getQueryParameter("token") ?: return

        try {
            val jwt = JWT(token)
            val userData = UserData(
                user_id    = jwt.getClaim("user_id").asInt() ?: 0,
                nombre     = jwt.getClaim("nombre").asString() ?: "",
                apellido   = jwt.getClaim("apellido").asString() ?: "",
                correo     = jwt.getClaim("correo").asString() ?: "",
                rol_id     = jwt.getClaim("rol_id").asInt() ?: 1,
                avatar_url = jwt.getClaim("avatar_url").asString(),
                auth_provider = "google"
            )

            // CAMBIO: envolver en lifecycleScope.launch
            lifecycleScope.launch {
                tokenManager.saveSession(token, userData)
                googleAuthResult.value = userData.rol_id
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error con token Google: ${e.message}")
        }
    }
}
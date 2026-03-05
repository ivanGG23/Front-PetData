package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.ReporteResponse
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class RescuerState {
    object Loading : RescuerState()
    data class Success(val reportes: List<ReporteResponse>) : RescuerState()
    data class Error(val message: String) : RescuerState()
}

class RescuerViewModel(
    private val tokenManager: TokenManager,
    private val estadoId: Int  // 4 = completados, 3 = activos
) : ViewModel() {

    private val _state = MutableStateFlow<RescuerState>(RescuerState.Loading)
    val state: StateFlow<RescuerState> = _state

    init {
        loadReportes()
    }

    fun loadReportes() {
        viewModelScope.launch {
            _state.value = RescuerState.Loading
            try {
                val token = tokenManager.token.first() ?: throw Exception("Sin sesión")
                val userId = tokenManager.getUserId().first()?.toInt()
                    ?: throw Exception("Sin user_id")

                val reportes = RetrofitClient.apiService.getReports(
                    token = "Bearer $token",
                    estadoId = estadoId,
                    // Filtramos por rescatista_id pasándolo como query param
                )

                // Filtramos localmente por rescatista_id ya que el ApiService
                // no expone ese parámetro aún
                val filtrados = reportes.filter { it.rescatista_id == userId }
                _state.value = RescuerState.Success(filtrados)

            } catch (e: Exception) {
                android.util.Log.e("RescuerVM", "Error: ${e.message}", e)
                _state.value = RescuerState.Error("Error al cargar los reportes")
            }
        }
    }

    class Factory(
        private val tokenManager: TokenManager,
        private val estadoId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RescuerViewModel(tokenManager, estadoId) as T
        }
    }
}
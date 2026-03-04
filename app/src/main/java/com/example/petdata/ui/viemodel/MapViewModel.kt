package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.HeatmapPoint
import com.example.petdata.data.model.ReporteResponse
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MapState {
    object Loading : MapState()
    data class Success(
        val puntos: List<HeatmapPoint>,
        val reportes: List<ReporteResponse>
    ) : MapState()
    data class Error(val message: String) : MapState()
}

class MapViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _mapState = MutableStateFlow<MapState>(MapState.Loading)
    val mapState: StateFlow<MapState> = _mapState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _mapState.value = MapState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                val api = RetrofitClient.apiServiceWithToken(token)

                val puntos = api.getHeatmap("Bearer $token")
                val reportes = api.getReports("Bearer $token")

                _mapState.value = MapState.Success(puntos, reportes)
            } catch (e: Exception) {
                android.util.Log.e("MapViewModel", "Error: ${e.message}", e)
                _mapState.value = MapState.Error("Error al cargar el mapa")
            }
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(tokenManager) as T
        }
    }
}
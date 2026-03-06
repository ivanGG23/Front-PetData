package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.GlobalStats
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val stats: GlobalStats) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                val api = RetrofitClient.apiServiceWithToken(token)
                val stats = api.getGlobalStats("Bearer $token")
                _state.value = DashboardState.Success(stats)
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Error al cargar estadísticas")
            }
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(tokenManager) as T
        }
    }
}
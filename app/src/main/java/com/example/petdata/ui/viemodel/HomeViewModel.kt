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

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val reportes: List<ReporteResponse>) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    init {
        loadReportes()
    }

    fun loadReportes() {
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                val reportes = RetrofitClient.apiServiceWithToken(token).getReports("Bearer $token")
                _homeState.value = HomeState.Success(reportes)
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error: ${e.message}")
                _homeState.value = HomeState.Error("Error al cargar los reportes")
            }
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(tokenManager) as T
        }
    }
}
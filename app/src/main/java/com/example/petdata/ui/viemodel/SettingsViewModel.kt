package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.ReputacionResponse
import com.example.petdata.data.model.UserData
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val user: UserData,
        val reputacion: ReputacionResponse?
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

class SettingsViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _logoutDone = MutableStateFlow(false)
    val logoutDone: StateFlow<Boolean> = _logoutDone

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val token = tokenManager.token.first() ?: throw Exception("Sin sesión")
                val authHeader = "Bearer $token"

                val userIdStr = tokenManager.getUserId().first()
                    ?: throw Exception("Sin user_id")
                val userId = userIdStr.toInt()

                val user = RetrofitClient.apiService.getUserById(authHeader, userId)

                val reputacion = try {
                    RetrofitClient.apiService.getReputacion(authHeader, userId)
                } catch (e: Exception) {
                    null
                }

                _uiState.value = SettingsUiState.Success(user, reputacion)

            } catch (e: Exception) {
                android.util.Log.e("SettingsVM", "Error: ${e.message}", e) // ← esta línea
                _uiState.value = SettingsUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
            _logoutDone.value = true
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(tokenManager) as T
        }
    }
}
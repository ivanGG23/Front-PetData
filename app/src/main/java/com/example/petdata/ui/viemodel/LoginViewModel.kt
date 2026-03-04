package com.example.petdata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.LoginRequest
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val rolId: Int) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            android.util.Log.d("LoginViewModel", "Iniciando login con: $correo")
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(correo, contrasena)
                )
                android.util.Log.d("LoginViewModel", "Login exitoso: ${response.token}")
                tokenManager.saveSession(response.token, response.user)
                _loginState.value = LoginState.Success(response.user.rol_id)
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error en login: ${e.message}")
                _loginState.value = LoginState.Error("Correo o contraseña incorrectos")
            }
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(tokenManager) as T
        }
    }
}
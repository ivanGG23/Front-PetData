package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.RegisterRequest
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(
        nombre: String,
        apellido: String,
        correo: String,
        contrasena: String,
        telefono: String?,
        fechaNacimiento: String?,
        isRescuer: Boolean
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(
                        nombre = nombre,
                        apellido = apellido,
                        correo = correo,
                        contrasena = contrasena,
                        rol_id = if (isRescuer) 2 else 1,
                        estado_cuenta = if (isRescuer) "pendiente" else "activo",
                        telefono = telefono?.ifEmpty { null },
                        fecha_nacimiento = fechaNacimiento?.ifEmpty { null }
                    )
                )
                if (!isRescuer) {
                    tokenManager.saveSession(response.token, response.user)
                }
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                android.util.Log.e("RegisterViewModel", "Error: ${e.message}")
                _registerState.value = RegisterState.Error("Error al crear la cuenta, intenta de nuevo")
            }
        }
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(tokenManager) as T
        }
    }
}
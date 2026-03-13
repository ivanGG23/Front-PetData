package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ── Estado de la pantalla ─────────────────────────────────────────────────

sealed class PersonalInfoUiState {
    object Loading : PersonalInfoUiState()

    data class Success(
        // Campos editables
        val nombre: String = "",
        val apellido: String = "",
        val telefono: String = "",
        val fechaNacimiento: String = "",
        val nuevaContrasena: String = "",
        // Solo lectura
        val correo: String = "",
        val isGoogleUser: Boolean = false,
        val isRescatista: Boolean = false,
        // Control de guardado
        val isSaving: Boolean = false
    ) : PersonalInfoUiState()

    data class Error(val message: String) : PersonalInfoUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────

class PersonalInfoViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonalInfoUiState>(PersonalInfoUiState.Loading)
    val uiState: StateFlow<PersonalInfoUiState> = _uiState

    // Mensaje resultado de guardar (snackbar)
    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PersonalInfoUiState.Loading
            try {
                val token = tokenManager.token.first() ?: throw Exception("Sin sesión")
                val authHeader = "Bearer $token"

                val userIdStr = tokenManager.getUserId().first()
                    ?: throw Exception("Sin user_id")
                val userId = userIdStr.toInt()

                val user = RetrofitClient.apiService.getUserById(authHeader, userId)

                _uiState.value = PersonalInfoUiState.Success(
                    nombre          = user.nombre,
                    apellido        = user.apellido,
                    telefono        = user.telefono ?: "",
                    fechaNacimiento = user.fecha_nacimiento ?: "",
                    correo          = user.correo,
                    isGoogleUser    = user.auth_provider == "google",
                    isRescatista    = user.rol_id == 2
                )
            } catch (e: Exception) {
                android.util.Log.e("PersonalInfoVM", "Error cargando: ${e.message}", e)
                _uiState.value = PersonalInfoUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }

    // ── Mutadores de campos ───────────────────────────────────────────────

    fun onNombreChange(value: String) = updateSuccess { copy(nombre = value) }
    fun onApellidoChange(value: String) = updateSuccess { copy(apellido = value) }
    fun onTelefonoChange(value: String) = updateSuccess { copy(telefono = value) }
    fun onFechaNacimientoChange(value: String) = updateSuccess { copy(fechaNacimiento = value) }
    fun onNuevaContrasenaChange(value: String) = updateSuccess { copy(nuevaContrasena = value) }

    // ── Guardar cambios ───────────────────────────────────────────────────

    fun guardarCambios() {
        val current = _uiState.value as? PersonalInfoUiState.Success ?: return

        viewModelScope.launch {
            updateSuccess { copy(isSaving = true) }
            try {
                val token = tokenManager.token.first() ?: throw Exception("Sin sesión")
                val authHeader = "Bearer $token"

                val userIdStr = tokenManager.getUserId().first()
                    ?: throw Exception("Sin user_id")
                val userId = userIdStr.toInt()

                val body = mutableMapOf<String, String>(
                    "nombre"   to current.nombre.trim(),
                    "apellido" to current.apellido.trim()
                )
                if (current.telefono.isNotBlank())
                    body["telefono"] = current.telefono.trim()
                if (current.fechaNacimiento.isNotBlank())
                    body["fecha_nacimiento"] = current.fechaNacimiento.trim()
                if (!current.isGoogleUser && current.nuevaContrasena.isNotBlank())
                    body["contrasena"] = current.nuevaContrasena

                RetrofitClient.apiService.updateUser(authHeader, userId, body)

                updateSuccess { copy(isSaving = false, nuevaContrasena = "") }
                _saveResult.value = "✓ Cambios guardados correctamente"

            } catch (e: Exception) {
                android.util.Log.e("PersonalInfoVM", "Error guardando: ${e.message}", e)
                updateSuccess { copy(isSaving = false) }
                _saveResult.value = "Error al guardar: ${e.message}"
            }
        }
    }

    // ── Solicitar rol rescatista ──────────────────────────────────────────

    fun solicitarRescatista() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: return@launch
                val authHeader = "Bearer $token"

                val userIdStr = tokenManager.getUserId().first() ?: return@launch
                val userId = userIdStr.toInt()

                RetrofitClient.apiService.solicitarRescatista(authHeader, userId)
                _saveResult.value = "✓ Solicitud enviada. Un administrador la revisará pronto."
            } catch (e: Exception) {
                android.util.Log.e("PersonalInfoVM", "Error solicitud rescatista: ${e.message}", e)
                _saveResult.value = "Error al enviar solicitud: ${e.message}"
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private fun updateSuccess(block: PersonalInfoUiState.Success.() -> PersonalInfoUiState.Success) {
        val current = _uiState.value as? PersonalInfoUiState.Success ?: return
        _uiState.value = current.block()
    }

    // ── Factory ───────────────────────────────────────────────────────────

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PersonalInfoViewModel(tokenManager) as T
        }
    }
}
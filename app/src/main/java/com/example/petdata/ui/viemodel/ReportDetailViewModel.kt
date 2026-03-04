package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.*
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ReportDetailState(
    val reporte: ReporteResponse? = null,
    val historial: List<HistorialEstado> = emptyList(),
    val evidencias: List<Evidencia> = emptyList(),
    val comentarios: List<ComentarioResponse> = emptyList(),
    val userStats: UserStats? = null,
    val reputacion: ReputacionResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ComentarioState {
    object Idle : ComentarioState()
    object Loading : ComentarioState()
    object Success : ComentarioState()
    data class Error(val message: String) : ComentarioState()
}

sealed class AccionState {
    object Idle : AccionState()
    object Loading : AccionState()
    object Success : AccionState()
    data class Error(val message: String) : AccionState()
}

class ReportDetailViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _state = MutableStateFlow(ReportDetailState())
    val state: StateFlow<ReportDetailState> = _state

    private val _comentarioState = MutableStateFlow<ComentarioState>(ComentarioState.Idle)
    val comentarioState: StateFlow<ComentarioState> = _comentarioState

    private val _accionState = MutableStateFlow<AccionState>(AccionState.Idle)
    val accionState: StateFlow<AccionState> = _accionState

    fun loadReporte(reporteId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.token.first() ?: ""
                val api = RetrofitClient.apiServiceWithToken(token)

                val reporte = api.getReportById("Bearer $token", reporteId)
                val historial = api.getHistorial("Bearer $token", reporteId)
                val evidencias = api.getEvidencias("Bearer $token", reporteId)
                val comentarios = api.getComentarios("Bearer $token", reporteId)

                // Llamadas paralelas para stats y reputación del creador
                val (userStats, reputacion) = try {
                    val stats = async { api.getUserStats("Bearer $token", reporte.usuario_creador_id) }
                    val rep = async { api.getReputacion("Bearer $token", reporte.usuario_creador_id) }
                    Pair(stats.await(), rep.await())
                } catch (e: Exception) {
                    Pair(null, null)
                }

                _state.value = ReportDetailState(
                    reporte = reporte,
                    historial = historial,
                    evidencias = evidencias,
                    comentarios = comentarios,
                    userStats = userStats,
                    reputacion = reputacion,
                    isLoading = false
                )
            } catch (e: Exception) {
                android.util.Log.e("ReportDetailVM", "Error loadReporte: ${e.message}", e)
                _state.value = _state.value.copy(isLoading = false, error = "Error al cargar el reporte")
            }
        }
    }

    fun startPollingComentarios(reporteId: Int) {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L)
                try {
                    val token = tokenManager.token.first() ?: ""
                    val comentarios = RetrofitClient.apiServiceWithToken(token)
                        .getComentarios("Bearer $token", reporteId)
                    _state.value = _state.value.copy(comentarios = comentarios)
                } catch (e: Exception) {
                    android.util.Log.e("ReportDetailVM", "Error polling: ${e.message}", e)
                }
            }
        }
    }

    fun enviarComentario(reporteId: Int, comentario: String) {
        viewModelScope.launch {
            _comentarioState.value = ComentarioState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                val api = RetrofitClient.apiServiceWithToken(token)
                api.createComentario(
                    "Bearer $token",
                    CreateComentarioRequest(reporte_id = reporteId, comentario = comentario)
                )
                val comentarios = api.getComentarios("Bearer $token", reporteId)
                _state.value = _state.value.copy(comentarios = comentarios)
                _comentarioState.value = ComentarioState.Success
            } catch (e: Exception) {
                android.util.Log.e("ReportDetailVM", "Error enviarComentario: ${e.message}", e)
                _comentarioState.value = ComentarioState.Error("Error al enviar el comentario")
            }
        }
    }

    fun asignarReporte(reporteId: Int) {
        viewModelScope.launch {
            _accionState.value = AccionState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                RetrofitClient.apiServiceWithToken(token)
                    .asignarReporte("Bearer $token", reporteId, emptyMap())
                loadReporte(reporteId)
                _accionState.value = AccionState.Success
            } catch (e: retrofit2.HttpException) {
                // Extraer el mensaje real que manda el backend
                val errorBody = e.response()?.errorBody()?.string()
                val mensaje = try {
                    org.json.JSONObject(errorBody ?: "").getString("error")
                } catch (ex: Exception) {
                    "No se pudo asignar el reporte"
                }
                android.util.Log.e("ReportDetailVM", "Error asignar: ${e.message}", e)
                _accionState.value = AccionState.Error(mensaje)
            } catch (e: Exception) {
                android.util.Log.e("ReportDetailVM", "Error asignar: ${e.message}", e)
                _accionState.value = AccionState.Error("No se pudo asignar el reporte")
            }
        }
    }

    fun desasignarReporte(reporteId: Int) {
        viewModelScope.launch {
            _accionState.value = AccionState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                RetrofitClient.apiServiceWithToken(token)
                    .desasignarReporte("Bearer $token", reporteId)
                loadReporte(reporteId)
                _accionState.value = AccionState.Success
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val mensaje = try {
                    org.json.JSONObject(errorBody ?: "").getString("error")
                } catch (ex: Exception) {
                    "No se pudo desasignar el reporte"
                }
                android.util.Log.e("ReportDetailVM", "Error desasignar: ${e.message}", e)
                _accionState.value = AccionState.Error(mensaje)
            } catch (e: Exception) {
                android.util.Log.e("ReportDetailVM", "Error desasignar: ${e.message}", e)
                _accionState.value = AccionState.Error("No se pudo desasignar el reporte")
            }
        }
    }

    fun cambiarEstado(
        reporteId: Int,
        nuevoEstadoId: Int,
        comentario: String? = null,
        urlImgs: List<String>? = null
    ) {
        viewModelScope.launch {
            _accionState.value = AccionState.Loading
            try {
                val token = tokenManager.token.first() ?: ""
                RetrofitClient.apiServiceWithToken(token).cambiarEstado(
                    "Bearer $token",
                    reporteId,
                    CambiarEstadoRequest(
                        nuevo_estado_id = nuevoEstadoId,
                        comentario = comentario,
                        url_imgs = urlImgs
                    )
                )
                loadReporte(reporteId)
                _accionState.value = AccionState.Success
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val mensaje = try {
                    org.json.JSONObject(errorBody ?: "").getString("error")
                } catch (ex: Exception) {
                    "No se pudo cambiar el estado"
                }
                android.util.Log.e("ReportDetailVM", "Error cambiarEstado: ${e.message}", e)
                _accionState.value = AccionState.Error(mensaje)
            } catch (e: Exception) {
                android.util.Log.e("ReportDetailVM", "Error cambiarEstado: ${e.message}", e)
                _accionState.value = AccionState.Error("No se pudo cambiar el estado")
            }
        }
    }

    fun resetAccionState() {
        _accionState.value = AccionState.Idle
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReportDetailViewModel(tokenManager) as T
        }
    }
}
package com.example.petdata.ui.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.AppEvents
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.GlobalStats
import com.example.petdata.data.model.ReporteResponse
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val reportes: List<ReporteResponse>) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState

    // Filtros individuales
    private val _filtroTipo     = MutableStateFlow<Int?>(null)
    private val _filtroEstado   = MutableStateFlow<Int?>(null)
    private val _filtroPrioridad = MutableStateFlow<Int?>(null)
    private val _filtroFecha    = MutableStateFlow<String?>(null)
    private val _globalStats = MutableStateFlow<GlobalStats?>(null)
    val globalStats: StateFlow<GlobalStats?> = _globalStats
    val filtroTipo:      StateFlow<Int?>    = _filtroTipo
    val filtroEstado:    StateFlow<Int?>    = _filtroEstado
    val filtroPrioridad: StateFlow<Int?>    = _filtroPrioridad
    val filtroFecha:     StateFlow<String?> = _filtroFecha

    init {
        loadReportes()
        loadGlobalStats()
        // Recargar solo cuando otra pantalla notifique un cambio
        viewModelScope.launch {
            AppEvents.reporteModificado.collect {
                loadReportes()
                loadGlobalStats()
            }
        }
    }

    fun loadGlobalStats() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first() ?: ""
                val stats = RetrofitClient
                    .apiServiceWithToken(token)
                    .getGlobalStats("Bearer $token")
                _globalStats.value = stats
                android.util.Log.d("HomeViewModel", "Stats: activos=${stats.activos} rescatados=${stats.rescatados} pendientes=${stats.pendientes}")
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Stats error: ${e.message}", e)
            }
        }
    }

    fun setFiltroTipo(id: Int?)      { _filtroTipo.value = id;      loadReportes() }
    fun setFiltroEstado(id: Int?)    { _filtroEstado.value = id;    loadReportes() }
    fun setFiltroPrioridad(id: Int?) { _filtroPrioridad.value = id; loadReportes() }
    fun setFiltroFecha(valor: String?) { _filtroFecha.value = valor; loadReportes() }

    fun limpiarFiltros() {
        _filtroTipo.value      = null
        _filtroEstado.value    = null
        _filtroPrioridad.value = null
        _filtroFecha.value     = null
        loadReportes()
    }

    fun loadReportes() {
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            try {
                val token = tokenManager.token.first() ?: ""

                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val hoy = LocalDate.now()
                val (fechaInicio, fechaFin) = when (_filtroFecha.value) {
                    "hoy"    -> "${hoy}T00:00:00" to "${hoy}T23:59:59"
                    "semana" -> "${hoy.minusDays(7)}T00:00:00" to "${hoy}T23:59:59"
                    "mes"    -> "${hoy.minusDays(30)}T00:00:00" to "${hoy}T23:59:59"
                    else     -> null to null
                }

                val reportes = RetrofitClient
                    .apiServiceWithToken(token)
                    .getReports(
                        token        = "Bearer $token",
                        tipoAnimalId = _filtroTipo.value,
                        estadoId     = _filtroEstado.value,
                        prioridadId  = _filtroPrioridad.value,
                        fechaInicio  = fechaInicio,
                        fechaFin     = fechaFin
                    )

                // Ocultar resueltos (estado 4) y falsos (estado 5)
                // a menos que el usuario haya filtrado explícitamente por ese estado
                val filtrados = if (_filtroEstado.value == null) {
                    reportes.filter { it.estado_reporte_actual != 4 && it.estado_reporte_actual != 5 }
                } else {
                    reportes
                }

                _homeState.value = HomeState.Success(filtrados)
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
package com.example.petdata.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppEvents {
    private val _reporteModificado = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val reporteModificado = _reporteModificado.asSharedFlow()

    fun notificarReporteModificado() {
        _reporteModificado.tryEmit(Unit)
    }
}
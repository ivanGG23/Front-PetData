package com.example.petdata.ui.viemodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.TipoAnimal
import com.example.petdata.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class ReportFormState {
    object Idle : ReportFormState()
    object Loading : ReportFormState()
    data class Success(val reporteId: Int) : ReportFormState()
    data class Error(val message: String) : ReportFormState()
}

class ReportFormViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _formState = MutableStateFlow<ReportFormState>(ReportFormState.Idle)
    val formState: StateFlow<ReportFormState> = _formState

    fun crearReporte(
        context: Context,
        estadoAnimalId: Int,
        tipoAnimalId: Int,
        prioridadId: Int,
        descripcion: String,
        latitud: Double,
        longitud: Double,
        precisionMetros: Double?,
        contactoOpcional: String?,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            _formState.value = ReportFormState.Loading
            try {
                val token = tokenManager.token.first() ?: ""

                // Convertir Uri a File temporal
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("No se pudo leer la imagen")
                val tempFile = File(context.cacheDir, "imagen_reporte.jpg")
                FileOutputStream(tempFile).use { output ->
                    inputStream.copyTo(output)
                }

                // Construir multipart
                val imagenPart = MultipartBody.Part.createFormData(
                    "imagenes",
                    tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                val toText = { s: String -> s.toRequestBody("text/plain".toMediaTypeOrNull()) }

                val api = RetrofitClient.apiServiceWithToken(token)
                val result = api.createReport(
                    token       = "Bearer $token",
                    estadoAnimalId = toText(estadoAnimalId.toString()),
                    tipoAnimalId   = toText(tipoAnimalId.toString()),
                    prioridadId    = toText(prioridadId.toString()),
                    descripcion    = toText(descripcion),
                    latitud        = toText(latitud.toString()),
                    longitud       = toText(longitud.toString()),
                    precisionMetros = precisionMetros?.let { toText(it.toString()) },
                    contactoOpcional = contactoOpcional?.let { toText(it) },
                    imagen         = imagenPart
                )

                tempFile.delete()
                _formState.value = ReportFormState.Success(result.reporte_id)

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val mensaje = try {
                    org.json.JSONObject(errorBody ?: "").getString("error")
                } catch (ex: Exception) {
                    "Error al crear el reporte"
                }
                _formState.value = ReportFormState.Error(mensaje)
            } catch (e: Exception) {
                android.util.Log.e("ReportFormVM", "Error: ${e.message}", e)
                _formState.value = ReportFormState.Error("Error al crear el reporte")
            }
        }
    }

    fun resetState() {
        _formState.value = ReportFormState.Idle
    }

    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReportFormViewModel(tokenManager) as T
        }
    }
}
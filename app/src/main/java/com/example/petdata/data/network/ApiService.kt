package com.example.petdata.data.network

import com.example.petdata.data.model.AddEvidenciaResponse
import com.example.petdata.data.model.CambiarEstadoRequest
import com.example.petdata.data.model.ComentarioResponse
import com.example.petdata.data.model.CreateComentarioRequest
import com.example.petdata.data.model.CreateReporteResponse
import com.example.petdata.data.model.Evidencia
import com.example.petdata.data.model.GlobalStats
import com.example.petdata.data.model.HeatmapPoint
import com.example.petdata.data.model.HistorialEstado
import com.example.petdata.data.model.LoginRequest
import com.example.petdata.data.model.LoginResponse
import com.example.petdata.data.model.RegisterRequest
import com.example.petdata.data.model.RegisterResponse
import com.example.petdata.data.model.ReporteResponse
import com.example.petdata.data.model.ReputacionResponse
import com.example.petdata.data.model.UserData
import com.example.petdata.data.model.UserStats
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("reports")
    suspend fun getReports(
        @Header("Authorization") token: String,
        @Query("tipo_animal_id") tipoAnimalId:  Int?    = null,
        @Query("estado_id")      estadoId:      Int?    = null,
        @Query("prioridad_id")   prioridadId:   Int?    = null,
        @Query("fecha_inicio")   fechaInicio:   String? = null,
        @Query("fecha_fin")      fechaFin:      String? = null,
        @Query("rescatista_id")  rescatistaId: Int? = null
    ): List<ReporteResponse>

    @GET("reports/{id}")
    suspend fun getReportById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): ReporteResponse

    @GET("tracking/historial/{reporte_id}")
    suspend fun getHistorial(
        @Header("Authorization") token: String,
        @Path("reporte_id") reporteId: Int
    ): List<HistorialEstado>

    @GET("tracking/evidencia/{reporte_id}")
    suspend fun getEvidencias(
        @Header("Authorization") token: String,
        @Path("reporte_id") reporteId: Int
    ): List<Evidencia>

    @GET("comments/{reporte_id}")
    suspend fun getComentarios(
        @Header("Authorization") token: String,
        @Path("reporte_id") reporteId: Int
    ): List<ComentarioResponse>

    @POST("comments")
    suspend fun createComentario(
        @Header("Authorization") token: String,
        @Body request: CreateComentarioRequest
    ): ComentarioResponse

    @POST("reports/{id}/asignar")
    suspend fun asignarReporte(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String> = emptyMap<String, String>()
    )

    @DELETE("reports/{id}/asignar")
    suspend fun desasignarReporte(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    @PUT("reports/{id}/estado")
    suspend fun cambiarEstado(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CambiarEstadoRequest
    )

    @GET("reports/users/{usuario_id}/stats")
    suspend fun getUserStats(
        @Header("Authorization") token: String,
        @Path("usuario_id") usuarioId: Int
    ): UserStats

    @GET("reputation/{usuario_id}")
    suspend fun getReputacion(
        @Header("Authorization") token: String,
        @Path("usuario_id") usuarioId: Int
    ): ReputacionResponse

    @Multipart
    @POST("reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Part("estado_animal_id") estadoAnimalId: RequestBody,
        @Part("tipo_animal_id") tipoAnimalId: RequestBody,
        @Part("prioridad_id") prioridadId: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part("precision_metros") precisionMetros: RequestBody?,
        @Part("contacto_opcional") contactoOpcional: RequestBody?,
        @Part imagen: MultipartBody.Part
    ): CreateReporteResponse

    @GET("reports/heatmap")
    suspend fun getHeatmap(
        @Header("Authorization") token: String,
        @Query("estado_reporte_id") estadoReporteId: Int? = null
    ): List<HeatmapPoint>

    @GET("reports/stats/global")
    suspend fun getGlobalStats(
        @Header("Authorization") token: String
    ): GlobalStats

    @GET("auth/users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): UserData

    @PUT("auth/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): UserData

    @POST("auth/users/{id}/solicitar-rescatista")
    suspend fun solicitarRescatista(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    @POST("auth/users/{id}/dejar-rescatista")
    suspend fun dejarRescatista(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    @Multipart
    @POST("reports/evidencia")
    suspend fun addEvidencia(
        @Header("Authorization") token: String,
        @Part("reporte_id") reporte_id: okhttp3.RequestBody,
        @Part("tipo") tipo: okhttp3.RequestBody,
        @Part imagen: okhttp3.MultipartBody.Part
    ): AddEvidenciaResponse
}
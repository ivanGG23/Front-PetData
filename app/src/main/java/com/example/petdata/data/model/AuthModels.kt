package com.example.petdata.data.model

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class LoginResponse(
    val token: String,
    val user: UserData
)

data class UserData(
    val user_id: Int,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val rol_id: Int,
    val avatar_url: String?,
    val telefono: String? = null,
    val fecha_nacimiento: String? = null,
    val auth_provider: String? = null
)

data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val contrasena: String,
    val rol_id: Int,
    val estado_cuenta: String,
    val telefono: String? = null,
    val fecha_nacimiento: String? = null
)

data class RegisterResponse(
    val token: String,
    val user: UserData
)

data class EstadoAnimal(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

data class EstadoReporte(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

data class Prioridad(
    val id: Int,
    val nombre: String,
    val nivel: Int
)

data class ReporteResponse(
    val id: Int,
    val usuario_creador_id: Int,
    val rescatista_id: Int?,
    val estado_animal_id: Int,
    val estado_reporte_actual: Int,
    val prioridad_id: Int,
    val tipo_animal_id: Int,
    val descripcion: String,
    val fecha_creacion: String,
    val fecha_asig: String?,
    val fecha_cierre: String?,
    val contacto_opcional: String?,
    val estado_animal: EstadoAnimal,
    val estado_reporte: EstadoReporte,
    val prioridad: Prioridad,
    val tipoAnimal: TipoAnimal,
    val creador: UsuarioResumen?,
    val rescatista: UsuarioResumen?,
    val imagen_url: String?
)

data class HistorialEstado(
    val _id: String,
    val reporte_id: Int,
    val estado_reporte_id: Int,
    val usuario_id: Int,
    val comentario: String?,
    val fecha_cambio: String
)

data class Evidencia(
    val _id: String,
    val reporte_id: Int,
    val subido_por: Int,
    val url_img: String,
    val tipo: String,
    val fecha_subido: String
)

data class ComentarioResponse(
    val _id: String,
    val reporte_id: Int,
    val usuario_id: Int,
    val nombre_usuario: String?,
    val comentario: String,
    val fecha: String
)

data class CreateComentarioRequest(
    val reporte_id: Int,
    val comentario: String
)

// Agrega estos dos data class nuevos
data class UsuarioResumen(
    val id: Int,
    val nombre: String
)

data class CambiarEstadoRequest(
    val nuevo_estado_id: Int,
    val comentario: String? = null,
    val url_imgs: List<String>? = null
)

data class UserStats(
    val usuario_id: Int,
    val total_reportes: Int,
    val reportes_falsos: Int
)

data class ReputacionResponse(
    val usuario_id: Int,
    val total_puntos: Int
)

data class CreateReporteResponse(
    val message: String,
    val reporte_id: Int
)

data class HeatmapPoint(
    val reporte_id: Int,
    val latitud: Double,
    val longitud: Double,
    val prioridad_id: Int,
    val estado_reporte_actual: Int
)

data class TipoAnimal(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

data class GlobalStats(
    val especies: List<EspecieStat>,
    val historial: List<HistorialStat>,
    val zonas: List<ZonaStat>,
    val activos: Int,
    val rescatados: Int,
    val pendientes: Int
)

data class EspecieStat(
    val tipo_animal_id: Int,
    val nombre: String,
    val total: Int
)

data class HistorialStat(
    val periodo: String,
    val nuevos: Int,
    val resueltos: Int
)

data class ZonaStat(
    val zona: String,
    val total: Int,
    val resueltos: Int
)
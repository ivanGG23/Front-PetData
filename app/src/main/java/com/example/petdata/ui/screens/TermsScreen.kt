package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petdata.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Términos y Condiciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {

            // Encabezado
            Text(
                text = "PetData",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
            Text(
                text = "Plataforma de reportes de animales en situación de abandono o maltrato",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Versión 1.0 · México · 2025",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(20.dp))

            // Sección 1
            TermsSection(number = "1", title = "Responsable de la aplicación") {
                TermsBody("PetData es un proyecto de desarrollo académico universitario. Para cualquier duda o solicitud relacionada con esta aplicación, puedes contactarnos en:")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "243836@ids.upchiapas.edu.mx",
                    fontSize = 14.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Sección 2
            TermsSection(number = "2", title = "Descripción del servicio") {
                TermsBody("PetData es una plataforma móvil que permite a los ciudadanos reportar animales en situación de abandono o maltrato en México. Los reportes son atendidos por rescatistas registrados y supervisados por administradores del sistema.")
                Spacer(modifier = Modifier.height(8.dp))
                TermsBody("Existen tres tipos de usuario:")
                Spacer(modifier = Modifier.height(4.dp))
                TermsBullet("Ciudadano: crea reportes, sigue el estado de sus casos y acumula reputación.")
                TermsBullet("Rescatista: revisa y atiende reportes, puede marcarlos como falsos con justificación.")
                TermsBullet("Administrador: modera la plataforma, gestiona usuarios y consulta estadísticas.")
            }

            // Sección 3
            TermsSection(number = "3", title = "Datos personales que recopilamos") {
                TermsTag(text = "Obligatorios para el registro", color = GreenPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                TermsBullet("Nombre y apellido")
                TermsBullet("Correo electrónico")
                TermsBullet("Contraseña (almacenada en forma cifrada)")
                Spacer(modifier = Modifier.height(10.dp))
                TermsTag(text = "Generados al usar la app", color = Color(0xFF856404))
                Spacer(modifier = Modifier.height(4.dp))
                TermsBullet("Ubicación GPS al crear un reporte (latitud, longitud y precisión en metros)")
                TermsBullet("Imágenes subidas como evidencia de un reporte")
                TermsBullet("Comentarios escritos en reportes")
                TermsBullet("Historial de acciones y cambios de estado")
                Spacer(modifier = Modifier.height(10.dp))
                TermsTag(text = "Opcionales", color = Color(0xFF0F6E56))
                Spacer(modifier = Modifier.height(4.dp))
                TermsBullet("Teléfono de contacto")
                TermsBullet("Fecha de nacimiento")
                TermsBullet("Foto de perfil")
                TermsBullet("Contacto adicional dentro del reporte")
            }

            // Sección 4 - GPS (con aviso destacado)
            TermsSection(number = "4", title = "Uso de la ubicación GPS") {
                // Aviso destacado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3CD), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠ La ubicación GPS que proporcionas al crear un reporte es visible para todos los usuarios de la plataforma a través del mapa interactivo.",
                        fontSize = 13.sp,
                        color = Color(0xFF856404),
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                TermsBody("Tus coordenadas se usan para:")
                Spacer(modifier = Modifier.height(4.dp))
                TermsBullet("Registrar el lugar exacto donde se encontró al animal reportado.")
                TermsBullet("Mostrar el reporte en el mapa para que rescatistas puedan localizarlo.")
                TermsBullet("Generar un mapa de calor agregado que muestra zonas con mayor concentración de reportes, visible para rescatistas y administradores.")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Recomendamos no crear reportes desde tu domicilio. Usa la ubicación del lugar donde se encuentra el animal.",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32),
                        lineHeight = 20.sp
                    )
                }
            }

            // Sección 5
            TermsSection(number = "5", title = "Obligaciones del usuario") {
                TermsBullet("Los reportes deben ser verídicos. El usuario declara que la situación descrita y las imágenes corresponden a un hecho real.")
                TermsBullet("No se permite crear reportes falsos, duplicados o con información fabricada.")
                TermsBullet("No se permite subir imágenes de contenido violento, inapropiado o ajeno al reporte.")
                TermsBullet("No se permite usar los comentarios para acosar, insultar o atacar a otros usuarios.")
                TermsBullet("Los ciudadanos tienen un límite de 3 reportes por día.")
            }

            // Sección 6
            TermsSection(number = "6", title = "Sistema de reputación y sanciones") {
                TermsBody("PetData cuenta con un sistema de puntos que premia los reportes verídicos y penaliza los falsos. Si la puntuación de un usuario cae por debajo de −10 puntos, su cuenta puede ser suspendida automáticamente.")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Los administradores pueden suspender o banear cuentas por uso indebido de la plataforma, sin previo aviso, en casos graves.",
                        fontSize = 13.sp,
                        color = Color(0xFFC62828),
                        lineHeight = 20.sp
                    )
                }
            }

            // Sección 7
            TermsSection(number = "7", title = "Derechos ARCO") {
                TermsBody("De acuerdo con la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP), tienes derecho a:")
                Spacer(modifier = Modifier.height(6.dp))
                TermsBullet("Acceso: conocer qué datos tenemos sobre ti.")
                TermsBullet("Rectificación: corregir datos incorrectos o desactualizados.")
                TermsBullet("Cancelación: solicitar la eliminación de tus datos.")
                TermsBullet("Oposición: oponerte al uso de tus datos para fines específicos.")
                Spacer(modifier = Modifier.height(8.dp))
                TermsBody("Para ejercer cualquiera de estos derechos, escríbenos a:")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "243836@ids.upchiapas.edu.mx",
                    fontSize = 14.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Sección 8
            TermsSection(number = "8", title = "Menores de edad") {
                TermsBody("PetData no tiene restricción de edad. Sin embargo, si eres menor de 18 años, te recomendamos usar la aplicación con conocimiento y supervisión de un adulto responsable, especialmente al compartir tu ubicación GPS.")
            }

            // Sección 9
            TermsSection(number = "9", title = "Cambios a estos términos") {
                TermsBody("El equipo de PetData se reserva el derecho de modificar estos términos en cualquier momento. En caso de cambios importantes, se notificará a los usuarios a través de la aplicación. El uso continuado de la app después de la notificación implica la aceptación de los nuevos términos.")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Este documento es orientativo y no constituye asesoría legal profesional.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

// ── Componentes auxiliares ─────────────────────────────────────────────────

@Composable
private fun TermsSection(
    number: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(GreenPrimary, shape = RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        content()
    }
}

@Composable
private fun TermsBody(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = TextSecondary,
        lineHeight = 22.sp
    )
}

@Composable
private fun TermsBullet(text: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text("•  ", fontSize = 14.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
        Text(text = text, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
    }
}

@Composable
private fun TermsTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
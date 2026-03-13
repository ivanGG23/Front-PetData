package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.ComentarioResponse
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.AccionState
import com.example.petdata.ui.viemodel.ComentarioState
import com.example.petdata.ui.viemodel.ReportDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reporteId: Int,
    rolId: Int = 1,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit = {},
    onNavigate: (route: String) -> Unit = {}
) {
    val viewModel: ReportDetailViewModel = viewModel(
        factory = ReportDetailViewModel.Factory(tokenManager)
    )

    val state by viewModel.state.collectAsStateWithLifecycle()
    val comentarioState by viewModel.comentarioState.collectAsStateWithLifecycle()
    var nuevoComentario by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val accionState by viewModel.accionState.collectAsStateWithLifecycle()
    var showCambiarEstadoDialog by remember { mutableStateOf(false) }
    var showDesasignarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reporteId) {
        viewModel.loadReporte(reporteId)
        viewModel.startPollingComentarios(reporteId)
    }

    LaunchedEffect(comentarioState) {
        when (comentarioState) {
            is ComentarioState.Success -> nuevoComentario = ""
            is ComentarioState.Error -> {
                errorMessage = (comentarioState as ComentarioState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    LaunchedEffect(accionState) {
        when (accionState) {
            is AccionState.Error -> {
                errorMessage = (accionState as AccionState.Error).message
                showErrorDialog = true
                viewModel.resetAccionState()
            }
            else -> {}
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = { Text("❌", fontSize = 32.sp) },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text("Aceptar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Reporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
            return@Scaffold
        }

        if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.error!!, color = Color.Red)
            }
            return@Scaffold
        }

        val reporte = state.reporte ?: return@Scaffold

        if (showDesasignarDialog) {
            AlertDialog(
                onDismissRequest = { showDesasignarDialog = false },
                icon = { Text("⚠️", fontSize = 32.sp) },
                title = { Text("¿Desasignarte?", fontWeight = FontWeight.Bold) },
                text = { Text("Se te restarán 5 puntos de reputación y el reporte volverá a estado Pendiente.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDesasignarDialog = false
                            viewModel.desasignarReporte(reporteId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) { Text("Desasignarme") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDesasignarDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showCambiarEstadoDialog) {
            CambiarEstadoDialog(
                estadoActual = reporte.estado_reporte_actual,
                onConfirm = { nuevoEstado, comentario, urlImgs ->
                    showCambiarEstadoDialog = false
                    viewModel.cambiarEstado(reporteId, nuevoEstado, comentario, urlImgs)
                },
                onDismiss = { showCambiarEstadoDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Datos básicos ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reporte.estado_animal.nombre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        PriorityBadge(
                            priority = when (reporte.prioridad_id) {
                                4 -> "CRÍTICA"
                                3 -> "ALTA"
                                2 -> "MEDIA"
                                else -> "BAJA"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = reporte.descripcion,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = calcularTiempo(reporte.fecha_creacion),
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reporte.creador?.nombre ?: "Usuario #${reporte.usuario_creador_id}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    if (state.userStats != null || state.reputacion != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Puntos de reputación
                            StatChip(
                                icon = "⭐",
                                value = "${state.reputacion?.total_puntos ?: 0}",
                                label = "Puntos"
                            )

                            // Total de reportes
                            StatChip(
                                icon = "📋",
                                value = "${state.userStats?.total_reportes ?: 0}",
                                label = "Reportes"
                            )

                            // Reportes falsos
                            StatChip(
                                icon = "❌",
                                value = "${state.userStats?.reportes_falsos ?: 0}",
                                label = "Falsos"
                            )
                        }
                    }

                    // Teléfono solo para rescatistas
                    if (rolId == 2 && reporte.contacto_opcional != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reporte.contacto_opcional,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // ── Barra de progreso de estados ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Estado del Reporte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    EstadoProgressBar(estadoActual = reporte.estado_reporte_actual)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Rescatista asignado ──
            if (reporte.rescatista_id != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🦺", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rescatista asignado",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = reporte.rescatista?.nombre ?: "Rescatista #${reporte.rescatista_id}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Evidencias ──
            if (state.evidencias.isNotEmpty()) {
                var imagenSeleccionada by remember { mutableStateOf<String?>(null) }

                // Dialog visor pantalla completa
                if (imagenSeleccionada != null) {
                    AlertDialog(
                        onDismissRequest = { imagenSeleccionada = null },
                        confirmButton = {
                            TextButton(onClick = { imagenSeleccionada = null }) {
                                Text("Cerrar", color = GreenPrimary)
                            }
                        },
                        text = {
                            coil.compose.AsyncImage(
                                model = imagenSeleccionada,
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp, max = 400.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Evidencias (${state.evidencias.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.evidencias) { evidencia ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { imagenSeleccionada = evidencia.url_img }
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = evidencia.url_img,
                                            contentDescription = null,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        // Badge tipo
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.55f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = evidencia.tipo.replaceFirstChar { it.uppercase() },
                                                fontSize = 10.sp,
                                                color = White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = calcularTiempo(evidencia.fecha_subido),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Botones de acción (solo rescatistas) ──
            if (rolId == 2) {
                val estadoActual = reporte.estado_reporte_actual
                val esMiReporte = reporte.rescatista_id != null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Acciones",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (estadoActual == 1 && !esMiReporte) {
                            Button(
                                onClick = { viewModel.asignarReporte(reporteId) },
                                enabled = accionState !is AccionState.Loading,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (accionState is AccionState.Loading) {
                                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Asignarme a este caso", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (esMiReporte && estadoActual != 4 && estadoActual != 5) {
                            Button(
                                onClick = { showCambiarEstadoDialog = true },
                                enabled = accionState !is AccionState.Loading,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (accionState is AccionState.Loading) {
                                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cambiar estado", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { showDesasignarDialog = true },
                                enabled = accionState !is AccionState.Loading,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                            ) {
                                Icon(Icons.Default.PersonRemove, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Desasignarme", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Comentarios ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Comentarios (${state.comentarios.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.comentarios.isEmpty()) {
                        Text(
                            text = "No hay comentarios aún. ¡Sé el primero!",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    } else {
                        state.comentarios.forEach { comentario ->
                            ComentarioItem(comentario = comentario)
                            if (comentario != state.comentarios.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color(0xFFEEEEEE)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input nuevo comentario
                    OutlinedTextField(
                        value = nuevoComentario,
                        onValueChange = { if (it.length <= 500) nuevoComentario = it },
                        placeholder = { Text("Escribe un comentario...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = GreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (nuevoComentario.isNotEmpty()) {
                                viewModel.enviarComentario(reporteId, nuevoComentario)
                            }
                        },
                        enabled = nuevoComentario.isNotEmpty() &&
                                comentarioState !is ComentarioState.Loading,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (comentarioState is ComentarioState.Loading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar comentario", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun EstadoProgressBar(estadoActual: Int) {
    val estados = listOf("Pendiente", "Revisión", "Proceso", "Resuelto", "Falso")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        estados.forEachIndexed { index, nombre ->
            val estadoId = index + 1
            val completado = estadoActual >= estadoId
            val esFalso = estadoActual == 5 && estadoId == 5

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                esFalso -> Color(0xFFE53935)
                                completado -> GreenPrimary
                                else -> Color(0xFFE0E0E0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completado) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nombre,
                    fontSize = 9.sp,
                    color = if (completado) GreenPrimary else TextSecondary,
                    fontWeight = if (completado) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            if (index < estados.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (estadoActual > estadoId) GreenPrimary
                            else Color(0xFFE0E0E0)
                        )
                )
            }
        }
    }
}

@Composable
fun ComentarioItem(comentario: ComentarioResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comentario.nombre_usuario ?: "Usuario #${comentario.usuario_id}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = calcularTiempo(comentario.fecha),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comentario.comentario,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun CambiarEstadoDialog(
    estadoActual: Int,
    onConfirm: (nuevoEstado: Int, comentario: String?, urlImgs: List<String>?) -> Unit,
    onDismiss: () -> Unit
) {
    // Opciones según estado actual: rescatista solo puede avanzar linealmente o marcar como falso
    val opciones = buildList {
        when (estadoActual) {
            1 -> add(2 to "En revisión")
            2 -> add(3 to "En proceso")
            3 -> add(4 to "Resuelto")
        }
        if (estadoActual in 1..3) add(5 to "Falso")
    }

    var estadoSeleccionado by remember { mutableStateOf(opciones.first().first) }
    var comentario by remember { mutableStateOf("") }
    var urlImg by remember { mutableStateOf("") }

    val requiereComentario = estadoSeleccionado == 5
    val requiereImagen = estadoSeleccionado == 4

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar estado", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Selecciona el nuevo estado:", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                opciones.forEach { (id, nombre) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = estadoSeleccionado == id,
                            onClick = { estadoSeleccionado = id },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                        )
                        Text(nombre, fontSize = 14.sp, color = TextPrimary)
                    }
                }

                if (requiereComentario) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Justificación (obligatoria):", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        placeholder = { Text("Explica por qué es falso...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }

                if (requiereImagen) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("URL de imagen de cierre (obligatoria):", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = urlImg,
                        onValueChange = { urlImg = it },
                        placeholder = { Text("https://...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }
        },
        confirmButton = {
            val habilitado = when {
                requiereComentario -> comentario.isNotBlank()
                requiereImagen -> urlImg.isNotBlank()
                else -> true
            }
            Button(
                onClick = {
                    onConfirm(
                        estadoSeleccionado,
                        if (requiereComentario) comentario else null,
                        if (requiereImagen) listOf(urlImg) else null
                    )
                },
                enabled = habilitado,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) { Text("Confirmar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun StatChip(icon: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
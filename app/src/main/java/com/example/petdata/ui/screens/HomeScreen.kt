package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.petdata.data.model.GlobalStats
import com.example.petdata.data.model.ReporteResponse
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.HomeState
import com.example.petdata.ui.viemodel.HomeViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Data models (solo UI, sin lógica)
// ─────────────────────────────────────────────────────────────────────────────

data class ReportItem(
    val title: String,
    val description: String,
    val location: String,
    val timeAgo: String,
    val health: String?,
    val priority: String,
    val status: String,
    val imageUrl: String
)

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    rolId: Int = 1,
    viewModel: HomeViewModel,
    onNavigateToReport: (mode: String) -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigate: (route: String) -> Unit = {}
) {
    val globalStats by viewModel.globalStats.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadReportes()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar    = { RescateTopBar() },
        bottomBar = { BottomNavigationBar(
            selectedIndex = 0,
            rolId = rolId,
            onNavigate = onNavigate) },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero banner ──
            HeroBanner(onNavigateToReport = onNavigateToReport)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Monthly summary ──
            MonthlySummarySection(stats = globalStats)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Latest reports header + filter chips ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Últimos Reportes",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Chips tipo animal ──
            val filtroTipo      by viewModel.filtroTipo.collectAsStateWithLifecycle()
            val filtroEstado    by viewModel.filtroEstado.collectAsStateWithLifecycle()
            val filtroPrioridad by viewModel.filtroPrioridad.collectAsStateWithLifecycle()
            val filtroFecha     by viewModel.filtroFecha.collectAsStateWithLifecycle()

            val tipoFiltros = listOf("Todos" to null, "Perros" to 1, "Gatos" to 2, "Otro" to 3)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tipoFiltros.forEach { (label, id) ->
                    FilterChip(
                        label    = label,
                        selected = filtroTipo == id,
                        onClick  = { viewModel.setFiltroTipo(id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

// ── Dropdowns de filtros ──
            var expandedEstado    by remember { mutableStateOf(false) }
            var expandedPrioridad by remember { mutableStateOf(false) }
            var expandedFecha     by remember { mutableStateOf(false) }

            val estadoOpciones    = listOf(null to "Estado", 1 to "Pendiente", 2 to "En revisión", 3 to "En proceso", 4 to "Resuelto", 5 to "Falso")
            val prioridadOpciones = listOf(null to "Prioridad", 1 to "Baja", 2 to "Media", 3 to "Alta", 4 to "Crítica")
            val fechaOpciones     = listOf(null to "Fecha", "hoy" to "Hoy", "semana" to "Esta semana", "mes" to "Este mes")

            val estadoLabel    = estadoOpciones.find    { it.first == filtroEstado }?.second    ?: "Estado"
            val prioridadLabel = prioridadOpciones.find { it.first == filtroPrioridad }?.second ?: "Prioridad"
            val fechaLabel     = fechaOpciones.find     { it.first == filtroFecha }?.second     ?: "Fecha"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dropdown Estado
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedEstado = true },
                        shape  = RoundedCornerShape(50),
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (filtroEstado != null) GreenPrimary else Color.Transparent,
                            contentColor   = if (filtroEstado != null) Color.White else TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(estadoLabel, fontSize = 12.sp, maxLines = 1)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                        estadoOpciones.forEach { (id, nombre) ->
                            DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                viewModel.setFiltroEstado(id)
                                expandedEstado = false
                            })
                        }
                    }
                }

                // Dropdown Prioridad
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedPrioridad = true },
                        shape  = RoundedCornerShape(50),
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (filtroPrioridad != null) GreenPrimary else Color.Transparent,
                            contentColor   = if (filtroPrioridad != null) Color.White else TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(prioridadLabel, fontSize = 12.sp, maxLines = 1)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = expandedPrioridad, onDismissRequest = { expandedPrioridad = false }) {
                        prioridadOpciones.forEach { (id, nombre) ->
                            DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                viewModel.setFiltroPrioridad(id)
                                expandedPrioridad = false
                            })
                        }
                    }
                }

                // Dropdown Fecha
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedFecha = true },
                        shape  = RoundedCornerShape(50),
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (filtroFecha != null) GreenPrimary else Color.Transparent,
                            contentColor   = if (filtroFecha != null) Color.White else TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(fechaLabel, fontSize = 12.sp, maxLines = 1)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = expandedFecha, onDismissRequest = { expandedFecha = false }) {
                        fechaOpciones.forEach { (valor, nombre) ->
                            DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                viewModel.setFiltroFecha(valor)
                                expandedFecha = false
                            })
                        }
                    }
                }
            }

            // Botón limpiar filtros (solo visible si hay algún filtro activo)
            if (filtroTipo != null || filtroEstado != null || filtroPrioridad != null || filtroFecha != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.limpiarFiltros() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar filtros", fontSize = 12.sp, color = GreenPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report cards
            when (homeState) {
                is HomeState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is HomeState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE53935)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (homeState as HomeState.Error).message,
                                color = Color(0xFFE53935),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is HomeState.Success -> {
                    val reportes = (homeState as HomeState.Success).reportes
                    if (reportes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay reportes disponibles",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        reportes.forEach { reporte ->
                            ReporteCard(
                                reporte = reporte,
                                onNavigateToDetail = onNavigateToDetail
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescateTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Paw icon placeholder (green circle)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFEB3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text       = "RescateAnimal",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = White
                    )
                    Text(
                        text     = "APP OFICIAL",
                        fontSize = 10.sp,
                        color    = White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector        = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint               = White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GreenPrimary
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero banner (green card with CTA)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeroBanner(onNavigateToReport: (mode: String) -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GreenPrimary)
            .padding(20.dp)
    ) {
        Column {
            // "¡Tu ayuda importa!" pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFFEB3B))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = "¡Tu ayuda importa!",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF212121)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text       = "Reporta un caso al\ninstante",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = White,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text      = "Usa tu ubicación actual para ayudar a los rescatistas a encontrar animales en peligro.",
                fontSize  = 13.sp,
                color     = White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CTA button
            Button(
                onClick = { onNavigateToReport("crear") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White
                )
            ) {
                Text(
                    text       = "+ Crear Nuevo Reporte",
                    color      = GreenPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Monthly summary stats
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MonthlySummarySection(stats: GlobalStats?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text       = "Resumen Mensual",
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            color      = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label    = "En Proceso",
                value    = stats?.activos?.toString() ?: "...",
                change   = null,
                changeUp = true,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label    = "Rescatados",
                value    = stats?.rescatados?.toString() ?: "...",
                change   = null,
                changeUp = true,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label    = "Pendientes",
                value    = stats?.pendientes?.toString() ?: "...",
                change   = null,
                changeUp = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    change: String?,
    changeUp: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text     = label,
                fontSize = 11.sp,
                color    = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = value,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
                if (change != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text     = change,
                        fontSize = 11.sp,
                        color    = if (changeUp) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick      = onClick,
        shape        = RoundedCornerShape(50),
        color        = if (selected) GreenPrimary else White,
        border       = if (!selected) ButtonDefaults.outlinedButtonBorder else null,
        tonalElevation = 0.dp
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color    = if (selected) White else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Report card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ReporteCard(
    reporte: ReporteResponse,
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val prioridad = when (reporte.prioridad_id) {
        4 -> "CRÍTICA"
        3 -> "ALTA"
        2 -> "MEDIA"
        else -> "BAJA"
    }
    val estado = reporte.estado_reporte.nombre
    val timeAgo = calcularTiempo(reporte.fecha_creacion)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Imagen del reporte ──
            if (reporte.imagen_url != null) {
                AsyncImage(
                    model = reporte.imagen_url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // ── Contenido ──
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reporte.estado_animal.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PriorityBadge(priority = prioridad)
                        StatusBadge(status = estado)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reporte.descripcion,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = timeAgo, fontSize = 12.sp, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onNavigateToDetail(reporte.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Ver Detalles", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

fun calcularTiempo(fechaStr: String): String {
    return try {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        formato.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val fecha = formato.parse(fechaStr) ?: return "Hace un momento"
        val ahora = java.util.Date()
        val diferencia = ahora.time - fecha.time
        val minutos = diferencia / 60000
        val horas = minutos / 60
        val dias = horas / 24
        when {
            minutos < 1 -> "Hace un momento"
            minutos < 60 -> "Hace $minutos min"
            horas < 24 -> "Hace $horas horas"
            else -> "Hace $dias días"
        }
    } catch (e: Exception) {
        "Hace un momento"
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Badges
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val color = when (priority) {
        "ALTA"  -> BadgeAlta
        "BAJA"  -> BadgeBaja
        else    -> BadgeMedia   // MEDIA
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text       = priority,
            color      = White,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "En Proceso" -> BadgeEnProceso
        "Rescatado"  -> BadgeRescatado
        else         -> BadgePendiente   // Pendiente
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text       = status,
            color      = White,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
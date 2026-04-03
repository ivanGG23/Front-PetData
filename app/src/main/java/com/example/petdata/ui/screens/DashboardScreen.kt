package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.EspecieStat
import com.example.petdata.data.model.HistorialStat
import com.example.petdata.data.model.ZonaStat
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.DashboardState
import com.example.petdata.ui.viemodel.DashboardViewModel

@Composable
fun DashboardScreen(
    rolId: Int = 2,
    tokenManager: TokenManager,
    onNavigate: (route: String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Zonas") }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(tokenManager)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { RescateTopBar() },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 3,
                rolId = 2,
                onNavigate = onNavigate
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        when (state) {
            is DashboardState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            is DashboardState.Error -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as DashboardState.Error).message,
                        color = Color.Red
                    )
                }
            }

            is DashboardState.Success -> {
                val stats = (state as DashboardState.Success).stats

                // Calcular tarjetas de resumen
                val totalReportes = stats.especies.sumOf { it.total }
                val totalActivos = stats.activos
                val totalResueltos = stats.rescatados
                val porcentajeResolucion = if ((totalActivos + totalResueltos) > 0)
                    (totalResueltos * 100 / (totalActivos + totalResueltos)) else 0

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Panel de Control",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Text(
                                    text = "Métricas clave de rescate y abandono",
                                    fontSize = 12.sp,
                                    color = White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Stats cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardStatCard(
                            title = "CASOS\nACTIVOS",
                            value = totalActivos.toString(),
                            change = "Total - resueltos",
                            changePositive = false,
                            icon = Icons.Default.Warning,
                            backgroundColor = Color(0xFFFFEBEE),
                            iconColor = Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "RESUELTOS",
                            value = totalResueltos.toString(),
                            change = "Casos cerrados",
                            changePositive = true,
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = Color(0xFFE8F5E9),
                            iconColor = GreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "RESOLUCIÓN",
                            value = "$porcentajeResolucion%",
                            change = "Del total",
                            changePositive = true,
                            icon = Icons.Default.TrendingUp,
                            backgroundColor = Color(0xFFE3F2FD),
                            iconColor = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardTab(
                            icon = Icons.Default.Map,
                            label = "Zonas",
                            selected = selectedTab == "Zonas",
                            onClick = { selectedTab = "Zonas" }
                        )
                        DashboardTab(
                            icon = Icons.Default.Pets,
                            label = "Especies",
                            selected = selectedTab == "Especies",
                            onClick = { selectedTab = "Especies" }
                        )
                        DashboardTab(
                            icon = Icons.Default.TrendingUp,
                            label = "Historial",
                            selected = selectedTab == "Historial",
                            onClick = { selectedTab = "Historial" }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTab) {
                        "Zonas"    -> FrequencyByZoneChart(stats.zonas)
                        "Especies" -> DistributionBySpeciesChart(stats.especies)
                        "Historial" -> CaseTrendChart(stats.historial)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ── Tarjeta de estadística ────────────────────────────────────────────────────

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    change: String,
    changePositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = change,
                fontSize = 10.sp,
                color = if (changePositive) GreenLight else Color(0xFFE53935),
                lineHeight = 12.sp
            )
        }
    }
}

// ── Tab ───────────────────────────────────────────────────────────────────────

@Composable
fun DashboardTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GreenPrimary else White,
            contentColor = if (selected) White else TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Zonas ─────────────────────────────────────────────────────────────────────

@Composable
fun FrequencyByZoneChart(zonas: List<ZonaStat>) {
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
                text = "Frecuencia por Zona",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Reportes vs resueltos por ciudad",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (zonas.isEmpty()) {
                Text(
                    text = "Sin datos de zonas disponibles",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val maxTotal = zonas.maxOf { it.total }.coerceAtLeast(1)
                zonas.forEach { zona ->
                    ZoneBar(
                        zone = zona.zona,
                        totalFraction = zona.total.toFloat() / maxTotal,
                        resueltaFraction = zona.resueltos.toFloat() / maxTotal,
                        totalCount = zona.total,
                        resueltosCount = zona.resueltos
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LegendItemBar("Reportes", Color(0xFFFFA726))
                    Spacer(modifier = Modifier.width(20.dp))
                    LegendItemBar("Resueltos", GreenLight)
                }
            }
        }
    }
}

@Composable
fun ZoneBar(
    zone: String,
    totalFraction: Float,
    resueltaFraction: Float,
    totalCount: Int,
    resueltosCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = zone,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.width(80.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(totalFraction)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFA726))
                )
                if (totalFraction < 1f) Spacer(modifier = Modifier.weight(1f - totalFraction))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$totalCount", fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(resueltaFraction.coerceAtLeast(0.01f))
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GreenLight)
                )
                if (resueltaFraction < 1f) Spacer(modifier = Modifier.weight(1f - resueltaFraction.coerceAtLeast(0.01f)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$resueltosCount", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun LegendItemBar(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}

// ── Especies ──────────────────────────────────────────────────────────────────

@Composable
fun DistributionBySpeciesChart(especies: List<EspecieStat>) {
    val colores = listOf(GreenLight, Color(0xFFFFA726), Color(0xFF7E57C2))
    val total = especies.sumOf { it.total }.coerceAtLeast(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Distribución por Especie",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Proporción de animales atendidos",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barras horizontales por especie
            especies.forEachIndexed { index, especie ->
                val color = colores.getOrElse(index) { Color.Gray }
                val fraccion = especie.total.toFloat() / total
                val porcentaje = (fraccion * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = especie.nombre,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.width(60.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF0F0F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraccion)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(color)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$porcentaje%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.width(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                especies.forEachIndexed { index, especie ->
                    val color = colores.getOrElse(index) { Color.Gray }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = especie.nombre,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (index < especies.size - 1) Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
    }
}

// ── Historial ─────────────────────────────────────────────────────────────────

@Composable
fun CaseTrendChart(historial: List<HistorialStat>) {
    val maxValor = historial.maxOf { maxOf(it.nuevos, it.resueltos) }.coerceAtLeast(1)

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
                text = "Tendencia de Casos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Últimos 30 días — intervalos de 5 días",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barras agrupadas por período
            historial.forEach { periodo ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = periodo.periodo,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(periodo.nuevos.toFloat() / maxValor)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFA726))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${periodo.nuevos}", fontSize = 10.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((periodo.resueltos.toFloat() / maxValor).coerceAtLeast(0f))
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GreenLight)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${periodo.resueltos}", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItemBar("Nuevos", Color(0xFFFFA726))
                Spacer(modifier = Modifier.width(20.dp))
                LegendItemBar("Resueltos", GreenLight)
            }
        }
    }
}
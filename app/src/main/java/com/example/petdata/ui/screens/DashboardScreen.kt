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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigate: (route: String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Zonas") }

    Scaffold(
        topBar = { RescateTopBar() },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 4,
                rolId = 2,
                onNavigate = onNavigate
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
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
                colors = CardDefaults.cardColors(
                    containerColor = GreenPrimary
                )
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
                    value = "142",
                    change = "+12% vs mes\nanterior",
                    changePositive = false,
                    icon = Icons.Default.Warning,
                    backgroundColor = Color(0xFFFFEBEE),
                    iconColor = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )

                DashboardStatCard(
                    title = "RESCATES",
                    value = "856",
                    change = "+5% vs mes\nanterior",
                    changePositive = true,
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = Color(0xFFE8F5E9),
                    iconColor = GreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                DashboardStatCard(
                    title = "ADOPCIÓN",
                    value = "64%",
                    change = "+2% vs mes\nanterior",
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
                    icon = Icons.Default.AccessTime,
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

            // Content based on selected tab
            when (selectedTab) {
                "Zonas" -> FrequencyByZoneChart()
                "Especies" -> DistributionBySpeciesChart()
                "Historial" -> CaseTrendChart()
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun FrequencyByZoneChart() {
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
                text = "Abandonos vs rescates por sector",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Simulated bar chart
            val zones = listOf(
                "Norte" to (65 to 40),
                "Sur" to (45 to 30),
                "Centro" to (95 to 70),
                "Oeste" to (30 to 15),
                "Este" to (55 to 45)
            )

            zones.forEach { (zone, values) ->
                ZoneBar(zone, values.first, values.second)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItemBar("Abandonos", Color(0xFFFFA726))
                Spacer(modifier = Modifier.width(20.dp))
                LegendItemBar("Rescates", GreenLight)
            }
        }
    }
}

@Composable
fun ZoneBar(zone: String, abandono: Int, rescate: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = zone,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.width(50.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            // Abandono bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(abandono / 100f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFA726))
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Rescate bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(rescate / 100f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GreenLight)
            )
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
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun DistributionBySpeciesChart() {
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

            Spacer(modifier = Modifier.height(32.dp))

            // Donut chart (simulado)
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring segments
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(90.dp))
                        .background(
                            Brush.sweepGradient(
                                0f to GreenLight,
                                0.5f to Color(0xFFFFA726),
                                0.88f to Color(0xFF7E57C2),
                                1f to GreenLight
                            )
                        )
                )
                // Inner white circle (donut hole)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(White)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpeciesLabel("Perros 50%", GreenLight)
                SpeciesLabel("Gatos 38%", Color(0xFFFFA726))
                SpeciesLabel("Otros 12%", Color(0xFF7E57C2))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFFFA726))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gatos", fontSize = 11.sp, color = TextSecondary)

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF7E57C2))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Otros", fontSize = 11.sp, color = TextSecondary)

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GreenLight)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Perros", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun SpeciesLabel(text: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text.substringAfter(" "),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = text.substringBefore(" "),
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun CaseTrendChart() {
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
                text = "Historial últimos 6 meses",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Line chart (simulado con gradiente)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Gradient fill under line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    GreenLight.copy(alpha = 0.3f),
                                    GreenLight.copy(alpha = 0.0f)
                                )
                            )
                        )
                )

                // Line path placeholder
                Canvas(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun").forEach { month ->
                    Text(
                        text = month,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// Canvas placeholder
@Composable
fun Canvas(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}
package com.example.petdata.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petdata.data.local.TokenManager
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.RescuerState
import com.example.petdata.ui.viemodel.RescuerViewModel

// ── Pantalla: Historial de Rescates (estado 4) ────────────────────────────────

@Composable
fun RescuerHistoryScreen(
    rolId: Int = 2,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    RescuerCasesScreen(
        title = "Historial de Rescates",
        emptyMessage = "No tienes rescates completados aún",
        estadoId = 4,
        rolId = rolId,
        tokenManager = tokenManager,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = onNavigateToDetail,
        onNavigate = onNavigate
    )
}

// ── Pantalla: Casos Activos (estado 3) ────────────────────────────────────────

@Composable
fun RescuerActiveCasesScreen(
    rolId: Int = 2,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    RescuerCasesScreen(
        title = "Casos Activos",
        emptyMessage = "No tienes casos activos en este momento",
        estadoId = 3,
        rolId = rolId,
        tokenManager = tokenManager,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = onNavigateToDetail,
        onNavigate = onNavigate
    )
}

// ── Pantalla base reutilizable ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescuerCasesScreen(
    title: String,
    emptyMessage: String,
    estadoId: Int,
    rolId: Int,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel: RescuerViewModel = viewModel(
        key = "rescuer_$estadoId",
        factory = RescuerViewModel.Factory(tokenManager, estadoId)
    )

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 3,
                rolId = rolId,
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
            when (val s = state) {

                is RescuerState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }

                is RescuerState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = s.message,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.loadReportes() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is RescuerState.Success -> {
                    val reportes = s.reportes

                    // Header con contador
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (estadoId == 4) GreenPrimary else Color(0xFF2196F3)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "${reportes.size} ${if (reportes.size == 1) "caso" else "casos"}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    color = White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    if (reportes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🐾", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = emptyMessage,
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
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

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
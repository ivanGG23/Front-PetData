package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petdata.data.local.TokenManager
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.SettingsUiState
import com.example.petdata.ui.viemodel.SettingsViewModel

@Composable
fun SettingsScreen(
    rolId: Int = 1,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(tokenManager)
    )

    val uiState by viewModel.uiState.collectAsState()
    val logoutDone by viewModel.logoutDone.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // Navegar al login cuando logout se complete
    LaunchedEffect(logoutDone) {
        if (logoutDone) onLogout()
    }

    // Dialog cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Cerrar sesión") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialog cambiar contraseña (próximamente)
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Esta funcionalidad estará disponible próximamente.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showChangePasswordDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(
        topBar = { RescateTopBar() },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 3,
                rolId = rolId,
                onNavigate = { route ->
                    if (route != "settings") onNavigateBack()
                }
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
            when (val state = uiState) {

                is SettingsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }

                is SettingsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se pudieron cargar los datos",
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.loadData() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is SettingsUiState.Success -> {
                    val user = state.user
                    val reputacion = state.reputacion

                    // Header perfil
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
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 32.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "${user.nombre} ${user.apellido}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Text(
                                    text = user.correo,
                                    fontSize = 12.sp,
                                    color = White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (rolId == 2) "Rescatista" else "Ciudadano",
                                    fontSize = 12.sp,
                                    color = White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    // Sección Perfil
                    SettingsSection(title = "Perfil") {
                        SettingsItem(
                            icon = Icons.Default.Lock,
                            iconColor = Color(0xFF2196F3),
                            title = "Cambiar contraseña",
                            subtitle = "Actualiza tu contraseña",
                            onClick = { showChangePasswordDialog = true }
                        )
                    }

                    // Sección Mi Actividad
                    SettingsSection(title = "Mi Actividad") {
                        // Reputación
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFC107).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mi reputación",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (reputacion != null)
                                        "${reputacion.total_puntos} puntos acumulados"
                                    else
                                        "Sin datos de reputación",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            if (reputacion != null) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = GreenPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${reputacion.total_puntos} pts",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                            }
                        }

                        SettingsDivider()

                        if (rolId == 2) {
                            SettingsItem(
                                icon = Icons.Default.CheckCircle,
                                iconColor = GreenPrimary,
                                title = "Historial de rescates",
                                subtitle = "Ver mis rescates completados",
                                onClick = { onNavigate("rescuer_history") }
                            )
                            SettingsDivider()
                            SettingsItem(
                                icon = Icons.Default.Assignment,
                                iconColor = Color(0xFF2196F3),
                                title = "Casos activos",
                                subtitle = "Ver mis casos en proceso",
                                onClick = { onNavigate("rescuer_active_cases") }
                            )
                        }
                    }

                    // Sección Sesión
                    SettingsSection(title = "Sesión") {
                        SettingsItem(
                            icon = Icons.Default.ExitToApp,
                            iconColor = Color(0xFFE53935),
                            title = "Cerrar sesión",
                            subtitle = "Salir de tu cuenta",
                            titleColor = Color(0xFFE53935),
                            onClick = { showLogoutDialog = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ── Componentes reutilizables ──────────────────────────────────────────────

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    titleColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp),
        color = Color(0xFFEEEEEE),
        thickness = 1.dp
    )
}
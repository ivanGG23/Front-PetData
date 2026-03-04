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
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*

@Composable
fun SettingsScreen(
    rolId: Int = 1,
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var nearbyReportsEnabled by remember { mutableStateOf(true) }
    var myReportsEnabled by remember { mutableStateOf(true) }

    // Dialog cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro que deseas cerrar sesión?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
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
                            text = "Mi Perfil",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = if (rolId == 2) "Rescatista" else "Ciudadano",
                            fontSize = 13.sp,
                            color = White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Sección Perfil
            SettingsSection(title = "Perfil") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    iconColor = GreenPrimary,
                    title = "Ver y editar perfil",
                    subtitle = "Nombre, apellido y foto",
                    onClick = {}
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF2196F3),
                    title = "Cambiar contraseña",
                    subtitle = "Actualiza tu contraseña",
                    onClick = {}
                )
            }

            // Sección Notificaciones
            SettingsSection(title = "Notificaciones") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFFFF9800),
                    title = "Notificaciones",
                    subtitle = "Activar todas las notificaciones",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                SettingsDivider()
                SettingsToggleItem(
                    icon = Icons.Default.LocationOn,
                    iconColor = Color(0xFFE53935),
                    title = "Reportes cercanos",
                    subtitle = "Notificarme de reportes cerca de mí",
                    checked = nearbyReportsEnabled,
                    onCheckedChange = { nearbyReportsEnabled = it }
                )
                SettingsDivider()
                SettingsToggleItem(
                    icon = Icons.Default.Assignment,
                    iconColor = GreenPrimary,
                    title = "Mis reportes",
                    subtitle = "Cambios en mis reportes",
                    checked = myReportsEnabled,
                    onCheckedChange = { myReportsEnabled = it }
                )
            }

            // Sección Mi Actividad
            SettingsSection(title = "Mi Actividad") {
                SettingsItem(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFC107),
                    title = "Mi reputación",
                    subtitle = "Ver mis puntos y logros",
                    onClick = {}
                )
                SettingsDivider()
                if (rolId == 2) {
                    SettingsItem(
                        icon = Icons.Default.CheckCircle,
                        iconColor = GreenPrimary,
                        title = "Historial de rescates",
                        subtitle = "Ver mis rescates completados",
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = Icons.Default.Assignment,
                        iconColor = Color(0xFF2196F3),
                        title = "Casos activos",
                        subtitle = "Ver mis casos en proceso",
                        onClick = {}
                    )
                } else {
                    SettingsItem(
                        icon = Icons.Default.List,
                        iconColor = Color(0xFF2196F3),
                        title = "Mis reportes",
                        subtitle = "Ver reportes que he creado",
                        onClick = {}
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

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

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
fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = GreenPrimary
            )
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
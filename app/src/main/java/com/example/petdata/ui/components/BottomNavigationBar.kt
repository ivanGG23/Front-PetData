package com.example.petdata.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.petdata.ui.theme.GreenPrimary
import com.example.petdata.ui.theme.NavUnselected

@Composable
fun BottomNavigationBar(
    selectedIndex: Int = 0,
    rolId: Int = 1,
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(4f)
    ) {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedIconColor = NavUnselected,
                unselectedTextColor = NavUnselected,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onNavigate("report/crear") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Reportar") },
            label = { Text("Reportar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedIconColor = NavUnselected,
                unselectedTextColor = NavUnselected,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onNavigate("map") },
            icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
            label = { Text("Mapa") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedIconColor = NavUnselected,
                unselectedTextColor = NavUnselected,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
            label = { Text("Ajustes") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedIconColor = NavUnselected,
                unselectedTextColor = NavUnselected,
                indicatorColor = Color.Transparent
            )
        )

        // Solo rescatistas ven Dashboard
        if (rolId == 2) {
            NavigationBarItem(
                selected = selectedIndex == 3,
                onClick = { onNavigate("dashboard") },
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Datos") },
                label = { Text("Datos") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    unselectedIconColor = NavUnselected,
                    unselectedTextColor = NavUnselected,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
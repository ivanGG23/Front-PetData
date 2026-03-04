package com.example.petdata.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petdata.data.local.TokenManager
import com.example.petdata.data.model.HeatmapPoint
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.MapState
import com.example.petdata.ui.viemodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@Composable
fun MapScreen(
    rolId: Int = 1,
    tokenManager: TokenManager,
    onNavigate: (route: String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel(
        factory = MapViewModel.Factory(tokenManager)
    )
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()

    // Modo del mapa: "marcadores" o "calor"
    var modoMapa by remember { mutableStateOf("marcadores") }

    // Inicializar OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = { RescateTopBar() },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 2,
                rolId = rolId,
                onNavigate = onNavigate
            )
        },
        containerColor = White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (mapState) {
                is MapState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is MapState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((mapState as MapState.Error).message, color = Color.Red)
                    }
                }
                is MapState.Success -> {
                    val data = mapState as MapState.Success

                    // Mapa OSMDroid
                    AndroidView(
                        factory = { ctx ->
                            crearMapView(ctx)
                        },
                        update = { mapView ->
                            mapView.overlays.clear()
                            if (modoMapa == "marcadores") {
                                agregarMarcadores(mapView, data.puntos, context)
                            } else {
                                agregarCalor(mapView, data.puntos)
                            }
                            // Centrar mapa en el primer punto si hay datos
                            if (data.puntos.isNotEmpty()) {
                                val centro = GeoPoint(
                                    data.puntos.map { it.latitud }.average(),
                                    data.puntos.map { it.longitud }.average()
                                )
                                mapView.controller.animateTo(centro)
                                mapView.controller.setZoom(if (data.puntos.size == 1) 12.0 else 8.0)
                            }
                            mapView.invalidate()
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Botón para cambiar modo
                    FloatingActionButton(
                        onClick = {
                            modoMapa = if (modoMapa == "marcadores") "calor" else "marcadores"
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        containerColor = GreenPrimary
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = White)
                    }

                    // Leyenda
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (modoMapa == "marcadores") "Mapa de Marcadores" else "Mapa de Calor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LegendItem("Crítica", Color(0xFFE53935))
                                LegendItem("Alta", Color(0xFFFF9800))
                                LegendItem("Media", Color(0xFFFFC107))
                                LegendItem("Baja", Color(0xFF2196F3))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${data.puntos.size} reportes con ubicación",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun crearMapView(context: Context): MapView {
    val mapView = MapView(context)
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setMultiTouchControls(true)
    mapView.controller.setZoom(5.0)
    mapView.controller.setCenter(GeoPoint(23.6345, -102.5528)) // Centro de México
    return mapView
}

private fun agregarMarcadores(mapView: MapView, puntos: List<HeatmapPoint>, context: Context) {
    puntos.forEach { punto ->
        val marker = Marker(mapView)
        marker.position = GeoPoint(punto.latitud, punto.longitud)
        marker.title = "Reporte #${punto.reporte_id}"
        marker.snippet = "Prioridad: ${
            when (punto.prioridad_id) {
                4 -> "Crítica"
                3 -> "Alta"
                2 -> "Media"
                else -> "Baja"
            }
        }"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
    }
}

private fun agregarCalor(mapView: MapView, puntos: List<HeatmapPoint>) {
    val overlay = object : Overlay() {
        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow) return
            puntos.forEach { punto ->
                val geoPoint = GeoPoint(punto.latitud, punto.longitud)
                val screenPoint = mapView.projection.toPixels(geoPoint, null)

                val color = when (punto.prioridad_id) {
                    4 -> android.graphics.Color.argb(120, 229, 57, 53)   // Crítica - rojo
                    3 -> android.graphics.Color.argb(120, 255, 152, 0)   // Alta - naranja
                    2 -> android.graphics.Color.argb(120, 255, 193, 7)   // Media - amarillo
                    else -> android.graphics.Color.argb(120, 33, 150, 243) // Baja - azul
                }

                val paint = Paint().apply {
                    this.color = color
                    style = Paint.Style.FILL
                }

                val radio = when (punto.prioridad_id) {
                    4 -> 80f
                    3 -> 65f
                    2 -> 50f
                    else -> 35f
                }

                canvas.drawCircle(
                    screenPoint.x.toFloat(),
                    screenPoint.y.toFloat(),
                    radio,
                    paint
                )
            }
        }
    }
    mapView.overlays.add(overlay)
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}
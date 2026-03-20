package com.example.petdata.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    focusLat: Double? = null,
    focusLng: Double? = null,
    onNavigate: (route: String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel(
        factory = MapViewModel.Factory(tokenManager)
    )
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()

    var modoMapa by remember { mutableStateOf("marcadores") }

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

                    key(data.puntos.size, modoMapa, focusLat, focusLng) {
                        AndroidView(
                            factory = { ctx -> crearMapView(ctx) },
                            update = { mapView ->
                                mapView.overlays.clear()
                                if (modoMapa == "marcadores") {
                                    agregarMarcadores(mapView, data.puntos, context, onNavigate)
                                } else {
                                    agregarCalor(mapView, data.puntos)
                                }

                                if (focusLat != null && focusLng != null) {
                                    mapView.controller.setZoom(17.0)
                                    mapView.controller.setCenter(GeoPoint(focusLat, focusLng))
                                    val marker = Marker(mapView).apply {
                                        position = GeoPoint(focusLat, focusLng)
                                        title = "Ubicación del reporte"
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }
                                    mapView.overlays.add(marker)
                                } else if (data.puntos.isNotEmpty()) {
                                    val centro = GeoPoint(
                                        data.puntos.map { it.latitud }.average(),
                                        data.puntos.map { it.longitud }.average()
                                    )
                                    mapView.controller.setZoom(13.0)
                                    mapView.controller.setCenter(centro)
                                }
                                mapView.invalidate()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

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
                            if (modoMapa != "marcadores") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    LegendItem("Baja (0–1)", Color(0xFF2196F3))
                                    LegendItem("Media (2–4)", Color(0xFFFFC107))
                                    LegendItem("Alta (5–9)", Color(0xFFFF9800))
                                    LegendItem("Crítica (10+)", Color(0xFFE53935))
                                }
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
    mapView.controller.setCenter(GeoPoint(23.6345, -102.5528))
    return mapView
}

private fun agregarMarcadores(
    mapView: MapView,
    puntos: List<HeatmapPoint>,
    context: Context,
    onNavigate: (String) -> Unit
) {
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
        } — Toca para ver detalles"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.setOnMarkerClickListener { clickedMarker, _ ->
            if (clickedMarker.isInfoWindowShown) {
                onNavigate("report_detail/${punto.reporte_id}")
                true
            } else {
                clickedMarker.showInfoWindow()
                true
            }
        }
        mapView.overlays.add(marker)
    }
}

private fun agregarCalor(mapView: MapView, puntos: List<HeatmapPoint>) {
    val overlay = object : Overlay() {
        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow) return

            // Contar vecinos reales dentro de ~1km para cada punto
            // Se excluye el punto mismo (por eso se resta 1)
            val vecinosPorPunto = puntos.map { punto ->
                puntos.count { otro ->
                    val dlat = punto.latitud - otro.latitud
                    val dlng = punto.longitud - otro.longitud
                    Math.sqrt(dlat * dlat + dlng * dlng) < 0.01  // ~1km de radio
                } - 1  // no contarse a sí mismo
            }

            puntos.forEachIndexed { index, punto ->
                val geoPoint = GeoPoint(punto.latitud, punto.longitud)
                val screenPoint = mapView.projection.toPixels(geoPoint, null)
                val vecinos = vecinosPorPunto[index]

                // ── Umbrales absolutos ──────────────────────────────────────
                // Baja:    0–1 vecinos  → azul    (punto aislado o casi solo)
                // Media:   2–4 vecinos  → amarillo (pequeño grupo)
                // Alta:    5–9 vecinos  → naranja  (zona activa)
                // Crítica: 10+ vecinos  → rojo     (zona de alta concentración)
                //
                // Si tu ciudad tiene muchos reportes y todo queda azul,
                // baja los umbrales (ej. 1 / 3 / 6).
                // Si todo queda rojo, súbelos (ej. 5 / 15 / 30).
                // ────────────────────────────────────────────────────────────
                val color = when {
                    vecinos >= 10 -> android.graphics.Color.argb(180, 220, 30,  30)   // rojo
                    vecinos >= 5  -> android.graphics.Color.argb(160, 255, 120,  0)   // naranja
                    vecinos >= 2  -> android.graphics.Color.argb(140, 255, 220,  0)   // amarillo
                    else          -> android.graphics.Color.argb(120,   0, 150, 255)  // azul
                }

                val radio = when {
                    vecinos >= 10 -> 140f
                    vecinos >= 5  -> 120f
                    vecinos >= 2  -> 100f
                    else          ->  80f
                }

                val colorTransparente = android.graphics.Color.argb(
                    0,
                    android.graphics.Color.red(color),
                    android.graphics.Color.green(color),
                    android.graphics.Color.blue(color)
                )

                val gradient = android.graphics.RadialGradient(
                    screenPoint.x.toFloat(),
                    screenPoint.y.toFloat(),
                    radio,
                    intArrayOf(color, colorTransparente),
                    floatArrayOf(0f, 1f),
                    android.graphics.Shader.TileMode.CLAMP
                )

                val paint = Paint().apply {
                    shader = gradient
                    style = Paint.Style.FILL
                    isAntiAlias = true
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
package com.example.petdata.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.petdata.data.local.TokenManager
import com.example.petdata.ui.components.BottomNavigationBar
import com.example.petdata.ui.components.RescateTopBar
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.ReportFormState
import com.example.petdata.ui.viemodel.ReportFormViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices

private const val MAX_IMAGENES = 3

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReportFormScreen(
    mode: String = "crear",
    rolId: Int = 1,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit = {},
    onNavigate: (route: String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ReportFormViewModel = viewModel(
        factory = ReportFormViewModel.Factory(tokenManager)
    )
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    // Form fields
    var estadoAnimalId     by remember { mutableStateOf(1) }
    var estadoAnimalNombre by remember { mutableStateOf("Herido") }
    var prioridadId        by remember { mutableStateOf(1) }
    var prioridadNombre    by remember { mutableStateOf("Baja") }
    var descripcion        by remember { mutableStateOf("") }
    var contacto           by remember { mutableStateOf("") }
    var latitud            by remember { mutableStateOf<Double?>(null) }
    var longitud           by remember { mutableStateOf<Double?>(null) }
    var expandedEstado     by remember { mutableStateOf(false) }
    var expandedPrioridad  by remember { mutableStateOf(false) }
    var showErrorDialog    by remember { mutableStateOf(false) }
    var errorMessage       by remember { mutableStateOf("") }
    var precisionMetros    by remember { mutableStateOf<Double?>(null) }
    var tipoAnimalId       by remember { mutableStateOf(1) }
    var tipoAnimalNombre   by remember { mutableStateOf("Perro") }
    var expandedTipo       by remember { mutableStateOf(false) }

    // ── Lista de imágenes (máximo 3) ───────────────────────────────────────
    var imagenesUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val puedeAgregarMas = imagenesUris.size < MAX_IMAGENES
    // ──────────────────────────────────────────────────────────────────────

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermission   = rememberPermissionState(Manifest.permission.CAMERA)

    // Galería — agrega la imagen seleccionada a la lista
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (imagenesUris.size < MAX_IMAGENES) {
                imagenesUris = imagenesUris + it
            }
        }
    }

    // Cámara — convierte el bitmap y lo agrega a la lista
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            if (imagenesUris.size < MAX_IMAGENES) {
                val index = imagenesUris.size
                val file = java.io.File(context.cacheDir, "camara_temp_$index.jpg")
                file.outputStream().use { out ->
                    it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                imagenesUris = imagenesUris + Uri.fromFile(file)
            }
        }
    }

    LaunchedEffect(formState) {
        when (formState) {
            is ReportFormState.Success -> {
                onNavigateBack()
                viewModel.resetState()
            }
            is ReportFormState.Error -> {
                errorMessage = (formState as ReportFormState.Error).message
                showErrorDialog = true
                viewModel.resetState()
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
        topBar = { RescateTopBar() },
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = 1,
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
            // Header
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Reportar un Caso", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ayúdanos a localizar animales que necesitan ayuda",
                        fontSize = 13.sp,
                        color = White.copy(alpha = 0.9f)
                    )
                }
            }

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Tipo de Animal
                    Text("Tipo de Animal *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = tipoAnimalNombre,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandedTipo = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor   = GreenPrimary,
                                disabledBorderColor  = Color(0xFFE0E0E0),
                                disabledTextColor    = TextPrimary
                            ),
                            enabled = false
                        )
                        DropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                            listOf(1 to "Perro", 2 to "Gato", 3 to "Otro").forEach { (id, nombre) ->
                                DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                    tipoAnimalId     = id
                                    tipoAnimalNombre = nombre
                                    expandedTipo     = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estado del animal
                    Text("Estado del Animal *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = estadoAnimalNombre,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandedEstado = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor   = GreenPrimary,
                                disabledBorderColor  = Color(0xFFE0E0E0),
                                disabledTextColor    = TextPrimary
                            ),
                            enabled = false
                        )
                        DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                            listOf(1 to "Herido", 2 to "Desnutrido", 3 to "Abandonado", 4 to "Grave").forEach { (id, nombre) ->
                                DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                    estadoAnimalId     = id
                                    estadoAnimalNombre = nombre
                                    expandedEstado     = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prioridad
                    Text("Prioridad *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = prioridadNombre,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandedPrioridad = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor   = GreenPrimary,
                                disabledBorderColor  = Color(0xFFE0E0E0),
                                disabledTextColor    = TextPrimary
                            ),
                            enabled = false
                        )
                        DropdownMenu(expanded = expandedPrioridad, onDismissRequest = { expandedPrioridad = false }) {
                            listOf(1 to "Baja", 2 to "Media", 3 to "Alta", 4 to "Crítica").forEach { (id, nombre) ->
                                DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                    prioridadId     = id
                                    prioridadNombre = nombre
                                    expandedPrioridad = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descripción
                    Text("Descripción *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Detalles sobre el animal...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor   = GreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contacto opcional
                    Text("Contacto (Opcional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contacto,
                        onValueChange = { contacto = it },
                        placeholder = { Text("Teléfono o correo", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor   = GreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ubicación GPS
                    Text("Ubicación *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val ubicacionObtenida = latitud != null
                    var ubicacionCargando by remember { mutableStateOf(false) }
                    var ubicacionError    by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            if (locationPermission.status.isGranted) {
                                ubicacionCargando = true
                                ubicacionError = false
                                val client = LocationServices.getFusedLocationProviderClient(context)

                                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                    1000L
                                )
                                    .setMaxUpdates(1)
                                    .setWaitForAccurateLocation(true)
                                    .setMinUpdateIntervalMillis(500L)
                                    .setMaxUpdateDelayMillis(10_000L)
                                    .build()

                                val callback = object : com.google.android.gms.location.LocationCallback() {
                                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                        client.removeLocationUpdates(this)
                                        val location = result.lastLocation
                                        if (location != null) {
                                            if (location.accuracy <= 50f) {
                                                latitud = location.latitude
                                                longitud = location.longitude
                                                precisionMetros = location.accuracy.toDouble()
                                                ubicacionCargando = false
                                            } else {
                                                ubicacionError = true
                                                ubicacionCargando = false
                                            }
                                        } else {
                                            ubicacionError = true
                                            ubicacionCargando = false
                                        }
                                    }
                                }

                                try {
                                    client.requestLocationUpdates(
                                        locationRequest,
                                        callback,
                                        android.os.Looper.getMainLooper()
                                    )
                                    android.os.Handler(android.os.Looper.getMainLooper())
                                        .postDelayed({
                                            client.removeLocationUpdates(callback)
                                            if (ubicacionCargando) {
                                                ubicacionCargando = false
                                                ubicacionError = true
                                            }
                                        }, 15_000L)
                                } catch (e: SecurityException) {
                                    ubicacionCargando = false
                                    ubicacionError = true
                                }
                            } else {
                                locationPermission.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                ubicacionError    -> Color(0xFFFFEBEE)
                                ubicacionObtenida -> Color(0xFFE8F5E9)
                                else              -> GreenPrimary
                            },
                            contentColor = when {
                                ubicacionError    -> Color(0xFFE53935)
                                ubicacionObtenida -> GreenPrimary
                                else              -> White
                            }
                        )
                    ) {
                        if (ubicacionCargando) {
                            CircularProgressIndicator(
                                color = GreenPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Obteniendo ubicación...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(
                                imageVector = when {
                                    ubicacionError    -> Icons.Default.LocationOff
                                    ubicacionObtenida -> Icons.Default.CheckCircle
                                    else              -> Icons.Default.MyLocation
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    ubicacionError    -> "No se pudo obtener — Reintentar"
                                    ubicacionObtenida -> "✓ Ubicación obtenida"
                                    else              -> "Obtener ubicación actual"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }  // ← solo una llave de cierre, nada más después

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Sección de imágenes ────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Fotografías *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${imagenesUris.size}/$MAX_IMAGENES",
                            fontSize = 12.sp,
                            color = if (imagenesUris.size == MAX_IMAGENES) GreenPrimary else TextSecondary,
                            fontWeight = if (imagenesUris.size == MAX_IMAGENES) FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista horizontal con scroll
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Miniaturas de imágenes ya seleccionadas
                        items(imagenesUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Botón eliminar imagen
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .clickable {
                                            imagenesUris = imagenesUris - uri
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar imagen",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Botones agregar (solo si no se llegó al máximo)
                        if (puedeAgregarMas) {
                            item {
                                // Galería
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFAFAFA))
                                        .clickable { galleryLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Photo,
                                            null,
                                            tint = Color(0xFFBDBDBD),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Galería", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                            item {
                                // Cámara
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFAFAFA))
                                        .clickable {
                                            if (cameraPermission.status.isGranted) {
                                                cameraLauncher.launch(null)
                                            } else {
                                                cameraPermission.launchPermissionRequest()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            null,
                                            tint = Color(0xFFBDBDBD),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Cámara", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    // Mensaje cuando se llega al máximo
                    if (imagenesUris.size == MAX_IMAGENES) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Máximo de imágenes alcanzado",
                            fontSize = 12.sp,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // ──────────────────────────────────────────────────────

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón enviar — requiere al menos 1 imagen
                    val formValido = descripcion.isNotBlank() &&
                            latitud != null &&
                            imagenesUris.isNotEmpty()

                    Button(
                        onClick = {
                            if (formValido) {
                                viewModel.crearReporte(
                                    context          = context,
                                    estadoAnimalId   = estadoAnimalId,
                                    tipoAnimalId     = tipoAnimalId,
                                    prioridadId      = prioridadId,
                                    descripcion      = descripcion,
                                    latitud          = latitud!!,
                                    longitud         = longitud!!,
                                    precisionMetros  = precisionMetros,
                                    contactoOpcional = contacto.ifBlank { null },
                                    imageUris        = imagenesUris
                                )
                            }
                        },
                        enabled = formValido && formState !is ReportFormState.Loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (formState is ReportFormState.Loading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar Reporte", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
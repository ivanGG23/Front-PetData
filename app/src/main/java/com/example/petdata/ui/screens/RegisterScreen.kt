package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viemodel.RegisterState
import com.example.petdata.ui.viemodel.RegisterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRescuer by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showPendingDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                if (isRescuer) showPendingDialog = true
                else showSuccessDialog = true
            }
            is RegisterState.Error -> {
                errorMessage = (registerState as RegisterState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    // DatePicker state
    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            birthDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = GreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = GreenPrimary,
                    todayDateBorderColor = GreenPrimary
                )
            )
        }
    }

    // Dialog cuenta creada (ciudadano)
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Text("✅", fontSize = 32.sp) },
            title = {
                Text(
                    text = "¡Cuenta creada!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesión.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Iniciar Sesión")
                }
            }
        )
    }

    // Dialog cuenta pendiente (rescatista)
    if (showPendingDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Text("⏳", fontSize = 32.sp) },
            title = {
                Text(
                    text = "Solicitud enviada",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tu solicitud como rescatista está siendo revisada. Te notificaremos cuando sea aprobada.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPendingDialog = false
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Dialog error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = { Text("❌", fontSize = 32.sp) },
            title = {
                Text(
                    text = "Error al registrarse",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Intentar de nuevo")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenPrimary, Color(0xFFE8F5E9))
                )
            )
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Text("❤️", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "RescateAnimal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )

            Text(
                text = "Únete a nuestra comunidad",
                fontSize = 13.sp,
                color = White.copy(alpha = 0.9f)
            )
        }

        // Form card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Regístrate para empezar a ayudar",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Nombre
                Text(
                    text = "Nombre",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Juan", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Apellido
                Text(
                    text = "Apellido",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Pérez", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Teléfono
                Text(
                    text = "Teléfono (opcional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("9671234567", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fecha de nacimiento
                Text(
                    text = "Fecha de nacimiento (opcional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar fecha", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (birthDate.isNotEmpty()) {
                            IconButton(onClick = { birthDate = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary,
                        disabledBorderColor = Color(0xFFE0E0E0),
                        disabledTextColor = TextPrimary,
                        disabledPlaceholderColor = TextSecondary,
                        disabledLeadingIconColor = TextSecondary,
                        disabledTrailingIconColor = TextSecondary
                    ),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Text(
                    text = "Correo Electrónico",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("tu@email.com", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contraseña
                Text(
                    text = "Contraseña",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirmar Contraseña
                Text(
                    text = "Confirmar Contraseña",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("••••••••", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Rescuer checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF9E6))
                        .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isRescuer,
                        onCheckedChange = { isRescuer = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFC107))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Soy Rescatista",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF795548)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Accede a funciones avanzadas para gestionar rescates y crear casos oficiales",
                            fontSize = 12.sp,
                            color = Color(0xFF795548).copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Validación de contraseñas
                val passwordsMatch = password == confirmPassword
                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Loading state
                if (registerState is RegisterState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        color = GreenPrimary
                    )
                }

                // Botón crear cuenta
                Button(
                    onClick = {
                        if (name.isNotEmpty() && lastName.isNotEmpty() &&
                            email.isNotEmpty() && password.isNotEmpty() &&
                            passwordsMatch
                        ) {
                            viewModel.register(
                                nombre = name,
                                apellido = lastName,
                                correo = email,
                                contrasena = password,
                                telefono = phone,
                                fechaNacimiento = birthDate,
                                isRescuer = isRescuer
                            )
                        }
                    },
                    enabled = registerState !is RegisterState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Crear Cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Inicia sesión aquí",
                        fontSize = 14.sp,
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
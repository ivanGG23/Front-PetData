package com.example.petdata.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
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
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit   // ← NUEVO parámetro
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

    // ── NUEVO: estado del checkbox de términos ─────────────────────────────
    var termsAccepted by remember { mutableStateOf(false) }
    var termsError by remember { mutableStateOf(false) }
    // ──────────────────────────────────────────────────────────────────────

    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showPendingDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    fun validar(): Boolean {
        var valido = true
        val emailTrimmed = email.trim()

        nameError = if (name.isBlank()) { valido = false; "El nombre es requerido" } else null
        lastNameError = if (lastName.isBlank()) { valido = false; "El apellido es requerido" } else null
        emailError = when {
            emailTrimmed.isBlank() -> { valido = false; "El correo es requerido" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches() -> {
                valido = false; "Ingresa un correo válido"
            }
            !emailTrimmed.contains("@") || emailTrimmed.substringAfterLast(".").length < 2 -> {
                valido = false; "Ingresa un correo válido"
            }
            else -> {
                val dominiosPermitidos = listOf(
                    "gmail.com", "hotmail.com", "outlook.com",
                    "yahoo.com", "icloud.com", "live.com",
                    "hotmail.es", "outlook.es", "yahoo.es"
                )
                val dominio = emailTrimmed.substringAfter("@").lowercase()
                if (dominiosPermitidos.none { emailTrimmed.lowercase().endsWith("@$it") }) {
                    valido = false; "Solo se permiten correos de Gmail, Hotmail, Outlook, Yahoo o iCloud"
                } else null
            }
        }
        passwordError = when {
            password.isBlank() -> { valido = false; "La contraseña es requerida" }
            password.length < 8 -> { valido = false; "Mínimo 8 caracteres" }
            else -> null
        }
        confirmPasswordError = when {
            confirmPassword.isBlank() -> { valido = false; "Confirma tu contraseña" }
            confirmPassword != password -> { valido = false; "Las contraseñas no coinciden" }
            else -> null
        }

        // ── NUEVO: validar que aceptó los términos ─────────────────────────
        if (!termsAccepted) {
            termsError = true
            valido = false
        }
        // ──────────────────────────────────────────────────────────────────

        return valido
    }

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

    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        birthDate = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Aceptar", color = GreenPrimary) }
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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Text("✅", fontSize = 32.sp) },
            title = { Text("¡Cuenta creada!", fontWeight = FontWeight.Bold) },
            text = { Text("Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesión.") },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false; onNavigateToLogin() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text("Iniciar Sesión") }
            }
        )
    }

    if (showPendingDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Text("⏳", fontSize = 32.sp) },
            title = { Text("Solicitud enviada", fontWeight = FontWeight.Bold) },
            text = { Text("Tu solicitud como rescatista está siendo revisada. Te notificaremos cuando sea aprobada.") },
            confirmButton = {
                Button(
                    onClick = { showPendingDialog = false; onNavigateToLogin() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text("Entendido") }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = { Text("❌", fontSize = 32.sp) },
            title = { Text("Error al registrarse", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text("Intentar de nuevo") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(GreenPrimary, Color(0xFFE8F5E9))))
    ) {
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
            ) { Text("❤️", fontSize = 40.sp) }
            Spacer(modifier = Modifier.height(12.dp))
            Text("RescateAnimal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Únete a nuestra comunidad", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text("Crear Cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "Regístrate para empezar a ayudar",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // ── Nombre ──
                Text("Nombre *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; if (nameError != null) nameError = null },
                    placeholder = { Text("Ej. Juan", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, null,
                            tint = if (nameError != null) Color.Red else TextSecondary)
                    },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) Text(nameError!!, color = Color.Red, fontSize = 12.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary,
                        errorBorderColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Apellido ──
                Text("Apellido *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it; if (lastNameError != null) lastNameError = null },
                    placeholder = { Text("Ej. Pérez", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, null,
                            tint = if (lastNameError != null) Color.Red else TextSecondary)
                    },
                    isError = lastNameError != null,
                    supportingText = {
                        if (lastNameError != null) Text(lastNameError!!, color = Color.Red, fontSize = 12.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary,
                        errorBorderColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Teléfono (opcional) ──
                Text("Teléfono (opcional)", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.all { c -> c.isDigit() }) phone = it },
                    placeholder = { Text("Ej. 9671234567", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = TextSecondary) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Fecha de nacimiento (opcional) ──
                Text("Fecha de nacimiento (opcional)", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar fecha", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = TextSecondary) },
                    trailingIcon = {
                        if (birthDate.isNotEmpty()) {
                            IconButton(onClick = { birthDate = "" }) {
                                Icon(Icons.Default.Close, null, tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
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

                // ── Correo ──
                Text("Correo Electrónico *", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; if (emailError != null) emailError = null },
                    placeholder = { Text("Ej. correo@gmail.com", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null,
                            tint = if (emailError != null) Color.Red else TextSecondary)
                    },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) Text(emailError!!, color = Color.Red, fontSize = 12.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary,
                        errorBorderColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Contraseña ──
                Text("Contraseña *", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; if (passwordError != null) passwordError = null },
                    placeholder = { Text("Mínimo 8 caracteres", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null,
                            tint = if (passwordError != null) Color.Red else TextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null, tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) Text(passwordError!!, color = Color.Red, fontSize = 12.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary,
                        errorBorderColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Confirmar contraseña ──
                Text("Confirmar Contraseña *", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; if (confirmPasswordError != null) confirmPasswordError = null },
                    placeholder = { Text("Repite tu contraseña", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null,
                            tint = if (confirmPasswordError != null) Color.Red else TextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null, tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    isError = confirmPasswordError != null,
                    supportingText = {
                        if (confirmPasswordError != null) Text(confirmPasswordError!!, color = Color.Red, fontSize = 12.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = GreenPrimary,
                        errorBorderColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── NUEVO: Checkbox de términos y condiciones ──────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = {
                            termsAccepted = it
                            if (it) termsError = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GreenPrimary,
                            uncheckedColor = if (termsError) Color.Red else Color(0xFFBDBDBD)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("He leído y acepto los ")
                                withStyle(SpanStyle(color = GreenPrimary, fontWeight = FontWeight.SemiBold)) {
                                    append("Términos y Condiciones")
                                }
                                append(" de PetData, incluyendo el uso de mi ubicación GPS.")
                            },
                            fontSize = 13.sp,
                            color = if (termsError) Color.Red else TextSecondary,
                            lineHeight = 19.sp,
                            modifier = Modifier.clickable { onNavigateToTerms() }
                        )
                        if (termsError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Debes aceptar los términos para continuar",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }
                    }
                }
                // ── Enlace separado para leer los términos ─────────────────────────────
                TextButton(
                    onClick = onNavigateToTerms,
                    modifier = Modifier.padding(start = 36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Leer términos completos →",
                        fontSize = 12.sp,
                        color = GreenPrimary
                    )
                }
                // ──────────────────────────────────────────────────────────────────────

                Spacer(modifier = Modifier.height(8.dp))

                if (registerState is RegisterState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        color = GreenPrimary
                    )
                }

                Button(
                    onClick = {
                        if (validar()) {
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
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear Cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("¿Ya tienes cuenta? ", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        "Inicia sesión aquí",
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
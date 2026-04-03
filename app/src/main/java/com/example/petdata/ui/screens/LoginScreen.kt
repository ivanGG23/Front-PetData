package com.example.petdata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.petdata.MainActivity
import com.example.petdata.ui.theme.*
import com.example.petdata.ui.viewmodel.LoginState
import com.example.petdata.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (rolId: Int) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToTerms: () -> Unit,   // ← NUEVO parámetro
    onGoogleSignIn: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // ── NUEVO: modal de T&C antes de Google Sign-In ────────────────────────
    var showGoogleTermsDialog by remember { mutableStateOf(false) }
    var googleTermsAccepted by remember { mutableStateOf(false) }
    // ──────────────────────────────────────────────────────────────────────

    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val googleRolId by MainActivity.googleAuthResult.collectAsStateWithLifecycle()

    fun validar(): Boolean {
        var valido = true
        val emailTrimmed = email.trim()
        val passwordTrimmed = password.trim()

        emailError = when {
            emailTrimmed.isBlank() -> { valido = false; "El correo es requerido" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches() -> {
                valido = false; "Ingresa un correo válido"
            }
            else -> null
        }
        passwordError = if (passwordTrimmed.isBlank()) { valido = false; "La contraseña es requerida" } else null
        return valido
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess((loginState as LoginState.Success).rolId)
        }
    }

    // ── NUEVO: Modal de T&C para Google Sign-In ────────────────────────────
    if (showGoogleTermsDialog) {
        AlertDialog(
            onDismissRequest = {
                showGoogleTermsDialog = false
                googleTermsAccepted = false
            },
            title = {
                Text(
                    text = "Antes de continuar con Google",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Al continuar, PetData creará una cuenta vinculada a tu cuenta de Google y recopilará los siguientes datos:",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Nombre y correo de tu cuenta Google", fontSize = 13.sp, color = TextSecondary)
                    Text("• Ubicación GPS al crear reportes", fontSize = 13.sp, color = TextSecondary)
                    Text("• Imágenes y comentarios que subas", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkbox dentro del dialog
                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(
                            checked = googleTermsAccepted,
                            onCheckedChange = { googleTermsAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Acepto los ")
                                    withStyle(SpanStyle(color = GreenPrimary, fontWeight = FontWeight.SemiBold)) {
                                        append("Términos y Condiciones")
                                    }
                                    append(" de PetData")
                                },
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.clickable { onNavigateToTerms() }
                            )
                        }
                    }

                    // Enlace para leer los términos completos
                    TextButton(
                        onClick = onNavigateToTerms,
                        contentPadding = PaddingValues(start = 40.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                    ) {
                        Text(
                            text = "Leer términos completos →",
                            fontSize = 12.sp,
                            color = GreenPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (googleTermsAccepted) {
                            showGoogleTermsDialog = false
                            googleTermsAccepted = false
                            onGoogleSignIn()   // ← Solo se lanza Google aquí
                        }
                    },
                    enabled = googleTermsAccepted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        disabledContainerColor = Color(0xFFBDBDBD)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Continuar con Google", fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGoogleTermsDialog = false
                        googleTermsAccepted = false
                    }
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = White
        )
    }
    // ──────────────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenPrimary, Color(0xFFE8F5E9))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con logo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RescateAnimal",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Juntos salvamos vidas",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            // Formulario
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
                        text = "Accede para reportar y ayudar",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Text(
                        text = "Contraseña",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; if (passwordError != null) passwordError = null },
                        placeholder = { Text("Tu contraseña", fontSize = 14.sp) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error
                    if (loginState is LoginState.Error) {
                        Text(
                            text = (loginState as LoginState.Error).message,
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    // Botón login
                    Button(
                        onClick = {
                            if (validar()) {
                                viewModel.login(email.trim(), password.trim())
                            }
                        },
                        enabled = loginState !is LoginState.Loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Iniciar Sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divisor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(text = "  o  ", fontSize = 13.sp, color = TextSecondary)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Botón Google — ahora abre el modal primero ─────────────────────
                    OutlinedButton(
                        onClick = {
                            showGoogleTermsDialog = true   // ← Abre el modal, NO llama a onGoogleSignIn directamente
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continuar con Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    // ──────────────────────────────────────────────────────────────────

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(text = "¿No tienes cuenta? ", fontSize = 14.sp, color = TextSecondary)
                        Text(
                            text = "Regístrate aquí",
                            fontSize = 14.sp,
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
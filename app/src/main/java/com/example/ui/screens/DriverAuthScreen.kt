package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.viewmodel.DriverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAuthScreen(
    viewModel: DriverViewModel,
    modifier: Modifier = Modifier
) {
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isRegisteredSuccess by viewModel.isRegisteredSuccess.collectAsStateWithLifecycle()

    var isLoginMode by remember { mutableStateOf(true) }
    
    // Form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }

    // Clear messages when mode changes
    LaunchedEffect(isLoginMode) {
        viewModel.resetAuthError()
        viewModel.resetRegistrationSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF2F6),
                        Color(0xFFDCE4EC)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .size(96.dp)
                    .testTag("auth_logo")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo_foreground_1781815070116),
                    contentDescription = "Shri Krishna logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Shri Krishna",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E3A8A),
                textAlign = TextAlign.Center
            )
            Text(
                text = "DRIVER COMPANION PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLoginMode) "Log In to Account" else "Create Driver Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (isLoginMode) "Enter credentials below to enter dashboard" else "Register path for first-time premium partners",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Success Message banner
                    if (isRegisteredSuccess) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Registration Successful! Log in below.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    // Error Message banner
                    if (authError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }

                    // EMAIL input (always needed)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.resetAuthError() },
                        label = { Text("Email Address") },
                        placeholder = { Text("driver@shrikrishna.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = Color(0xFF1E3A8A)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // PASSWORD input (always needed)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.resetAuthError() },
                        label = { Text("Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = Color(0xFF1E3A8A)) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = "Toggle Password Visibility")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Registration ONLY fields
                    if (!isLoginMode) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // FULL NAME
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("Alex Carter") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon", tint = Color(0xFF1E3A8A)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // VEHICLE DESCRIPTION
                        OutlinedTextField(
                            value = vehicleModel,
                            onValueChange = { vehicleModel = it },
                            label = { Text("Vehicle Model & Color") },
                            placeholder = { Text("Toyota Prius Prime (Silver)") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Vehicle Icon", tint = Color(0xFF1E3A8A)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_vehicle_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // LICENSE PLATE
                        OutlinedTextField(
                            value = licensePlate,
                            onValueChange = { licensePlate = it },
                            label = { Text("License Plate Number") },
                            placeholder = { Text("DRV-137X") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "License Icon", tint = Color(0xFF1E3A8A)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_plate_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACTION BUTTON
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.signUp(
                                    email = email,
                                    passwordKey = password,
                                    name = name,
                                    vehicleModel = vehicleModel,
                                    licensePlate = licensePlate,
                                    onNavigateLogin = { isLoginMode = true }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_action_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) "Log In Now" else "Sign Up / Register",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // MODE TOGGLE LINK
                    Row(
                        modifier = Modifier.clickable { isLoginMode = !isLoginMode },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLoginMode) "First time here? " else "Already registered? ",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = if (isLoginMode) "Create an Account" else "Log In Instead",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Demo Shortcut Section (For quick evaluation testing)
            if (isLoginMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ QUICK DEVELOPER PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A default test account is pre-populated in your Room database for convenient testing.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.md)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                email = "alex@shrikrishna.com"
                                password = "alex123"
                                viewModel.login(email, password)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Auto-Fill & Log In",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Float helper for padding extension
private val Int.md get() = this.dp

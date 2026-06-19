package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.DriverViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAuthScreen(
    viewModel: DriverViewModel,
    modifier: Modifier = Modifier
) {
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isRegisteredSuccess by viewModel.isRegisteredSuccess.collectAsStateWithLifecycle()

    var isLoginMode by remember { mutableStateOf(true) }
    var loginMethod by remember { mutableStateOf(0) } // 0: Mobile OTP, 1: Driver ID, 2: Email

    // Form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }
    var securityPin by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var isOtpRequested by remember { mutableStateOf(false) }

    // Clear messages when mode changes
    LaunchedEffect(isLoginMode, loginMethod) {
        viewModel.resetAuthError()
        viewModel.resetRegistrationSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumBlack), // Modern Off-white backdrop
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
            // App Branding Header (No Yellow, No Black)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MetallicGold) // Indigo Icon box
                    .testTag("auth_logo"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "UNOXIA Logo",
                    tint = ObsidianBlack, // White Icon
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "UNOXIA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MetallicGold, // Deep Indigo
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "DRIVER COMPANION PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted, // Slate/Silver Muted secondary
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Auth Card Form (Pristine White Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack), // Clean white surface
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLoginMode) "Log In to Account" else "Request Registration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText // High readability dark text
                    )
                    Text(
                        text = if (isLoginMode) "Access premium dispatch & welfare systems" else "Register path for premium fleet partners",
                        fontSize = 12.sp,
                        color = GrayMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Method Tabs for Login Mode
                    if (isLoginMode) {
                        TabRow(
                            selectedTabIndex = loginMethod,
                            containerColor = DarkSlate, // Soft container background
                            contentColor = MetallicGold, // Indigo selector
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            indicator = {}
                        ) {
                            Tab(
                                selected = loginMethod == 0,
                                onClick = { loginMethod = 0 },
                                text = { Text("Mobile OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (loginMethod == 0) MetallicGold else GrayMuted) }
                            )
                            Tab(
                                selected = loginMethod == 1,
                                onClick = { loginMethod = 1 },
                                text = { Text("Driver ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (loginMethod == 1) MetallicGold else GrayMuted) }
                            )
                            Tab(
                                selected = loginMethod == 2,
                                onClick = { loginMethod = 2 },
                                text = { Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (loginMethod == 2) MetallicGold else GrayMuted) }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Success Message banner
                    if (isRegisteredSuccess) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)) // Soft green background
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF137333),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Registered Successfully! Log in below.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF137333)
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)) // Soft red background
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFC5221F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFC5221F)
                                )
                            }
                        }
                    }

                    if (isLoginMode) {
                        when (loginMethod) {
                            0 -> { // Mobile OTP Dynamic View
                                OutlinedTextField(
                                    value = mobileNumber,
                                    onValueChange = { mobileNumber = it },
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("98765 43012") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon", tint = MetallicGold) },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_mobile_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MetallicGold,
                                        focusedLabelColor = MetallicGold,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = LightText,
                                        unfocusedTextColor = LightText,
                                        focusedPlaceholderColor = GrayMuted,
                                        unfocusedPlaceholderColor = GrayMuted
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                
                                if (isOtpRequested) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { otpCode = it },
                                        label = { Text("Enter 4-Digit OTP") },
                                        placeholder = { Text("1234") },
                                        leadingIcon = { Icon(Icons.Default.Verified, contentDescription = "OTP Icon", tint = MetallicGold) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MetallicGold,
                                            focusedLabelColor = MetallicGold,
                                            unfocusedBorderColor = BorderGray,
                                            focusedTextColor = LightText,
                                            unfocusedTextColor = LightText,
                                            focusedPlaceholderColor = GrayMuted,
                                            unfocusedPlaceholderColor = GrayMuted
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                            1 -> { // Driver ID Dynamic View
                                OutlinedTextField(
                                    value = driverId,
                                    onValueChange = { driverId = it },
                                    label = { Text("UNOXIA Driver ID") },
                                    placeholder = { Text("DRV-X90") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Driver Badge", tint = MetallicGold) },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_driver_id_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MetallicGold,
                                        focusedLabelColor = MetallicGold,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = LightText,
                                        unfocusedTextColor = LightText,
                                        focusedPlaceholderColor = GrayMuted,
                                        unfocusedPlaceholderColor = GrayMuted
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = securityPin,
                                    onValueChange = { securityPin = it },
                                    label = { Text("Security Pin") },
                                    placeholder = { Text("••••") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PIN Lock", tint = MetallicGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MetallicGold,
                                        focusedLabelColor = MetallicGold,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = LightText,
                                        unfocusedTextColor = LightText,
                                        focusedPlaceholderColor = GrayMuted,
                                        unfocusedPlaceholderColor = GrayMuted
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = PasswordVisualTransformation()
                                )
                            }
                            2 -> { // Email Dynamic View
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; viewModel.resetAuthError() },
                                    label = { Text("Registered Email") },
                                    placeholder = { Text("driver@unoxia.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = MetallicGold) },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MetallicGold,
                                        focusedLabelColor = MetallicGold,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = LightText,
                                        unfocusedTextColor = LightText,
                                        focusedPlaceholderColor = GrayMuted,
                                        unfocusedPlaceholderColor = GrayMuted
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; viewModel.resetAuthError() },
                                    label = { Text("Secret Password") },
                                    placeholder = { Text("••••••••") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = MetallicGold) },
                                    trailingIcon = {
                                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(icon, contentDescription = "Toggle Password Visibility", tint = GrayMuted)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MetallicGold,
                                        focusedLabelColor = MetallicGold,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = LightText,
                                        unfocusedTextColor = LightText,
                                        focusedPlaceholderColor = GrayMuted,
                                        unfocusedPlaceholderColor = GrayMuted
                                    ),
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )
                            }
                        }
                    } else { // Sign Up Register Panel
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Work Email") },
                            placeholder = { Text("driver@unoxia.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = MetallicGold) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                focusedLabelColor = MetallicGold,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedPlaceholderColor = GrayMuted,
                                unfocusedPlaceholderColor = GrayMuted
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Set Password") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = MetallicGold) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                focusedLabelColor = MetallicGold,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedPlaceholderColor = GrayMuted,
                                unfocusedPlaceholderColor = GrayMuted
                            ),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name (नाम)") },
                            placeholder = { Text("Alex Carter") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = MetallicGold) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                focusedLabelColor = MetallicGold,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedPlaceholderColor = GrayMuted,
                                unfocusedPlaceholderColor = GrayMuted
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = vehicleModel,
                            onValueChange = { vehicleModel = it },
                            label = { Text("Vehicle Spec (गाड़ी का विवरण)") },
                            placeholder = { Text("Toyota Prius Prime") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Car", tint = MetallicGold) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_vehicle_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                focusedLabelColor = MetallicGold,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedPlaceholderColor = GrayMuted,
                                unfocusedPlaceholderColor = GrayMuted
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = licensePlate,
                            onValueChange = { licensePlate = it },
                            label = { Text("License Plate (गाड़ी नंबर)") },
                            placeholder = { Text("DRV-137X") },
                            leadingIcon = { Icon(Icons.Default.CropFree, contentDescription = "Plate", tint = MetallicGold) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_plate_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                focusedLabelColor = MetallicGold,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedPlaceholderColor = GrayMuted,
                                unfocusedPlaceholderColor = GrayMuted
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACTION BUTTON (Beautiful Indigo background, High Contrast Text)
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                if (loginMethod == 0) { // OTP Setup Simulation
                                    if (!isOtpRequested) {
                                        if (mobileNumber.length >= 10) {
                                            isOtpRequested = true
                                        } else {
                                            viewModel.login("", "") // trigger empty check
                                        }
                                    } else {
                                        if (otpCode == "1234" || otpCode.length == 4) {
                                            viewModel.login("alex@shrikrishna.com", "alex123")
                                        } else {
                                            viewModel.login("alex@shrikrishna.com", "wrong")
                                        }
                                    }
                                } else if (loginMethod == 1) { // Driver ID simulation
                                    if (driverId.isNotBlank() && securityPin == "1234") {
                                        viewModel.login("alex@shrikrishna.com", "alex123")
                                    } else {
                                        viewModel.login("alex@shrikrishna.com", "wrongPin")
                                    }
                                } else { // standard email password login
                                    viewModel.login(email, password)
                                }
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
                            .height(52.dp)
                            .testTag("auth_action_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) {
                                if (loginMethod == 0 && !isOtpRequested) "Send Verification OTP" else "Secure Login"
                            } else "Complete Driver Registration",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack // Pure White text
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // MODE TOGGLE LINK
                    Row(
                        modifier = Modifier.clickable { isLoginMode = !isLoginMode },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLoginMode) "First time here? " else "Already registered? ",
                            fontSize = 13.sp,
                            color = GrayMuted
                        )
                        Text(
                            text = if (isLoginMode) "Join UNOXIA Group" else "Log In Instead",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MetallicGold // Beautiful Indigo callout
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Demo Shortcut Section
            if (isLoginMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlate), // Soft container gray
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ UNOXIA SECURE DEMO ACCESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MetallicGold, // Deep Indigo
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ready credentials preloaded in local Room Database.",
                            fontSize = 12.sp,
                            color = GoldMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.login("alex@shrikrishna.com", "alex123")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack), // Soft white
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Auto Payout Profile Auto-Fill & Log In",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MetallicGold // Indigo active text
                            )
                        }
                    }
                }
            }
        }
    }
}

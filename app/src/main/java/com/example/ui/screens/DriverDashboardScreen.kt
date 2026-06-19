package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.example.data.model.Trip
import com.example.ui.viewmodel.ActiveJobState
import com.example.ui.viewmodel.DriverProfile
import com.example.ui.viewmodel.DriverViewModel
import com.example.ui.viewmodel.JobRequest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import com.example.ui.theme.*

private data class NavDetailsUi(
    val job: JobRequest?,
    val headerTitle: String,
    val stepInstruction: String,
    val buttonLabel: String,
    val onActionClick: () -> Unit
)

private data class HeaderRouteInfo(
    val hasRoute: Boolean,
    val pX: Float,
    val pY: Float,
    val dX: Float,
    val dY: Float,
    val carPos: Offset,
    val lineAngle: Float
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DriverDashboardScreen(
    viewModel: DriverViewModel,
    modifier: Modifier = Modifier
) {
    val completedTrips by viewModel.completedTrips.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val onlineSeconds by viewModel.onlineSeconds.collectAsStateWithLifecycle()
    val activeJobState by viewModel.activeJobState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var showSplash by remember { mutableStateOf(true) }
    var splashStepIndex by remember { mutableStateOf(0) }
    val splashSteps = listOf(
        "Connecting to UNOXIA Elite servers...",
        "Validating driver session keys...",
        "Querying Room database local cache...",
        "Synchronizing PF & Pension ledger accounts...",
        "Starting GPS tracking trackers..."
    )

    LaunchedEffect(Unit) {
        while (splashStepIndex < splashSteps.size - 1) {
            delay(350)
            splashStepIndex++
        }
        delay(350)
        showSplash = false
    }

    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(showSplash) {
        if (!showSplash) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    var currentTab by remember { mutableStateOf(0) } // 0: Home, 1: Welfare & Pass, 2: Finance & Wallet, 3: News & Circle, 4: Support & Profile

    if (showSplash) {
        // Uniform Clean Indigo/Teal Splash Window
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Outer clean branding ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(ObsidianBlack)
                        .shadow(12.dp, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_foreground_1781815070116),
                        contentDescription = "UNOXIA Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "UNOXIA",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MetallicGold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "DRIVER PROFESSIONAL PORTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldMuted,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(48.dp))
                
                CircularProgressIndicator(
                    color = MetallicGold,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Crossfade(targetState = splashSteps[splashStepIndex]) { stepText ->
                    Text(
                        text = stepText,
                        fontSize = 12.sp,
                        color = GrayMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
        if (currentUser == null) {
            DriverAuthScreen(viewModel = viewModel)
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        containerColor = ObsidianBlack,
                        contentColor = MetallicGold,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Home Tab") },
                            label = { Text("Console", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ObsidianBlack,
                                selectedTextColor = MetallicGold,
                                indicatorColor = MetallicGold,
                                unselectedIconColor = GrayMuted,
                                unselectedTextColor = GrayMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Welfare Tab") },
                            label = { Text("Welfare", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ObsidianBlack,
                                selectedTextColor = MetallicGold,
                                indicatorColor = MetallicGold,
                                unselectedIconColor = GrayMuted,
                                unselectedTextColor = GrayMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = { Icon(Icons.Default.Payments, contentDescription = "Finance Tab") },
                            label = { Text("Earnings", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ObsidianBlack,
                                selectedTextColor = MetallicGold,
                                indicatorColor = MetallicGold,
                                unselectedIconColor = GrayMuted,
                                unselectedTextColor = GrayMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = { Icon(Icons.Default.Forum, contentDescription = "Community Tab") },
                            label = { Text("Circle", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ObsidianBlack,
                                selectedTextColor = MetallicGold,
                                indicatorColor = MetallicGold,
                                unselectedIconColor = GrayMuted,
                                unselectedTextColor = GrayMuted
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = { Icon(Icons.Default.FolderShared, contentDescription = "Support & Settings") },
                            label = { Text("Portal", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ObsidianBlack,
                                selectedTextColor = MetallicGold,
                                indicatorColor = MetallicGold,
                                unselectedIconColor = GrayMuted,
                                unselectedTextColor = GrayMuted
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(PremiumBlack)
                ) {
                    when (currentTab) {
                        0 -> HomeCenterTab(
                            isOnline = isOnline,
                            activeJobState = activeJobState,
                            viewModel = viewModel,
                            completedTripsSize = completedTrips.size,
                            onlineSeconds = onlineSeconds,
                            profile = profile,
                            onEarningClick = { currentTab = 2 },
                            modifier = Modifier.weight(1f)
                        )
                        1 -> WelfareTabContent(viewModel = viewModel)
                        2 -> FinanceTabContent(completedTrips = completedTrips, viewModel = viewModel)
                        3 -> CircleTabContent(viewModel = viewModel)
                        4 -> PortalAuditorTabContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// Format duration helper
fun formatOnlineDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return "%02d:%02d:%02d".format(hrs, mins, secs)
}

@Composable
fun HomeHudMiniChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        modifier = Modifier
            .size(width = 110.dp, height = 70.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MetallicGold, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, color = GoldMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = LightText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ---------------------- 0. HOME CONSOLE & ACTIVE DISPATCH RIDES ----------------------
@Composable
fun HomeCenterTab(
    isOnline: Boolean,
    activeJobState: ActiveJobState,
    viewModel: DriverViewModel,
    completedTripsSize: Int,
    onlineSeconds: Long,
    profile: DriverProfile,
    onEarningClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val isSOSActive by viewModel.isSOSActive.collectAsStateWithLifecycle()
    val driverRank by viewModel.driverRank.collectAsStateWithLifecycle()

    var showSelfiePopup by remember { mutableStateOf(false) }
    val faceVerified by viewModel.faceVerified.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        // Corporate slate header HUD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
            border = androidx.compose.foundation.BorderStroke(2.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Row 1: Profile & SOS and Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "ONLINE SYSTEM GPS ACTIVE" else "CONSOLE OFFLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Text(
                            text = profile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                    }

                    // Indigo online switch
                    Button(
                        onClick = { viewModel.toggleOnline() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) Color(0xFFEF4444) else MetallicGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isOnline) "Go Offline" else "Go Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats summaries row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HomeHudMiniChip(
                        title = "Earning Wallet",
                        value = "₹${"%,.2f".format(walletBalance)}",
                        icon = Icons.Default.AccountBalanceWallet,
                        onClick = onEarningClick
                    )
                    HomeHudMiniChip(
                        title = "Online Duty",
                        value = formatOnlineDuration(onlineSeconds),
                        icon = Icons.Default.AccessTime
                    )
                    HomeHudMiniChip(
                        title = "Rank Badge",
                        value = "$driverRank VIP",
                        icon = Icons.Default.Stars
                    )
                }
            }
        }

        // SOS bar indicator if active
        if (isSOSActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEF4444))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Emergency SOS", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY SOS MODE ACTIVE: REALTIME GPS TRANSMITTED TO CENTRAL DISPATCH HUB",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Active request/navigation canvas
            item {
                Text(
                    text = "GPS ROUTE & DISPATCH PANEL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Drawing map route canvas simulation
                        SimulationMapCanvas(
                            isOnline = isOnline,
                            activeJobState = activeJobState,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating map info (Speed, Fuel, Tolls estimation)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .background(ObsidianBlack.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text("Est. Tolls: ₹250.00", color = LightText, fontSize = 11.sp)
                            Text("Est. Fuel: ₹380.00", color = LightText, fontSize = 11.sp)
                            Text("Route: AI Optimized", color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Floating SOS panic emergency button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSOSActive) Color.White else Color(0xFFEF4444))
                                .clickable { viewModel.triggerSOS() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "SOS Button",
                                tint = if (isSOSActive) Color(0xFFEF4444) else Color.White
                            )
                        }

                        // Selfie verification alert
                        if (!faceVerified) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ObsidianBlack.copy(alpha = 0.95f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                    Icon(Icons.Default.Face, contentDescription = "Face Security", tint = MetallicGold, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Unoxia Quality Audit Check", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Take a secure face check login verification to activate routing duties.", color = GrayMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { showSelfiePopup = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                                    ) {
                                        Text("Verify Selfie", color = ObsidianBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Active Dispatch state actions
            item {
                ActiveWorkflowPanel(
                    isOnline = isOnline,
                    activeJobState = activeJobState,
                    viewModel = viewModel
                )
            }

            // Quick AI Prediction insights
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BorderGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = "AI insights", tint = MetallicGold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AI Dynamic Oracle Guidance", color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            val peakText = if (isOnline) "High surge forecast in Downtown Business Hub (2.4x Multiplier in next 15 mins)." else "Go online to dispatch AI route estimations."
                            Text(peakText, color = GrayMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showSelfiePopup) {
        AlertDialog(
            onDismissRequest = { showSelfiePopup = false },
            containerColor = ObsidianBlack,
            title = { Text("Selfie Liveness Verification", color = LightText) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(BorderGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Face, contentDescription = "Place face here", tint = MetallicGold, modifier = Modifier.size(64.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Face fraud risk index lookup: Safe. Positioning guidelines matched.",
                        fontSize = 12.sp,
                        color = GrayMuted,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.triggerFaceCheckVerification {
                            showSelfiePopup = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                ) {
                    Text("Capture & Verify", color = ObsidianBlack)
                }
            }
        )
    }
}

// ---------------------- WORKFLOW PANEL (JOB DISPATCH STATEMACHINE) ----------------------
@Composable
fun ActiveWorkflowPanel(
    isOnline: Boolean,
    activeJobState: ActiveJobState,
    viewModel: DriverViewModel
) {
    if (!isOnline) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PowerOff, contentDescription = "Go Online Reminder", tint = GrayMuted, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("YOUR CONSOLE IS OFFLINE", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Toggle Online status in top banner to connect to our dispatch networks.", color = GrayMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        when (activeJobState) {
            is ActiveJobState.Idle -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MetallicGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("AWAITING INCOMING TRIPS...", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("AI routing is actively polling surrounding passenger demand matrix.", color = GrayMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.spawnRandomOffer() },
                            colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
                        ) {
                            Text("Force Demo Offer Dispatch", color = ObsidianBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is ActiveJobState.Requested -> {
                val job = activeJobState.job
                val countdown = activeJobState.timeLeftSeconds
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack), // Highlight offer
                    border = androidx.compose.foundation.BorderStroke(2.dp, MetallicGold),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationImportant, contentDescription = "Alert", tint = MetallicGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OUTSTANDING RIDE DISPATCH!", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            // Countdown timer
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(BorderGray)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${countdown}s left", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text("PICKUP FROM:", color = GrayMuted, fontSize = 11.sp)
                        Text(job.pickupAddress, color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("DESTINATION:", color = GrayMuted, fontSize = 11.sp)
                        Text(job.dropoffAddress, color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("METRIC DISTANCE", color = GrayMuted, fontSize = 10.sp)
                                Text("${"%.2f".format(job.distanceMiles)} km", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text("ESTIMATED PAYOUT", color = GrayMuted, fontSize = 10.sp)
                                Text("₹${"%.2f".format(job.payout)}", color = MetallicGold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.declineOffer() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject Trip")
                            }
                            Button(
                                onClick = { viewModel.acceptOffer(job) },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Accept Dispatch", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            else -> {
                // Navigating active ride details
                val uiDetails = when (activeJobState) {
                    is ActiveJobState.Accepted -> NavDetailsUi(
                        job = activeJobState.job,
                        headerTitle = "DISPATCH COMPLETED: TRAVEL TO CLIENT",
                        stepInstruction = "Head towards: " + activeJobState.job.pickupAddress,
                        buttonLabel = "Simulate GPS: Drive to Pickup",
                        onActionClick = { viewModel.startDriveToPickup(activeJobState.job) }
                    )
                    is ActiveJobState.EnRouteToPickup -> NavDetailsUi(
                        job = activeJobState.job,
                        headerTitle = "EN-ROUTE TO PICKUP",
                        stepInstruction = "Driving... ETA: 2 mins",
                        buttonLabel = "Arrive at Passenger Pickup",
                        onActionClick = { viewModel.triggerPickupArrived(activeJobState.job) }
                    )
                    is ActiveJobState.ArrivedAtPickup -> NavDetailsUi(
                        job = activeJobState.job,
                        headerTitle = "ARRIVED: WAITING FOR CLIENT",
                        stepInstruction = "Passenger: " + activeJobState.job.partnerName + " notified.",
                        buttonLabel = "Start Ride (In-Trip)",
                        onActionClick = { viewModel.startTripAndEnRoute(activeJobState.job) }
                    )
                    is ActiveJobState.EnRouteToDropoff -> NavDetailsUi(
                        job = activeJobState.job,
                        headerTitle = "IN-TRIP TO DROP-OFF",
                        stepInstruction = "Heading to: " + activeJobState.job.dropoffAddress,
                        buttonLabel = "Arrive at Destination",
                        onActionClick = { viewModel.completeTrip(activeJobState.job) }
                    )
                    is ActiveJobState.ArrivedAtDropoff -> NavDetailsUi(
                        job = activeJobState.job,
                        headerTitle = "ARRIVED AT DESTINATION",
                        stepInstruction = "Reached " + activeJobState.job.dropoffAddress + ". Confirm details.",
                        buttonLabel = "Complete Ride & Collect Payout",
                        onActionClick = { viewModel.completeTrip(activeJobState.job) }
                    )
                    is ActiveJobState.CompletedGreeting -> NavDetailsUi(
                        job = activeJobState.job,
                        headerTitle = "TRIP COMPLETED SUCCESSFULLY",
                        stepInstruction = "Fare settled securely. ₹" + (activeJobState.job.payout) + " successfully credited.",
                        buttonLabel = "Acknowledge and Clear Console",
                        onActionClick = { viewModel.dismissGreeting() }
                    )
                    else -> null
                }

                if (uiDetails != null && uiDetails.job != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(uiDetails.headerTitle, color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(uiDetails.stepInstruction, color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("RIDER NAME (नाम)", color = GrayMuted, fontSize = 10.sp)
                                    Text(uiDetails.job.partnerName, color = LightText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("FARE PAYOUT", color = GrayMuted, fontSize = 10.sp)
                                    Text("₹${uiDetails.job.payout}", color = MetallicGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Button(
                                onClick = uiDetails.onActionClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(uiDetails.buttonLabel, color = ObsidianBlack, fontWeight = FontWeight.Bold)
                            }

                            // Offer Cancellation Reason Flow during pickup
                            if (activeJobState !is ActiveJobState.CompletedGreeting && activeJobState !is ActiveJobState.EnRouteToDropoff && activeJobState !is ActiveJobState.ArrivedAtDropoff) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { viewModel.dismissGreeting() }, // Cancel helper
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel Ride (Rider No-Show)", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 1. WELFARE SYSTEMS (PASS, PF, PENSION, REFERRALS, FLEET) ----------------------
@Composable
fun WelfareTabContent(viewModel: DriverViewModel) {
    val pfEmployee by viewModel.pfBalanceEmployee.collectAsStateWithLifecycle()
    val pfEmployer by viewModel.pfBalanceEmployer.collectAsStateWithLifecycle()
    val pensionCont by viewModel.pensionContribution.collectAsStateWithLifecycle()
    val pensionProj by viewModel.pensionProjected.collectAsStateWithLifecycle()
    val passDaysLeft by viewModel.unoxiaPassRemainingDays.collectAsStateWithLifecycle()
    val refCode by viewModel.referralCode.collectAsStateWithLifecycle()
    val refEarnings by viewModel.referralEarnings.collectAsStateWithLifecycle()
    val refCount by viewModel.referralCount.collectAsStateWithLifecycle()
    val driverXP by viewModel.driverXP.collectAsStateWithLifecycle()
    val driverRank by viewModel.driverRank.collectAsStateWithLifecycle()
    val fleetOwnerName by viewModel.fleetOwnerName.collectAsStateWithLifecycle()
    val fleetCut by viewModel.fleetOwnerCutPercent.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "UNOXIA GROUP DRIVER WELFARE CORNER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MetallicGold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Welfare, Pass & Social Security Accounts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // UNOXIA Zero-Commission Pass Dashboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ACTIVE PASS STATUS", color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Unoxia Elite Zero-Commission", color = LightText, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BorderGray)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("$passDaysLeft Days Left", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Unoxia Zero-Commisssion pass guarantees 100% of the rider's trip payment goes directly into your earnings wallet (No hidden deductions).",
                        color = GrayMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.purchaseOrRenewPass() },
                        colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Renew Group Pass (₹499/Month)", color = ObsidianBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // PF & Pension Combined Account Ledger
        item {
            Text(
                text = "SOCIAL SECURITY LEDGERS (पीएफ व पेंशन)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Provident fund summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PROVIDENT FUND STATEMENT (PF Balance)", color = GrayMuted, fontSize = 11.sp)
                            Text("₹${"%,.2f".format(pfEmployee + pfEmployer)}", color = LightText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = MetallicGold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BorderGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Your Contribution", color = GrayMuted, fontSize = 10.sp)
                            Text("₹${"%,.2f".format(pfEmployee)}", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Employer Contribution", color = GrayMuted, fontSize = 10.sp)
                            Text("₹${"%,.2f".format(pfEmployer)}", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderGray)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Pension Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PENSION ACCRUALS", color = GrayMuted, fontSize = 11.sp)
                            Text("₹${"%,.2f".format(pensionCont)}", color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Projected Retirement Lump", color = GrayMuted, fontSize = 10.sp)
                            Text("₹${"%,.2f".format(pensionProj)}", color = MetallicGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Driver Rank Milestones progress
        item {
            Text(
                text = "DRIVER LEVELLING XP TRACKER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rank Level: $driverRank Partner", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$driverXP / 1000 XP", color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = driverXP / 1000f,
                        color = MetallicGold,
                        trackColor = BorderGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Next Badge: Diamond level benefits (Reduced pass costs, VIP call queue prioritization & dedicated WhatsApp support channels).",
                        fontSize = 11.sp,
                        color = GrayMuted
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Fleet Module Association
        item {
            Text(
                text = "FLEET PARTNER ASSOCIATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Linked Entity: $fleetOwnerName", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Owner Commission Cap:", color = GrayMuted, fontSize = 11.sp)
                        Text("$fleetCut% Payout Ded.", color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Settlements are processed weekly directly with your managing operator account.",
                        color = GrayMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Referral invite code generator
        item {
            Text(
                text = "REFERRAL LINK (आमंत्रण कोड)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR GOLD REFERRAL CODE", color = GrayMuted, fontSize = 10.sp)
                            Text(refCode, color = MetallicGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BorderGray)
                                .clickable { /* Copy */ }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Copy Code", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = BorderGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Successful Invites", color = GrayMuted, fontSize = 10.sp)
                            Text("$refCount Drivers", color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Referral Revenue Saved", color = GrayMuted, fontSize = 10.sp)
                            Text("₹${"%,.2f".format(refEarnings)}", color = MetallicGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// ---------------------- 2. FINANCE TAB CONTENT (EARNINGS METRICS & WALLET) ----------------------
@Composable
fun FinanceTabContent(completedTrips: List<Trip>, viewModel: DriverViewModel) {
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()

    val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "UNOXIA GROUP SETTLEMENTS LEDGER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MetallicGold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Earnings Analytics & Secured Payouts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Wallet Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MetallicGold)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("AVAILABLE BALANCE SECURED", color = GoldMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("₹${"%,.2f".format(walletBalance)}", color = LightText, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { 
                                viewModel.requestInstantPayout(1000.0) 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MetallicGold,
                                contentColor = ObsidianBlack,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Withdraw ₹1K", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.addMockFunds(5000.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderGray),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add Funds ₹5K", color = LightText, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Dynamic Graph visual chart
        item {
            Text(
                text = "WEEKLY DISPATCH PAYOUT GRAPH",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EarningsWeeklyBarChart(completedTrips)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Taxes & GST summary
        item {
            Text(
                text = "GST TAXES SUMMARY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Fare Earnings:", color = GrayMuted, fontSize = 12.sp)
                        Text("₹${"%,.2f".format(completedTrips.sumOf { it.payout } * 1.05)}", color = LightText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Settlement Payout:", color = GrayMuted, fontSize = 12.sp)
                        Text("₹${"%,.2f".format(completedTrips.sumOf { it.payout })}", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CGST Deducted (2.5%):", color = GrayMuted, fontSize = 12.sp)
                        Text("₹${"%,.2f".format(completedTrips.sumOf { it.payout } * 0.025)}", color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SGST Deducted (2.5%):", color = GrayMuted, fontSize = 12.sp)
                        Text("₹${"%,.2f".format(completedTrips.sumOf { it.payout } * 0.025)}", color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Local historical settlements logs list
        item {
            Text(
                text = "COMPLETED DISPATCH HISTORY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (completedTrips.isEmpty()) {
            item {
                Card(
                     modifier = Modifier.fillMaxWidth(),
                     shape = RoundedCornerShape(12.dp),
                     colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                     border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No completed jobs on record.", color = GrayMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(completedTrips.sortedByDescending { it.timestamp }) { trip ->
                TripHistoryItemRow(trip = trip, df = df)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// Simple bar chart visualizer
@Composable
fun EarningsWeeklyBarChart(completedTrips: List<Trip>) {
    val sampleDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayTotals = DoubleArray(7)
    
    // Distribute actual payouts across days (mock/modulo)
    completedTrips.forEach {
        val i = (it.timestamp / (24 * 3600 * 1000) % 7).toInt()
        dayTotals[i] += it.payout
    }

    val maxPayout = (dayTotals.maxOrNull() ?: 1.0).coerceAtLeast(100.0)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dayTotals.forEachIndexed { index, amt ->
                val barHeightFrac = (amt / maxPayout).toFloat().coerceIn(0.05f, 1.0f)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.height(140.dp)
                ) {
                    Text("₹${amt.toInt()}", color = LightText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(barHeightFrac)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(MetallicGold, BrightGold)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sampleDays[index], color = GrayMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun TripHistoryItemRow(trip: Trip, df: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (trip.type == "Ride") Color(0xFFEEF2F6) else Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (trip.type == "Ride") Icons.Default.DirectionsCar else Icons.Default.Inventory2,
                        contentDescription = trip.type,
                        tint = if (trip.type == "Ride") MetallicGold else Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(trip.partnerName, color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(df.format(Date(trip.timestamp)), color = GrayMuted, fontSize = 11.sp)
                }
            }
            Text("+₹${"%.2f".format(trip.payout)}", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}


// ---------------------- 3. NEWS FEED & CIRCLE COMMUNITY (CHAT) ----------------------
@Composable
fun CircleTabContent(viewModel: DriverViewModel) {
    var newsMethod by remember { mutableStateOf(0) } // 0: News Feed, 1: Live Chat, 2: Training

    var chatMessage by remember { mutableStateOf("") }
    val mockChatList = remember {
        mutableStateListOf(
            "Ramesh (Bengaluru): Huge demand spike near international airport. Safe driving brothers!",
            "Sukhwinder (Delhi NCR): CNG pricing unchanged this week. Verified at petrol pumps.",
            "Aniket (Mumbai): Complete your Aadhaar KYC update early to prevent terminal lockout.",
            "Alexander (UNOXIA Group Admin): Welcome premium partners to the elite Black & Gold platform."
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "UNOXIA GROUP DRIVER ASSOCIATION CIRCLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MetallicGold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Driver Community Center",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LightText
        )
        
        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Subtolls
        TabRow(
            selectedTabIndex = newsMethod,
            containerColor = ObsidianBlack,
            contentColor = MetallicGold,
            modifier = Modifier
                .fillModifierWidthOnly()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp)),
            indicator = {}
        ) {
            Tab(
                selected = newsMethod == 0,
                onClick = { newsMethod = 0 },
                text = { Text("News Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = newsMethod == 1,
                onClick = { newsMethod = 1 },
                text = { Text("Circle Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = newsMethod == 2,
                onClick = { newsMethod = 2 },
                text = { Text("Road Training", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (newsMethod) {
            0 -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        NewsArticleRow(
                            title = "UNOXIA Group Social pension scheme launched for veterans.",
                            descr = "Verified drivers with 12 months consecutive Platinum rankings are automatically eligible for employer-matched retirement contributions.",
                            age = "2 hours ago"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        NewsArticleRow(
                            title = "Monsoon Safety Protocols: Mandatory updates.",
                            descr = "Ensure tyre treads are inspected and vehicle insurance documents are kept ready inside local file explorer vaults to satisfy emergency audit check controls.",
                            age = "1 day ago"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        NewsArticleRow(
                            title = "Zero-Commission pass pricing reduced by 20%.",
                            descr = "To support fuel inflationary shifts, our management has authorized temporary passes discount reductions.",
                            age = "3 days ago"
                        )
                    }
                }
            }
            1 -> {
                // Live Chat simulation
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(mockChatList) { msg ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                            ) {
                                Text(msg, color = LightText, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessage,
                            onValueChange = { chatMessage = it },
                            placeholder = { Text("Broadcast message to surrounding drivers...", color = GrayMuted, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MetallicGold,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = LightText
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (chatMessage.isNotBlank()) {
                                    mockChatList.add("You: $chatMessage")
                                    chatMessage = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MetallicGold)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = ObsidianBlack)
                        }
                    }
                }
            }
            2 -> {
                // Road Training list
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Liveness & Customer Ethics Training", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Required for airport privilege dispatch levels.", color = GrayMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Progress: Completed", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Navigation, Tolls & Cost Minimization", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Tips to minimize fuel expenses & bypass high-toll flyovers safely.", color = GrayMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(progress = 0.45f, color = MetallicGold, trackColor = BorderGray, modifier = Modifier.fillMaxWidth().height(4.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Progress: 45%", color = MetallicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsArticleRow(title: String, descr: String, age: String) {
    Card(
        modifier = Modifier.fillModifierWidthOnly(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(descr, color = GrayMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(age, color = GoldMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun Modifier.fillModifierWidthOnly(): Modifier = this.fillMaxWidth()

// ---------------------- 4. PORTAL HUB (READ ONLY PROFILE, VERIFIED DOCUMENTS, SUPPORT & SETTINGS) ----------------------
@Composable
fun PortalAuditorTabContent(viewModel: DriverViewModel) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Documents Vault, 1: Create Ticket, 2: System Settings
    val docAadhaar by viewModel.aadhaarVerified.collectAsStateWithLifecycle()
    val docPan by viewModel.panVerified.collectAsStateWithLifecycle()
    val docDl by viewModel.dlVerified.collectAsStateWithLifecycle()
    val docRc by viewModel.rcVerified.collectAsStateWithLifecycle()
    val docInsurance by viewModel.insuranceVerified.collectAsStateWithLifecycle()

    var ticketDesc by remember { mutableStateOf("") }
    val supportTicketsList = remember {
        mutableStateListOf(
            "TKT-991A: Toll fee reimbursement under review.",
            "TKT-884D: Welfare PF contribution gap verification resolved."
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "UNOXIA COMPLIANCE AUDITOR PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MetallicGold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Documents Audit & Help Center",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Verified locked Profile Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("VERIFIED PROFILE DETAILS (LOCKED)", color = GoldMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileFieldRow("Driver Name", profile.name)
                    ProfileFieldRow("Vehicle Specs", profile.vehicleModel)
                    ProfileFieldRow("License Plate", profile.licensePlate)
                    ProfileFieldRow("Average Rating", "${profile.rating} / 5.0 ⭐")
                    ProfileFieldRow("Welfare Rank", "${profile.level}")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Subtolls navigation
        item {
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = ObsidianBlack,
                contentColor = MetallicGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
                indicator = {}
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("KYC Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Support Tickets", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (activeSubTab) {
            0 -> {
                item {
                    Text("Secured Document Auditor check Vault", color = GrayMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 10.dp))
                    AuditDocumentStatusRow("Aadhaar Identity Verification", docAadhaar)
                    Spacer(modifier = Modifier.height(6.dp))
                    AuditDocumentStatusRow("Permanent Account Number (PAN)", docPan)
                    Spacer(modifier = Modifier.height(6.dp))
                    AuditDocumentStatusRow("Commercial Driving License (DL)", docDl)
                    Spacer(modifier = Modifier.height(6.dp))
                    AuditDocumentStatusRow("Registration Certificate (RC)", docRc)
                    Spacer(modifier = Modifier.height(6.dp))
                    AuditDocumentStatusRow("Commercial Vehicle Insurance Policy", docInsurance)
                }
            }
            1 -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Create Secure Support Ticket", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = ticketDesc,
                                onValueChange = { ticketDesc = it },
                                placeholder = { Text("Describe ticket details (e.g. accident reports, toll dispute etc.)...", color = GrayMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MetallicGold,
                                    unfocusedBorderColor = BorderGray,
                                    focusedTextColor = LightText
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (ticketDesc.isNotBlank()) {
                                        supportTicketsList.add("TKT-${(100..999).random()}X: $ticketDesc (PENDING)")
                                        ticketDesc = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Submit Ticket", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ACTIVE COMPLAINT TICKETS HISTORY", color = GoldMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(bottom = 6.dp))
                    
                    supportTicketsList.forEach { ticket ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                        ) {
                            Text(ticket, color = LightText, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }
            2 -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Portal Settings", color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Strict Slate & Indigo Premium Theme", color = LightText, fontSize = 12.sp)
                                Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = MetallicGold))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Language: English (Default)", color = LightText, fontSize = 12.sp)
                                Text("Modify", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Online Sync Queue status", color = LightText, fontSize = 12.sp)
                                Text("0 Action pending", color = Color(0xFF10B981), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = BorderGray)
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Disconnect & Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = GrayMuted, fontSize = 12.sp)
        Text(value, color = LightText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
fun AuditDocumentStatusRow(docName: String, isVerified: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isVerified) BrightGold else Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(docName, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isVerified) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isVerified) "Verified Locked" else "Pending Audit",
                    color = if (isVerified) Color(0xFF15803D) else Color(0xFFB91C1C),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ---------------------- 2D MAP VECTOR RENDER SIMULATOR ----------------------
@Composable
fun SimulationMapCanvas(
    isOnline: Boolean,
    activeJobState: ActiveJobState,
    modifier: Modifier = Modifier
) {
    // We animate a dynamic pulsating user tracking coordinate dot around map intersections representation
    val infiniteTransition = rememberInfiniteTransition()
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.background(DarkSlate)) {
        val w = size.width
        val h = size.height

        // Draw grid intersections block representing city navigation streets
        val streetPaint = ObsidianBlack
        val routePaint = MetallicGold // Golden Route Path trace

        // Streets lines
        for (i in 1..8) {
            val gridX = w * (i / 9f)
            drawLine(color = streetPaint, start = Offset(gridX, 0f), end = Offset(gridX, h), strokeWidth = 3f)
        }
        for (j in 1..6) {
            val gridY = h * (j / 7f)
            drawLine(color = streetPaint, start = Offset(0f, gridY), end = Offset(w, gridY), strokeWidth = 3f)
        }

        if (isOnline) {
            // Draw optimal routing path trace if a job is active
            val activeJob = when (activeJobState) {
                is ActiveJobState.Requested -> activeJobState.job
                is ActiveJobState.Accepted -> activeJobState.job
                is ActiveJobState.EnRouteToPickup -> activeJobState.job
                is ActiveJobState.ArrivedAtPickup -> activeJobState.job
                is ActiveJobState.EnRouteToDropoff -> activeJobState.job
                is ActiveJobState.ArrivedAtDropoff -> activeJobState.job
                is ActiveJobState.CompletedGreeting -> activeJobState.job
                else -> null
            }

            if (activeJob != null) {
                // Determine source & destination coordinates scale
                val startX = w * 0.25f
                val startY = h * 0.35f
                val endX = w * 0.75f
                val endY = h * 0.65f

                // Routing path line drawing
                drawLine(
                    color = routePaint,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Pickup location ring pin
                drawCircle(color = Color(0xFF10B981), center = Offset(startX, startY), radius = 8f)
                // Destination location ring pin
                drawCircle(color = Color(0xFFEF4444), center = Offset(endX, endY), radius = 8f)

                // Driving car position animation representation along the route line
                val currentCarOffset = when (activeJobState) {
                    is ActiveJobState.EnRouteToPickup -> {
                        val progress = activeJobState.progress
                        Offset(
                            startX + (endX - startX) * progress * 0.4f,
                            startY + (endY - startY) * progress * 0.4f
                        )
                    }
                    is ActiveJobState.EnRouteToDropoff -> {
                        val progress = activeJobState.progress
                        Offset(
                            startX + (endX - startX) * (0.4f + progress * 0.6f),
                            startY + (endY - startY) * (0.4f + progress * 0.6f)
                        )
                    }
                    else -> Offset(startX, startY)
                }

                // Pulsating glow ring for car GPS point
                drawCircle(color = MetallicGold.copy(alpha = 0.3f), center = currentCarOffset, radius = pulseSize)
                drawCircle(color = MetallicGold, center = currentCarOffset, radius = 6f)
            } else {
                // Idle roaming tracking pulsing marker dot at screen center
                val centerOffset = Offset(w * 0.5f, h * 0.5f)
                drawCircle(color = MetallicGold.copy(alpha = 0.25f), center = centerOffset, radius = pulseSize)
                drawCircle(color = MetallicGold, center = centerOffset, radius = 6f)
            }
        }
    }
}

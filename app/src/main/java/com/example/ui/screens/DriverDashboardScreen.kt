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

    LaunchedEffect(Unit) {
        delay(2200)
        showSplash = false
    }

    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    var hasBgLocationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasBgLocationPermission = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
        }
    }

    val requestAllPermissions = {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        launcher.launch(list.toTypedArray())
    }

    val requestBgLocationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }

    LaunchedEffect(showSplash) {
        if (!showSplash) {
            requestAllPermissions()
        }
    }

    var currentTab by remember { mutableStateOf(0) } // 0: Home, 1: Discover, 2: Earnings, 3: Inbox, 4: Menu

    if (showSplash) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.size(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_foreground_1781815070116),
                        contentDescription = "Shri Krishna Driver Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Shri Krishna",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E3A8A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DRIVER COMPANION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(36.dp))
                CircularProgressIndicator(
                    color = Color(0xFF1E3A8A),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
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
                        modifier = Modifier
                            .testTag("bottom_nav_bar")
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(if (currentTab == 0) Icons.Default.Home else Icons.Default.Home, contentDescription = "Home Tab") },
                            label = { Text("Home") },
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Discover Tab") },
                            label = { Text("Discover") },
                            modifier = Modifier.testTag("nav_tab_discover")
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = { Icon(Icons.Default.Payments, contentDescription = "Earnings Tab") },
                            label = { Text("Earnings") },
                            modifier = Modifier.testTag("nav_tab_earnings")
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = Color(0xFF2563EB),
                                            contentColor = Color.White
                                        ) {
                                            Text("20")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "Inbox Tab")
                                }
                            },
                            label = { Text("Inbox") },
                            modifier = Modifier.testTag("nav_tab_inbox")
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu Tab") },
                            label = { Text("Menu") },
                            modifier = Modifier.testTag("nav_tab_menu")
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        when (currentTab) {
                            0 -> Column(modifier = Modifier.fillMaxSize()) {
                                DriverHudHeader(
                                    isOnline = isOnline,
                                    onlineSeconds = onlineSeconds,
                                    tripsCount = completedTrips.size,
                                    todayRevenue = completedTrips.sumOf { it.payout },
                                    profile = profile,
                                    onToggleOnline = { viewModel.toggleOnline() }
                                )
                                HomeTabContent(
                                    isOnline = isOnline,
                                    activeJobState = activeJobState,
                                    viewModel = viewModel,
                                    completedTripsSize = completedTrips.size,
                                    todayRevenue = completedTrips.sumOf { it.payout },
                                    onlineSeconds = onlineSeconds,
                                    profile = profile,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            1 -> DiscoverTabContent(viewModel = viewModel)
                            2 -> EarningsTabContent(
                                completedTrips = completedTrips,
                                viewModel = viewModel
                            )
                            3 -> InboxTabContent(viewModel = viewModel)
                            4 -> MenuTabContent(
                                profile = profile,
                                viewModel = viewModel,
                                hasLocationPermission = hasLocationPermission,
                                hasCameraPermission = hasCameraPermission,
                                hasNotificationPermission = hasNotificationPermission,
                                hasBgLocationPermission = hasBgLocationPermission,
                                onRequestPermissions = requestAllPermissions,
                                onRequestBgLocation = requestBgLocationPermission
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- DRIVER HUD HEADER ----------------------
@Composable
fun DriverHudHeader(
    isOnline: Boolean,
    onlineSeconds: Long,
    tripsCount: Int,
    todayRevenue: Double,
    profile: DriverProfile,
    onToggleOnline: () -> Unit
) {
    val hrs = onlineSeconds / 3600
    val mins = (onlineSeconds % 3600) / 60
    val secs = onlineSeconds % 60
    val formattedTime = String.format("%02dh %02dm %02ds", hrs, mins, secs)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver Avatar Title Box
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.firstOrNull()?.toString() ?: "D",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${profile.rating} ★",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${profile.level}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Interactive GO ONLINE pill button
                Button(
                    onClick = onToggleOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("toggle_duty_button")
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                        contentDescription = "Duty Status Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "GO OFFLINE" else "GO ONLINE",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pulse Status Band
            AnimatedVisibility(
                visible = isOnline,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F2FE))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Tiny pulsing green dot
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .graphicsLayer(alpha = dotAlpha)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Awaiting Jobs...",
                            color = Color(0xFF0369A1),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Active Run: $formattedTime",
                        color = Color(0xFF0369A1),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            AnimatedVisibility(
                visible = !isOnline,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You are currently OFF-DUTY. Go online to find dispatch tasks.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick HUD Statistics Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Today's Aggregate Payout
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TODAY'S PAYOUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("$%.2f", todayRevenue),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                }

                // Total Trips
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL SHIFTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$tripsCount completed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Acceptance Rate (Calculated mock)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ACCEPT RATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "97.4%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


// ========================================== TAB 1: HOME MAP SCREEN ==========================================
@Composable
fun HomeTabContent(
    isOnline: Boolean,
    activeJobState: ActiveJobState,
    viewModel: DriverViewModel,
    completedTripsSize: Int,
    todayRevenue: Double,
    onlineSeconds: Long,
    profile: DriverProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // 2. Map Container with Rounded Corners and local Landmarks text overlays
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive simulation coordinates map
                SimulationMapCanvas(
                    activeJobState = activeJobState,
                    onCarCoordinateCalculated = { _, _ -> }
                )

                // High-contrast City Landmark Text Overlays matching original picture Noida/Delhi coordinates
                Text(
                    text = "Noida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 10.dp, y = (-20).dp)
                )

                Text(
                    text = "South East Delhi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 16.dp, y = 64.dp)
                )

                // Map Overlay icon circles
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .size(42.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "Expand",
                        tint = Color(0xFF334155)
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(42.dp)
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Area",
                        tint = Color(0xFF334155)
                    )
                }

                // --- INTEGRATED: ACTIVE REQUEST PRESENT OVERLAYS (RINGING MECHANISM) ---
                if (activeJobState is ActiveJobState.Requested) {
                    val job = activeJobState.job
                    val timeLeft = activeJobState.timeLeftSeconds

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(16.dp)
                                .testTag("job_offer_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (job.type == "Ride") Color(0xFF0284C7) else Color(0xFFF97316))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = job.type.uppercase(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (timeLeft <= 5) Color(0xFFFEE2E2) else Color(0xFFF0FDF4)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$timeLeft",
                                            color = if (timeLeft <= 5) Color(0xFFEF4444) else Color(0xFF22C55E),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = String.format("$%.2f", job.payout),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "4.9 ★ • ${job.distanceMiles} mi • ${job.durationMinutes} mins",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Pickup Location",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp).rotate(45f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PICKUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(job.pickupAddress, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Destination",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp).rotate(135f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("DROPOFF", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(job.dropoffAddress, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.declineOffer() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("decline_offer_btn"),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("DECLINE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { viewModel.acceptOffer(job) },
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .height(44.dp)
                                            .testTag("accept_offer_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("ACCEPT DISPATCH", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- INTEGRATED: ACTIVE NAVIGATION IN-PROGRESS CAR CONSOLES ---
                if (activeJobState is ActiveJobState.Accepted ||
                    activeJobState is ActiveJobState.EnRouteToPickup ||
                    activeJobState is ActiveJobState.ArrivedAtPickup ||
                    activeJobState is ActiveJobState.EnRouteToDropoff ||
                    activeJobState is ActiveJobState.ArrivedAtDropoff
                ) {
                    val uiDetails = remember(activeJobState) {
                        when (activeJobState) {
                            is ActiveJobState.Accepted -> {
                                val requestJob = activeJobState.job
                                NavDetailsUi(
                                    requestJob,
                                    "TASK ACCEPTED",
                                    "Prepare vehicle and head to customer.",
                                    "DRIVE TO PICKUP",
                                    { viewModel.startDriveToPickup(requestJob) }
                                )
                            }
                            is ActiveJobState.EnRouteToPickup -> {
                                val requestJob = activeJobState.job
                                NavDetailsUi(
                                    requestJob,
                                    "DRIVING TO PICKUP",
                                    "Navigating corridors toward subscriber.",
                                    "SPEED UP TO ARRIVAL",
                                    { viewModel.triggerPickupArrived(requestJob) }
                                )
                            }
                            is ActiveJobState.ArrivedAtPickup -> {
                                val requestJob = activeJobState.job
                                NavDetailsUi(
                                    requestJob,
                                    "VEHICLE AT PICKUP",
                                    "Passenger notified. Initiate boarding sequence.",
                                    "START TRIP",
                                    { viewModel.startTripAndEnRoute(requestJob) }
                                )
                            }
                            is ActiveJobState.EnRouteToDropoff -> {
                                val requestJob = activeJobState.job
                                NavDetailsUi(
                                    requestJob,
                                    "TRIP IN PROGRESS",
                                    "Progressing towards final dropoff point.",
                                    "ARRIVE",
                                    {}
                                )
                            }
                            is ActiveJobState.ArrivedAtDropoff -> {
                                val requestJob = activeJobState.job
                                NavDetailsUi(
                                    requestJob,
                                    "DESTINATION REACHED",
                                    "Trip finished. Settle payment dispatch.",
                                    "COMPLETE DISPATCH",
                                    { viewModel.completeTrip(requestJob) }
                                )
                            }
                            else -> NavDetailsUi(null, "", "", "", {})
                        }
                    }

                    uiDetails.job?.let { currentJob ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(12.dp)
                                    .testTag("nav_session_car_console"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (activeJobState is ActiveJobState.ArrivedAtPickup || activeJobState is ActiveJobState.ArrivedAtDropoff)
                                                            Color(0xFFEF4444) else Color(0xFF22C55E)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = uiDetails.headerTitle,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(text = currentJob.id, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (currentJob.type == "Ride") Icons.Default.DirectionsCar else Icons.Default.LocalShipping,
                                                contentDescription = "Task type",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = currentJob.partnerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = uiDetails.stepInstruction, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (activeJobState is ActiveJobState.EnRouteToPickup) {
                                        LinearProgressIndicator(
                                            progress = { activeJobState.progress },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = Color(0xFF10B981)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    } else if (activeJobState is ActiveJobState.EnRouteToDropoff) {
                                        LinearProgressIndicator(
                                            progress = { activeJobState.progress },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = Color(0xFF3B82F6)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Est. Payout", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(String.format("$%.2f", currentJob.payout), fontWeight = FontWeight.Black, color = Color(0xFF10B981), fontSize = 16.sp)
                                        }

                                        Button(
                                            onClick = uiDetails.onActionClick,
                                            modifier = Modifier
                                                .height(38.dp)
                                                .testTag("nav_action_submit_btn"),
                                            enabled = activeJobState !is ActiveJobState.EnRouteToDropoff,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (activeJobState is ActiveJobState.ArrivedAtDropoff) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            if (activeJobState is ActiveJobState.EnRouteToDropoff) {
                                                Icon(Icons.Default.Navigation, "Navulating", modifier = Modifier.size(12.dp).rotate(45f))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("LIVE RIDE...", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            } else {
                                                Text(uiDetails.buttonLabel, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- INTEGRATED: DISPATCH COMPLETION CHEERS CONSOLE GREETINGS ---
                if (activeJobState is ActiveJobState.CompletedGreeting) {
                    val completedJob = activeJobState.job

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(16.dp)
                                .testTag("completion_greeting_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payments, "Complete Success", tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("TRIP SETTLED!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                Text("Fare deposited successfully to account logs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Fare Payout:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format("$%.2f", completedJob.payout), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Sim Mileages:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${completedJob.distanceMiles} mi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.dismissGreeting() },
                                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("greeting_confirm_dismiss_btn")
                                ) {
                                    Text("BACK TO COCKPIT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}

// ---------------------- THE SIMULATION MAP CANVAS ----------------------
@Composable
fun SimulationMapCanvas(
    activeJobState: ActiveJobState,
    onCarCoordinateCalculated: (Float, Float) -> Unit
) {
    val roadBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val parkColor = Color(0xFFECFDF5) // soft green for parks
    val riverColor = Color(0xFFEFF6FF) // soft blue for water body

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw grid background texture
        val gridSize = (60.dp.toPx()).coerceAtLeast(10f)
        var curX = 0f
        while (curX < width) {
            drawLine(
                color = gridColor,
                start = Offset(curX, 0f),
                end = Offset(curX, height),
                strokeWidth = 2f
            )
            curX += gridSize
        }
        var curY = 0f
        while (curY < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, curY),
                end = Offset(width, curY),
                strokeWidth = 2f
            )
            curY += gridSize
        }

        // 2. Draw scenic accents (Park Green zone)
        drawRect(
            color = parkColor,
            topLeft = Offset(width * 0.15f, height * 0.1f),
            size = Size(width * 0.3f, height * 0.2f)
        )
        // Accent river
        val riverPath = Path().apply {
            moveTo(0f, height * 0.75f)
            quadraticTo(width * 0.4f, height * 0.8f, width, height * 0.68f)
            lineTo(width, height * 0.73f)
            quadraticTo(width * 0.4f, height * 0.85f, 0f, height * 0.8f)
            close()
        }
        drawPath(path = riverPath, color = riverColor)

        // 3. Draw City Main Road networks
        val roadStroke = 24.dp.toPx()
        // Horizontal main boulevard
        drawLine(
            color = roadBgColor,
            start = Offset(0f, height * 0.45f),
            end = Offset(width, height * 0.45f),
            strokeWidth = roadStroke
        )
        // Vertical interstate
        drawLine(
            color = roadBgColor,
            start = Offset(width * 0.5f, 0f),
            end = Offset(width * 0.5f, height),
            strokeWidth = roadStroke
        )
        // Diagonal bypass
        drawLine(
            color = roadBgColor,
            start = Offset(0f, 0f),
            end = Offset(width, height),
            strokeWidth = roadStroke * 0.4f
        )

        // Active Routing drawings
        val (hasRoute, pX, pY, dX, dY, carPos, lineAngle) = when (activeJobState) {
            is ActiveJobState.Requested -> {
                val job = activeJobState.job
                // Scale coordinate relative inside canvas width
                val startX = (job.pickupX / 300f) * width
                val startY = (job.pickupY / 300f) * height
                val endX = (job.dropoffX / 300f) * width
                val endY = (job.dropoffY / 300f) * height
                // Default car at middle during offer
                val car = Offset(width * 0.5f, height * 0.5f)
                val angle = atan2(endY - startY, endX - startX) * (180f / Math.PI.toFloat())
                HeaderRouteInfo(true, startX, startY, endX, endY, car, angle)
            }
            is ActiveJobState.Accepted -> {
                val job = activeJobState.job
                val pickupX = (job.pickupX / 300f) * width
                val pickupY = (job.pickupY / 300f) * height
                val startX = width * 0.5f
                val startY = height * 0.6f
                val car = Offset(startX, startY)
                val angle = atan2(pickupY - startY, pickupX - startX) * (180f / Math.PI.toFloat())
                HeaderRouteInfo(true, startX, startY, pickupX, pickupY, car, angle)
            }
            is ActiveJobState.EnRouteToPickup -> {
                val job = activeJobState.job
                val pickupX = (job.pickupX / 300f) * width
                val pickupY = (job.pickupY / 300f) * height
                val startX = width * 0.5f
                val startY = height * 0.6f
                val progress = activeJobState.progress
                val carCoords = Offset(
                    startX + (pickupX - startX) * progress,
                    startY + (pickupY - startY) * progress
                )
                val angle = atan2(pickupY - startY, pickupX - startX) * (180f / Math.PI.toFloat())
                HeaderRouteInfo(true, startX, startY, pickupX, pickupY, carCoords, angle)
            }
            is ActiveJobState.ArrivedAtPickup -> {
                val job = activeJobState.job
                val pickupX = (job.pickupX / 300f) * width
                val pickupY = (job.pickupY / 300f) * height
                val dropoffX = (job.dropoffX / 300f) * width
                val dropoffY = (job.dropoffY / 300f) * height
                val car = Offset(pickupX, pickupY)
                val angle = atan2(dropoffY - pickupY, dropoffX - pickupX) * (180f / Math.PI.toFloat())
                HeaderRouteInfo(true, pickupX, pickupY, dropoffX, dropoffY, car, angle)
            }
            is ActiveJobState.EnRouteToDropoff -> {
                val job = activeJobState.job
                val pickupX = (job.pickupX / 300f) * width
                val pickupY = (job.pickupY / 300f) * height
                val dropoffX = (job.dropoffX / 300f) * width
                val dropoffY = (job.dropoffY / 300f) * height
                val progress = activeJobState.progress
                val carCoords = Offset(
                    pickupX + (dropoffX - pickupX) * progress,
                    pickupY + (dropoffY - pickupY) * progress
                )
                val angle = atan2(dropoffY - pickupY, dropoffX - pickupX) * (180f / Math.PI.toFloat())
                HeaderRouteInfo(true, pickupX, pickupY, dropoffX, dropoffY, carCoords, angle)
            }
            is ActiveJobState.ArrivedAtDropoff -> {
                val job = activeJobState.job
                val dropoffX = (job.dropoffX / 300f) * width
                val dropoffY = (job.dropoffY / 300f) * height
                val car = Offset(dropoffX, dropoffY)
                HeaderRouteInfo(true, dropoffX, dropoffY, dropoffX, dropoffY, car, 0f)
            }
            else -> {
                // Return defaults
                HeaderRouteInfo(false, 0f, 0f, 0f, 0f, Offset(width * 0.5f, height * 0.5f), 0f)
            }
        }

        // Draw HUD Route Lines
        if (hasRoute) {
            // Draw routing path with dashes
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            drawLine(
                color = Color(0xFF3B82F6),
                start = Offset(pX, pY),
                end = Offset(dX, dY),
                strokeWidth = 4.dp.toPx(),
                pathEffect = dashedEffect
            )

            // Draw pickup pins
            drawCircle(
                color = Color(0xFF10B981),
                radius = 10.dp.toPx(),
                center = Offset(pX, pY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(pX, pY)
            )

            // Draw dropoff pins if different
            if (pX != dX || pY != dY) {
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 10.dp.toPx(),
                    center = Offset(dX, dY)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(dX - 3.dp.toPx(), dY - 3.dp.toPx()),
                    size = Size(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // Draw Interactive Moving vehicle marker with steering arrow direction
        drawCircle(
            color = Color(0xFF1E3A8A).copy(alpha = 0.15f),
            radius = 24.dp.toPx(),
            center = carPos
        )
        drawCircle(
            color = Color(0xFF3B82F6),
            radius = 12.dp.toPx(),
            center = carPos
        )

        // Vehicle Arrow Directions Pointer
        val arrowPath = Path().apply {
            moveTo(carPos.x, carPos.y - 8.dp.toPx())
            lineTo(carPos.x - 6.dp.toPx(), carPos.y + 6.dp.toPx())
            lineTo(carPos.x, carPos.y + 3.dp.toPx())
            lineTo(carPos.x + 6.dp.toPx(), carPos.y + 6.dp.toPx())
            close()
        }

        rotate(degrees = lineAngle, pivot = carPos) {
            drawPath(path = arrowPath, color = Color.White)
        }
    }
}


// ========================================== TAB 2: EARNINGS AND LOGS ==========================================
@Composable
fun EarningsTabContent(
    completedTrips: List<Trip>,
    viewModel: DriverViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("earnings_records_container")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Net balance scorecard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL METRICS BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("$%.2f", completedTrips.sumOf { it.payout }),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Trips Work", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${completedTrips.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Miles logged", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(String.format("%.1f mi", completedTrips.sumOf { it.distanceMiles }), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Avg Pay/Trip", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            val avgPay = if (completedTrips.isNotEmpty()) completedTrips.sumOf { it.payout } / completedTrips.size else 0.0
                            Text(String.format("$%.2f", avgPay), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Weekly visual column chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Activity Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    EarningsWeeklyBarChart(completedTrips = completedTrips)
                }
            }
        }

        // Historical Shifts Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATEST LOGGED SHIFTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Trigger Clear Database button instantly
                Text(
                    text = "Clear Database",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { viewModel.clearHistory() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }

        // Log Lists scroll view
        if (completedTrips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ClearAll, "Database clean", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shift history database is currently empty. Complete simulated jobs of your online dispatcher to record trip metrics logs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(completedTrips) { log ->
                TripHistoryItemRow(trip = log, onDeleteClick = { viewModel.clearHistory() })
            }
        }
    }
}

// ---------------------- NATIVE WEEKLY CHART CANVAS ----------------------
@Composable
fun EarningsWeeklyBarChart(completedTrips: List<Trip>) {
    // Generate simulated aggregates for Mon-Sun
    val weekdaySums = remember(completedTrips) {
        val sums = doubleArrayOf(15.0, 45.0, 32.0, 68.0, 92.0, 0.0, 0.0) // preset template history
        // Add current jobs dynamically to Friday/Thursday depending on day
        val cal = Calendar.getInstance()
        completedTrips.forEach { trip ->
            cal.timeInMillis = trip.timestamp
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2... Saturday=7
            val index = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            sums[index] += trip.payout
        }
        sums
    }

    val daysLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxVal = (weekdaySums.maxOrNull() ?: 100.0).let { if (it < 100.0) 100.0 else it }.toFloat()

    val primaryBarColor = MaterialTheme.colorScheme.primary
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // Grid divisions
                val gridStep = canvasH / 4f
                for (i in 0..4) {
                    val y = i * gridStep
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(canvasW, y),
                        strokeWidth = 1f
                    )
                }

                // Drawing Columns
                val barSpacingRatio = 0.4f
                val colWidth = canvasW / 7f
                val barWidth = colWidth * (1f - barSpacingRatio)

                for (idx in weekdaySums.indices) {
                    val amount = weekdaySums[idx].toFloat()
                    val barHeight = (amount / maxVal) * (canvasH - 20.dp.toPx())
                    val barX = idx * colWidth + (colWidth * barSpacingRatio / 2f)
                    val barY = canvasH - barHeight

                    drawRoundRect(
                        color = primaryBarColor,
                        topLeft = Offset(barX, barY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysLabels.forEachIndexed { i, label ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("$%.0f", weekdaySums[i]),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (weekdaySums[i] > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ---------------------- WORK TRIP ITEM ROW CARD ----------------------
@Composable
fun TripHistoryItemRow(
    trip: Trip,
    onDeleteClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("EEE, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(trip.timestamp) { formatter.format(Date(trip.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (trip.type == "Ride") Color(0xFFEFF6FF) else Color(0xFFFFF7ED)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (trip.type == "Ride") Icons.Default.DirectionsCar else Icons.Default.LocalShipping,
                            contentDescription = "Ride",
                            tint = if (trip.type == "Ride") Color(0xFF3B82F6) else Color(0xFFF97316),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = trip.partnerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$formattedDate • ${trip.distanceMiles} mi in ${trip.durationMinutes}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Text(
                    text = String.format("+$%.2f", trip.payout),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF10B981)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(10.dp))

            // Pickup dropoff points descriptions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = trip.pickupAddress,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = trip.dropoffAddress,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun ProfileFieldRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked Field",
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(14.dp)
        )
    }
}


// ========================================== TAB 5: MENU AND COCKPIT ==========================================
@Composable
fun MenuTabContent(
    profile: DriverProfile,
    viewModel: DriverViewModel,
    hasLocationPermission: Boolean,
    hasCameraPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasBgLocationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onRequestBgLocation: () -> Unit
) {
    var nameField by remember { mutableStateOf(profile.name) }
    var vehicleField by remember { mutableStateOf(profile.vehicleModel) }
    var licenseField by remember { mutableStateOf(profile.licensePlate) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Brand Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_foreground_1781815070116),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Shri Krishna",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "DRIVER COMPANION APP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Driver Level Badge HUD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("GOLD BADGE PARTNER", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("★ 4.95 Rating", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Verified Driver Profile & Credentials Section (Locked)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_view_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Security Locked Badge Notice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF2F2))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Security",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PROFILE SECURELY LOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "For safety compliance, driver profile updates are locked. (प्रोफ़ाइल सुरक्षित रूप से लॉक है)",
                                fontSize = 10.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Verified Driver Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Driver Name Row
                    ProfileFieldRow(
                        label = "Driver Name (नाम)",
                        value = profile.name,
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Vehicle Spec Row
                    ProfileFieldRow(
                        label = "Vehicle Details (गाड़ी/वाहन विवरण)",
                        value = profile.vehicleModel,
                        icon = Icons.Default.DirectionsCar
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. License Plate Number Row
                    ProfileFieldRow(
                        label = "License Plate Number (वाहन नंबर)",
                        value = profile.licensePlate,
                        icon = Icons.Default.CropFree
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Contact Mobile Row (Specified as "number change nahi kar sakta")
                    ProfileFieldRow(
                        label = "Verified Mobile Number (मोबाइल नंबर)",
                        value = "+91 98765-43012",
                        icon = Icons.Default.Phone
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Driver License ID Row (Specified as "licence ID change nahi kar sakta")
                    ProfileFieldRow(
                        label = "Driver License ID (ड्राइवर लाइसेंस नंबर)",
                        value = "DL-" + profile.licensePlate.replace("-", "").uppercase() + "98",
                        icon = Icons.Default.VerifiedUser
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Support Help notice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Support Agent",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "To edit any profile parameters, please submit verified credentials to support.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("logout_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C))
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = Color(0xFFB91C1C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOG OUT OF PROFILE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sound and Alert Settings
        item {
            val appSoundsEnabled by viewModel.appSoundsEnabled.collectAsStateWithLifecycle()
            Card(
                modifier = Modifier.fillMaxWidth().testTag("sound_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification Icons",
                            tint = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notification Sound Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enable or disable audio sound alerts when a new duty request / trip offer arrives dynamically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEEF2F6))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Trip Offer Sound Warning",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Plays alarm ring when trip arrives",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = appSoundsEnabled,
                            onCheckedChange = { viewModel.toggleAppSounds() },
                            modifier = Modifier.testTag("sound_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.playDispatchSound() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("test_sound_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Volume Up", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TEST NOTIFICATION CHIME", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Driver App Security & Permissions Center Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("permissions_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Shield Icon",
                            tint = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Driver App Security & Permissions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Manage system hardware access permissions to allow real-time driver tracking, delivery proofs, and urgent audible alarms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Permission Status Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location status icon",
                                tint = if (hasLocationPermission) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "GPS Location Tracking",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                        Text(
                            text = if (hasLocationPermission) "Granted" else "Required",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasLocationPermission) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))

                    // Camera Permission Status Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera status icon",
                                tint = if (hasCameraPermission) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Camera Profile & Uploads",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                        Text(
                            text = if (hasCameraPermission) "Granted" else "Required",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasCameraPermission) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))

                    // Post Notifications Permission Status Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notification status icon",
                                tint = if (hasNotificationPermission) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Persistent Push Alerts & Alarms",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                        Text(
                            text = if (hasNotificationPermission) "Granted" else "Required",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasNotificationPermission) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))

                    // Background Location Permission Status Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Background location status icon",
                                tint = if (hasBgLocationPermission) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Background Location Run Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                        Text(
                            text = if (hasBgLocationPermission) "Granted" else "Optional/Not Set",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasBgLocationPermission) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onRequestPermissions() },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .testTag("request_main_permissions_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                        ) {
                            Text("GRANT MAIN ACCESS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onRequestBgLocation() },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .testTag("request_bg_permission_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                        ) {
                            Text("BACKGROUND ACCESS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Developer Dispatch Center Console (Sandbox Simulator Cockpit)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsMotorsports,
                            contentDescription = "Sim logo",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sandbox Simulator Cockpit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Manually trigger notifications, generate instant mock dispatches on the active Noida route mapping, or wipe historic databases for testing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons of Sandbox Simulator
                    Button(
                        onClick = { viewModel.spawnRandomOffer() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sim_trigger_offer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Navigation, "Incoming Icon", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FORCE TRIGGER DISPATCH INSTANTLY", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val randomTrip = Trip(
                                    type = listOf("Ride", "Delivery").random(),
                                    pickupAddress = "Simulated Noida Sector 15",
                                    dropoffAddress = "Simulated Noida Greater Bypass",
                                    payout = (20..90).random().toDouble() + 0.50,
                                    distanceMiles = (3..15).random().toDouble(),
                                    durationMinutes = (10..40).random(),
                                    partnerName = "Manual Sandbox Inject #${(1000..9999).random()}",
                                    rating = 5.0f
                                )
                                viewModel.insertMockTrip(randomTrip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("sim_credit_cash_btn")
                        ) {
                            Text("CREDIT +$50 CASH", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("sim_erase_history_btn")
                        ) {
                            Text("WIPE DATABASE", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Support Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Emergency Contact Support", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Immediate Hotline: 1800-DRV-SAFE (24/7 Support)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ========================================== TAB 2: DISCOVER SURGES ==========================================
@Composable
fun DiscoverTabContent(viewModel: DriverViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Discover Corridors",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Explore high surge coordinates in Delhi-NCR",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Radar Highlights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ NCR Active Surges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val surges = listOf(
                        Triple("Noida Sector 62", "1.5x Multiplier", "2.1 mi near you"),
                        Triple("Saket Metro Precinct", "1.8x Multiplier", "6.5 mi near you"),
                        Triple("Connaught Place Circle", "2.0x Multiplier", "4.0 mi near you"),
                        Triple("Indira Gandhi Intl Airport (T3)", "2.2x Multiplier", "12.8 mi near you"),
                        Triple("Ghaziabad Crossing", "1.3x Multiplier", "8.2 mi near you")
                    )
                    
                    surges.forEach { (loc, surge, dist) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(loc, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(dist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFEDD5))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = surge,
                                    color = Color(0xFFC2410C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }

        // Peak Hours Heatmap Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📅 Peak Hour Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Expect high dispatch frequency during these hours:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val peaks = listOf(
                        "08:00 AM - 11:00 AM (Morning Rush)" to 1.0f,
                        "11:00 AM - 04:00 PM (Afternoon Lunch)" to 0.45f,
                        "04:00 PM - 09:00 PM (Evening Peak)" to 0.95f,
                        "09:00 PM - 12:00 AM (Night Shift)" to 0.7f
                    )
                    
                    peaks.forEach { (time, ratio) ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(time, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(String.format("%.0f%% Load", ratio * 100), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (ratio > 0.8f) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========================================== TAB 4: INBOX BRIEFINGS ==========================================
@Composable
fun InboxTabContent(viewModel: DriverViewModel) {
    var supportQuery by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf(
        "Support Agent" to "Hello Alex! How can I assist you with your driver companion app today? Try asking about 'Zero Commission' or 'Surge'!"
    )) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Inbox Header
        item {
            Column {
                Text(
                    text = "Inbox Messages",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Stay updated on recent notices and support briefs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Unread Badge Alert banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("20", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Active Unread Briefings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E40AF))
                        Text("You have unread system alerts in your regional driver loop.", fontSize = 12.sp, color = Color(0xFF1E40AF).copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Support Assistant Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💬 Live Driver Support AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Support chat log box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(chatMessages) { (sender, text) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = sender,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sender == "You") MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                                    )
                                    Text(text = text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = supportQuery,
                            onValueChange = { supportQuery = it },
                            placeholder = { Text("What is my commission fee?", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (supportQuery.isNotBlank()) {
                                    val userMsg = supportQuery
                                    val reply = when {
                                        userMsg.contains("commission", ignoreCase = true) || userMsg.contains("pass", ignoreCase = true) -> 
                                            "Our zero commission promotion is active today! Complete any trip and you'll get a 24-hour dispatch pass automatically."
                                        userMsg.contains("surge", ignoreCase = true) || userMsg.contains("hot", ignoreCase = true) -> 
                                            "Delhi Connaught Place and Noida Sector 62 are currently reflecting the highest surge multipliers. Activate online mode!"
                                        else -> "I have received your request regarding '$userMsg'. Support has flagged this query for instant dispatcher review!"
                                    }
                                    chatMessages = chatMessages + ("You" to userMsg) + ("Support Agent" to reply)
                                    supportQuery = ""
                                }
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Ask", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Alerts List Section Headers
        item {
            Text("PRIORITY BRIEFINGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        // List of bulletins
        val bulletins = listOf(
            Triple("⛈️ Monsoon Rainfall Alert", "Noida region is registering high demand spikes due to light precipitation. Multipliers elevated to 1.8x near corridors.", "20 mins ago"),
            Triple("🎫 Zero-Commission Promo Enabled", "Your account has been enrolled in the Shri Krishna Zero-Commission drive. Complete your next active trip to unlock.", "1 hour ago"),
            Triple("⚠️ Construction Road Closure", "Major traffic diversion implemented along Sector 62 grid bypass. Expect 15-minute delays; routing auto-calculates bypass.", "3 hours ago"),
            Triple("🏆 Gold Badge Partner unlocked", "Congratulations Alex! You have completed consecutive peak dispatches this week, increasing your terminal priority status.", "1 day ago"),
            Triple("💳 Wallet Settlement Complete", "Your accumulated balance was successfully cleared to default bank account. Check your local bank logs.", "2 days ago")
        )

        items(bulletins) { (title, text, time) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

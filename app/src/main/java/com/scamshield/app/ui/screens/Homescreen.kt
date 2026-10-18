package com.scamshield.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat

import androidx.lifecycle.compose.LocalLifecycleOwner

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

import com.scamshield.app.InputSource
import com.scamshield.app.RiskEngine
import com.scamshield.app.ScamShieldResult
import com.scamshield.app.ScanStorage

import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.DangerTint
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightBorder
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.LightSurfaceVariant
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen
import com.scamshield.app.ui.theme.SecondaryText
import com.scamshield.app.ui.theme.WarningOrange

// =====================================================================
// PRIMARY LIGHT COLORS
// =====================================================================

private val PrimaryLightBackground = Color(0xFFE8F1FF)
private val BlueGlow = Color(0xFF007AFF)

// =====================================================================
// HOME SCREEN
// =====================================================================

@Composable
fun HomeScreen(
    storage: ScanStorage,
    refreshKey: Int,
    onCheckMessage: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenAccount: () -> Unit = {},
    onOpenEmergency: () -> Unit = {},
    onLogout: () -> Unit = {}
) {

    val context = LocalContext.current

    var showQrScanner by remember { mutableStateOf(false) }
    var qrContent by remember { mutableStateOf("") }
    var qrAssessment by remember { mutableStateOf<ScamShieldResult?>(null) }
    val scope = rememberCoroutineScope()

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var locationStatus by remember { mutableStateOf("Finding your location...") }
    var locationName by remember { mutableStateOf("Detecting location...") }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }


    // ================================================================
    // LOCATION PERMISSION
    // ================================================================

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            locationPermissionGranted = granted

            locationStatus =
                if (granted) "Location active" else "Location permission denied"
        }


    // ================================================================
    // CAMERA PERMISSION
    // ================================================================

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                showQrScanner = true
            }
        }


    // ================================================================
    // NOTIFICATION PERMISSION
    // ================================================================

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }


    // ================================================================
    // INITIAL PERMISSIONS
    // ================================================================

    LaunchedEffect(Unit) {

        if (!locationPermissionGranted) {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        } else {
            locationStatus = "Location active"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val notificationGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!notificationGranted) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }


    // ================================================================
    // FUSED LOCATION CLIENT
    // ================================================================

    val locationClient =
        remember {
            LocationServices.getFusedLocationProviderClient(context)
        }


    // ================================================================
    // LIVE LOCATION
    // ================================================================

    DisposableEffect(locationPermissionGranted) {

        val callback =
            object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {

                    val location = result.lastLocation ?: return

                    latitude = location.latitude
                    longitude = location.longitude

                    locationName =
                        getLocationName(
                            context = context,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )

                    locationStatus = "Live location updating"
                }
            }

        if (locationPermissionGranted) {

            locationStatus = "Finding your location..."

            // ---------------- CURRENT LOCATION ----------------

            try {

                val cancellationTokenSource = CancellationTokenSource()

                locationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    )
                    .addOnSuccessListener { location ->

                        if (location != null) {

                            latitude = location.latitude
                            longitude = location.longitude

                            locationName =
                                getLocationName(
                                    context = context,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )

                            locationStatus = "Location detected"

                        } else {
                            locationStatus = "Finding your location..."
                        }
                    }
                    .addOnFailureListener {
                        locationStatus = "Unable to get location"
                    }

            } catch (_: SecurityException) {
                locationStatus = "Location permission required"
            }

            // ---------------- CONTINUOUS UPDATES ----------------

            val request =
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5000L
                )
                    .setMinUpdateIntervalMillis(3000L)
                    .setMinUpdateDistanceMeters(10f)
                    .build()

            try {

                locationClient.requestLocationUpdates(
                    request,
                    callback,
                    context.mainLooper
                )

            } catch (_: SecurityException) {
                locationStatus = "Location permission required"
            }
        }

        onDispose {
            try {
                locationClient.removeLocationUpdates(callback)
            } catch (_: Exception) {
            }
        }
    }


    // ================================================================
    // DASHBOARD DATA
    // ================================================================

    val totalScans = storage.getTotalScans()
    val safeScans = storage.getSafeScans()
    val threatScans = storage.getThreatScans()
    val securityScore = storage.getSecurityScore()
    val lastRisk = storage.getLastRisk()
    val lastLevel = storage.getLastLevel()
    val lastMessage = storage.getLastMessage()
    val last7Days = storage.getLast7Days()

    // Make refreshKey participate in recomposition.
    if (refreshKey < 0) {
        return
    }


    // ================================================================
    // QR SCANNER
    // ================================================================

    if (showQrScanner) {

        RealQRScanner(
            onClose = { showQrScanner = false },
            onQrDetected = { value ->
                qrContent = value
                qrAssessment = null
                showQrScanner = false

                // Check the QR code with the ScamShield risk engine.
                scope.launch {
                    val assessed =
                        RiskEngine.assess(
                            text = value,
                            source = InputSource.QR
                        )

                    storage.saveScan(
                        message = value,
                        result = assessed.toScanResult(),
                        sender = "QR code"
                    )

                    qrAssessment = assessed
                }
            }
        )

        return
    }


    // ================================================================
    // MAIN DASHBOARD
    // ================================================================

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {

            // ---------------- HEADER ----------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "ScamShield",
                        color = DarkText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your digital security dashboard",
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryLightBackground)
                        .clickable { onOpenAccount() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛡️", fontSize = 23.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------------- LOCATION ----------------

            LiveLocationCard(
                locationName = locationName,
                status = locationStatus
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- SECURITY SCORE ----------------

            SecurityScoreCard(
                score = securityScore,
                onOpenAccount = onOpenAccount
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- STATISTICS ----------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                StatisticCard(
                    modifier = Modifier.weight(1f),
                    number = totalScans.toString(),
                    label = "SCANS",
                    description = "Total"
                )

                StatisticCard(
                    modifier = Modifier.weight(1f),
                    number = safeScans.toString(),
                    label = "SAFE",
                    description = "Protected",
                    numberColor = SafeGreen
                )

                StatisticCard(
                    modifier = Modifier.weight(1f),
                    number = threatScans.toString(),
                    label = "THREATS",
                    description = "Detected",
                    numberColor = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---------------- SCAN ACTIVITY ----------------

            ThreatActivityCard(values = last7Days)

            Spacer(modifier = Modifier.height(18.dp))

            // ---------------- MAIN ACTIONS ----------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                GlowingActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    text = "🛡  ScamShield AI",
                    onClick = onCheckMessage
                )

                SecondaryActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    text = "📷  QR Code",
                    onClick = {

                        val cameraGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                        if (cameraGranted) {
                            showQrScanner = true
                        } else {
                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- I'VE BEEN SCAMMED ----------------

            EmergencyActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                text = "🚨  I've been scammed",
                onClick = onOpenEmergency
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- HISTORY ----------------

            SecondaryActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                text = "📋  Scan History",
                onClick = onOpenHistory
            )

            // ---------------- QR RESULT ----------------

            if (qrContent.isNotBlank()) {

                Spacer(modifier = Modifier.height(14.dp))

                QRResultCard(
                    content = qrContent,
                    assessment = qrAssessment
                )
            }

            // ---------------- LATEST SCAN ----------------

            if (lastMessage.isNotBlank()) {

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Latest scan",
                    color = DarkText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LastScanCard(
                    message = lastMessage,
                    risk = lastRisk,
                    level = lastLevel
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


// =====================================================================
// EMERGENCY ACTION BUTTON
// =====================================================================

@Composable
fun EmergencyActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DangerTint)
            .clickable { onClick() }
            .drawBehind {
                drawRoundRect(
                    color = DangerRed.copy(alpha = 0.35f),
                    style = Stroke(width = 2f),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            color = DangerRed,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// =====================================================================
// GLOWING ACTION BUTTON
// =====================================================================

@Composable
fun GlowingActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "buttonGlow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .drawBehind {

                // Outer breathing glow
                drawRoundRect(
                    color = BlueGlow.copy(alpha = glowAlpha),
                    topLeft = Offset(-12f, -12f),
                    size = Size(size.width + 24f, size.height + 24f),
                    cornerRadius = CornerRadius(32f, 32f)
                )

                // Inner soft glow
                drawRoundRect(
                    color = BlueGlow.copy(alpha = glowAlpha * 0.65f),
                    topLeft = Offset(-6f, -6f),
                    size = Size(size.width + 12f, size.height + 12f),
                    cornerRadius = CornerRadius(26f, 26f)
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF007AFF),
                        Color(0xFF0A84FF)
                    )
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// =====================================================================
// SECONDARY ACTION BUTTON
// =====================================================================

@Composable
fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LightSurface)
            .clickable { onClick() }
            .drawBehind {
                drawRoundRect(
                    color = LightBorder,
                    style = Stroke(width = 1f),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            color = DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// =====================================================================
// LIVE LOCATION CARD
// =====================================================================

@Composable
fun LiveLocationCard(
    locationName: String,
    status: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(LightSurface)
            .padding(17.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAF7EE)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📍", fontSize = 21.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                Text(
                    text = "CURRENT LOCATION",
                    color = DarkText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = status,
                    color = SafeGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = locationName,
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// =====================================================================
// SECURITY SCORE (animated ring)
// =====================================================================

@Composable
fun SecurityScoreCard(
    score: Int,
    onOpenAccount: () -> Unit
) {

    val scoreColor =
        when {
            score >= 70 -> SafeGreen
            score >= 40 -> WarningOrange
            else -> DangerRed
        }

    val status =
        when {
            score >= 80 -> "Excellent protection"
            score >= 60 -> "Good protection"
            score >= 40 -> "Needs attention"
            else -> "High risk"
        }

    val safeScore = score.coerceIn(0, 100)

    val animatedScore by animateFloatAsState(
        targetValue = safeScore.toFloat(),
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "securityScore"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LightSurface)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = "SECURITY STATUS",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "$score",
                color = scoreColor,
                fontSize = 43.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = status,
                color = scoreColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // ---------------- SCORE RING ----------------

        Box(
            modifier = Modifier.size(82.dp),
            contentAlignment = Alignment.Center
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {

                val strokeWidth = 7.dp.toPx()

                // Background ring
                drawArc(
                    color = LightSurfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress ring
                drawArc(
                    color = scoreColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (animatedScore / 100f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Soft glow ring
                drawArc(
                    color = scoreColor.copy(alpha = 0.10f),
                    startAngle = -90f,
                    sweepAngle = 360f * (animatedScore / 100f),
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth + 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            Text(
                text = "${animatedScore.toInt()}%",
                color = DarkText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // ---------------- ACCOUNT BUTTON ----------------

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(PrimaryLightBackground)
                .clickable { onOpenAccount() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👤", fontSize = 20.sp)
        }
    }
}


// =====================================================================
// STATISTIC CARD
// =====================================================================

@Composable
fun StatisticCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    description: String,
    numberColor: Color = DarkText
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LightSurface)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = number,
            color = numberColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = description,
            color = SecondaryText,
            fontSize = 10.sp
        )
    }
}


// =====================================================================
// THREAT ACTIVITY
// =====================================================================

@Composable
fun ThreatActivityCard(
    values: List<Int>
) {

    val data = if (values.size == 7) values else List(7) { 0 }

    val total = data.sum()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(LightSurface)
            .padding(17.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {

                Text(
                    text = "Scan activity",
                    color = DarkText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Last 7 days",
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            Text(
                text = if (total == 0) "NO ACTIVITY" else "$total SCANS",
                color = if (total == 0) MutedText else ElectricBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
        ) {

            val maximum = (data.maxOrNull() ?: 1).coerceAtLeast(1)

            val path = Path()

            data.forEachIndexed { index, value ->

                val x =
                    if (data.size == 1) 0f
                    else size.width * index / (data.size - 1)

                val y =
                    size.height -
                            (value.toFloat() / maximum) * (size.height * 0.8f)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = ElectricBlue,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(day, color = MutedText, fontSize = 10.sp)
            }
        }
    }
}


// =====================================================================
// LAST SCAN CARD
// =====================================================================

@Composable
fun LastScanCard(
    message: String,
    risk: Int,
    level: String
) {

    val riskColor =
        when (level) {
            "HIGH" -> DangerRed
            "MEDIUM" -> WarningOrange
            else -> SafeGreen
        }

    val status =
        when (level) {
            "HIGH" -> "🔴 DANGEROUS"
            "MEDIUM" -> "🟠 SUSPICIOUS"
            else -> "🟢 SAFE"
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LightSurface)
            .padding(17.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = status,
                color = riskColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$risk%",
                color = riskColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            color = DarkText,
            fontSize = 13.sp,
            maxLines = 4
        )
    }
}


// =====================================================================
// QR RESULT CARD
// =====================================================================

@Composable
fun QRResultCard(
    content: String,
    assessment: ScamShieldResult?
) {

    val color =
        when (assessment?.level) {
            "HIGH" -> DangerRed
            "MEDIUM" -> WarningOrange
            "LOW" -> SafeGreen
            else -> MutedText
        }

    val verdict =
        when (assessment?.level) {
            "HIGH" -> "🔴 DANGEROUS QR CODE"
            "MEDIUM" -> "🟠 SUSPICIOUS QR CODE"
            "LOW" -> "🟢 QR CODE LOOKS SAFE"
            else -> "Checking QR code..."
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LightSurface)
            .padding(17.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = verdict,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            if (assessment != null) {
                Text(
                    text = "${assessment.riskPercentage}%",
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            color = DarkText,
            fontSize = 13.sp,
            maxLines = 3
        )

        if (assessment != null) {

            if (assessment.level != "LOW") {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Category: ${assessment.category.title}",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (assessment.reasons.isNotEmpty()) {

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Why?",
                    color = DarkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                assessment.reasons.take(4).forEach { reason ->
                    Text(
                        text = "•  $reason",
                        color = color,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "What to do",
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            assessment.actions.take(3).forEach { action ->
                Text(
                    text = "✓  $action",
                    color = DarkText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}


// =====================================================================
// REAL QR SCANNER
// =====================================================================

@OptIn(ExperimentalGetImage::class)
@Composable
fun RealQRScanner(
    onClose: () -> Unit,
    onQrDetected: (String) -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    var scanning by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),

                factory = { ctx ->

                    val previewView = PreviewView(ctx)

                    val cameraProviderFuture =
                        ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({

                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build()

                        preview.surfaceProvider = previewView.surfaceProvider

                        val scanner = BarcodeScanning.getClient()

                        val imageAnalysis =
                            ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build()

                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(ctx)
                        ) { imageProxy ->

                            if (!scanning) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val mediaImage = imageProxy.image

                            if (mediaImage == null) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val image =
                                InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                            scanner
                                .process(image)
                                .addOnSuccessListener { barcodes ->

                                    val value =
                                        barcodes.firstOrNull()?.rawValue

                                    if (!value.isNullOrBlank() && scanning) {
                                        scanning = false
                                        onQrDetected(value)
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }

                        try {

                            cameraProvider.unbindAll()

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )

                        } catch (_: Exception) {
                        }

                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // ---------------- HEADER ----------------

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "QR Scanner",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Point your camera at a QR code",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }

            // ---------------- SCAN FRAME ----------------

            Box(
                modifier = Modifier
                    .size(245.dp)
                    .align(Alignment.Center)
                    .drawBehind {

                        drawRoundRect(
                            color = ElectricBlue.copy(alpha = 0.9f),
                            style = Stroke(width = 4f),
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                        )

                        drawRoundRect(
                            color = ElectricBlue.copy(alpha = 0.20f),
                            topLeft = Offset(-8f, -8f),
                            size = Size(size.width + 16f, size.height + 16f),
                            style = Stroke(width = 3f),
                            cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
                        )
                    }
            )

            // ---------------- CLOSE BUTTON ----------------

            Button(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(30.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                )
            ) {
                Text(
                    text = "Close Scanner",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// =====================================================================
// REVERSE GEOCODING
// =====================================================================

fun getLocationName(
    context: android.content.Context,
    latitude: Double,
    longitude: Double
): String {

    return try {

        val geocoder = Geocoder(context, java.util.Locale.getDefault())

        @Suppress("DEPRECATION")
        val addresses: List<Address> =
            geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()

        val address = addresses.firstOrNull()

        if (address != null) {

            val city = address.locality ?: address.subAdminArea ?: ""
            val state = address.adminArea ?: ""
            val country = address.countryName ?: ""

            listOf(city, state, country)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(", ")
                .ifBlank { "Location detected" }

        } else {
            "Location detected"
        }

    } catch (_: Exception) {
        "Location detected"
    }
}
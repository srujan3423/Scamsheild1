package com.scamshield.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scamshield.app.ScanStorage
import com.scamshield.app.ui.theme.DarkBackground
import com.scamshield.app.ui.theme.DarkSurface
import com.scamshield.app.ui.theme.DarkSurfaceVariant
import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightText
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen
import com.scamshield.app.ui.theme.WarningOrange

@Composable
fun HomeScreen(
    storage: ScanStorage,
    refreshKey: Int,
    onCheckMessage: () -> Unit
) {

    // Force dashboard recomposition after a scan.
    remember(refreshKey) {
        refreshKey
    }

    val totalScans =
        storage.getTotalScans()

    val safeScans =
        storage.getSafeScans()

    val threatScans =
        storage.getThreatScans()

    val securityScore =
        storage.getSecurityScore()

    val lastRisk =
        storage.getLastRisk()

    val lastLevel =
        storage.getLastLevel()

    val lastMessage =
        storage.getLastMessage()

    val lastSender =
        storage.getLastSender()

    val last7Days =
        storage.getLast7Days()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                )
        ) {

            // =================================================
            // HEADER
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "ScamShield",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Digital security dashboard",
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            DarkSurfaceVariant
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "🛡️",
                        fontSize = 21.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // =================================================
            // SECURITY SCORE
            // =================================================

            SecurityScoreCard(
                score = securityScore
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =================================================
            // STATISTICS
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
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

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // =================================================
            // THREAT ACTIVITY
            // =================================================

            ThreatActivityCard(
                values = last7Days
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // =================================================
            // SCAN BUTTON
            // =================================================

            Button(
                onClick = onCheckMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                )
            ) {

                Text(
                    text = "🔍  Scan a suspicious message",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // =================================================
            // LAST SCAN
            // =================================================

            if (lastMessage.isNotBlank()) {

                Text(
                    text = "Latest scan",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                LastScanCard(
                    sender = lastSender,
                    message = lastMessage,
                    risk = lastRisk,
                    level = lastLevel
                )
            }
        }
    }
}


// =============================================================
// SECURITY SCORE
// =============================================================

@Composable
fun SecurityScoreCard(
    score: Int
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(DarkSurface)
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "SECURITY STATUS",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "$score",
                    color = scoreColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = status,
                    color = scoreColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(
                        DarkSurfaceVariant
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "🛡️",
                    fontSize = 35.sp
                )
            }
        }
    }
}


// =============================================================
// STATISTIC CARD
// =============================================================

@Composable
fun StatisticCard(
    modifier: Modifier,
    number: String,
    label: String,
    description: String,
    numberColor: Color = Color.White
) {

    Box(
        modifier = modifier
            .height(105.dp)
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(DarkSurface)
            .padding(14.dp)
    ) {

        Column {

            Text(
                text = number,
                color = numberColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = label,
                color = LightText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = description,
                color = MutedText,
                fontSize = 10.sp
            )
        }
    }
}


// =============================================================
// THREAT ACTIVITY
// =============================================================

@Composable
fun ThreatActivityCard(
    values: List<Int>
) {

    val safeValues =
        if (values.size == 7) {
            values
        } else {
            List(7) { 0 }
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(DarkSurface)
            .padding(16.dp)
    ) {

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Scan activity",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Last 7 days",
                        color = MutedText,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text =
                        if (safeValues.sum() == 0)
                            "NO ACTIVITY"
                        else
                            "${safeValues.sum()} SCANS",
                    color =
                        if (safeValues.sum() == 0)
                            MutedText
                        else
                            ElectricBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ) {

                val maxValue =
                    (safeValues.maxOrNull() ?: 1)
                        .coerceAtLeast(1)

                val path = Path()

                safeValues.forEachIndexed {
                        index,
                        value ->

                    val x =
                        if (safeValues.size == 1) {
                            0f
                        } else {
                            size.width *
                                    index /
                                    (safeValues.size - 1)
                        }

                    val y =
                        size.height -
                                (
                                        value.toFloat() /
                                                maxValue
                                        ) *
                                (size.height * 0.8f) -
                                5f

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = ElectricBlue,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text("M", color = MutedText, fontSize = 10.sp)
                Text("T", color = MutedText, fontSize = 10.sp)
                Text("W", color = MutedText, fontSize = 10.sp)
                Text("T", color = MutedText, fontSize = 10.sp)
                Text("F", color = MutedText, fontSize = 10.sp)
                Text("S", color = MutedText, fontSize = 10.sp)
                Text("S", color = MutedText, fontSize = 10.sp)
            }
        }
    }
}


// =============================================================
// LAST SCAN
// =============================================================

@Composable
fun LastScanCard(
    sender: String,
    message: String,
    risk: Int,
    level: String
) {

    val color =
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
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(DarkSurface)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = status,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$risk%",
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (sender.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "📱 $sender",
                color = MutedText,
                fontSize = 12.sp
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = message,
            color = LightText,
            fontSize = 13.sp,
            maxLines = 3
        )
    }
}
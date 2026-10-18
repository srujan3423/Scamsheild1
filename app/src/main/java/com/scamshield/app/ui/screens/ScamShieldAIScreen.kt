package com.scamshield.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scamshield.app.InputSource
import com.scamshield.app.RiskEngine
import com.scamshield.app.ScamShieldResult
import com.scamshield.app.ScanStorage
import com.scamshield.app.ScreenshotReader
import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightBorder
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.PrimaryTint
import com.scamshield.app.ui.theme.SafeGreen
import com.scamshield.app.ui.theme.SecondaryText
import com.scamshield.app.ui.theme.WarningOrange
import kotlinx.coroutines.launch

// =====================================================================
// SCAMSHIELD AI SCREEN
// =====================================================================

@Composable
fun ScamShieldAIScreen(
    storage: ScanStorage,
    onBack: () -> Unit,
    onScanCompleted: () -> Unit
) {

    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ScamShieldResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    var notice by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Runs the risk engine and saves the scan. Used by text and screenshots.
    fun analyze(content: String, source: InputSource) {

        if (content.isBlank() || isAnalyzing) {
            return
        }

        isAnalyzing = true
        result = null
        notice = ""

        scope.launch {

            try {

                val analysis =
                    RiskEngine.assess(
                        text = content.trim(),
                        source = source
                    )

                storage.saveScan(
                    message = content.trim(),
                    result = analysis.toScanResult(),
                    sender = if (source == InputSource.SCREENSHOT) "Screenshot" else ""
                )

                // Show the result. The user returns to the
                // dashboard with the button at the bottom.
                result = analysis

            } finally {

                isAnalyzing = false
            }
        }
    }

    // Screenshot -> OCR -> ScamShield AI
    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null && !isAnalyzing) {

                isAnalyzing = true
                result = null
                notice = "Reading screenshot..."

                scope.launch {

                    val extracted = ScreenshotReader.readText(context, uri)

                    isAnalyzing = false

                    if (extracted.isBlank()) {

                        notice = "No readable text was found. Try a clearer screenshot."

                    } else {

                        text = extracted
                        analyze(extracted, InputSource.SCREENSHOT)
                    }
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "ScamShield AI",
            color = DarkText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Analyze suspicious messages and understand why they may be dangerous.",
            color = SecondaryText,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        AIBanner()

        Spacer(modifier = Modifier.height(18.dp))

        // ---------------- INPUT CARD ----------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, LightBorder)
        ) {

            Column(modifier = Modifier.padding(18.dp)) {

                Text(
                    text = "MESSAGE TO ANALYZE",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        result = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    shape = RoundedCornerShape(16.dp),
                    label = {
                        Text("Paste suspicious message")
                    },
                    placeholder = {
                        Text(
                            "Example:\n\nYour bank KYC has expired. " +
                                    "Click this link immediately to avoid account blocking..."
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        analyze(text, InputSource.MESSAGE)
                    },
                    enabled = text.isNotBlank() && !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
                ) {

                    if (isAnalyzing) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Analyzing...",
                            fontWeight = FontWeight.Bold
                        )

                    } else {

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Analyze with ScamShield AI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    border = BorderStroke(1.dp, LightBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DarkText
                    )
                ) {

                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = ElectricBlue
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Scan a screenshot",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (notice.isNotBlank()) {

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = notice,
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------------- RESULT ----------------

        result?.let { analysis ->

            AIResultCard(result = analysis)

            Spacer(modifier = Modifier.height(20.dp))
        }

        // ---------------- BACK ----------------

        Button(
            onClick = {
                if (result != null) onScanCompleted() else onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(17.dp),
            border = BorderStroke(1.dp, LightBorder),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightSurface,
                contentColor = DarkText
            )
        ) {
            Text(
                text = if (result != null) "Back to Dashboard" else "← Back",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}


// =====================================================================
// BANNER
// =====================================================================

@Composable
private fun AIBanner() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(color = PrimaryTint)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ElectricBlue),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = "Smart Threat Analysis",
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Detect • Explain • Protect",
                color = ElectricBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// =====================================================================
// RESULT CARD
// =====================================================================

@Composable
private fun AIResultCard(
    result: ScamShieldResult
) {

    val riskColor =
        when (result.level) {
            "HIGH" -> DangerRed
            "MEDIUM" -> WarningOrange
            else -> SafeGreen
        }

    val riskLabel =
        when (result.level) {
            "HIGH" -> "HIGH RISK"
            "MEDIUM" -> "SUSPICIOUS"
            else -> "LOW RISK"
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {

        Column(modifier = Modifier.padding(20.dp)) {

            // ---------------- RISK HEADER ----------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(riskColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            if (result.level == "LOW") Icons.Default.CheckCircle
                            else Icons.Default.Warning,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = riskLabel,
                        color = riskColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${result.riskPercentage}% risk",
                        color = DarkText,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            InfoRow(
                title = "CATEGORY",
                value = result.category.title
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- WHY ----------------

            SectionTitle(text = "WHY WAS THIS FLAGGED?")

            Spacer(modifier = Modifier.height(8.dp))

            if (result.reasons.isEmpty()) {

                Text(
                    text = "No major suspicious indicators were detected.",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

            } else {

                result.reasons.forEach { reason ->

                    ReasonRow(text = reason, color = riskColor)

                    Spacer(modifier = Modifier.height(7.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---------------- EXPLANATION ----------------

            SectionTitle(text = "SCAMSHIELD EXPLANATION")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.explanation,
                color = SecondaryText,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ---------------- ACTIONS ----------------

            SectionTitle(text = "WHAT SHOULD YOU DO?")

            Spacer(modifier = Modifier.height(8.dp))

            result.actions.forEach { action ->

                ActionRow(text = action)

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---------------- LINKS ----------------

            if (result.links.isNotEmpty()) {

                Spacer(modifier = Modifier.height(12.dp))

                SectionTitle(text = "DETECTED LINKS")

                Spacer(modifier = Modifier.height(8.dp))

                result.links.forEach { link ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightBackground)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = link,
                            color = DarkText,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}


// =====================================================================
// SMALL PIECES
// =====================================================================

@Composable
private fun InfoRow(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            color = DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionTitle(
    text: String
) {

    Text(
        text = text,
        color = MutedText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
    )
}

@Composable
private fun ReasonRow(
    text: String,
    color: Color
) {

    Row(verticalAlignment = Alignment.Top) {

        Text(
            text = "•",
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = DarkText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionRow(
    text: String
) {

    Row(verticalAlignment = Alignment.Top) {

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "→",
                color = ElectricBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            color = DarkText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
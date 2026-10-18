package com.scamshield.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.scamshield.app.InputSource
import com.scamshield.app.RiskEngine
import com.scamshield.app.ScamShieldResult
import com.scamshield.app.ScanStorage

import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightBorder
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen
import com.scamshield.app.ui.theme.SecondaryText
import com.scamshield.app.ui.theme.WarningOrange

import kotlinx.coroutines.launch

// =====================================================================
// MESSAGE CHECKER SCREEN
// =====================================================================

@Composable
fun MessageCheckerScreen(
    storage: ScanStorage,
    onBack: () -> Unit,
    onScanCompleted: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var sender by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var analyzing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ScamShieldResult?>(null) }

    val fieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
            focusedBorderColor = ElectricBlue,
            unfocusedBorderColor = LightBorder,
            cursorColor = ElectricBlue,
            focusedContainerColor = LightSurface,
            unfocusedContainerColor = LightSurface
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // ---------------- BACK ----------------

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 22.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkText),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "← Back",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ---------------- HEADER ----------------

        Text(
            text = "ScamShield AI",
            color = DarkText,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Paste a suspicious message or link and I will check it.",
            color = SecondaryText,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        // ---------------- SENDER ----------------

        Text(
            text = "SENDER",
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(7.dp))

        OutlinedTextField(
            value = sender,
            onValueChange = { sender = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Phone number or sender", color = MutedText) },
            colors = fieldColors,
            shape = RoundedCornerShape(15.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ---------------- MESSAGE ----------------

        Text(
            text = "MESSAGE",
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(7.dp))

        OutlinedTextField(
            value = message,
            onValueChange = {
                message = it
                result = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            placeholder = { Text("Paste or type the message or link here...", color = MutedText) },
            colors = fieldColors,
            shape = RoundedCornerShape(15.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ---------------- ANALYZE ----------------

        Button(
            onClick = {

                if (message.isNotBlank() && !analyzing) {

                    analyzing = true

                    scope.launch {

                        val assessed =
                            RiskEngine.assess(
                                text = message,
                                source = InputSource.MESSAGE
                            )

                        storage.saveScan(
                            message = message,
                            result = assessed.toScanResult(),
                            sender = sender
                        )

                        result = assessed
                        analyzing = false
                    }
                }
            },
            enabled = !analyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = if (analyzing) "Analyzing..." else "🛡  Analyze with ScamShield AI",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ---------------- RESULT ----------------

        result?.let { current ->

            Spacer(modifier = Modifier.height(22.dp))

            ScamShieldResultCard(result = current)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onScanCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = LightSurface,
                    contentColor = DarkText
                ),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text(
                    text = "←  Back to Dashboard",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))
    }
}


// =====================================================================
// RESULT CARD
//   SCAMSHIELD AI / Risk / Category / Why? / What to do
// =====================================================================

@Composable
fun ScamShieldResultCard(
    result: ScamShieldResult
) {

    val color =
        when (result.level) {
            "HIGH" -> DangerRed
            "MEDIUM" -> WarningOrange
            else -> SafeGreen
        }

    val dot =
        when (result.level) {
            "HIGH" -> "🔴"
            "MEDIUM" -> "🟠"
            else -> "🟢"
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(LightSurface)
            .border(1.dp, LightBorder, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {

        // ---------------- TITLE ----------------

        Text(
            text = "🛡  SCAMSHIELD AI",
            color = ElectricBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ---------------- RISK ----------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = "Risk", color = SecondaryText, fontSize = 14.sp)

            Text(
                text = "${result.riskPercentage}%  $dot",
                color = color,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ---------------- CATEGORY ----------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = "Category", color = SecondaryText, fontSize = 14.sp)

            Text(
                text = result.category.title.uppercase(),
                color = DarkText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        HorizontalDivider(color = LightBorder)

        // ---------------- WHY ----------------

        if (result.reasons.isNotEmpty()) {

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Why?",
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            result.reasons.forEach { reason ->
                BulletRow(bullet = "•", bulletColor = color, text = reason)
            }
        }

        // ---------------- WHAT TO DO ----------------

        if (result.actions.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What to do",
                color = DarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            result.actions.forEach { action ->
                BulletRow(bullet = "✓", bulletColor = ElectricBlue, text = action)
            }
        }
    }
}


@Composable
private fun BulletRow(
    bullet: String,
    bulletColor: Color,
    text: String
) {

    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = bullet,
            color = bulletColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = DarkText,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
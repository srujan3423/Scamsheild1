package com.scamshield.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scamshield.app.ui.theme.DarkBackground
import com.scamshield.app.ui.theme.DarkSurface
import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightText
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen
import com.scamshield.app.ui.theme.WarningOrange

@Composable
fun MessageCheckerScreen(
    storage: ScanStorage,
    onBack: () -> Unit,
    onScanCompleted: () -> Unit
) {

    var sender by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf<ScanResult?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // WORKING BACK BUTTON
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LightText
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ElectricBlue
            )
        ) {
            Text(
                text = "← Back",
                fontSize = 16.sp
            )
        }

        Text(
            text = "Message Scanner",
            color = LightText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Check an SMS for scam indicators.",
            color = MutedText,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = sender,
            onValueChange = {
                sender = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    "Sender / Phone Number",
                    color = MutedText
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LightText,
                unfocusedTextColor = LightText,
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = MutedText,
                cursorColor = ElectricBlue
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = message,
            onValueChange = {
                message = it
                result = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            placeholder = {
                Text(
                    "SMS Message",
                    color = MutedText
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LightText,
                unfocusedTextColor = LightText,
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = MutedText,
                cursorColor = ElectricBlue
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                if (message.isNotBlank()) {

                    val scanResult =
                        analyzeMessage(message)

                    storage.saveScan(
                        message = message,
                        result = scanResult
                    )

                    result = scanResult
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue
            ),
            shape = RoundedCornerShape(30.dp)
        ) {

            Text(
                text = "🔍  Analyze Message",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let {
            ScanResultCard(it)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (result != null) {

            OutlinedButton(
                onClick = onScanCompleted,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LightText
                )
            ) {
                Text("← Back to Dashboard")
            }
        }
    }
}

@Composable
fun ScanResultCard(
    result: ScanResult
) {

    val resultColor = when (result.level) {
        "HIGH" -> DangerRed
        "MEDIUM" -> WarningOrange
        else -> SafeGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(20.dp)
    ) {

        Text(
            text = when (result.level) {
                "HIGH" -> "🚨 SCAM DETECTED"
                "MEDIUM" -> "⚠️ SUSPICIOUS MESSAGE"
                else -> "🟢 MESSAGE LOOKS SAFE"
            },
            color = resultColor,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Risk Score: ${result.riskPercentage}%",
            color = LightText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = result.explanation,
            color = MutedText,
            fontSize = 15.sp
        )

        if (result.indicators.isNotEmpty()) {

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Detected Indicators",
                color = LightText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            result.indicators.distinct().forEach { indicator ->

                Text(
                    text = "• $indicator",
                    color = resultColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}
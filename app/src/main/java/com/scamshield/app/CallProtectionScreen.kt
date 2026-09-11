package com.scamshield.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CallProtectionScreen(
    storage: ScanStorage,
    onBack: () -> Unit
) {

    var phoneNumber by remember {
        mutableStateOf("")
    }

    var checked by remember {
        mutableStateOf(false)
    }

    var suspicious by remember {
        mutableStateOf(false)
    }

    var reportCount by remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("← Back")
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "📞 Call Protection",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Check a phone number before answering or report a suspicious caller.",
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                checked = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Phone Number")
            },
            placeholder = {
                Text("+919876543210")
            },
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Button(
            onClick = {

                if (phoneNumber.isBlank()) {
                    return@Button
                }

                suspicious =
                    storage.isSuspiciousCaller(
                        phoneNumber
                    )

                reportCount =
                    storage.getSuspiciousCallerCount(
                        phoneNumber
                    )

                checked = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔎 Check Number")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (checked) {

            if (suspicious) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            Color(0xFFFF4D4D)
                                .copy(alpha = 0.12f)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "🔴 KNOWN SCAMMER",
                            color = Color(0xFFFF4D4D),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Number: $phoneNumber"
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = "Reports: $reportCount",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "⚠️ Be careful. Do not share OTPs, UPI PINs, passwords or banking information."
                        )
                    }
                }

            } else {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            Color(0xFF28C76F)
                                .copy(alpha = 0.12f)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "🟢 NUMBER NOT REPORTED",
                            color = Color(0xFF28C76F),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "No ScamShield reports were found for this number."
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Button(
                onClick = {

                    if (phoneNumber.isBlank()) {
                        return@Button
                    }

                    storage.reportSuspiciousCaller(
                        phoneNumber
                    )

                    suspicious = true

                    reportCount =
                        storage.getSuspiciousCallerCount(
                            phoneNumber
                        )

                    checked = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📝 Report as Scam")
            }
        }
    }
}
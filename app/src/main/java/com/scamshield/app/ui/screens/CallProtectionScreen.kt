package com.scamshield.app.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen

@Composable
fun CallProtectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var isEnabled by remember {
        mutableStateOf(
            isCallScreeningRoleHeld(context)
        )
    }

    val roleLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            isEnabled = isCallScreeningRoleHeld(context)
        }

    LaunchedEffect(Unit) {
        isEnabled = isCallScreeningRoleHeld(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
    ) {

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 18.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            androidx.compose.material3.IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkText
                )
            }

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                text = "Call Protection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // Main icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        color = if (isEnabled) {
                            SafeGreen.copy(alpha = 0.12f)
                        } else {
                            ElectricBlue.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = "Call Protection",
                    modifier = Modifier.size(45.dp),
                    tint = if (isEnabled) {
                        SafeGreen
                    } else {
                        ElectricBlue
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Protect your calls",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "ScamShield can check incoming callers against numbers previously detected as suspicious.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightSurface
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = if (isEnabled) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.Security
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isEnabled) {
                            SafeGreen
                        } else {
                            ElectricBlue
                        }
                    )

                    Spacer(
                        modifier = Modifier.size(14.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = if (isEnabled) {
                                "Call Protection is ON"
                            } else {
                                "Call Protection is OFF"
                            },
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = if (isEnabled) {
                                "ScamShield will screen incoming calls."
                            } else {
                                "Enable Call Protection to screen incoming calls."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // Enable button
            if (!isEnabled) {

                Button(
                    onClick = {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            val roleManager =
                                context.getSystemService(
                                    RoleManager::class.java
                                )

                            if (
                                roleManager != null &&
                                roleManager.isRoleAvailable(
                                    RoleManager.ROLE_CALL_SCREENING
                                )
                            ) {

                                val intent =
                                    roleManager.createRequestRoleIntent(
                                        RoleManager.ROLE_CALL_SCREENING
                                    )

                                roleLauncher.launch(intent)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Call,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    Text(
                        text = "Enable Call Protection",
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SafeGreen.copy(alpha = 0.10f)
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SafeGreen
                        )

                        Spacer(
                            modifier = Modifier.size(8.dp)
                        )

                        Text(
                            text = "Protection is active",
                            color = SafeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // How it works
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightSurface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    ProtectionStep(
                        number = "1",
                        text = "An incoming call is detected."
                    )

                    ProtectionStep(
                        number = "2",
                        text = "ScamShield checks the caller number."
                    )

                    ProtectionStep(
                        number = "3",
                        text = "Previously suspicious numbers can trigger a warning."
                    )

                    ProtectionStep(
                        number = "4",
                        text = "The call is currently allowed so you can decide whether to answer."
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Text(
                    text = "Call screening role support requires Android 10 or later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerRed
                )
            }
        }
    }
}

@Composable
private fun ProtectionStep(
    number: String,
    text: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    color = ElectricBlue.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = number,
                color = ElectricBlue,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkText
        )
    }
}

private fun isCallScreeningRoleHeld(
    context: Context
): Boolean {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return false
    }

    val roleManager =
        context.getSystemService(
            RoleManager::class.java
        )

    return roleManager?.isRoleHeld(
        RoleManager.ROLE_CALL_SCREENING
    ) == true
}
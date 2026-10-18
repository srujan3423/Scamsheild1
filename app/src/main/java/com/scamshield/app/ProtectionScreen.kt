package com.scamshield.app.ui.screens

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen


@Composable
fun ProtectionScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var notificationAccessEnabled by remember {
        mutableStateOf(
            isNotificationAccessEnabled(context)
        )
    }

    var callProtectionEnabled by remember {
        mutableStateOf(
            isCallScreeningRoleHeld(context)
        )
    }

    var smsProtectionEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }


    /*
     * Call screening role launcher
     */
    val callRoleLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            callProtectionEnabled =
                isCallScreeningRoleHeld(context)
        }


    /*
     * Refresh statuses whenever this screen appears.
     */
    LaunchedEffect(Unit) {

        notificationAccessEnabled =
            isNotificationAccessEnabled(context)

        callProtectionEnabled =
            isCallScreeningRoleHeld(context)

        smsProtectionEnabled =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // =========================================================
        // HEADER
        // =========================================================

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = onBack,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightSurface
                )
            ) {

                Text(
                    text = "←",
                    color = DarkText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column {

                Text(
                    text = "Protection Center",
                    color = DarkText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Manage your ScamShield protection",
                    color = MutedText,
                    fontSize = 13.sp
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =========================================================
        // SECURITY HEADER
        // =========================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
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
                    imageVector = Icons.Outlined.Security,
                    contentDescription = "Security",
                    tint = ElectricBlue,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                Column {

                    Text(
                        text = "ScamShield Protection",
                        color = DarkText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Keep your digital protection active.",
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(22.dp)
        )


        // =========================================================
        // SMS PROTECTION
        // =========================================================

        ProtectionCard(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Message,
                    contentDescription = "SMS",
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "SMS Protection",
            description = "Detect suspicious SMS messages automatically.",
            enabled = smsProtectionEnabled,
            buttonText = if (smsProtectionEnabled) {
                "Enabled"
            } else {
                "Manage"
            },
            onClick = {

                if (!smsProtectionEnabled) {

                    val intent =
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).apply {
                            data = Uri.parse(
                                "package:${context.packageName}"
                            )
                        }

                    context.startActivity(intent)

                } else {

                    smsProtectionEnabled =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECEIVE_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                }
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =========================================================
        // CALL PROTECTION
        // =========================================================

        ProtectionCard(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = "Calls",
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "Call Protection",
            description = "Screen incoming calls for suspicious numbers.",
            enabled = callProtectionEnabled,
            buttonText = if (callProtectionEnabled) {
                "Enabled"
            } else {
                "Enable"
            },
            onClick = {

                if (!callProtectionEnabled) {

                    if (android.os.Build.VERSION.SDK_INT >= 29) {

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

                            callRoleLauncher.launch(intent)
                        }
                    }
                }
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =========================================================
        // NOTIFICATION PROTECTION
        // =========================================================

        ProtectionCard(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "Notification Protection",
            description = "Check notifications from WhatsApp, Instagram, Telegram, Gmail and other apps.",
            enabled = notificationAccessEnabled,
            buttonText = if (notificationAccessEnabled) {
                "Enabled"
            } else {
                "Enable"
            },
            onClick = {

                val intent =
                    Intent(
                        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )

                context.startActivity(intent)
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =========================================================
        // LINK PROTECTION
        // =========================================================

        ProtectionCard(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = "Links",
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "Link Protection",
            description = "Analyze suspicious links and URLs.",
            enabled = true,
            buttonText = "Available",
            onClick = {}
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =========================================================
        // QR PROTECTION
        // =========================================================

        ProtectionCard(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "QR",
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = "QR Protection",
            description = "Scan QR codes and check their contents.",
            enabled = true,
            buttonText = "Available",
            onClick = {}
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =========================================================
        // PRIVACY
        // =========================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightSurface
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "PRIVACY",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "ScamShield processes notification content exposed by Android. Normal notifications are not permanently stored. Suspicious content can be saved as a security threat.",
                    color = DarkText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightSurface
            )
        ) {

            Text(
                text = "Back to Account",
                color = DarkText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }
}


// =====================================================================
// PROTECTION CARD
// =====================================================================

@Composable
private fun ProtectionCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    enabled: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightSurface
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                icon()

                Spacer(
                    modifier = Modifier.width(14.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        color = DarkText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = description,
                        color = MutedText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "●",
                        color = if (enabled) {
                            SafeGreen
                        } else {
                            DangerRed
                        },
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = if (enabled) {
                            "Active"
                        } else {
                            "Not enabled"
                        },
                        color = if (enabled) {
                            SafeGreen
                        } else {
                            DangerRed
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enabled) {
                            Color(0xFFE8F1FF)
                        } else {
                            ElectricBlue
                        }
                    )
                ) {

                    Text(
                        text = buttonText,
                        color = if (enabled) {
                            ElectricBlue
                        } else {
                            Color.White
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// =====================================================================
// NOTIFICATION ACCESS CHECK
// =====================================================================

private fun isNotificationAccessEnabled(
    context: Context
): Boolean {

    return NotificationManagerCompat
        .getEnabledListenerPackages(context)
        .contains(context.packageName)
}


// =====================================================================
// CALL SCREENING ROLE CHECK
// =====================================================================

private fun isCallScreeningRoleHeld(
    context: Context
): Boolean {

    if (android.os.Build.VERSION.SDK_INT < 29) {
        return false
    }

    val roleManager =
        context.getSystemService(
            RoleManager::class.java
        ) ?: return false

    return roleManager.isRoleHeld(
        RoleManager.ROLE_CALL_SCREENING
    )
}
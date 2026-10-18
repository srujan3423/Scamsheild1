package com.scamshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Security

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.LightSurfaceVariant
import com.scamshield.app.ui.theme.MutedText


@Composable
fun AccountScreen(
    name: String,
    email: String,
    phone: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onCallProtectionClick: () -> Unit,
    onProtectionClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // =========================================================
        // HEADER
        // =========================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = onBack,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(0.dp),
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

            Text(
                text = "Account",
                color = DarkText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(30.dp)
        )


        // =========================================================
        // PROFILE HEADER
        // =========================================================

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(LightSurfaceVariant),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "🛡️",
                    fontSize = 40.sp
                )
            }


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Text(
                text = name.ifBlank {
                    "ScamShield User"
                },
                color = DarkText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = "Verified account",
                color = ElectricBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // =========================================================
        // PROFILE INFORMATION
        // =========================================================

        ProfileItem(
            title = "FULL NAME",
            value = name.ifBlank {
                "Not available"
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        ProfileItem(
            title = "EMAIL",
            value = email.ifBlank {
                "Not available"
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        ProfileItem(
            title = "PHONE NUMBER",
            value = phone.ifBlank {
                "Not available"
            }
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        ProfileItem(
            title = "AUTHENTICATION",
            value = "Firebase verified"
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =========================================================
        // PROTECTION CENTER
        // =========================================================

        Button(
            onClick = onProtectionClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightSurface
            ),
            contentPadding = PaddingValues(
                horizontal = 18.dp
            )
        ) {

            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Protection Center",
                tint = ElectricBlue,
                modifier = Modifier.size(28.dp)
            )


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    text = "Protection Center",
                    color = DarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )


                Spacer(
                    modifier = Modifier.height(4.dp)
                )


                Text(
                    text = "Manage SMS, calls and notifications",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }


            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Open",
                tint = MutedText
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =========================================================
        // CALL PROTECTION
        // =========================================================

        Button(
            onClick = onCallProtectionClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightSurface
            ),
            contentPadding = PaddingValues(
                horizontal = 18.dp
            )
        ) {

            Icon(
                imageVector = Icons.Outlined.Call,
                contentDescription = "Call Protection",
                tint = ElectricBlue,
                modifier = Modifier.size(25.dp)
            )


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    text = "Call Protection",
                    color = DarkText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = "Protect against suspicious callers",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }


            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Open",
                tint = MutedText
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =========================================================
        // LOGOUT BUTTON
        // =========================================================

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerRed
            )
        ) {

            Text(
                text = "LOGOUT",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =========================================================
        // BACK BUTTON
        // =========================================================

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
                text = "Back to Home",
                color = DarkText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }
}


// =====================================================================
// PROFILE ITEM
// =====================================================================

@Composable
private fun ProfileItem(
    title: String,
    value: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(LightSurface)
            .padding(16.dp)
    ) {

        Text(
            text = title,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(5.dp)
        )


        Text(
            text = value,
            color = DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
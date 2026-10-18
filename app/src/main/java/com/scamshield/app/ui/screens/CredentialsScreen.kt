package com.scamshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scamshield.app.ui.theme.DarkBackground
import com.scamshield.app.ui.theme.DarkSurface
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightText
import com.scamshield.app.ui.theme.MutedText

@Composable
fun CredentialsScreen(
    name: String,
    email: String,
    phone: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← Back",
                    color = ElectricBlue
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "🛡️",
            fontSize = 48.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Account Created",
            color = LightText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Check your credentials before continuing",
            color = MutedText,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        CredentialItem(
            title = "Full Name",
            value = name
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        CredentialItem(
            title = "Email",
            value = email
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        CredentialItem(
            title = "Mobile Number",
            value = phone
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue
            )
        ) {
            Text(
                text = "CONTINUE TO SCAMSHIELD",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )
    }
}

@Composable
private fun CredentialItem(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = MutedText,
            fontSize = 11.sp
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = value,
            color = LightText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
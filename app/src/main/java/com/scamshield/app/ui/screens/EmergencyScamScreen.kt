package com.scamshield.app.ui.screens

import android.content.Context
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightBorder
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SecondaryText

// =====================================================================
// INCIDENT OPTIONS  (what happened -> what to do)
// =====================================================================

private data class IncidentOption(
    val id: String,
    val title: String,
    val steps: List<String>
)

private val incidentOptions =
    listOf(
        IncidentOption(
            id = "otp",
            title = "I shared an OTP or PIN",
            steps = listOf(
                "Contact your bank immediately",
                "Change your PIN and passwords",
                "Monitor your bank account for unusual activity"
            )
        ),
        IncidentOption(
            id = "money",
            title = "I paid or sent money",
            steps = listOf(
                "Call your bank and block the card or UPI",
                "Call 1930 (cyber crime helpline)",
                "Report at cybercrime.gov.in"
            )
        ),
        IncidentOption(
            id = "link",
            title = "I clicked a link",
            steps = listOf(
                "Do not open the suspicious link again",
                "Change passwords if you entered credentials",
                "Monitor accounts connected to the service"
            )
        ),
        IncidentOption(
            id = "app",
            title = "I installed an app",
            steps = listOf(
                "Disconnect from the internet if you see suspicious activity",
                "Uninstall the suspicious app",
                "Review the permissions it was given"
            )
        ),
        IncidentOption(
            id = "documents",
            title = "I shared documents",
            steps = listOf(
                "Contact the organization whose documents were shared",
                "Watch for suspicious account activity",
                "Report the incident if identity misuse is suspected"
            )
        )
    )

// =====================================================================
// EMERGENCY SCREEN
// =====================================================================

@Composable
fun EmergencyScamScreen(
    onBack: () -> Unit
) {

    // Must be read here, not inside a click handler.
    val context = LocalContext.current

    var selected by remember { mutableStateOf(setOf<String>()) }
    var showChecklist by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // ---------------- BACK ----------------

        Text(
            text = "← Back",
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onBack() }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            color = DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ---------------- HEADER ----------------

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(DangerRed.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column {

                Text(
                    text = "I've been scammed",
                    color = DarkText,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Let's secure your accounts and information.",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        if (!showChecklist) {

            // ---------------- WHAT HAPPENED ----------------

            Text(
                text = "What happened?",
                color = DarkText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Select everything that applies.",
                color = SecondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            incidentOptions.forEach { option ->

                val isSelected = selected.contains(option.id)

                CheckRow(
                    text = option.title,
                    checked = isSelected,
                    highlight = isSelected,
                    onClick = {
                        selected =
                            if (isSelected) selected - option.id
                            else selected + option.id
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showChecklist = true },
                enabled = selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    disabledContainerColor = ElectricBlue.copy(alpha = 0.35f)
                )
            ) {
                Text(
                    text = "Continue",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        } else {

            // ---------------- CHECKLIST ----------------

            Text(
                text = "Do this now",
                color = DarkText,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Steps for what you selected. Tap a step when it is done.",
                color = SecondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Money steps first, because they are the most urgent.
            val steps =
                incidentOptions
                    .filter { it.id in selected }
                    .sortedBy { if (it.id == "money") 0 else 1 }
                    .flatMap { it.steps }
                    .distinct()

            steps.forEach { step ->
                ChecklistRow(text = step)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- SAVE INCIDENT ----------------

            Button(
                onClick = {
                    saveScamIncident(context, selected)
                    saved = true
                },
                enabled = !saved,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkText,
                    disabledContainerColor = DarkText.copy(alpha = 0.45f)
                )
            ) {

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (saved) "Incident saved" else "Save incident",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    showChecklist = false
                    saved = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = DarkText
                )
            ) {
                Text(
                    text = "← Change selections",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))
    }
}

// =====================================================================
// ROWS
// =====================================================================

@Composable
private fun CheckBoxMark(checked: Boolean) {

    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (checked) ElectricBlue else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) ElectricBlue else LightBorder,
                shape = RoundedCornerShape(7.dp)
            ),
        contentAlignment = Alignment.Center
    ) {

        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun CheckRow(
    text: String,
    checked: Boolean,
    highlight: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(LightSurface)
            .border(
                width = 1.dp,
                color = if (highlight) ElectricBlue else LightBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        CheckBoxMark(checked = checked)

        Spacer(modifier = Modifier.width(13.dp))

        Text(
            text = text,
            color = if (checked && !highlight) MutedText else DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChecklistRow(text: String) {

    var completed by remember { mutableStateOf(false) }

    CheckRow(
        text = text,
        checked = completed,
        highlight = false,
        onClick = { completed = !completed }
    )
}

// =====================================================================
// SAVE INCIDENT
// =====================================================================

private fun saveScamIncident(
    context: Context,
    selectedOptions: Set<String>
) {

    val preferences =
        context.getSharedPreferences(
            "scamshield_incidents",
            Context.MODE_PRIVATE
        )

    preferences.edit()
        .putInt("incident_count", preferences.getInt("incident_count", 0) + 1)
        .putString(
            "incident_${System.currentTimeMillis()}",
            selectedOptions.joinToString(",")
        )
        .apply()

    Toast.makeText(
        context,
        "Incident saved successfully",
        Toast.LENGTH_SHORT
    ).show()
}
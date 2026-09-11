package com.example.secuity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { ScamShieldApp() } }
    }
}

@Composable
fun ScamShieldApp() {
    var screen by remember { mutableStateOf("home") }
    when (screen) {
        "checker" -> CheckerScreen(onBack = { screen = "home" })
        "report" -> ReportScreen(onBack = { screen = "home" })
        else -> HomeScreen(
            onCheck = { screen = "checker" },
            onReport = { screen = "report" }
        )
    }
}

@Composable
fun HomeScreen(onCheck: () -> Unit, onReport: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ScamShield", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Stay alert. Verify before you trust.")
            Spacer(modifier = Modifier.height(28.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Safety status: Ready", modifier = Modifier.padding(20.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
                Text("Check a suspicious message")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                Text("Report a scam")
            }
        }
    }
}

@Composable
fun CheckerScreen(onBack: () -> Unit) {
    var details by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    FormScreen("Check details", onBack) {
        Text("Paste a suspicious message, phone number, or link. Never enter OTPs, passwords, or bank details.")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Suspicious message or number") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                result = if (details.isBlank()) "Please enter details first."
                else "Details received. This is a demo screen."
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Check now") }
        if (result.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(result, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun ReportScreen(onBack: () -> Unit) {
    var contact by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    FormScreen("Report a scam", onBack) {
        Text("Report a suspicious caller, message, or website. Do not include OTPs, passwords, or bank details.")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text("Phone number or website") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("What happened?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                result = if (contact.isBlank() || description.isBlank()) {
                    "Please complete both fields."
                } else {
                    "Report saved for this app session."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save report") }
        if (result.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(result, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun FormScreen(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}

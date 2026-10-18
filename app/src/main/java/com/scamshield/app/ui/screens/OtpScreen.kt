package com.scamshield.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val TAG = "ScamShieldOtp"

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun OtpScreen(
    phone: String,
    onBack: () -> Unit,
    onVerificationSuccess: (PhoneAuthCredential) -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }

    var code by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var message by remember { mutableStateOf<String?>("Sending code to $phone...") }
    var isError by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var verifying by remember { mutableStateOf(false) }
    var autoHandled by remember { mutableStateOf(false) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Instant verification / auto SMS retrieval
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (autoHandled) return
                autoHandled = true
                onVerificationSuccess(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Verification failed", e)
                isError = true
                message = e.localizedMessage ?: "Could not send code"
            }

            // Always keep the LATEST id. A resend produces a new id and a new code.
            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                resendToken = token
                secondsLeft = 30
                isError = false
                message = "Code sent to $phone"
            }
        }
    }

    fun sendCode(resend: Boolean) {
        if (activity == null) {
            isError = true
            message = "Cannot start verification (no activity)"
            return
        }

        val builder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        val token = resendToken
        if (resend && token != null) builder.setForceResendingToken(token)

        message = "Sending code to $phone..."
        isError = false
        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    fun verify() {
        val id = verificationId
        val clean = code.trim()

        when {
            id == null -> {
                isError = true
                message = "Code not sent yet. Please wait or resend."
            }
            clean.length != 6 || !clean.all { it.isDigit() } -> {
                isError = true
                message = "Enter the 6-digit code"
            }
            verifying || autoHandled -> Unit
            else -> {
                verifying = true
                onVerificationSuccess(PhoneAuthProvider.getCredential(id, clean))
            }
        }
    }

    // Send the first code once
    LaunchedEffect(Unit) { sendCode(resend = false) }

    // Resend countdown
    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    // Re-enable the verify button shortly after a tap
    LaunchedEffect(verifying) {
        if (verifying) {
            delay(4000)
            verifying = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verify phone", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(8.dp))

        message?.let {
            Text(
                text = it,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
            label = { Text("6-digit code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { verify() },
            enabled = !verifying && verificationId != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { sendCode(resend = true) },
            enabled = secondsLeft == 0
        ) {
            Text(if (secondsLeft > 0) "Resend code in ${secondsLeft}s" else "Resend code")
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
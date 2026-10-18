package com.scamshield.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SecondaryText

private const val PREFS_NAME = "scamshield_login_preferences"
private const val KEY_REMEMBER_ME = "remember_me"
private const val KEY_REMEMBERED_EMAIL = "remembered_email"

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit
) {

    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    var email by rememberSaveable {
        mutableStateOf(
            preferences.getString(
                KEY_REMEMBERED_EMAIL,
                ""
            ) ?: ""
        )
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var rememberMe by rememberSaveable {
        mutableStateOf(
            preferences.getBoolean(
                KEY_REMEMBER_ME,
                false
            )
        )
    }

    var errorMessage by rememberSaveable {
        mutableStateOf("")
    }

    var isLoading by rememberSaveable {
        mutableStateOf(false)
    }

    fun login() {

        val cleanEmail = email.trim()

        when {
            cleanEmail.isBlank() -> {
                errorMessage = "Please enter your email."
            }

            password.isBlank() -> {
                errorMessage = "Please enter your password."
            }

            else -> {

                errorMessage = ""
                isLoading = true

                FirebaseAuth
                    .getInstance()
                    .signInWithEmailAndPassword(
                        cleanEmail,
                        password
                    )
                    .addOnSuccessListener {

                        isLoading = false

                        if (rememberMe) {

                            preferences.edit()
                                .putBoolean(
                                    KEY_REMEMBER_ME,
                                    true
                                )
                                .putString(
                                    KEY_REMEMBERED_EMAIL,
                                    cleanEmail
                                )
                                .apply()

                        } else {

                            preferences.edit()
                                .remove(KEY_REMEMBER_ME)
                                .remove(KEY_REMEMBERED_EMAIL)
                                .apply()
                        }

                        onLoginSuccess()
                    }
                    .addOnFailureListener {

                        isLoading = false

                        errorMessage =
                            "Incorrect email or password."
                    }
            }
        }
    }

    fun forgotPassword() {

        val cleanEmail = email.trim()

        if (cleanEmail.isBlank()) {

            errorMessage =
                "Enter your email first to reset your password."

            return
        }

        errorMessage = ""

        FirebaseAuth
            .getInstance()
            .sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {

                errorMessage =
                    "Password reset email sent. Check your inbox."

            }
            .addOnFailureListener {

                errorMessage =
                    "Unable to send password reset email."
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "ScamShield",
            color = Color.Black,
            fontSize = 42.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Log in to continue",
            color = DarkText,
            fontSize = 20.sp
        )

        Spacer(
            modifier = Modifier.height(42.dp)
        )

        // EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Email")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // PASSWORD
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation =
                if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {

                IconButton(
                    onClick = {
                        passwordVisible =
                            !passwordVisible
                    }
                ) {

                    Icon(
                        imageVector =
                            if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                        contentDescription =
                            if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                        tint = SecondaryText
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        // REMEMBER ME + FORGOT PASSWORD
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = {
                        rememberMe = it
                    }
                )

                Text(
                    text = "Remember me",
                    color = DarkText,
                    fontSize = 13.sp
                )
            }

            TextButton(
                onClick = {
                    forgotPassword()
                }
            ) {

                Text(
                    text = "Forgot password?",
                    color = ElectricBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ERROR / SUCCESS MESSAGE
        if (errorMessage.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = errorMessage,
                color =
                    if (
                        errorMessage.startsWith(
                            "Password reset email sent"
                        )
                    ) {
                        ElectricBlue
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        // LOGIN BUTTON
        Button(
            onClick = {
                login()
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue
            )
        ) {

            Text(
                text =
                    if (isLoading) {
                        "Logging in..."
                    } else {
                        "Log in"
                    },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        // CREATE ACCOUNT
        TextButton(
            onClick = onSignupClick
        ) {

            Text(
                text = "Create account",
                color = ElectricBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
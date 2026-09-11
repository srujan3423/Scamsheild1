package com.scamshield.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.scamshield.app.ui.screens.HomeScreen
import com.scamshield.app.ui.theme.ScamshieldTheme

enum class AppScreen {
    HOME,
    SCANNER
}

class MainActivity : ComponentActivity() {

    // SMS permission
    private val smsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            // SMS permission result
        }

    // Phone permission
    private val phonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            // Phone permission result
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        // Request permissions
        requestSmsPermission()
        requestPhonePermission()

        setContent {
            ScamshieldTheme {
                ScamShieldApp()
            }
        }
    }

    private fun requestSmsPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            smsPermissionLauncher.launch(
                Manifest.permission.RECEIVE_SMS
            )
        }
    }

    private fun requestPhonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            phonePermissionLauncher.launch(
                Manifest.permission.READ_PHONE_STATE
            )
        }
    }
}

@Composable
fun ScamShieldApp() {

    val context = LocalContext.current

    val storage = remember {
        ScanStorage(
            context.applicationContext
        )
    }

    var currentScreen by remember {
        mutableStateOf(
            AppScreen.HOME
        )
    }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    when (currentScreen) {

        AppScreen.HOME -> {

            HomeScreen(
                storage = storage,
                refreshKey = refreshKey,
                onCheckMessage = {
                    currentScreen =
                        AppScreen.SCANNER
                }
            )
        }

        AppScreen.SCANNER -> {

            MessageCheckerScreen(
                storage = storage,

                onBack = {
                    currentScreen =
                        AppScreen.HOME
                },

                onScanCompleted = {
                    refreshKey++

                    currentScreen =
                        AppScreen.HOME
                }
            )
        }
    }
}
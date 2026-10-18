package com.scamshield.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.core.content.ContextCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

import com.scamshield.app.ui.components.ScamShieldBottomNavigation
import com.scamshield.app.ui.screens.AccountScreen
import com.scamshield.app.ui.screens.CallProtectionScreen
import com.scamshield.app.ui.screens.EmergencyScamScreen
import com.scamshield.app.ui.screens.HistoryScreen
import com.scamshield.app.ui.screens.HomeScreen
import com.scamshield.app.ui.screens.LoginScreen
import com.scamshield.app.ui.screens.MessageCheckerScreen
import com.scamshield.app.ui.screens.OtpScreen
import com.scamshield.app.ui.screens.ProtectionScreen
import com.scamshield.app.ui.screens.ScamShieldAIScreen
import com.scamshield.app.ui.screens.SignupScreen
import com.scamshield.app.ui.theme.ScamshieldTheme


/* =========================================================
   AUTH SCREEN
   ========================================================= */

enum class AuthScreen {
    LOGIN,
    SIGNUP,
    OTP,
    HOME
}


/* =========================================================
   APP SCREEN
   ========================================================= */

enum class AppScreen {
    HOME,
    SCANNER,
    AI,
    HISTORY,
    ACCOUNT,
    CALL_PROTECTION,
    EMERGENCY,
    PROTECTION
}


/* =========================================================
   MAIN ACTIVITY
   ========================================================= */

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // Permission result handled by Android.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()

        setContent {
            ScamshieldTheme {
                ScamShieldApp()
            }
        }
    }

    /*
     * SMS and phone permissions are requested here.
     * Location, camera and notification permissions are
     * handled by HomeScreen.
     */
    private fun requestRequiredPermissions() {

        val permissions = mutableListOf<String>()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(
                Manifest.permission.RECEIVE_SMS
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(
                Manifest.permission.READ_PHONE_STATE
            )
        }

        if (permissions.isNotEmpty()) {

            permissionLauncher.launch(
                permissions.toTypedArray()
            )
        }
    }
}


/* =========================================================
   MAIN APP
   ========================================================= */

@Composable
fun ScamShieldApp() {

    val context = LocalContext.current


    /* ---------------- FIREBASE ---------------- */

    val auth =
        remember {
            FirebaseAuth.getInstance()
        }

    val firestore =
        remember {
            FirebaseFirestore.getInstance()
        }


    /* ---------------- LOCAL STORAGE ---------------- */

    val storage =
        remember {
            ScanStorage(
                context.applicationContext
            )
        }


    /* ---------------- AUTH STATE ---------------- */

    val authPrefs =
        remember {
            context.getSharedPreferences(
                "scamshield_auth",
                Context.MODE_PRIVATE
            )
        }

    fun setSignupPending(
        pending: Boolean
    ) {

        authPrefs
            .edit()
            .putBoolean(
                "signup_pending",
                pending
            )
            .apply()
    }


    /*
     * Keep logged-in users logged in.
     *
     * If signup was started but phone verification
     * was never completed, remove the incomplete
     * Firebase account.
     */

    var authScreen by rememberSaveable {

        mutableStateOf(

            run {

                val user =
                    auth.currentUser

                when {

                    user == null ->
                        AuthScreen.LOGIN

                    authPrefs.getBoolean(
                        "signup_pending",
                        false
                    ) -> {

                        user.delete()

                        auth.signOut()

                        setSignupPending(false)

                        AuthScreen.LOGIN
                    }

                    else ->
                        AuthScreen.HOME
                }
            }
        )
    }


    /* ---------------- APP NAVIGATION ---------------- */

    var appScreen by rememberSaveable {

        mutableStateOf(
            AppScreen.HOME
        )
    }


    /* ---------------- DASHBOARD REFRESH ---------------- */

    var refreshKey by remember {

        mutableIntStateOf(0)
    }


    /* ---------------- USER INFORMATION ---------------- */

    var userName by rememberSaveable {

        mutableStateOf("")
    }

    var userEmail by rememberSaveable {

        mutableStateOf(
            auth.currentUser?.email ?: ""
        )
    }

    var userPhone by rememberSaveable {

        mutableStateOf(
            auth.currentUser?.phoneNumber ?: ""
        )
    }


    /* ---------------- SIGNUP TEMPORARY DATA ---------------- */

    var pendingName by rememberSaveable {

        mutableStateOf("")
    }

    var pendingEmail by rememberSaveable {

        mutableStateOf("")
    }

    var pendingPhone by rememberSaveable {

        mutableStateOf("")
    }


    /* =====================================================
       HELPERS
       ===================================================== */

    fun toast(
        message: String
    ) {

        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }


    /*
     * Delete incomplete signup account.
     */

    fun discardIncompleteAccount() {

        auth.currentUser?.delete()

        auth.signOut()

        setSignupPending(false)
    }


    /* =====================================================
       SAVE PROFILE TO FIRESTORE
       ===================================================== */

    fun saveUserProfile() {

        val user =
            auth.currentUser
                ?: return

        val profile =
            hashMapOf(

                "uid" to user.uid,

                "name" to userName,

                "email" to userEmail,

                "phone" to userPhone,

                "createdAt" to
                        FieldValue.serverTimestamp()
            )

        firestore
            .collection("users")
            .document(user.uid)
            .set(
                profile,
                SetOptions.merge()
            )
    }


    /* =====================================================
       LOAD PROFILE FROM FIRESTORE
       ===================================================== */

    fun loadUserProfile(
        onComplete: () -> Unit
    ) {

        val user =
            auth.currentUser

        if (user == null) {

            onComplete()

            return
        }

        userEmail =
            user.email ?: ""

        userPhone =
            user.phoneNumber ?: ""

        firestore
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    userName =
                        document.getString(
                            "name"
                        ) ?: ""

                    userEmail =
                        document.getString(
                            "email"
                        ) ?: userEmail

                    userPhone =
                        document.getString(
                            "phone"
                        ) ?: userPhone
                }

                onComplete()
            }
            .addOnFailureListener {

                onComplete()
            }
    }


    /* =====================================================
       LOAD PROFILE ON APP START
       ===================================================== */

    LaunchedEffect(Unit) {

        if (auth.currentUser != null) {

            loadUserProfile {
                // Profile loaded.
            }
        }
    }


    /* =====================================================
       LOGOUT
       ===================================================== */

    fun logout() {

        auth.signOut()

        userName = ""

        userEmail = ""

        userPhone = ""

        pendingName = ""

        pendingEmail = ""

        pendingPhone = ""

        appScreen =
            AppScreen.HOME

        authScreen =
            AuthScreen.LOGIN
    }


    /* =====================================================
       AUTHENTICATION NAVIGATION
       ===================================================== */

    when (authScreen) {


        /* =================================================
           LOGIN
           ================================================= */

        AuthScreen.LOGIN -> {

            LoginScreen(

                onLoginSuccess = {

                    loadUserProfile {

                        appScreen =
                            AppScreen.HOME

                        authScreen =
                            AuthScreen.HOME
                    }
                },

                onSignupClick = {

                    authScreen =
                        AuthScreen.SIGNUP
                }
            )
        }


        /* =================================================
           SIGN UP
           ================================================= */

        AuthScreen.SIGNUP -> {

            SignupScreen(

                onBackToLogin = {

                    authScreen =
                        AuthScreen.LOGIN
                },

                onSignupSuccess = {
                        name,
                        email,
                        phone,
                        password ->

                    pendingName =
                        name

                    pendingEmail =
                        email

                    pendingPhone =
                        phone

                    userName =
                        name

                    userEmail =
                        email

                    userPhone =
                        phone


                    /*
                     * Create Firebase email/password
                     * account first.
                     */

                    auth
                        .createUserWithEmailAndPassword(
                            email.trim(),
                            password
                        )
                        .addOnSuccessListener {

                            setSignupPending(
                                true
                            )

                            authScreen =
                                AuthScreen.OTP
                        }
                        .addOnFailureListener { error ->

                            toast(
                                error.message
                                    ?: "Could not create account"
                            )

                            authScreen =
                                AuthScreen.SIGNUP
                        }
                }
            )
        }


        /* =================================================
           OTP
           ================================================= */

        AuthScreen.OTP -> {

            OtpScreen(

                phone =
                    pendingPhone,

                onBack = {

                    discardIncompleteAccount()

                    authScreen =
                        AuthScreen.SIGNUP
                },

                onVerificationSuccess = {
                        credential: PhoneAuthCredential ->

                    val user =
                        auth.currentUser

                    if (user == null) {

                        authScreen =
                            AuthScreen.SIGNUP

                    } else {

                        /*
                         * Link phone credential to the
                         * existing email/password account.
                         */

                        user
                            .linkWithCredential(
                                credential
                            )
                            .addOnSuccessListener {

                                userName =
                                    pendingName

                                userEmail =
                                    pendingEmail

                                userPhone =
                                    pendingPhone

                                saveUserProfile()

                                setSignupPending(
                                    false
                                )

                                appScreen =
                                    AppScreen.HOME

                                authScreen =
                                    AuthScreen.HOME
                            }
                            .addOnFailureListener { error ->

                                toast(
                                    error.message
                                        ?: "Phone verification failed"
                                )

                                discardIncompleteAccount()

                                authScreen =
                                    AuthScreen.SIGNUP
                            }
                    }
                }
            )
        }


        /* =================================================
           MAIN APP
           ================================================= */

        AuthScreen.HOME -> {

            MainAppContent(

                storage =
                    storage,

                refreshKey =
                    refreshKey,

                appScreen =
                    appScreen,

                userName =
                    userName,

                userEmail =
                    userEmail,

                userPhone =
                    userPhone,


                onScreenChange = {
                        newScreen ->

                    appScreen =
                        newScreen
                },


                onCheckMessage = {

                    /*
                     * Home's ScamShield AI button.
                     */

                    appScreen =
                        AppScreen.AI
                },


                onScanCompleted = {

                    refreshKey++

                    appScreen =
                        AppScreen.HOME
                },


                onOpenHistory = {

                    appScreen =
                        AppScreen.HISTORY
                },


                onOpenEmergency = {

                    appScreen =
                        AppScreen.EMERGENCY
                },


                onOpenAccount = {

                    loadUserProfile {

                        appScreen =
                            AppScreen.ACCOUNT
                    }
                },


                onBackToHome = {

                    appScreen =
                        AppScreen.HOME
                },


                onLogout = {

                    logout()
                }
            )
        }
    }
}


/* =========================================================
   MAIN APP CONTENT
   ========================================================= */

@Composable
fun MainAppContent(

    storage: ScanStorage,

    refreshKey: Int,

    appScreen: AppScreen,

    userName: String,

    userEmail: String,

    userPhone: String,

    onScreenChange:
        (AppScreen) -> Unit,

    onCheckMessage:
        () -> Unit,

    onScanCompleted:
        () -> Unit,

    onOpenHistory:
        () -> Unit,

    onOpenEmergency:
        () -> Unit,

    onOpenAccount:
        () -> Unit,

    onBackToHome:
        () -> Unit,

    onLogout:
        () -> Unit

) {

    Scaffold(

        bottomBar = {

            ScamShieldBottomNavigation(

                currentScreen =
                    appScreen,

                onScreenChange =
                    onScreenChange
            )
        }

    ) { paddingValues ->


        /*
         * Prevent bottom navigation from
         * covering screen content.
         */

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    )

        ) {


            when (appScreen) {


                /* =========================================
                   HOME
                   ========================================= */

                AppScreen.HOME -> {

                    HomeScreen(

                        storage =
                            storage,

                        refreshKey =
                            refreshKey,

                        onCheckMessage =
                            onCheckMessage,

                        onOpenHistory =
                            onOpenHistory,

                        onOpenAccount =
                            onOpenAccount,

                        onOpenEmergency =
                            onOpenEmergency,

                        onLogout =
                            onLogout
                    )
                }


                /* =========================================
                   MESSAGE SCANNER
                   ========================================= */

                AppScreen.SCANNER -> {

                    MessageCheckerScreen(

                        storage =
                            storage,

                        onBack =
                            onBackToHome,

                        onScanCompleted =
                            onScanCompleted
                    )
                }


                /* =========================================
                   HISTORY
                   ========================================= */

                AppScreen.HISTORY -> {

                    HistoryScreen(

                        storage =
                            storage,

                        onBack =
                            onBackToHome
                    )
                }


                /* =========================================
                   ACCOUNT
                   ========================================= */

                AppScreen.ACCOUNT -> {

                    AccountScreen(

                        name =
                            userName,

                        email =
                            userEmail,

                        phone =
                            userPhone,

                        onBack =
                            onBackToHome,

                        onLogout =
                            onLogout,

                        onCallProtectionClick = {

                            onScreenChange(
                                AppScreen.CALL_PROTECTION
                            )
                        },

                        onProtectionClick = {

                            onScreenChange(
                                AppScreen.PROTECTION
                            )
                        }
                    )
                }


                /* =========================================
                   CALL PROTECTION
                   ========================================= */

                AppScreen.CALL_PROTECTION -> {

                    CallProtectionScreen(

                        onBack = {

                            onScreenChange(
                                AppScreen.ACCOUNT
                            )
                        }
                    )
                }


                /* =========================================
                   EMERGENCY
                   ========================================= */

                AppScreen.EMERGENCY -> {

                    EmergencyScamScreen(

                        onBack =
                            onBackToHome
                    )
                }


                /* =========================================
                   AI
                   ========================================= */

                AppScreen.AI -> {

                    ScamShieldAIScreen(

                        storage =
                            storage,

                        onBack =
                            onBackToHome,

                        onScanCompleted =
                            onScanCompleted
                    )
                }


                /* =========================================
                   PROTECTION CENTER
                   ========================================= */

                AppScreen.PROTECTION -> {

                    ProtectionScreen(

                        onBack = {

                            onScreenChange(
                                AppScreen.ACCOUNT
                            )
                        }
                    )
                }
            }
        }
    }
}
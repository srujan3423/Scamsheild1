package com.scamshield.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scamshield.app.AppScreen

@Composable
fun ScamShieldBottomNavigation(
    currentScreen: AppScreen,
    onScreenChange: (AppScreen) -> Unit
) {

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {

        // HOME
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,

            onClick = {
                onScreenChange(AppScreen.HOME)
            },

            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },

            label = {
                Text("Home")
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF007AFF),
                selectedTextColor = Color(0xFF007AFF),
                indicatorColor = Color(0xFFE8F1FF),

                unselectedIconColor = Color(0xFF8E8E93),
                unselectedTextColor = Color(0xFF8E8E93)
            )
        )


        // SCAN
        NavigationBarItem(
            selected = currentScreen == AppScreen.SCANNER,

            onClick = {
                onScreenChange(AppScreen.SCANNER)
            },

            icon = {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan"
                )
            },

            label = {
                Text("Scan")
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF007AFF),
                selectedTextColor = Color(0xFF007AFF),
                indicatorColor = Color(0xFFE8F1FF),

                unselectedIconColor = Color(0xFF8E8E93),
                unselectedTextColor = Color(0xFF8E8E93)
            )
        )


        // HISTORY
        NavigationBarItem(
            selected = currentScreen == AppScreen.HISTORY,

            onClick = {
                onScreenChange(AppScreen.HISTORY)
            },

            icon = {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "History"
                )
            },

            label = {
                Text("History")
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF007AFF),
                selectedTextColor = Color(0xFF007AFF),
                indicatorColor = Color(0xFFE8F1FF),

                unselectedIconColor = Color(0xFF8E8E93),
                unselectedTextColor = Color(0xFF8E8E93)
            )
        )


        // ACCOUNT
        NavigationBarItem(
            selected = currentScreen == AppScreen.ACCOUNT,

            onClick = {
                onScreenChange(AppScreen.ACCOUNT)
            },

            icon = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Account"
                )
            },

            label = {
                Text("Account")
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF007AFF),
                selectedTextColor = Color(0xFF007AFF),
                indicatorColor = Color(0xFFE8F1FF),

                unselectedIconColor = Color(0xFF8E8E93),
                unselectedTextColor = Color(0xFF8E8E93)
            )
        )
    }
}
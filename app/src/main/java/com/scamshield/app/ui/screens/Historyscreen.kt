package com.scamshield.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scamshield.app.ScanHistoryItem
import com.scamshield.app.ScanStorage
import com.scamshield.app.ui.theme.DangerRed
import com.scamshield.app.ui.theme.DarkText
import com.scamshield.app.ui.theme.ElectricBlue
import com.scamshield.app.ui.theme.LightBackground
import com.scamshield.app.ui.theme.LightBorder
import com.scamshield.app.ui.theme.LightSurface
import com.scamshield.app.ui.theme.MutedText
import com.scamshield.app.ui.theme.SafeGreen
import com.scamshield.app.ui.theme.SecondaryText
import com.scamshield.app.ui.theme.WarningOrange

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    storage: ScanStorage,
    onBack: () -> Unit
) {

    var history by remember { mutableStateOf(storage.getScanHistory()) }
    var selectedItem by remember { mutableStateOf<ScanHistoryItem?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {

        // ---------------- TOP BAR ----------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkText
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "Scan History",
                    color = DarkText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${history.size} scan${if (history.size == 1) "" else "s"}",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            if (history.isNotEmpty()) {

                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear history",
                        tint = DangerRed
                    )
                }
            }
        }

        HorizontalDivider(color = LightBorder)

        // ---------------- CONTENT ----------------

        if (history.isEmpty()) {

            EmptyHistory(modifier = Modifier.fillMaxSize())

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = history,
                    key = { it.id }
                ) { item ->

                    HistoryCard(
                        item = item,
                        onClick = { selectedItem = item }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // ---------------- DETAIL DIALOG ----------------

    selectedItem?.let { item ->
        HistoryDetailDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }

    // ---------------- CLEAR HISTORY DIALOG ----------------

    if (showClearDialog) {

        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = LightSurface,

            title = {
                Text(
                    text = "Clear scan history?",
                    color = DarkText,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "All saved scan history will be removed from ScamShield.",
                    color = SecondaryText
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        storage.clearScanHistory()
                        history = emptyList()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "Clear",
                        color = DangerRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },

            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = "Cancel", color = ElectricBlue)
                }
            }
        )
    }
}


@Composable
private fun HistoryCard(
    item: ScanHistoryItem,
    onClick: () -> Unit
) {

    val riskColor =
        when (item.level) {
            "HIGH" -> DangerRed
            "MEDIUM" -> WarningOrange
            else -> SafeGreen
        }

    val icon =
        when (item.level) {
            "HIGH", "MEDIUM" -> Icons.Default.Warning
            else -> Icons.Default.CheckCircle
        }

    val label =
        when (item.level) {
            "HIGH" -> "DANGEROUS"
            "MEDIUM" -> "SUSPICIOUS"
            else -> "SAFE"
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(riskColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = label,
                        color = riskColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = formatTimestamp(item.timestamp),
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "${item.riskPercentage}%",
                    color = riskColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.sender.isNotBlank()) {

                Text(
                    text = "From: ${item.sender}",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = item.message,
                color = DarkText,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (item.indicators.isNotEmpty()) {

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${item.indicators.size} suspicious indicator" +
                            if (item.indicators.size == 1) "" else "s",
                    color = riskColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
private fun EmptyHistory(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F1FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Scan History",
            color = DarkText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Messages you analyze will appear here.",
            color = MutedText,
            fontSize = 14.sp
        )
    }
}


@Composable
private fun HistoryDetailDialog(
    item: ScanHistoryItem,
    onDismiss: () -> Unit
) {

    val riskColor =
        when (item.level) {
            "HIGH" -> DangerRed
            "MEDIUM" -> WarningOrange
            else -> SafeGreen
        }

    val label =
        when (item.level) {
            "HIGH" -> "DANGEROUS"
            "MEDIUM" -> "SUSPICIOUS"
            else -> "SAFE"
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LightSurface,

        title = {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = label,
                    color = riskColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${item.riskPercentage}% Risk",
                    color = DarkText
                )
            }
        },

        text = {

            Column {

                if (item.sender.isNotBlank()) {

                    Text(
                        text = "Sender",
                        color = DarkText,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = item.sender, color = SecondaryText)

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Message",
                    color = DarkText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = item.message, color = SecondaryText)

                if (item.indicators.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Detected indicators",
                        color = DarkText,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    item.indicators.forEach { indicator ->
                        Text(
                            text = "• $indicator",
                            color = riskColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = formatTimestamp(item.timestamp),
                    color = MutedText,
                    fontSize = 12.sp
                )
            }
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = ElectricBlue)
            }
        }
    )
}


private fun formatTimestamp(
    timestamp: Long
): String {

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}
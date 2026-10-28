package com.example.dottrail.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dottrail.data.UserSettings
import com.example.dottrail.model.AppTheme
import com.example.dottrail.model.TimerStyle
import com.example.dottrail.ui.theme.BrandError
import com.example.dottrail.ui.theme.BrandPrimary

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onThemeChanged: (AppTheme) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onTimerStyleChanged: (TimerStyle) -> Unit,
    onReduceMotionChanged: (Boolean) -> Unit,
    onResetStats: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(text = "Reset all scores and statistics?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This cannot be undone. All high scores, streak records, and game history will be cleared.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetStats()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandError)
                ) {
                    Text("RESET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Section
        SettingsCard(title = "APPEARANCE") {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            AppTheme.values().forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onThemeChanged(theme) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.theme == theme,
                        onClick = { onThemeChanged(theme) },
                        colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                    )
                    Text(
                        text = theme.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Audio & Feedback Section
        SettingsCard(title = "AUDIO & FEEDBACK") {
            SettingsSwitchRow(
                title = "Sound Effects",
                subtitle = "Subtle chimes and audio feedback",
                checked = settings.soundEnabled,
                onCheckedChange = onSoundChanged
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Vibrations for taps and round events",
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChanged
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gameplay Display Section
        SettingsCard(title = "GAMEPLAY DISPLAY") {
            Text(
                text = "Timer Display",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            TimerStyle.values().forEach { style ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTimerStyleChanged(style) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.timerStyle == style,
                        onClick = { onTimerStyleChanged(style) },
                        colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                    )
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSwitchRow(
                title = "Reduce Motion",
                subtitle = "Reduces pulse and scaling animations",
                checked = settings.reduceMotion,
                onCheckedChange = onReduceMotionChanged
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Data & Statistics Section
        SettingsCard(title = "DATA & RECORDS") {
            Text(
                text = "All game statistics and best scores are stored completely offline on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandError.copy(alpha = 0.15f),
                    contentColor = BrandError
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RESET STATISTICS",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = BrandPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandPrimary,
                checkedTrackColor = BrandPrimary.copy(alpha = 0.35f)
            )
        )
    }
}

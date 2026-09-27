package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserSettingsEntity
import com.example.data.model.CurrentUser
import com.example.data.model.FirebaseAuthUserInfo
import com.example.ui.components.DiscordAvatar
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentUser: CurrentUser,
    firebaseUser: FirebaseAuthUserInfo? = null,
    settings: UserSettingsEntity?,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onDisconnectDiscord: () -> Unit,
    onOpenConnectDialog: () -> Unit,
    onOpenFirebaseAuthDialog: () -> Unit = {},
    onLinkDiscordToFirebase: () -> Unit = {},
    onUnlinkDiscordFromFirebase: () -> Unit = {},
    onSignOutFirebase: () -> Unit = {}
) {
    val currentSettings = settings ?: UserSettingsEntity()

    var voiceNotifications by remember(settings) { mutableStateOf(currentSettings.voiceNotifications) }
    var voiceJoinAlerts by remember(settings) { mutableStateOf(currentSettings.voiceJoinAlerts) }
    var voiceLeaveAlerts by remember(settings) { mutableStateOf(currentSettings.voiceLeaveAlerts) }
    var messageNotifications by remember(settings) { mutableStateOf(currentSettings.messageNotifications) }
    var mentionOnly by remember(settings) { mutableStateOf(currentSettings.mentionOnly) }
    var reminderMinutes by remember(settings) { mutableStateOf(currentSettings.eventReminderMinutes) }
    var soundEnabled by remember(settings) { mutableStateOf(currentSettings.soundEnabled) }
    var vibrateEnabled by remember(settings) { mutableStateOf(currentSettings.vibrateEnabled) }
    var simulationMode by remember(settings) { mutableStateOf(currentSettings.simulationMode) }
    var webhookUrl by remember(settings) { mutableStateOf(currentSettings.discordWebhookUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Discord Account Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DiscordDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DiscordBlurple, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DiscordAvatar(
                                imageUrl = currentUser.avatarUrl,
                                displayName = currentUser.displayName,
                                status = currentUser.status,
                                size = 48
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentUser.username,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (currentUser.isConnected) "สถานะ: เชื่อมต่อ Discord แล้ว" else "สถานะ: ไม่ได้เชื่อมต่อ",
                                    fontSize = 11.sp,
                                    color = if (currentUser.isConnected) NeonGreen else NeonRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentUser.isConnected) {
                            OutlinedButton(
                                onClick = onDisconnectDiscord,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                                modifier = Modifier.weight(1f).testTag("disconnect_discord_btn")
                            ) {
                                Text("Disconnect Discord", fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = onOpenConnectDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                            modifier = Modifier.weight(1f).testTag("connect_discord_dialog_btn")
                        ) {
                            Text(if (currentUser.isConnected) "แก้ไขบัญชี" else "Connect Discord", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Firebase Authentication & Linked Account Management Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DiscordDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (firebaseUser != null) NeonOrange else BorderSubtle,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NeonOrange.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🔥", fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Firebase Authentication",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (firebaseUser != null) {
                                        if (firebaseUser.isAnonymous) "เข้าสู่ระบบแล้ว (Guest Mode)" else "เข้าสู่ระบบแล้ว: ${firebaseUser.email ?: ""}"
                                    } else {
                                        "ยังไม่ได้เข้าสู่ระบบ Firebase"
                                    },
                                    fontSize = 12.sp,
                                    color = if (firebaseUser != null) NeonOrange else TextMuted
                                )
                            }
                        }

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (firebaseUser != null) NeonGreen.copy(alpha = 0.15f) else DiscordDarkest
                        ) {
                            Text(
                                text = if (firebaseUser != null) "LOGGED IN" else "NOT CONNECTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (firebaseUser != null) NeonGreen else TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (firebaseUser != null) {
                        // User UID and Discord link status
                        Surface(
                            color = DiscordDarker,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("สถานะการผูก Discord:", fontSize = 11.sp, color = TextSecondary)
                                    Surface(
                                        color = if (currentUser.isFirebaseLinked) NeonGreen.copy(alpha = 0.2f) else NeonYellow.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (currentUser.isFirebaseLinked) "ผูกกับ Discord แล้ว 🔗" else "ยังไม่ได้ผูกบัญชี ⚠️",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentUser.isFirebaseLinked) NeonGreen else NeonYellow,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "UID: ${firebaseUser.uid}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!currentUser.isFirebaseLinked) {
                                Button(
                                    onClick = onLinkDiscordToFirebase,
                                    colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                                    modifier = Modifier.weight(1f).testTag("settings_link_discord_btn")
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ผูก Discord", fontSize = 11.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onUnlinkDiscordFromFirebase,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                                    modifier = Modifier.weight(1f).testTag("settings_unlink_discord_btn")
                                ) {
                                    Text("ยกเลิกการผูก", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = onOpenFirebaseAuthDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                                modifier = Modifier.weight(1f).testTag("settings_manage_firebase_btn")
                            ) {
                                Text("จัดการบัญชี", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = onSignOutFirebase,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.fillMaxWidth().testTag("settings_firebase_signout_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ออกจากระบบ Firebase", fontSize = 11.sp)
                        }
                    } else {
                        Text(
                            text = "เข้าสู่ระบบด้วย Firebase Authentication เพื่อบันทึกข้อมูล Discord, ตารางนัดเล่นเกม และการตั้งค่าอย่างปลอดภัยบน Cloud",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenFirebaseAuthDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("settings_signin_firebase_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("เข้าสู่ระบบด้วย Firebase (Sign In / Register)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Voice Notifications Settings
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "การแจ้งเตือนห้องเสียง (Voice Notifications)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchRow(
                    title = "เปิดแจ้งเตือนห้องเสียงทั้งหมด",
                    checked = voiceNotifications,
                    onCheckedChange = { voiceNotifications = it }
                )
                SettingSwitchRow(
                    title = "แจ้งเตือนเมื่อสมาชิกเข้าห้องเสียง",
                    checked = voiceJoinAlerts,
                    onCheckedChange = { voiceJoinAlerts = it },
                    enabled = voiceNotifications
                )
                SettingSwitchRow(
                    title = "แจ้งเตือนเมื่อสมาชิกออกจากห้องเสียง",
                    checked = voiceLeaveAlerts,
                    onCheckedChange = { voiceLeaveAlerts = it },
                    enabled = voiceNotifications
                )
            }
        }

        // Message Notifications Settings
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "การแจ้งเตือนข้อความ Discord",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchRow(
                    title = "แจ้งเตือนข้อความใหม่ในช่อง",
                    checked = messageNotifications,
                    onCheckedChange = { messageNotifications = it }
                )
                SettingSwitchRow(
                    title = "แจ้งเตือนเฉพาะเมื่อมีการ Mention (@)",
                    checked = mentionOnly,
                    onCheckedChange = { mentionOnly = it },
                    enabled = messageNotifications
                )
            }
        }

        // Game Event Reminder Minutes
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = NeonYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "แจ้งเตือนก่อนเวลานัดเล่นเกม",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("เลือกเวลาแจ้งเตือนล่วงหน้าก่อนกิจกรรมเริ่ม:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5, 15, 30, 60).forEach { mins ->
                        FilterChip(
                            selected = reminderMinutes == mins,
                            onClick = { reminderMinutes = mins },
                            label = { Text(if (mins == 60) "1 ชั่วโมง" else "$mins นาที", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonYellow,
                                selectedLabelColor = DiscordDarkest
                            )
                        )
                    }
                }
            }
        }

        // Sound, Vibrate & Real-time Simulation Engine
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = NeonPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ระบบและเสียงแจ้งเตือน",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchRow(
                    title = "เปิดเสียงแจ้งเตือน (Notification Sound)",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
                SettingSwitchRow(
                    title = "เปิดระบบสั่น (Haptic Vibration)",
                    checked = vibrateEnabled,
                    onCheckedChange = { vibrateEnabled = it }
                )
                SettingSwitchRow(
                    title = "Live Simulation Mode (จำลองเหตุการณ์อัตโนมัติ)",
                    checked = simulationMode,
                    onCheckedChange = { simulationMode = it }
                )
            }
        }

        // Bot Webhook URL input
        item {
            GlassCard {
                Text(
                    text = "Discord Bot Webhook URL (สำหรับการเชื่อมต่อจริง)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    placeholder = { Text("https://your-bot-server.com/api/events", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        voiceNotifications = voiceNotifications,
                        voiceJoinAlerts = voiceJoinAlerts,
                        voiceLeaveAlerts = voiceLeaveAlerts,
                        messageNotifications = messageNotifications,
                        mentionOnly = mentionOnly,
                        eventReminderMinutes = reminderMinutes,
                        soundEnabled = soundEnabled,
                        vibrateEnabled = vibrateEnabled,
                        simulationMode = simulationMode,
                        discordWebhookUrl = webhookUrl
                    )
                    onSaveSettings(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("บันทึกการตั้งค่า", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (enabled) TextPrimary else TextMuted
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = DiscordBlurpleDark
            )
        )
    }
}

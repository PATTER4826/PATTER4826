package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AppNotificationEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCenterScreen(
    notifications: List<AppNotificationEntity>,
    unreadCount: Int,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onJoinVoice: (String) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm • dd/MM", Locale.getDefault()) }

    val filteredNotifications = remember(notifications, selectedCategory) {
        when (selectedCategory) {
            "VOICE" -> notifications.filter { it.type == "VOICE_JOIN" || it.type == "VOICE_LEAVE" }
            "MESSAGE" -> notifications.filter { it.type == "MESSAGE" || it.type == "MENTION" }
            "EVENT" -> notifications.filter { it.type == "EVENT_REMINDER" }
            "LFG" -> notifications.filter { it.type == "LFG_ALERT" }
            else -> notifications
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Unread count and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ศูนย์แจ้งเตือน",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = NeonRed,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$unreadCount ใหม่",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onMarkAllRead,
                    modifier = Modifier.testTag("mark_all_read_btn")
                ) {
                    Text("อ่านทั้งหมด", fontSize = 12.sp, color = NeonCyan)
                }
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Categories Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val categories = listOf(
                "ALL" to "ทั้งหมด",
                "VOICE" to "🔊 ห้องเสียง",
                "MESSAGE" to "💬 ข้อความ",
                "EVENT" to "⏰ นัดเล่นเกม",
                "LFG" to "👥 หาเพื่อนเล่น"
            )
            items(categories) { (key, label) ->
                FilterChip(
                    selected = selectedCategory == key,
                    onClick = { onCategoryChange(key) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DiscordBlurple,
                        selectedLabelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ไม่มีการแจ้งเตือนในหมวดนี้", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredNotifications) { item ->
                    NotificationCard(
                        notification = item,
                        timeStr = timeFormat.format(Date(item.timestamp)),
                        onAction = {
                            when (item.type) {
                                "VOICE_JOIN", "VOICE_LEAVE" -> {
                                    if (item.targetChannelId != null) {
                                        onJoinVoice(item.targetChannelId)
                                    } else {
                                        onNavigate(AppScreen.VOICE)
                                    }
                                }
                                "MESSAGE", "MENTION" -> onNavigate(AppScreen.CHAT)
                                "EVENT_REMINDER" -> onNavigate(AppScreen.EVENTS)
                                "LFG_ALERT" -> onNavigate(AppScreen.LFG)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotificationEntity,
    timeStr: String,
    onAction: () -> Unit
) {
    val iconColor = when (notification.type) {
        "VOICE_JOIN" -> NeonGreen
        "VOICE_LEAVE" -> NeonRed
        "MESSAGE" -> DiscordBlurple
        "EVENT_REMINDER" -> NeonYellow
        "LFG_ALERT" -> NeonOrange
        else -> NeonCyan
    }

    val iconVector = when (notification.type) {
        "VOICE_JOIN" -> Icons.Default.VolumeUp
        "VOICE_LEAVE" -> Icons.Default.VolumeOff
        "MESSAGE" -> Icons.Default.ChatBubble
        "EVENT_REMINDER" -> Icons.Default.Alarm
        "LFG_ALERT" -> Icons.Default.GroupAdd
        else -> Icons.Default.Notifications
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (!notification.isRead) iconColor.copy(alpha = 0.5f) else BorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onAction)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = notification.body,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entities.GameEventEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun GameEventsScreen(
    events: List<GameEventEntity>,
    onRsvpEvent: (Long, String) -> Unit,
    onCreateEventClick: () -> Unit,
    onDeleteEvent: (Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = DiscordBlurple,
                contentColor = TextPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("สร้างนัดเล่นเกม", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_create_event")
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header info & Reminder note
            item {
                GlassCard(
                    borderColor = NeonCyan.copy(alpha = 0.4f),
                    backgroundColor = DiscordDarker
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ระบบแจ้งเตือนนัดเล่นเกม (Event Reminders)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "แจ้งเตือนก่อนเริ่ม 30 นาที และเมื่อถึงเวลากิจกรรม กดเพื่อวาร์ปเข้าห้องเสียงได้ทันที",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "รายการนัดเล่นเกมทั้งหมด (${events.size} กิจกรรม)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (events.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "ยังไม่มีการนัดเล่นเกม กดปุ่ม + ด้านล่างเพื่อสร้างกิจกรรมแรกของกลุ่ม",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(events) { event ->
                    DetailedEventCard(
                        event = event,
                        onRsvp = { rsvp -> onRsvpEvent(event.id, rsvp) },
                        onDelete = { onDeleteEvent(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedEventCard(
    event: GameEventEntity,
    onRsvp: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (event.userRsvp == "JOINED") NeonGreen.copy(alpha = 0.5f) else BorderSubtle,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Game Badge and Player Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DiscordBlurpleDark,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.game,
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = if (event.currentPlayers >= event.maxPlayers) NeonRed.copy(alpha = 0.2f) else DiscordCardLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ผู้เล่น ${event.currentPlayers} / ${event.maxPlayers} คน",
                        color = if (event.currentPlayers >= event.maxPlayers) NeonRed else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Time & Voice Channel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} • ${event.startTime} - ${event.endTime}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Discord ห้องเสียง: ${event.voiceChannel}",
                    fontSize = 12.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Participants List
            if (event.participantsCsv.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = DiscordDarker,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ผู้เข้าร่วม: ",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = event.participantsCsv.replace(",", ", "),
                            fontSize = 11.sp,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Requirement 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onRsvp("JOINED") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (event.userRsvp == "JOINED") NeonGreen else DiscordBlurple
                    ),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("rsvp_join_${event.id}")
                ) {
                    Icon(
                        if (event.userRsvp == "JOINED") Icons.Default.Check else Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (event.userRsvp == "JOINED") "เข้าร่วมแล้ว" else "เข้าร่วม", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onRsvp("DECLINED") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (event.userRsvp == "DECLINED") NeonRed else TextSecondary
                    ),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("rsvp_decline_${event.id}")
                ) {
                    Text("ไม่เข้าร่วม", fontSize = 12.sp)
                }

                if (event.userRsvp != "NONE") {
                    TextButton(
                        onClick = { onRsvp("NONE") },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("ยกเลิก", fontSize = 12.sp, color = TextMuted)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                }
            }
        }
    }
}

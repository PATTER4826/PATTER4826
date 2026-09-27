package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.CurrentUser
import com.example.data.model.MemberInfo
import com.example.data.model.VoiceChannelInfo
import com.example.ui.components.DiscordAvatar
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun VoiceChannelsScreen(
    voiceChannels: List<VoiceChannelInfo>,
    allMembers: List<MemberInfo>,
    currentUser: CurrentUser,
    onJoinVoice: (String) -> Unit,
    onLeaveVoice: () -> Unit,
    onSimulateVoiceHop: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Voice Status Banner
        item {
            if (currentUser.currentVoiceRoom != null) {
                GlassCard(
                    borderColor = NeonGreen,
                    backgroundColor = DiscordBlurpleDark
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = NeonGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "เชื่อมต่อเสียงอยู่: ${currentUser.currentVoiceRoom}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text("คุณภาพเสียง 128 kbps • สัญญาณเสถียร", fontSize = 11.sp, color = NeonGreen)
                            }
                        }
                        Button(
                            onClick = onLeaveVoice,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                            modifier = Modifier.testTag("leave_voice_btn")
                        ) {
                            Text("ออกจากห้อง", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                GlassCard(
                    borderColor = BorderSubtle,
                    backgroundColor = DiscordDarker
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Headset, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("ยังไม่ได้เข้าห้องเสียง", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("เลือกห้องด้านล่างเพื่อคุยกับเพื่อนในกลุ่ม", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        FilledTonalButton(
                            onClick = onSimulateVoiceHop,
                            modifier = Modifier.testTag("simulate_voice_activity_btn")
                        ) {
                            Text("จำลองคนเข้า/ออก", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "ห้องเสียงทั้งหมดในเซิร์ฟเวอร์ (${voiceChannels.size} ห้อง)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(voiceChannels) { channel ->
            DetailedVoiceChannelCard(
                channel = channel,
                allMembers = allMembers,
                currentUserId = currentUser.id,
                onJoin = { onJoinVoice(channel.id) },
                onLeave = onLeaveVoice
            )
        }
    }
}

@Composable
fun DetailedVoiceChannelCard(
    channel: VoiceChannelInfo,
    allMembers: List<MemberInfo>,
    currentUserId: String,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    val occupants = remember(channel.userIds, allMembers) {
        allMembers.filter { channel.userIds.contains(it.id) }
    }
    val isUserInside = channel.userIds.contains(currentUserId)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isUserInside) NeonGreen else BorderSubtle,
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
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (occupants.isNotEmpty()) NeonGreen else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bitrate: ${channel.bitrateKbps} kbps • สูงสุด ${channel.maxUsers} คน",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isUserInside) {
                    Button(
                        onClick = onLeave,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("ตัดการเชื่อมต่อ", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                        modifier = Modifier.height(36.dp).testTag("join_voice_btn_${channel.id}")
                    ) {
                        Text("เชื่อมต่อเสียง", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            if (occupants.isEmpty()) {
                Text(
                    text = "ไม่มีสมาชิกอยู่ในห้องนี้",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    occupants.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DiscordDarker, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DiscordAvatar(
                                    imageUrl = member.avatarUrl,
                                    displayName = member.displayName,
                                    status = member.status,
                                    size = 28
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = member.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                if (!member.currentGame.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🎮 ${member.currentGame}",
                                        fontSize = 11.sp,
                                        color = NeonCyan
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Speaking indicator",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

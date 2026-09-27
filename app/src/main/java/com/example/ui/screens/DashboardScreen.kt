package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entities.GameEventEntity
import com.example.data.model.CurrentUser
import com.example.data.model.DiscordGuildInfo
import com.example.data.model.FirebaseAuthUserInfo
import com.example.data.model.MemberInfo
import com.example.data.model.UserStatus
import com.example.data.model.VoiceChannelInfo
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@Composable
fun DashboardScreen(
    guildInfo: DiscordGuildInfo,
    currentUser: CurrentUser,
    firebaseUser: FirebaseAuthUserInfo? = null,
    members: List<MemberInfo>,
    voiceChannels: List<VoiceChannelInfo>,
    upcomingEvents: List<GameEventEntity>,
    onNavigate: (AppScreen) -> Unit,
    onJoinVoice: (String) -> Unit,
    onLeaveVoice: () -> Unit,
    onRsvpEvent: (Long, String) -> Unit,
    onMemberClick: (MemberInfo) -> Unit,
    onCreateEventClick: () -> Unit,
    onCreateLfgClick: () -> Unit,
    onConnectDiscordClick: () -> Unit,
    onOpenFirebaseAuthClick: () -> Unit = {},
    onTriggerSimVoice: () -> Unit,
    onTriggerSimChat: () -> Unit
) {
    val onlineMembers = remember(members) {
        members.filter { it.status != UserStatus.OFFLINE }
    }
    val playingMembers = remember(members) {
        members.filter { !it.currentGame.isNullOrBlank() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Guild Server Hero Banner Card
        item {
            ServerHeroCard(
                guildInfo = guildInfo,
                currentUser = currentUser,
                onlineCount = onlineMembers.size,
                playingCount = playingMembers.size,
                onConnectDiscord = onConnectDiscordClick
            )
        }

        // Firebase Auth & Linked Discord Account Management Bar
        item {
            Surface(
                color = DiscordDark,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (currentUser.isFirebaseLinked) NeonGreen.copy(alpha = 0.5f) else NeonOrange.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFirebaseAuthClick() }
                    .testTag("dashboard_firebase_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(if (firebaseUser != null) "🔥" else "🔐", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (firebaseUser != null) {
                                        firebaseUser.displayName ?: firebaseUser.email ?: "Firebase Account"
                                    } else {
                                        "Firebase Authentication"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (currentUser.isFirebaseLinked) NeonGreen.copy(alpha = 0.2f) else if (firebaseUser != null) NeonYellow.copy(alpha = 0.2f) else DiscordBlurpleDark,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (currentUser.isFirebaseLinked) "Discord Linked" else if (firebaseUser != null) "Not Linked" else "Sign In",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUser.isFirebaseLinked) NeonGreen else if (firebaseUser != null) NeonYellow else NeonCyan,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (firebaseUser != null) {
                                    if (currentUser.isFirebaseLinked) "บัญชี Discord ${currentUser.username} ผูกกับ Firebase เรียบร้อย" else "แตะเพื่อผูกบัญชี Discord กับ Firebase ของคุณ"
                                } else {
                                    "แตะเพื่อเข้าสู่ระบบ Firebase สำหรับจัดการบัญชี Discord"
                                },
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Manage Account",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Real-time Simulation & Action Pills
        item {
            QuickActionsBar(
                currentVoiceRoom = currentUser.currentVoiceRoom,
                onLeaveVoice = onLeaveVoice,
                onCreateEvent = onCreateEventClick,
                onCreateLfg = onCreateLfgClick,
                onTriggerVoiceSim = onTriggerSimVoice,
                onTriggerChatSim = onTriggerSimChat
            )
        }

        // 3. Voice Channels Live Section (Requirement 2 & 3)
        item {
            SectionHeader(
                title = "ห้องเสียง (VOICE CHANNELS)",
                subtitle = "อัปเดตสมาชิกที่เชื่อมต่อแบบ Real-time",
                icon = {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = NeonGreen
                    )
                },
                action = {
                    TextButton(onClick = { onNavigate(AppScreen.VOICE) }) {
                        Text("ดูทั้งหมด", color = NeonCyan, fontSize = 13.sp)
                    }
                }
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                voiceChannels.forEach { channel ->
                    VoiceChannelCard(
                        channel = channel,
                        allMembers = members,
                        currentUserId = currentUser.id,
                        onJoin = { onJoinVoice(channel.id) }
                    )
                }
            }
        }

        // 4. Currently Playing Members (Requirement 7)
        item {
            SectionHeader(
                title = "สมาชิกกำลังเล่นเกม (GAME PRESENCE)",
                subtitle = "${playingMembers.size} คนกำลังเล่นผ่าน Discord Presence",
                icon = {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                },
                action = {
                    TextButton(onClick = { onNavigate(AppScreen.MEMBERS) }) {
                        Text("รายชื่อทั้งหมด", color = NeonCyan, fontSize = 13.sp)
                    }
                }
            )

            if (playingMembers.isEmpty()) {
                GlassCard {
                    Text(
                        text = "ยังไม่มีสมาชิกที่เปิดเกมขณะนี้",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playingMembers) { member ->
                        PlayingMemberCard(member = member, onClick = { onMemberClick(member) })
                    }
                }
            }
        }

        // 5. Online Members Quick Bar (Requirement 2 & 9)
        item {
            SectionHeader(
                title = "สมาชิกออนไลน์ (${onlineMembers.size} คน)",
                icon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                }
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(onlineMembers) { member ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onMemberClick(member) }
                    ) {
                        DiscordAvatar(
                            imageUrl = member.avatarUrl,
                            displayName = member.displayName,
                            status = member.status,
                            size = 48
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = member.displayName,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 6. Upcoming Game Events (Requirement 5 & 6)
        item {
            SectionHeader(
                title = "นัดเล่นเกมเร็วๆ นี้ (GAME EVENTS)",
                subtitle = "ระบบเตือนความจำและตอบรับเข้าร่วม",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = NeonPink
                    )
                },
                action = {
                    TextButton(onClick = { onNavigate(AppScreen.EVENTS) }) {
                        Text("ดูตารางนัด", color = NeonCyan, fontSize = 13.sp)
                    }
                }
            )

            if (upcomingEvents.isEmpty()) {
                GlassCard {
                    Text("ยังไม่มีนัดเล่นเกม กดปุ่มเพื่อสร้างนัดใหม่ได้เลย!", color = TextSecondary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    upcomingEvents.take(2).forEach { event ->
                        DashboardEventCard(
                            event = event,
                            onRsvp = { rsvp -> onRsvpEvent(event.id, rsvp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServerHeroCard(
    guildInfo: DiscordGuildInfo,
    currentUser: CurrentUser,
    onlineCount: Int,
    playingCount: Int,
    onConnectDiscord: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderAccent, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDarker)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background image / Gradient banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(DiscordBlurpleDark, DiscordDarkest, Color(0xFF1B0E2E))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = guildInfo.iconUrl,
                            contentDescription = guildInfo.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = guildInfo.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Discord Connected • Verified Group",
                                    fontSize = 11.sp,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // OAuth / Settings button
                    IconButton(
                        onClick = onConnectDiscord,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DiscordCard)
                            .testTag("discord_server_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Discord Settings",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Bar (Members, Online, In Game, Voice)
                Surface(
                    color = DiscordDark.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            count = "${guildInfo.memberCount}",
                            label = "สมาชิกทั้งหมด",
                            dotColor = TextSecondary
                        )
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = BorderSubtle)
                        StatItem(
                            count = "$onlineCount",
                            label = "ออนไลน์",
                            dotColor = NeonGreen
                        )
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = BorderSubtle)
                        StatItem(
                            count = "$playingCount",
                            label = "เล่นเกมอยู่",
                            dotColor = NeonCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, dotColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun QuickActionsBar(
    currentVoiceRoom: String?,
    onLeaveVoice: () -> Unit,
    onCreateEvent: () -> Unit,
    onCreateLfg: () -> Unit,
    onTriggerVoiceSim: () -> Unit,
    onTriggerChatSim: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Voice active banner if connected
        AnimatedVisibility(visible = currentVoiceRoom != null) {
            Surface(
                color = DiscordBlurpleDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "กำลังเชื่อมต่อ: $currentVoiceRoom",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text("ไมค์เปิดใช้งาน • Realtime Voice", color = NeonGreen, fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = onLeaveVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ตัดสาย", fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreateEvent,
                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(44.dp).testTag("quick_create_event_btn")
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("นัดเล่นเกม", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onCreateLfg,
                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(44.dp).testTag("quick_create_lfg_btn")
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("หาเพื่อนเล่น", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Real-time Event Simulator Ticker Pill (Allows user to test notifications immediately!)
        Surface(
            color = DiscordDark,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ทดสอบ Realtime Event:", fontSize = 11.sp, color = TextSecondary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onTriggerVoiceSim,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("test_voice_sim_btn")
                    ) {
                        Text("เข้า/ออกห้องเสียง", fontSize = 10.sp)
                    }
                    FilledTonalButton(
                        onClick = onTriggerChatSim,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("test_chat_sim_btn")
                    ) {
                        Text("จำลองแชท", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceChannelCard(
    channel: VoiceChannelInfo,
    allMembers: List<MemberInfo>,
    currentUserId: String,
    onJoin: () -> Unit
) {
    val occupants = remember(channel.userIds, allMembers) {
        allMembers.filter { channel.userIds.contains(it.id) }
    }
    val isUserInside = channel.userIds.contains(currentUserId)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isUserInside) NeonGreen else BorderSubtle,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = DiscordCardLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${occupants.size}/${channel.maxUsers}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (!isUserInside) {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("join_voice_${channel.id}")
                    ) {
                        Text("เข้าห้อง", fontSize = 12.sp)
                    }
                } else {
                    Surface(
                        color = NeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                    ) {
                        Text(
                            text = "คุณอยู่ในห้องนี้",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (occupants.isEmpty()) {
                Text(
                    text = "— ไม่มีสมาชิกในห้อง —",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 28.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    occupants.forEach { member ->
                        Surface(
                            color = DiscordCardLight,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DiscordAvatar(
                                    imageUrl = member.avatarUrl,
                                    displayName = member.displayName,
                                    size = 20
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = member.displayName,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayingMemberCard(member: MemberInfo, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .width(220.dp)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DiscordAvatar(
                    imageUrl = member.avatarUrl,
                    displayName = member.displayName,
                    status = member.status,
                    size = 36
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = member.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "เล่นมา ${member.gameDurationMinutes} นาที",
                        fontSize = 10.sp,
                        color = NeonGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            GameBadge(
                game = member.currentGame ?: "Game",
                detail = member.gameDetail
            )
        }
    }
}

@Composable
fun DashboardEventCard(
    event: GameEventEntity,
    onRsvp: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🎮 ${event.game} • 🔊 ห้อง ${event.voiceChannel}",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = DiscordCardLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "👥 ${event.currentPlayers}/${event.maxPlayers} คน",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${event.date} • เวลา ${event.startTime} - ${event.endTime} น.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RSVP Action Buttons (Requirement 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onRsvp("JOINED") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (event.userRsvp == "JOINED") NeonGreen else DiscordBlurple
                    ),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(
                        if (event.userRsvp == "JOINED") Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (event.userRsvp == "JOINED") "เข้าร่วมแล้ว" else "เข้าร่วม", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onRsvp("DECLINED") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (event.userRsvp == "DECLINED") NeonRed else TextSecondary
                    ),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("ไม่เข้าร่วม", fontSize = 11.sp)
                }

                if (event.userRsvp != "NONE") {
                    TextButton(
                        onClick = { onRsvp("NONE") },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("ยกเลิก", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

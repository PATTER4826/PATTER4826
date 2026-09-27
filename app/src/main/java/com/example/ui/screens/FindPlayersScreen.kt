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
import com.example.data.local.entities.LfgPostEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun FindPlayersScreen(
    posts: List<LfgPostEntity>,
    onToggleJoin: (LfgPostEntity) -> Unit,
    onCreateLfgClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateLfgClick,
                containerColor = NeonOrange,
                contentColor = TextPrimary,
                icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                text = { Text("โพสต์หาตี้", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_create_lfg")
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
            item {
                GlassCard(
                    borderColor = NeonOrange.copy(alpha = 0.5f),
                    backgroundColor = DiscordDarker
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = NeonOrange,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ระบบหาเพื่อนเล่น (LFG Matchmaking)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "ค้นหาผู้เล่นในเซิร์ฟเวอร์ Discord ระบุระดับการเล่น เวลาเริ่ม และห้องเสียงได้ทันที",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ปาร์ตี้ที่กำลังรับสมัคร (${posts.size} โพสต์)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (posts.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "ยังไม่มีโพสต์หาเพื่อนเล่นเกม กดปุ่มเพื่อเป็นคนแรกที่เปิดห้อง!",
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(posts) { post ->
                    LfgPostCard(
                        post = post,
                        onJoin = { onToggleJoin(post) }
                    )
                }
            }
        }
    }
}

@Composable
fun LfgPostCard(
    post: LfgPostEntity,
    onJoin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (post.hasJoined) NeonGreen else BorderSubtle,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = NeonOrange.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.game,
                            color = NeonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = DiscordCardLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "👥 ต้องการอีก ${post.neededPlayers} คน",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Details pills (Time, Skill, Voice)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = DiscordDarker,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "⏰ ${post.startTime}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = DiscordDarker,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🎯 ${post.skillLevel}",
                        fontSize = 11.sp,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = DiscordDarker,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🔊 ${post.voiceChannel}",
                        fontSize = 11.sp,
                        color = NeonGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "โพสต์โดย ${post.creatorName}",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (post.hasJoined) NeonGreen else DiscordBlurple
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("join_lfg_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.hasJoined) Icons.Default.Check else Icons.Default.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (post.hasJoined) "คุณอยู่ในทีมนี้แล้ว (กดเพื่อออก)" else "เข้าร่วมทีม",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

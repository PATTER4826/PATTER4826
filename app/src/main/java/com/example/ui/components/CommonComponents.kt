package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.MemberInfo
import com.example.data.model.UserStatus
import com.example.ui.theme.*

@Composable
fun StatusIndicatorDot(
    status: UserStatus,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    val color = when (status) {
        UserStatus.ONLINE -> NeonGreen
        UserStatus.IDLE -> NeonYellow
        UserStatus.DND -> NeonRed
        UserStatus.OFFLINE -> TextMuted
    }
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, DiscordDarkest, CircleShape)
    )
}

@Composable
fun DiscordAvatar(
    imageUrl: String?,
    displayName: String,
    status: UserStatus? = null,
    size: Int = 42,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size.dp)) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$displayName avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(DiscordCardLight)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(DiscordBlurple)
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size * 0.45).sp
                )
            }
        }

        if (status != null) {
            StatusIndicatorDot(
                status = status,
                size = (size * 0.32).toInt().coerceAtLeast(10),
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DiscordDark,
    borderColor: Color = BorderSubtle,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun GameBadge(
    game: String,
    detail: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DiscordBlurpleDark.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DiscordBlurple.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = game,
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!detail.isNullOrBlank()) {
                    Text(
                        text = detail,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MemberProfileDialog(
    member: MemberInfo,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarker),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DiscordBlurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner & Avatar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DiscordBlurple, NeonCyan.copy(alpha = 0.6f))
                            )
                        )
                )

                DiscordAvatar(
                    imageUrl = member.avatarUrl,
                    displayName = member.displayName,
                    status = member.status,
                    size = 72,
                    modifier = Modifier.offset(y = (-36).dp)
                )

                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = member.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // Role tag
                Surface(
                    color = DiscordCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "★ ${member.roleName}",
                        color = Color(android.graphics.Color.parseColor(member.roleColorHex)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activity status card
                if (!member.currentGame.isNullOrBlank()) {
                    Surface(
                        color = DiscordDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "กำลังเล่นเกม (GAME PRESENCE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(member.currentGame, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (!member.gameDetail.isNullOrBlank()) {
                                        Text(member.gameDetail, color = TextSecondary, fontSize = 12.sp)
                                    }
                                    if (member.gameDurationMinutes > 0) {
                                        Text("เล่นมาแล้ว ${member.gameDurationMinutes} นาที", color = NeonGreen, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Voice status
                if (!member.voiceChannelId.isNullOrBlank()) {
                    Surface(
                        color = DiscordDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "อยู่ในห้องเสียง Discord",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Favorite games
                if (member.favoriteGames.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "เกมโปรด",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            member.favoriteGames.forEach { g ->
                                Surface(
                                    color = DiscordCardLight,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = g,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("ปิด")
                    }
                    Button(
                        onClick = {
                            onSendMessage("@${member.displayName} ")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("แท็กในแชท")
                    }
                }
            }
        }
    }
}

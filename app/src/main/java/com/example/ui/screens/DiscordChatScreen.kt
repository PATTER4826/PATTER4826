package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.model.CurrentUser
import com.example.data.model.TextChannelInfo
import com.example.ui.components.DiscordAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiscordChatScreen(
    textChannels: List<TextChannelInfo>,
    selectedChannelId: String,
    messages: List<ChatMessageEntity>,
    currentUser: CurrentUser,
    onSelectChannel: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscordDarkest)
    ) {
        // Channel Selector Tabs
        Surface(
            color = DiscordDarker,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(textChannels) { ch ->
                    val isSelected = ch.id == selectedChannelId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectChannel(ch.id) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#", color = if (isSelected) NeonCyan else TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(ch.name, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DiscordBlurple,
                            selectedLabelColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("channel_${ch.name}")
                    )
                }
            }
        }

        // Selected Channel Topic Bar
        val currentChannel = textChannels.find { it.id == selectedChannelId }
        Surface(
            color = DiscordDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Tag, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentChannel?.name ?: "channel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                if (!currentChannel?.topic.isNullOrBlank()) {
                    Text(" — ", color = TextMuted, fontSize = 12.sp)
                    Text(
                        text = currentChannel?.topic ?: "",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ยังไม่มีข้อความในห้องนี้", color = TextSecondary, fontSize = 14.sp)
                        Text("พิมพ์ข้อความแรกเพื่อทักทายเพื่อนๆ", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages) { msg ->
                        ChatMessageItem(
                            message = msg,
                            isSelf = msg.userId == currentUser.id,
                            timeStr = timeFormat.format(Date(msg.timestamp))
                        )
                    }
                }
            }
        }

        // Quick Preset Chips
        Surface(
            color = DiscordDarker,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    "🎮 มีใครเล่น Valorant ไหม?",
                    "🔊 อยู่ในห้อง Gaming Room นะ",
                    "⏰ อีก 10 นาทีพร้อมเล่น",
                    "🔥 GG WP ทุกคน!"
                )
                items(presets) { p ->
                    Surface(
                        color = DiscordDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { textInput = p }
                    ) {
                        Text(
                            text = p,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Message Input Bar (Requirement 10)
        Surface(
            color = DiscordDarker,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp) // Avoid overlap with bottom bar on mobile
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "ส่งข้อความไปยัง #${currentChannel?.name ?: "channel"}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DiscordDark,
                        unfocusedContainerColor = DiscordDark,
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) DiscordBlurple else DiscordCard)
                        .testTag("chat_send_button"),
                    enabled = textInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (textInput.isNotBlank()) Color.White else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isSelf: Boolean,
    timeStr: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        if (!isSelf) {
            DiscordAvatar(
                imageUrl = message.userAvatar,
                displayName = message.userName,
                size = 36
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSelf) "คุณ" else message.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSelf) NeonCyan else TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = if (isSelf) DiscordBlurpleDark else DiscordDark,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isSelf) 14.dp else 2.dp,
                    bottomEnd = if (isSelf) 2.dp else 14.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelf) DiscordBlurple.copy(alpha = 0.5f) else BorderSubtle
                )
            ) {
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        if (isSelf) {
            Spacer(modifier = Modifier.width(10.dp))
            DiscordAvatar(
                imageUrl = message.userAvatar,
                displayName = message.userName,
                size = 36
            )
        }
    }
}

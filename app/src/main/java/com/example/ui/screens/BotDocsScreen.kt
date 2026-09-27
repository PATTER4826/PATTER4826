package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun BotDocsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("สถาปัตยกรรมระบบ", "Node.js Bot Code", "Python Bot Code", "Database SQL Schema")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Discord Bot Backend & Database Integration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "โครงสร้างเชื่อมต่อจริงระหว่าง Discord Gateway กับ Hub Application",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DiscordDarker,
            contentColor = NeonCyan,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { ArchitectureFlowCard() }
                    item { GatewayIntentsCard() }
                }
                1 -> {
                    item { NodeJsBotCodeCard() }
                }
                2 -> {
                    item { PythonBotCodeCard() }
                }
                3 -> {
                    item { DatabaseSqlSchemaCard() }
                }
            }
        }
    }
}

@Composable
fun ArchitectureFlowCard() {
    GlassCard(
        borderColor = DiscordBlurple,
        backgroundColor = DiscordDark
    ) {
        Text(
            text = "🔄 Flow การทำงานของ Real-time Event System",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(10.dp))

        val steps = listOf(
            "1. Discord Server สมาชิกเข้า/ออกห้องเสียง หรือพิมพ์แชท",
            "2. Discord Gateway ส่ง Event (VOICE_STATE_UPDATE, MESSAGE_CREATE) ไปยัง Discord Bot",
            "3. Discord Bot (Node.js / Python) ดักจับ Event ตรวจสอบ Guild ID",
            "4. Bot ยิง Webhook POST ไปยัง Cloud API / Mobile Hub",
            "5. Mobile Hub บันทึกลง Room Database และสั่งเด้ง Notification (🔔 เสียง/การสั่น)",
            "6. หน้า Dashboard / Voice / Chat อัปเดต UI แบบ Real-time ทันที"
        )
        steps.forEach { step ->
            Text(
                text = step,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun GatewayIntentsCard() {
    GlassCard(
        borderColor = NeonYellow.copy(alpha = 0.5f),
        backgroundColor = DiscordDark
    ) {
        Text(
            text = "⚙️ Discord Developer Portal Privileged Intents",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NeonYellow
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ก่อนรันบอทจริง ต้องเปิด 3 สิทธิ์นี้ใน discord.com/developers -> Bot Settings:",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        val intents = listOf(
            "✓ Server Members Intent: เพื่อซิงก์รายชื่อสมาชิกและสถานะการเข้าออก",
            "✓ Presence Intent: เพื่ออ่านสถานะเกมที่กำลังเล่น (Valorant, Minecraft, ฯลฯ)",
            "✓ Message Content Intent: เพื่อรับข้อความแจ้งเตือนเมื่อมีคนพิมพ์แชทในช่องที่เลือก"
        )
        intents.forEach { i ->
            Text(text = i, fontSize = 11.sp, color = NeonGreen, modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}

@Composable
fun NodeJsBotCodeCard() {
    val code = """// discord-hub-bot.js
const { Client, GatewayIntentBits, Partials } = require('discord.js');
const axios = require('axios');

const client = new Client({
  intents: [
    GatewayIntentBits.Guilds,
    GatewayIntentBits.GuildVoiceStates,
    GatewayIntentBits.GuildMessages,
    GatewayIntentBits.MessageContent,
    GatewayIntentBits.GuildPresences,
    GatewayIntentBits.GuildMembers
  ]
});

const HUB_WEBHOOK_URL = process.env.HUB_WEBHOOK_URL;

// 1. ดักจับสมาชิกเข้า/ออกจากห้องเสียง (VOICE_STATE_UPDATE)
client.on('voiceStateUpdate', async (oldState, newState) => {
  const member = newState.member || oldState.member;
  if (!member || member.user.bot) return;

  const eventPayload = {
    type: newState.channelId ? 'VOICE_JOIN' : 'VOICE_LEAVE',
    guildId: newState.guild.id,
    userId: member.id,
    username: member.user.username,
    displayName: member.displayName,
    avatarUrl: member.user.displayAvatarURL(),
    channelId: newState.channelId || oldState.channelId,
    channelName: newState.channel?.name || oldState.channel?.name,
    timestamp: Date.now()
  };

  await axios.post(HUB_WEBHOOK_URL + '/events/voice', eventPayload);
});

// 2. ดักจับข้อความแชทใหม่ (MESSAGE_CREATE)
client.on('messageCreate', async (message) => {
  if (message.author.bot) return;

  const payload = {
    type: 'MESSAGE',
    guildId: message.guildId,
    channelId: message.channelId,
    channelName: message.channel.name,
    userId: message.author.id,
    username: message.author.username,
    content: message.content,
    timestamp: message.createdTimestamp
  };

  await axios.post(HUB_WEBHOOK_URL + '/events/chat', payload);
});

client.login(process.env.DISCORD_BOT_TOKEN);"""

    CodeViewerCard(title = "Node.js (Discord.js v14) Backend Script", code = code)
}

@Composable
fun PythonBotCodeCard() {
    val code = """# bot.py
import discord
import aiohttp
import os

intents = discord.Intents.default()
intents.members = True
intents.presences = True
intents.message_content = True
intents.voice_states = True

bot = discord.Client(intents=intents)
HUB_API_URL = os.getenv("HUB_WEBHOOK_URL")

@bot.event
async def on_voice_state_update(member, before, after):
    if member.bot:
        return
    
    event_type = "VOICE_JOIN" if after.channel else "VOICE_LEAVE"
    ch = after.channel if after.channel else before.channel
    
    payload = {
        "type": event_type,
        "guildId": str(member.guild.id),
        "userId": str(member.id),
        "displayName": member.display_name,
        "channelName": ch.name if ch else "Unknown",
        "timestamp": int(discord.utils.utcnow().timestamp() * 1000)
    }
    
    async with aiohttp.ClientSession() as session:
        await session.post(f"{HUB_API_URL}/events/voice", json=payload)

bot.run(os.getenv("DISCORD_BOT_TOKEN"))"""

    CodeViewerCard(title = "Python (Discord.py) Backend Script", code = code)
}

@Composable
fun DatabaseSqlSchemaCard() {
    val sql = """-- PostgreSQL / Supabase Schema for Discord Hub
CREATE TABLE users (
  id VARCHAR(64) PRIMARY KEY,
  discord_id VARCHAR(64) UNIQUE NOT NULL,
  username VARCHAR(64) NOT NULL,
  display_name VARCHAR(64),
  avatar_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE guilds (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  icon_url TEXT,
  owner_id VARCHAR(64) REFERENCES users(id)
);

CREATE TABLE voice_channels (
  id VARCHAR(64) PRIMARY KEY,
  guild_id VARCHAR(64) REFERENCES guilds(id) ON DELETE CASCADE,
  name VARCHAR(64) NOT NULL,
  bitrate INT DEFAULT 64000,
  user_limit INT DEFAULT 10
);

CREATE TABLE game_events (
  id BIGSERIAL PRIMARY KEY,
  guild_id VARCHAR(64) REFERENCES guilds(id) ON DELETE CASCADE,
  title VARCHAR(128) NOT NULL,
  game_name VARCHAR(64) NOT NULL,
  event_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  max_players INT DEFAULT 10,
  voice_channel_name VARCHAR(64),
  creator_name VARCHAR(64),
  description TEXT
);

CREATE TABLE lfg_posts (
  id BIGSERIAL PRIMARY KEY,
  guild_id VARCHAR(64) REFERENCES guilds(id) ON DELETE CASCADE,
  game_name VARCHAR(64) NOT NULL,
  needed_players INT NOT NULL,
  start_time VARCHAR(32) NOT NULL,
  skill_level VARCHAR(32),
  voice_channel VARCHAR(64),
  creator_name VARCHAR(64),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);"""

    CodeViewerCard(title = "PostgreSQL / Supabase Ready-to-Run Schema", code = sql)
}

@Composable
fun CodeViewerCard(title: String, code: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DiscordDarkest),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090A0E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFC5C8D4),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

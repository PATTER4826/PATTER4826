package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_events")
data class GameEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val game: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val maxPlayers: Int,
    val currentPlayers: Int,
    val description: String,
    val voiceChannel: String,
    val creatorName: String,
    val userRsvp: String = "NONE", // "JOINED", "DECLINED", "NONE"
    val participantsCsv: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentUrl: String? = null
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // VOICE_JOIN, VOICE_LEAVE, MESSAGE, GAME_ACTIVITY, EVENT_REMINDER, LFG_ALERT
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val targetChannelId: String? = null,
    val isRead: Boolean = false
)

@Entity(tableName = "lfg_posts")
data class LfgPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val game: String,
    val neededPlayers: Int,
    val currentPlayers: Int,
    val startTime: String,
    val skillLevel: String,
    val voiceChannel: String,
    val creatorName: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasJoined: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val discordConnected: Boolean = true,
    val discordUsername: String = "PlayerOne#0001",
    val discordDisplayName: String = "PlayerOne",
    val discordAvatarUrl: String = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
    val discordGuildId: String = "109823487123987123",
    val discordGuildName: String = "Siam Gaming Squad",
    val discordBotToken: String = "",
    val discordWebhookUrl: String = "",
    val voiceNotifications: Boolean = true,
    val voiceJoinAlerts: Boolean = true,
    val voiceLeaveAlerts: Boolean = true,
    val messageNotifications: Boolean = true,
    val messageChannelFilter: String = "all",
    val mentionOnly: Boolean = false,
    val eventReminderMinutes: Int = 30,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val simulationMode: Boolean = true,
    val firebaseUid: String? = null,
    val firebaseEmail: String? = null,
    val firebaseIsAnonymous: Boolean = false,
    val firebaseLinkedDiscord: Boolean = false,
    val firebaseLastSignIn: Long = 0L
)

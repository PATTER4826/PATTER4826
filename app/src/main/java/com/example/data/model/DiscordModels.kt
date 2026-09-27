package com.example.data.model

enum class UserStatus(val label: String) {
    ONLINE("Online"),
    IDLE("Idle"),
    DND("Do Not Disturb"),
    OFFLINE("Offline")
}

data class MemberInfo(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val status: UserStatus,
    val currentGame: String? = null,
    val gameDetail: String? = null,
    val gameDurationMinutes: Int = 0,
    val voiceChannelId: String? = null,
    val favoriteGames: List<String> = emptyList(),
    val roleName: String = "Member",
    val roleColorHex: String = "#5865F2",
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false
)

data class VoiceChannelInfo(
    val id: String,
    val name: String,
    val category: String = "VOICE CHANNELS",
    val userIds: List<String> = emptyList(),
    val maxUsers: Int = 10,
    val bitrateKbps: Int = 64
)

data class TextChannelInfo(
    val id: String,
    val name: String,
    val category: String = "TEXT CHANNELS",
    val unreadCount: Int = 0,
    val topic: String = ""
)

data class DiscordGuildInfo(
    val id: String,
    val name: String,
    val iconUrl: String,
    val memberCount: Int,
    val onlineCount: Int,
    val playingCount: Int,
    val voiceCount: Int,
    val bannerUrl: String? = null
)

enum class NotificationType(val label: String, val iconSymbol: String) {
    VOICE_JOIN("Voice Join", "🔊"),
    VOICE_LEAVE("Voice Leave", "🚪"),
    MESSAGE("Discord Message", "💬"),
    GAME_ACTIVITY("Game Activity", "🎮"),
    EVENT_REMINDER("Event Reminder", "⏰"),
    LFG_ALERT("Find Players", "👥"),
    MENTION("Mention", "🔔")
}

enum class EventRsvpStatus {
    JOINED,
    DECLINED,
    NONE
}

data class FirebaseAuthUserInfo(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = false,
    val isDiscordLinked: Boolean = false,
    val linkedDiscordUsername: String? = null,
    val linkedGuildId: String? = null,
    val creationTimestamp: Long = 0L,
    val lastSignInTimestamp: Long = 0L
)

data class CurrentUser(
    val id: String = "usr_current",
    val username: String = "PlayerOne#0001",
    val displayName: String = "PlayerOne",
    val avatarUrl: String = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
    val status: UserStatus = UserStatus.ONLINE,
    val currentGame: String? = "Valorant",
    val currentVoiceRoom: String? = "Gaming Room",
    val isConnected: Boolean = true,
    val firebaseUid: String? = null,
    val firebaseEmail: String? = null,
    val isFirebaseAnonymous: Boolean = false,
    val isFirebaseLinked: Boolean = false
)

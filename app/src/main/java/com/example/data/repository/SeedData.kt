package com.example.data.repository

import com.example.data.local.entities.AppNotificationEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.GameEventEntity
import com.example.data.local.entities.LfgPostEntity
import com.example.data.local.entities.UserSettingsEntity
import com.example.data.model.DiscordGuildInfo
import com.example.data.model.MemberInfo
import com.example.data.model.TextChannelInfo
import com.example.data.model.UserStatus
import com.example.data.model.VoiceChannelInfo

object SeedData {

    val defaultGuild = DiscordGuildInfo(
        id = "109823487123987123",
        name = "Siam Gaming Squad",
        iconUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
        memberCount = 42,
        onlineCount = 18,
        playingCount = 12,
        voiceCount = 6,
        bannerUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800"
    )

    val defaultMembers = listOf(
        MemberInfo(
            id = "user_01",
            username = "NeonStrike#1337",
            displayName = "User01",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            status = UserStatus.ONLINE,
            currentGame = "Valorant",
            gameDetail = "Competitive • Haven (Score 9-7)",
            gameDurationMinutes = 42,
            voiceChannelId = "vc_gaming",
            favoriteGames = listOf("Valorant", "CS2", "Apex Legends"),
            roleName = "Guild Leader",
            roleColorHex = "#FEE75C"
        ),
        MemberInfo(
            id = "user_02",
            username = "ShadowBlade#2049",
            displayName = "User02",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
            status = UserStatus.ONLINE,
            currentGame = "Minecraft",
            gameDetail = "Survival Realm • Building Castle",
            gameDurationMinutes = 115,
            voiceChannelId = "vc_valorant",
            favoriteGames = listOf("Minecraft", "GTA V", "Terraria"),
            roleName = "Officer",
            roleColorHex = "#57F287"
        ),
        MemberInfo(
            id = "user_03",
            username = "CyberViper#9921",
            displayName = "User03",
            avatarUrl = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=150",
            status = UserStatus.OFFLINE,
            currentGame = null,
            gameDetail = null,
            gameDurationMinutes = 0,
            voiceChannelId = null,
            favoriteGames = listOf("GTA V", "Dota 2"),
            roleName = "Member",
            roleColorHex = "#949BA4"
        ),
        MemberInfo(
            id = "user_04",
            username = "Astraea#7712",
            displayName = "User04",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            status = UserStatus.IDLE,
            currentGame = "League of Legends",
            gameDetail = "ARAM • Lobby",
            gameDurationMinutes = 15,
            voiceChannelId = "vc_chill",
            favoriteGames = listOf("League of Legends", "Genshin Impact", "Valorant"),
            roleName = "Member",
            roleColorHex = "#949BA4"
        ),
        MemberInfo(
            id = "user_05",
            username = "PixelKnight#4412",
            displayName = "User05",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            status = UserStatus.ONLINE,
            currentGame = "Valorant",
            gameDetail = "In Competitive Match",
            gameDurationMinutes = 35,
            voiceChannelId = "vc_gaming",
            favoriteGames = listOf("Valorant", "Overwatch 2"),
            roleName = "VIP",
            roleColorHex = "#EB459E"
        ),
        MemberInfo(
            id = "user_06",
            username = "EchoFox#3110",
            displayName = "User06",
            avatarUrl = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150",
            status = UserStatus.DND,
            currentGame = "GTA V",
            gameDetail = "Online Heist Prep",
            gameDurationMinutes = 80,
            voiceChannelId = null,
            favoriteGames = listOf("GTA V", "Cyberpunk 2077"),
            roleName = "Member",
            roleColorHex = "#949BA4"
        ),
        MemberInfo(
            id = "user_07",
            username = "GhostRecon#8891",
            displayName = "User07",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
            status = UserStatus.ONLINE,
            currentGame = "Apex Legends",
            gameDetail = "Trios Ranked",
            gameDurationMinutes = 55,
            voiceChannelId = "vc_valorant",
            favoriteGames = listOf("Apex Legends", "Call of Duty"),
            roleName = "Member",
            roleColorHex = "#949BA4"
        ),
        MemberInfo(
            id = "user_08",
            username = "VortexGamer#5501",
            displayName = "User08",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
            status = UserStatus.ONLINE,
            currentGame = "Valorant",
            gameDetail = "Custom 5v5 Practice",
            gameDurationMinutes = 20,
            voiceChannelId = "vc_gaming",
            favoriteGames = listOf("Valorant", "Rocket League"),
            roleName = "Member",
            roleColorHex = "#949BA4"
        )
    )

    val defaultVoiceChannels = listOf(
        VoiceChannelInfo(
            id = "vc_gaming",
            name = "Gaming Room",
            category = "VOICE CHANNELS",
            userIds = listOf("user_01", "user_05", "user_08"),
            maxUsers = 10,
            bitrateKbps = 128
        ),
        VoiceChannelInfo(
            id = "vc_valorant",
            name = "Valorant 5v5",
            category = "VOICE CHANNELS",
            userIds = listOf("user_02", "user_07"),
            maxUsers = 5,
            bitrateKbps = 96
        ),
        VoiceChannelInfo(
            id = "vc_chill",
            name = "Chill Lounge",
            category = "VOICE CHANNELS",
            userIds = listOf("user_04"),
            maxUsers = 20,
            bitrateKbps = 64
        ),
        VoiceChannelInfo(
            id = "vc_afk",
            name = "AFK",
            category = "VOICE CHANNELS",
            userIds = emptyList(),
            maxUsers = 50,
            bitrateKbps = 32
        )
    )

    val defaultTextChannels = listOf(
        TextChannelInfo(id = "tc_general", name = "general", topic = "พูดคุยทั่วไป ทักทายสมาชิกกลุ่มเกม", unreadCount = 2),
        TextChannelInfo(id = "tc_gaming", name = "gaming-chat", topic = "ห้องคุยเกม นัดเล่น แชร์ไฮไลท์", unreadCount = 0),
        TextChannelInfo(id = "tc_valorant", name = "valorant-lfg", topic = "หาตี้ Valorant ลงแรงค์ไต่แรงค์", unreadCount = 1),
        TextChannelInfo(id = "tc_announcements", name = "announcements", topic = "ข่าวสารและกิจกรรมกิลด์", unreadCount = 0)
    )

    val initialEvents = listOf(
        GameEventEntity(
            title = "เล่น Valorant กัน คืนนี้",
            game = "Valorant",
            date = "28 กันยายน 2026",
            startTime = "20:00",
            endTime = "23:00",
            maxPlayers = 10,
            currentPlayers = 5,
            description = "นัดซ้อมทีม 5v5 แข่งขำๆ ในกลุ่ม มีห้องแยกสองทีมและห้องรวม ใครว่างมากดจองที่ได้เลย!",
            voiceChannel = "Valorant 5v5",
            creatorName = "User01",
            userRsvp = "JOINED",
            participantsCsv = "User01,User02,User05,User08,PlayerOne"
        ),
        GameEventEntity(
            title = "Minecraft Server Survival Night",
            game = "Minecraft",
            date = "29 กันยายน 2026",
            startTime = "21:00",
            endTime = "01:00",
            maxPlayers = 8,
            currentPlayers = 4,
            description = "ลงเหมืองหาเพชรและสร้างปราสาทกลางเซิร์ฟเวอร์ Survival กิลด์ มือใหม่ก็มาร่วมสนุกได้",
            voiceChannel = "Gaming Room",
            creatorName = "User02",
            userRsvp = "NONE",
            participantsCsv = "User02,User04,User06,User07"
        ),
        GameEventEntity(
            title = "Apex Legends Ranked Grind",
            game = "Apex Legends",
            date = "30 กันยายน 2026",
            startTime = "19:30",
            endTime = "22:30",
            maxPlayers = 3,
            currentPlayers = 2,
            description = "ไต่แรงค์ Platinum ไป Diamond ขอคนที่ฟังคอลและสื่อสารได้ดี มีห้องเสียงพร้อม",
            voiceChannel = "Gaming Room",
            creatorName = "User07",
            userRsvp = "NONE",
            participantsCsv = "User07,User05"
        )
    )

    val initialLfgPosts = listOf(
        LfgPostEntity(
            game = "Valorant",
            neededPlayers = 2,
            currentPlayers = 3,
            startTime = "20:00",
            skillLevel = "Platinum+",
            voiceChannel = "Valorant 5v5",
            creatorName = "User01",
            description = "ขาดอีก 2 คนลง Competitive ด่วน! เล่น Duelist หรือ Initiator ได้",
            hasJoined = false
        ),
        LfgPostEntity(
            game = "GTA V",
            neededPlayers = 1,
            currentPlayers = 3,
            startTime = "21:30",
            skillLevel = "Casual",
            voiceChannel = "Gaming Room",
            creatorName = "User06",
            description = "หา 1 คนช่วยทำ Casino Heist จบไวได้เงินเยอะ ไม่จำกัดเลเวล",
            hasJoined = false
        ),
        LfgPostEntity(
            game = "Minecraft",
            neededPlayers = 3,
            currentPlayers = 5,
            startTime = "ตอนนี้เลย",
            skillLevel = "Any Level",
            voiceChannel = "Chill Lounge",
            creatorName = "User02",
            description = "สร้างบ้านชิลๆ ฟังเพลงในห้องเสียง มาแวะเล่นด้วยกันได้ครับ",
            hasJoined = true
        )
    )

    val initialMessages = listOf(
        ChatMessageEntity(
            channelId = "tc_general",
            userId = "user_01",
            userName = "User01",
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            content = "คืนนี้มีใครเล่นเกมไหม? จัด Valorant 5v5 หรือ Minecraft ดี",
            timestamp = System.currentTimeMillis() - 15 * 60 * 1000
        ),
        ChatMessageEntity(
            channelId = "tc_general",
            userId = "user_02",
            userName = "User02",
            userAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
            content = "ผมพร้อม Valorant 20:00 ครับ แล้วหลังสี่ทุ่มไปต่อ Minecraft",
            timestamp = System.currentTimeMillis() - 12 * 60 * 1000
        ),
        ChatMessageEntity(
            channelId = "tc_general",
            userId = "user_05",
            userName = "User05",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            content = "ผมกำลังวอร์มใน Deathmatch เดี๋ยว 20:00 เข้าห้อง Gaming Room เลย",
            timestamp = System.currentTimeMillis() - 5 * 60 * 1000
        ),
        ChatMessageEntity(
            channelId = "tc_gaming",
            userId = "user_08",
            userName = "User08",
            userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
            content = "เพิ่งได้ Ace รอบที่แล้ว เดี๋ยวตัดคลิปมาลงห้องคลิป!",
            timestamp = System.currentTimeMillis() - 8 * 60 * 1000
        )
    )

    val initialNotifications = listOf(
        AppNotificationEntity(
            type = "VOICE_JOIN",
            title = "🔔 สมาชิกเข้าห้องเสียง",
            body = "User01 เข้าห้อง Gaming Room แล้ว",
            timestamp = System.currentTimeMillis() - 5 * 60 * 1000,
            targetChannelId = "vc_gaming",
            isRead = false
        ),
        AppNotificationEntity(
            type = "MESSAGE",
            title = "💬 ข้อความใหม่จาก #general",
            body = "User01: \"คืนนี้มีใครเล่นเกมไหม?\"",
            timestamp = System.currentTimeMillis() - 15 * 60 * 1000,
            targetChannelId = "tc_general",
            isRead = false
        ),
        AppNotificationEntity(
            type = "EVENT_REMINDER",
            title = "⏰ อีก 30 นาทีจะถึงเวลานัดเล่น",
            body = "กิจกรรม: เล่น Valorant กัน คืนนี้ (ห้อง Valorant 5v5)",
            timestamp = System.currentTimeMillis() - 25 * 60 * 1000,
            targetChannelId = "vc_valorant",
            isRead = true
        )
    )
}

package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppNotificationEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.GameEventEntity
import com.example.data.local.entities.LfgPostEntity
import com.example.data.local.entities.UserSettingsEntity
import com.example.data.model.CurrentUser
import com.example.data.model.DiscordGuildInfo
import com.example.data.model.MemberInfo
import com.example.data.model.TextChannelInfo
import com.example.data.model.UserStatus
import com.example.data.model.VoiceChannelInfo
import com.example.data.auth.FirebaseAuthService
import com.example.data.auth.oauth.DiscordOAuthService
import com.example.data.auth.oauth.DiscordUserProfile
import com.example.data.auth.oauth.OAuthSessionState
import com.example.data.model.FirebaseAuthUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class DiscordHubRepository(
    private val database: AppDatabase,
    val authService: FirebaseAuthService,
    val oauthService: DiscordOAuthService,
    private val scope: CoroutineScope
) {
    private val gameEventDao = database.gameEventDao()
    private val chatMessageDao = database.chatMessageDao()
    private val notificationDao = database.notificationDao()
    private val lfgDao = database.lfgDao()
    private val userSettingsDao = database.userSettingsDao()

    // Room reactive streams
    val gameEvents: Flow<List<GameEventEntity>> = gameEventDao.getAllEvents()
    val lfgPosts: Flow<List<LfgPostEntity>> = lfgDao.getAllLfgPosts()
    val notifications: Flow<List<AppNotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getSettings()

    // Firebase Auth StateFlow
    val firebaseUser: StateFlow<FirebaseAuthUserInfo?> = authService.currentUserState

    // OAuth2 Service StateFlow
    val oauthSessionState: StateFlow<OAuthSessionState> = oauthService.sessionState

    // In-memory real-time Discord presence & channels
    private val _guildInfo = MutableStateFlow(SeedData.defaultGuild)
    val guildInfo: StateFlow<DiscordGuildInfo> = _guildInfo.asStateFlow()

    private val _members = MutableStateFlow(SeedData.defaultMembers)
    val members: StateFlow<List<MemberInfo>> = _members.asStateFlow()

    private val _voiceChannels = MutableStateFlow(SeedData.defaultVoiceChannels)
    val voiceChannels: StateFlow<List<VoiceChannelInfo>> = _voiceChannels.asStateFlow()

    private val _textChannels = MutableStateFlow(SeedData.defaultTextChannels)
    val textChannels: StateFlow<List<TextChannelInfo>> = _textChannels.asStateFlow()

    private val _currentUser = MutableStateFlow(CurrentUser())
    val currentUser: StateFlow<CurrentUser> = _currentUser.asStateFlow()

    // Toast/Alert message events to show snackbar
    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        scope.launch(Dispatchers.IO) {
            initSeedIfEmpty()
        }
    }

    private suspend fun initSeedIfEmpty() {
        val existingEvents = gameEvents.first()
        if (existingEvents.isEmpty()) {
            gameEventDao.insertEvents(SeedData.initialEvents)
        }
        val existingLfg = lfgPosts.first()
        if (existingLfg.isEmpty()) {
            lfgDao.insertLfgPosts(SeedData.initialLfgPosts)
        }
        val existingNotifs = notifications.first()
        if (existingNotifs.isEmpty()) {
            notificationDao.insertNotifications(SeedData.initialNotifications)
        }
        val existingMsgs = chatMessageDao.getRecentMessages().first()
        if (existingMsgs.isEmpty()) {
            chatMessageDao.insertMessages(SeedData.initialMessages)
        }
        val settings = userSettingsDao.getSettings().first()
        if (settings == null) {
            userSettingsDao.insertOrUpdate(UserSettingsEntity())
        }
    }

    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesForChannel(channelId)
    }

    suspend fun joinVoiceChannel(channelId: String) {
        val targetChannel = _voiceChannels.value.find { it.id == channelId } ?: return
        val currentUserId = _currentUser.value.id
        val currentName = _currentUser.value.displayName

        // Update voice channels list
        val updatedChannels = _voiceChannels.value.map { ch ->
            val userListWithoutCurrent = ch.userIds.filter { it != currentUserId }
            if (ch.id == channelId) {
                ch.copy(userIds = userListWithoutCurrent + currentUserId)
            } else {
                ch.copy(userIds = userListWithoutCurrent)
            }
        }
        _voiceChannels.value = updatedChannels
        _currentUser.value = _currentUser.value.copy(currentVoiceRoom = targetChannel.name)

        // Notification
        val notif = AppNotificationEntity(
            type = "VOICE_JOIN",
            title = "🔊 เชื่อมต่อห้องเสียงแล้ว",
            body = "คุณได้เข้าร่วมห้อง \"${targetChannel.name}\"",
            targetChannelId = channelId
        )
        notificationDao.insertNotification(notif)
        _toastEvent.tryEmit("🔊 เข้าสู่ห้องเสียง \"${targetChannel.name}\" แล้ว")
    }

    suspend fun leaveVoiceChannel() {
        val currentUserId = _currentUser.value.id
        val prevRoom = _currentUser.value.currentVoiceRoom

        val updatedChannels = _voiceChannels.value.map { ch ->
            ch.copy(userIds = ch.userIds.filter { it != currentUserId })
        }
        _voiceChannels.value = updatedChannels
        _currentUser.value = _currentUser.value.copy(currentVoiceRoom = null)

        if (prevRoom != null) {
            val notif = AppNotificationEntity(
                type = "VOICE_LEAVE",
                title = "🚪 ออกจากห้องเสียง",
                body = "คุณออกจากห้อง \"$prevRoom\" แล้ว"
            )
            notificationDao.insertNotification(notif)
            _toastEvent.tryEmit("🚪 ออกจากห้องเสียง \"$prevRoom\" แล้ว")
        }
    }

    suspend fun rsvpEvent(eventId: Long, rsvp: String) {
        val eventList = gameEvents.first()
        val event = eventList.find { it.id == eventId } ?: return
        val userName = _currentUser.value.displayName

        val currentParticipants = if (event.participantsCsv.isNotBlank()) {
            event.participantsCsv.split(",").map { it.trim() }.toMutableList()
        } else {
            mutableListOf()
        }

        var newCount = event.currentPlayers

        when (rsvp) {
            "JOINED" -> {
                if (!currentParticipants.contains(userName)) {
                    currentParticipants.add(userName)
                    newCount = currentParticipants.size
                }
                notificationDao.insertNotification(
                    AppNotificationEntity(
                        type = "EVENT_REMINDER",
                        title = "🎮 ตอบรับเข้าร่วมกิจกรรม",
                        body = "คุณเข้าร่วม: ${event.title} (${event.date} เวลา ${event.startTime})"
                    )
                )
                _toastEvent.tryEmit("✅ เข้าร่วมกิจกรรม \"${event.title}\" เรียบร้อย!")
            }
            "DECLINED" -> {
                currentParticipants.remove(userName)
                newCount = currentParticipants.size
                _toastEvent.tryEmit("❌ ไม่เข้าร่วมกิจกรรม \"${event.title}\"")
            }
            "NONE" -> {
                currentParticipants.remove(userName)
                newCount = currentParticipants.size
                _toastEvent.tryEmit("ยกเลิกการตอบรับกิจกรรมแล้ว")
            }
        }

        gameEventDao.updateRsvp(eventId, rsvp, newCount, currentParticipants.joinToString(","))
    }

    suspend fun createEvent(
        title: String,
        game: String,
        date: String,
        startTime: String,
        endTime: String,
        maxPlayers: Int,
        description: String,
        voiceChannel: String
    ) {
        val userName = _currentUser.value.displayName
        val newEvent = GameEventEntity(
            title = title,
            game = game,
            date = date,
            startTime = startTime,
            endTime = endTime,
            maxPlayers = maxPlayers,
            currentPlayers = 1,
            description = description,
            voiceChannel = voiceChannel,
            creatorName = userName,
            userRsvp = "JOINED",
            participantsCsv = userName
        )
        gameEventDao.insertEvent(newEvent)

        notificationDao.insertNotification(
            AppNotificationEntity(
                type = "EVENT_REMINDER",
                title = "🎉 สร้างกิจกรรมใหม่สำเร็จ",
                body = "กิจกรรม: $title วันที่ $date เวลา $startTime"
            )
        )
        _toastEvent.tryEmit("🎉 สร้างกิจกรรม \"$title\" เรียบร้อย!")
    }

    suspend fun deleteEvent(eventId: Long) {
        gameEventDao.deleteEvent(eventId)
        _toastEvent.tryEmit("ลบกิจกรรมแล้ว")
    }

    suspend fun createLfg(
        game: String,
        neededPlayers: Int,
        startTime: String,
        skillLevel: String,
        voiceChannel: String,
        description: String
    ) {
        val userName = _currentUser.value.displayName
        val post = LfgPostEntity(
            game = game,
            neededPlayers = neededPlayers,
            currentPlayers = 1,
            startTime = startTime,
            skillLevel = skillLevel,
            voiceChannel = voiceChannel,
            creatorName = userName,
            description = description,
            hasJoined = true
        )
        lfgDao.insertLfgPost(post)

        notificationDao.insertNotification(
            AppNotificationEntity(
                type = "LFG_ALERT",
                title = "👥 ประกาศหาเพื่อนเล่นสำเร็จ",
                body = "เกม: $game (ต้องการ $neededPlayers คน)"
            )
        )
        _toastEvent.tryEmit("📢 โพสต์หาเพื่อนเล่นเกม $game สำเร็จแล้ว!")
    }

    suspend fun toggleJoinLfg(post: LfgPostEntity) {
        val newJoined = !post.hasJoined
        val newCount = if (newJoined) post.currentPlayers + 1 else maxOf(1, post.currentPlayers - 1)
        lfgDao.updateJoinStatus(post.id, newJoined, newCount)

        if (newJoined) {
            notificationDao.insertNotification(
                AppNotificationEntity(
                    type = "LFG_ALERT",
                    title = "🎮 เข้าร่วมทีมหาเพื่อนเล่น",
                    body = "คุณเข้าร่วมทีมเกม ${post.game} ของ ${post.creatorName}"
                )
            )
            _toastEvent.tryEmit("🤝 เข้าร่วมทีม ${post.game} แล้ว! เตรียมตัวเข้าห้อง ${post.voiceChannel}")
        } else {
            _toastEvent.tryEmit("ออกจากทีมแล้ว")
        }
    }

    suspend fun sendMessage(channelId: String, content: String) {
        if (content.isBlank()) return
        val user = _currentUser.value
        val msg = ChatMessageEntity(
            channelId = channelId,
            userId = user.id,
            userName = user.displayName,
            userAvatar = user.avatarUrl,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(msg)
    }

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
        _toastEvent.tryEmit("ทำเครื่องหมายว่าอ่านทั้งหมดแล้ว")
    }

    suspend fun clearNotifications() {
        notificationDao.clearAll()
        _toastEvent.tryEmit("ล้างการแจ้งเตือนทั้งหมดแล้ว")
    }

    suspend fun saveSettings(settings: UserSettingsEntity) {
        userSettingsDao.insertOrUpdate(settings)
        _currentUser.value = _currentUser.value.copy(
            username = settings.discordUsername,
            displayName = settings.discordDisplayName,
            avatarUrl = settings.discordAvatarUrl,
            isConnected = settings.discordConnected
        )
        _toastEvent.tryEmit("💾 บันทึกการตั้งค่าแล้ว")
    }

    suspend fun disconnectDiscord() {
        oauthService.revokeAndDisconnect()
        val curr = userSettings.first() ?: UserSettingsEntity()
        val updated = curr.copy(discordConnected = false)
        userSettingsDao.insertOrUpdate(updated)
        _currentUser.value = _currentUser.value.copy(isConnected = false)
        _toastEvent.tryEmit("ตัดการเชื่อมต่อ Discord และล้าง Token ปลอดภัยแล้ว")
    }

    suspend fun connectDiscord(username: String, displayName: String, guildId: String) {
        val curr = userSettings.first() ?: UserSettingsEntity()
        val updated = curr.copy(
            discordConnected = true,
            discordUsername = username,
            discordDisplayName = displayName,
            discordGuildId = guildId
        )
        userSettingsDao.insertOrUpdate(updated)
        _currentUser.value = _currentUser.value.copy(
            isConnected = true,
            username = username,
            displayName = displayName
        )
        // Store simulated OAuth tokens in secure keystore
        oauthService.simulateSuccessfulOAuth(username, displayName, guildId)
        _toastEvent.tryEmit("เชื่อมต่อ Discord สำเร็จ (จัดเก็บ Token ด้วย AES-256 GCM)!")
    }

    fun startDiscordOAuthFlow(clientId: String = DiscordOAuthService.DEFAULT_CLIENT_ID, redirectUri: String = DiscordOAuthService.DEFAULT_REDIRECT_URI): String {
        val url = oauthService.initiateOAuthFlow(clientId, redirectUri)
        _toastEvent.tryEmit("🔐 สร้าง Discord OAuth2 Auth URL พร้อม PKCE เรียบร้อย")
        return url
    }

    suspend fun handleDiscordOAuthCallback(
        code: String,
        state: String,
        clientId: String = DiscordOAuthService.DEFAULT_CLIENT_ID,
        redirectUri: String = DiscordOAuthService.DEFAULT_REDIRECT_URI
    ): Result<DiscordUserProfile> {
        val result = oauthService.handleOAuthCallback(code, state, clientId, redirectUri)
        result.onSuccess { profile ->
            val curr = userSettings.first() ?: UserSettingsEntity()
            val formattedTag = if (profile.discriminator != null && profile.discriminator != "0") {
                "${profile.username}#${profile.discriminator}"
            } else {
                profile.username
            }
            val updated = curr.copy(
                discordConnected = true,
                discordUsername = formattedTag,
                discordDisplayName = profile.displayName,
                discordAvatarUrl = profile.avatarUrl
            )
            userSettingsDao.insertOrUpdate(updated)
            _currentUser.value = _currentUser.value.copy(
                isConnected = true,
                username = formattedTag,
                displayName = profile.displayName,
                avatarUrl = profile.avatarUrl
            )
            notificationDao.insertNotification(
                AppNotificationEntity(
                    type = "MENTION",
                    title = "🔒 Discord OAuth2 เข้าสู่ระบบสำเร็จ",
                    body = "เชื่อมต่อในฐานะ ${profile.displayName} (PKCE Code Exchange สำเร็จ, Token เข้ารหัสด้วย Android Keystore)"
                )
            )
            _toastEvent.tryEmit("🔒 เชื่อมต่อ Discord OAuth2 สำเร็จ (${profile.displayName})")
        }.onFailure { err ->
            _toastEvent.tryEmit("❌ OAuth2 Error: ${err.message ?: "การแลกเปลี่ยน Token ล้มเหลว"}")
        }
        return result
    }

    suspend fun simulateOAuthSuccess(username: String, displayName: String, guildId: String) {
        val profile = oauthService.simulateSuccessfulOAuth(username, displayName, guildId)
        val curr = userSettings.first() ?: UserSettingsEntity()
        val updated = curr.copy(
            discordConnected = true,
            discordUsername = username,
            discordDisplayName = displayName,
            discordGuildId = guildId
        )
        userSettingsDao.insertOrUpdate(updated)
        _currentUser.value = _currentUser.value.copy(
            isConnected = true,
            username = username,
            displayName = displayName
        )
        notificationDao.insertNotification(
            AppNotificationEntity(
                type = "MENTION",
                title = "🔒 บัญชี Discord ถูกบันทึกลง Keystore",
                body = "บันทึก OAuth Token ปลอดภัยด้วย AES-256 GCM Authenticated Encryption"
            )
        )
        _toastEvent.tryEmit("🔒 บัญชี Discord เชื่อมต่อแล้ว (จัดเก็บ Token ด้วย AES-256 GCM)")
    }

    // Firebase Authentication & Account Management
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseAuthUserInfo> {
        val result = authService.signInWithEmail(email, pass)
        result.onSuccess { info ->
            val curr = userSettings.first() ?: UserSettingsEntity()
            val updated = curr.copy(
                firebaseUid = info.uid,
                firebaseEmail = info.email,
                firebaseIsAnonymous = false,
                firebaseLastSignIn = System.currentTimeMillis()
            )
            userSettingsDao.insertOrUpdate(updated)
            _currentUser.value = _currentUser.value.copy(
                firebaseUid = info.uid,
                firebaseEmail = info.email,
                isFirebaseAnonymous = false
            )
            notificationDao.insertNotification(
                AppNotificationEntity(
                    type = "MENTION",
                    title = "🔥 เข้าสู่ระบบ Firebase สำเร็จ",
                    body = "ยินดีต้อนรับกลับ ${info.email} (UID: ${info.uid.take(8)}...)"
                )
            )
            _toastEvent.tryEmit("🔥 เข้าสู่ระบบ Firebase สำเร็จ (${info.email ?: "User"})")
        }.onFailure { err ->
            _toastEvent.tryEmit("❌ เข้าสู่ระบบไม่สำเร็จ: ${err.message ?: "กรุณาตรวจสอบข้อมูล"}")
        }
        return result
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<FirebaseAuthUserInfo> {
        val result = authService.signUpWithEmail(email, pass, displayName)
        result.onSuccess { info ->
            val curr = userSettings.first() ?: UserSettingsEntity()
            val updated = curr.copy(
                firebaseUid = info.uid,
                firebaseEmail = info.email,
                firebaseIsAnonymous = false,
                firebaseLastSignIn = System.currentTimeMillis()
            )
            userSettingsDao.insertOrUpdate(updated)
            _currentUser.value = _currentUser.value.copy(
                firebaseUid = info.uid,
                firebaseEmail = info.email,
                isFirebaseAnonymous = false
            )
            notificationDao.insertNotification(
                AppNotificationEntity(
                    type = "MENTION",
                    title = "🎉 สร้างบัญชี Firebase ใหม่สำเร็จ",
                    body = "สร้างบัญชี ${info.email} เรียบร้อยแล้ว พร้อมสำหรับจัดการบัญชี Discord"
                )
            )
            _toastEvent.tryEmit("🎉 สร้างบัญชี Firebase สำเร็จแล้ว!")
        }.onFailure { err ->
            _toastEvent.tryEmit("❌ สมัครสมาชิกไม่สำเร็จ: ${err.message ?: "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"}")
        }
        return result
    }

    suspend fun signInAnonymously(): Result<FirebaseAuthUserInfo> {
        val result = authService.signInAnonymously()
        result.onSuccess { info ->
            val curr = userSettings.first() ?: UserSettingsEntity()
            val updated = curr.copy(
                firebaseUid = info.uid,
                firebaseEmail = "Guest Gamer",
                firebaseIsAnonymous = true,
                firebaseLastSignIn = System.currentTimeMillis()
            )
            userSettingsDao.insertOrUpdate(updated)
            _currentUser.value = _currentUser.value.copy(
                firebaseUid = info.uid,
                firebaseEmail = "Guest Gamer",
                isFirebaseAnonymous = true
            )
            _toastEvent.tryEmit("🔥 เข้าสู่ระบบในฐานะ Guest (Anonymous UID: ${info.uid.take(8)}...)")
        }.onFailure { err ->
            _toastEvent.tryEmit("❌ เข้าสู่ระบบ Guest ไม่สำเร็จ: ${err.message}")
        }
        return result
    }

    suspend fun signOutFirebase() {
        authService.signOut()
        val curr = userSettings.first() ?: UserSettingsEntity()
        val updated = curr.copy(
            firebaseUid = null,
            firebaseEmail = null,
            firebaseIsAnonymous = false,
            firebaseLinkedDiscord = false
        )
        userSettingsDao.insertOrUpdate(updated)
        _currentUser.value = _currentUser.value.copy(
            firebaseUid = null,
            firebaseEmail = null,
            isFirebaseAnonymous = false,
            isFirebaseLinked = false
        )
        _toastEvent.tryEmit("ออกจากระบบ Firebase เรียบร้อยแล้ว")
    }

    suspend fun linkDiscordToFirebaseAccount(): Boolean {
        val currentFbUser = authService.getCurrentUser()
        if (currentFbUser == null) {
            _toastEvent.tryEmit("กรุณาเข้าสู่ระบบ Firebase ก่อนทำการผูกบัญชี")
            return false
        }
        val curr = userSettings.first() ?: UserSettingsEntity()
        val updated = curr.copy(
            firebaseUid = currentFbUser.uid,
            firebaseEmail = currentFbUser.email,
            firebaseLinkedDiscord = true
        )
        userSettingsDao.insertOrUpdate(updated)
        _currentUser.value = _currentUser.value.copy(
            firebaseUid = currentFbUser.uid,
            firebaseEmail = currentFbUser.email,
            isFirebaseLinked = true
        )
        notificationDao.insertNotification(
            AppNotificationEntity(
                type = "MENTION",
                title = "🔗 บัญชี Discord ถูกผูกกับ Firebase แล้ว",
                body = "Discord: ${curr.discordUsername} เชื่อมโยงกับ Firebase UID: ${currentFbUser.uid}"
            )
        )
        _toastEvent.tryEmit("🔗 ผูกบัญชี Discord (${curr.discordDisplayName}) กับ Firebase สำเร็จ!")
        return true
    }

    suspend fun unlinkDiscordFromFirebase(): Boolean {
        val curr = userSettings.first() ?: UserSettingsEntity()
        val updated = curr.copy(firebaseLinkedDiscord = false)
        userSettingsDao.insertOrUpdate(updated)
        _currentUser.value = _currentUser.value.copy(isFirebaseLinked = false)
        _toastEvent.tryEmit("ยกเลิกการผูกบัญชี Discord กับ Firebase แล้ว")
        return true
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val res = authService.sendPasswordReset(email)
        res.onSuccess {
            _toastEvent.tryEmit("✉️ ส่งลิงก์รีเซ็ตรหัสผ่านไปยัง $email แล้ว")
        }.onFailure { err ->
            _toastEvent.tryEmit("❌ ส่งลิงก์ไม่สำเร็จ: ${err.message}")
        }
        return res
    }

    // Realistic Real-time simulation event generator
    suspend fun triggerVoiceSimulation() {
        val candidateMembers = _members.value.filter { it.id != _currentUser.value.id }
        if (candidateMembers.isEmpty()) return
        val randomMember = candidateMembers.random()
        val channels = _voiceChannels.value

        val isInVoice = randomMember.voiceChannelId != null
        if (isInVoice) {
            // Leave current channel
            val currentCh = channels.find { it.id == randomMember.voiceChannelId }
            val chName = currentCh?.name ?: "Voice Channel"

            _members.value = _members.value.map {
                if (it.id == randomMember.id) it.copy(voiceChannelId = null) else it
            }
            _voiceChannels.value = channels.map { ch ->
                if (ch.id == randomMember.voiceChannelId) {
                    ch.copy(userIds = ch.userIds.filter { it != randomMember.id })
                } else ch
            }

            notificationDao.insertNotification(
                AppNotificationEntity(
                    type = "VOICE_LEAVE",
                    title = "🔔 สมาชิกออกจากห้องเสียง",
                    body = "${randomMember.displayName} ออกจาก $chName แล้ว",
                    targetChannelId = randomMember.voiceChannelId
                )
            )
            _toastEvent.tryEmit("🔔 ${randomMember.displayName} ออกจาก $chName แล้ว")
        } else {
            // Join a random channel (excluding AFK preferably)
            val targetChannel = channels.filter { it.id != "vc_afk" }.random()
            _members.value = _members.value.map {
                if (it.id == randomMember.id) it.copy(
                    voiceChannelId = targetChannel.id,
                    status = UserStatus.ONLINE
                ) else it
            }
            _voiceChannels.value = channels.map { ch ->
                if (ch.id == targetChannel.id) {
                    ch.copy(userIds = (ch.userIds + randomMember.id).distinct())
                } else ch
            }

            notificationDao.insertNotification(
                AppNotificationEntity(
                    type = "VOICE_JOIN",
                    title = "🔔 สมาชิกเข้าห้องเสียง",
                    body = "${randomMember.displayName} เข้าห้อง ${targetChannel.name} แล้ว",
                    targetChannelId = targetChannel.id
                )
            )
            _toastEvent.tryEmit("🔔 ${randomMember.displayName} เข้าห้อง ${targetChannel.name} แล้ว")
        }
    }

    suspend fun triggerChatSimulation() {
        val sampleChatters = listOf(
            Triple("user_01", "User01", "มีใครลงแข่ง Valorant ทัวร์สุดสัปดาห์นี้ไหม?"),
            Triple("user_02", "User02", "กำลังขุดเหมืองเจอ Diamond 12 ก้อนใน Minecraft!"),
            Triple("user_05", "User05", "ใครว่างมาตี้ Valorant ด่วน ขาด 1 คน"),
            Triple("user_07", "User07", "Apex Legends อัปเดตแพตช์ใหม่ ปืนเนิร์ฟเรียบร้อย"),
            Triple("user_08", "User08", "พร้อมลงห้อง Gaming Room แล้วนะ")
        )
        val chatter = sampleChatters.random()
        val member = _members.value.find { it.id == chatter.first }
        val avatar = member?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"

        val msg = ChatMessageEntity(
            channelId = "tc_general",
            userId = chatter.first,
            userName = chatter.second,
            userAvatar = avatar,
            content = chatter.third,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(msg)

        notificationDao.insertNotification(
            AppNotificationEntity(
                type = "MESSAGE",
                title = "💬 ข้อความใหม่จาก #general",
                body = "${chatter.second}: \"${chatter.third}\"",
                targetChannelId = "tc_general"
            )
        )
        _toastEvent.tryEmit("💬 ${chatter.second} ส่งข้อความใน #general")
    }

    suspend fun triggerGameStatusSimulation() {
        val games = listOf("Valorant", "Minecraft", "GTA V", "Apex Legends", "League of Legends", "CS2", "Overwatch 2")
        val candidate = _members.value.filter { it.id != _currentUser.value.id }.random()
        val newGame = games.random()

        _members.value = _members.value.map {
            if (it.id == candidate.id) {
                it.copy(
                    currentGame = newGame,
                    status = UserStatus.ONLINE,
                    gameDetail = "In Game • Playing with Squad",
                    gameDurationMinutes = Random.nextInt(5, 60)
                )
            } else it
        }

        notificationDao.insertNotification(
            AppNotificationEntity(
                type = "GAME_ACTIVITY",
                title = "🎮 สมาชิกเริ่มเล่นเกมใหม่",
                body = "${candidate.displayName} กำลังเล่น $newGame"
            )
        )
        _toastEvent.tryEmit("🎮 ${candidate.displayName} กำลังเล่น $newGame")
    }
}

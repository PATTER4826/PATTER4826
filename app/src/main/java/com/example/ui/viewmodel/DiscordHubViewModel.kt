package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.DiscordHubRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen(val titleTh: String, val iconName: String) {
    DASHBOARD("แดชบอร์ด", "Dashboard"),
    MEMBERS("สมาชิกกิลด์", "People"),
    EVENTS("นัดเล่นเกม", "Event"),
    LFG("หาเพื่อนเล่น", "GroupAdd"),
    VOICE("ห้องเสียง", "Mic"),
    CHAT("แชท Discord", "Chat"),
    NOTIFICATIONS("แจ้งเตือน", "Notifications"),
    BOT_DOCS("Discord Bot API", "Code"),
    SETTINGS("ตั้งค่า", "Settings")
}

enum class MemberFilter(val labelTh: String) {
    ALL("ทั้งหมด"),
    ONLINE("ออนไลน์"),
    PLAYING("กำลังเล่นเกม"),
    IN_VOICE("อยู่ในห้องเสียง"),
    OFFLINE("ออฟไลน์")
}

class DiscordHubViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val authService = com.example.data.auth.FirebaseAuthService(application)
    val oauthService = com.example.data.auth.oauth.DiscordOAuthService(application)
    val repository = DiscordHubRepository(database, authService, oauthService, viewModelScope)

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Member search and filters
    val memberSearchQuery = MutableStateFlow("")
    val memberFilter = MutableStateFlow(MemberFilter.ALL)

    // Selected Chat Channel
    val selectedTextChannelId = MutableStateFlow("tc_general")

    // Notification filter
    val notificationFilter = MutableStateFlow("ALL")

    // Dialog visibility states
    val showCreateEventDialog = MutableStateFlow(false)
    val showCreateLfgDialog = MutableStateFlow(false)
    val showConnectDiscordDialog = MutableStateFlow(false)
    val showFirebaseAuthDialog = MutableStateFlow(false)
    val showMemberProfileDialog = MutableStateFlow<MemberInfo?>(null)

    // Reactive streams from repository
    val guildInfo: StateFlow<DiscordGuildInfo> = repository.guildInfo
    val members: StateFlow<List<MemberInfo>> = repository.members
    val voiceChannels: StateFlow<List<VoiceChannelInfo>> = repository.voiceChannels
    val textChannels: StateFlow<List<TextChannelInfo>> = repository.textChannels
    val currentUser: StateFlow<CurrentUser> = repository.currentUser
    val firebaseUser = repository.firebaseUser
    val oauthSessionState = repository.oauthSessionState
    val toastMessage = repository.toastEvent

    val gameEvents: StateFlow<List<GameEventEntity>> = repository.gameEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lfgPosts: StateFlow<List<LfgPostEntity>> = repository.lfgPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentChannelMessages: StateFlow<List<ChatMessageEntity>> = selectedTextChannelId
        .flatMapLatest { channelId -> repository.getMessagesForChannel(channelId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered members list
    val filteredMembers: StateFlow<List<MemberInfo>> = combine(
        members,
        memberSearchQuery,
        memberFilter
    ) { memberList, query, filter ->
        memberList.filter { m ->
            val matchQuery = query.isBlank() ||
                    m.displayName.contains(query, ignoreCase = true) ||
                    m.username.contains(query, ignoreCase = true) ||
                    (m.currentGame?.contains(query, ignoreCase = true) ?: false)

            val matchFilter = when (filter) {
                MemberFilter.ALL -> true
                MemberFilter.ONLINE -> m.status != UserStatus.OFFLINE
                MemberFilter.PLAYING -> !m.currentGame.isNullOrBlank()
                MemberFilter.IN_VOICE -> !m.voiceChannelId.isNullOrBlank()
                MemberFilter.OFFLINE -> m.status == UserStatus.OFFLINE
            }
            matchQuery && matchFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulation Loop Job
    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(25000) // Every 25 seconds simulate a realistic Discord event
                val settings = userSettings.value
                if (settings?.simulationMode != false) {
                    when ((1..3).random()) {
                        1 -> repository.triggerVoiceSimulation()
                        2 -> repository.triggerChatSimulation()
                        3 -> repository.triggerGameStatusSimulation()
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun joinVoiceChannel(channelId: String) {
        viewModelScope.launch {
            repository.joinVoiceChannel(channelId)
        }
    }

    fun leaveVoiceChannel() {
        viewModelScope.launch {
            repository.leaveVoiceChannel()
        }
    }

    fun rsvpEvent(eventId: Long, rsvp: String) {
        viewModelScope.launch {
            repository.rsvpEvent(eventId, rsvp)
        }
    }

    fun createEvent(
        title: String,
        game: String,
        date: String,
        startTime: String,
        endTime: String,
        maxPlayers: Int,
        description: String,
        voiceChannel: String
    ) {
        viewModelScope.launch {
            repository.createEvent(title, game, date, startTime, endTime, maxPlayers, description, voiceChannel)
            showCreateEventDialog.value = false
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }

    fun createLfg(
        game: String,
        neededPlayers: Int,
        startTime: String,
        skillLevel: String,
        voiceChannel: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.createLfg(game, neededPlayers, startTime, skillLevel, voiceChannel, description)
            showCreateLfgDialog.value = false
        }
    }

    fun toggleJoinLfg(post: LfgPostEntity) {
        viewModelScope.launch {
            repository.toggleJoinLfg(post)
        }
    }

    fun sendChatMessage(content: String) {
        viewModelScope.launch {
            repository.sendMessage(selectedTextChannelId.value, content)
        }
    }

    fun selectTextChannel(channelId: String) {
        selectedTextChannelId.value = channelId
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    fun saveSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    fun disconnectDiscord() {
        viewModelScope.launch {
            repository.disconnectDiscord()
        }
    }

    fun connectDiscord(username: String, displayName: String, guildId: String) {
        viewModelScope.launch {
            repository.connectDiscord(username, displayName, guildId)
            showConnectDiscordDialog.value = false
        }
    }

    fun triggerManualVoiceSim() {
        viewModelScope.launch {
            repository.triggerVoiceSimulation()
        }
    }

    fun triggerManualChatSim() {
        viewModelScope.launch {
            repository.triggerChatSimulation()
        }
    }

    fun triggerManualGameSim() {
        viewModelScope.launch {
            repository.triggerGameStatusSimulation()
        }
    }

    // Firebase Auth delegates
    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            repository.signInWithEmail(email, pass)
            showFirebaseAuthDialog.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            repository.signUpWithEmail(email, pass, displayName)
            showFirebaseAuthDialog.value = false
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            repository.signInAnonymously()
            showFirebaseAuthDialog.value = false
        }
    }

    fun signOutFirebase() {
        viewModelScope.launch {
            repository.signOutFirebase()
        }
    }

    fun linkDiscordAccount() {
        viewModelScope.launch {
            repository.linkDiscordToFirebaseAccount()
        }
    }

    fun unlinkDiscordAccount() {
        viewModelScope.launch {
            repository.unlinkDiscordFromFirebase()
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            repository.sendPasswordReset(email)
        }
    }

    // Discord OAuth2 Flow delegates
    fun startDiscordOAuth(clientId: String = com.example.data.auth.oauth.DiscordOAuthService.DEFAULT_CLIENT_ID): String {
        return repository.startDiscordOAuthFlow(clientId)
    }

    fun handleDiscordOAuthCallback(
        code: String,
        state: String,
        clientId: String = com.example.data.auth.oauth.DiscordOAuthService.DEFAULT_CLIENT_ID
    ) {
        viewModelScope.launch {
            repository.handleDiscordOAuthCallback(code, state, clientId)
            showConnectDiscordDialog.value = false
        }
    }

    fun simulateDiscordOAuthSuccess(username: String, displayName: String, guildId: String) {
        viewModelScope.launch {
            repository.simulateOAuthSuccess(username, displayName, guildId)
            showConnectDiscordDialog.value = false
        }
    }
}


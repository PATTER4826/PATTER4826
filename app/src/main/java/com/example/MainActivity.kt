package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MemberInfo
import com.example.data.model.UserStatus
import com.example.data.model.VoiceChannelInfo
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DiscordHubViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: DiscordHubViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleOAuthDeepLink(intent)

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val guildInfo by viewModel.guildInfo.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val members by viewModel.filteredMembers.collectAsStateWithLifecycle()
                val rawMembers by viewModel.members.collectAsStateWithLifecycle()
                val voiceChannels by viewModel.voiceChannels.collectAsStateWithLifecycle()
                val textChannels by viewModel.textChannels.collectAsStateWithLifecycle()
                val gameEvents by viewModel.gameEvents.collectAsStateWithLifecycle()
                val lfgPosts by viewModel.lfgPosts.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
                val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
                val messages by viewModel.currentChannelMessages.collectAsStateWithLifecycle()
                val selectedChannelId by viewModel.selectedTextChannelId.collectAsStateWithLifecycle()
                val memberSearchQuery by viewModel.memberSearchQuery.collectAsStateWithLifecycle()
                val memberFilter by viewModel.memberFilter.collectAsStateWithLifecycle()
                val notificationFilter by viewModel.notificationFilter.collectAsStateWithLifecycle()

                // Dialog states
                val showCreateEvent by viewModel.showCreateEventDialog.collectAsStateWithLifecycle()
                val showCreateLfg by viewModel.showCreateLfgDialog.collectAsStateWithLifecycle()
                val showConnectDiscord by viewModel.showConnectDiscordDialog.collectAsStateWithLifecycle()
                val showFirebaseAuth by viewModel.showFirebaseAuthDialog.collectAsStateWithLifecycle()
                val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
                val memberProfileToView by viewModel.showMemberProfileDialog.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                // Collect snackbar toasts
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
                    }
                }

                // Handle system back navigation to return to Dashboard
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    viewModel.navigateTo(AppScreen.DASHBOARD)
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().background(DiscordDarkest)) {
                    val isWideScreen = maxWidth >= 840.dp
                    val isTablet = maxWidth >= 600.dp && maxWidth < 840.dp
                    val isDesktopWide = maxWidth >= 1100.dp

                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.padding(bottom = if (isWideScreen) 16.dp else 70.dp)
                            ) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = DiscordDarker,
                                    contentColor = TextPrimary,
                                    actionColor = NeonCyan,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.border(1.dp, DiscordBlurple, RoundedCornerShape(12.dp))
                                )
                            }
                        },
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        DiscordAvatar(
                                            imageUrl = guildInfo.iconUrl,
                                            displayName = guildInfo.name,
                                            size = 32
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = guildInfo.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonGreen)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Online ${rawMembers.count { it.status != UserStatus.OFFLINE }} คน",
                                                    fontSize = 10.sp,
                                                    color = NeonGreen
                                                )
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    // Notification Icon with unread badge
                                    IconButton(
                                        onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                                        modifier = Modifier.testTag("top_bar_notifications_btn")
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                if (unreadNotifsCount > 0) {
                                                    Badge(containerColor = NeonRed) {
                                                        Text("$unreadNotifsCount", color = Color.White)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = if (currentScreen == AppScreen.NOTIFICATIONS) NeonCyan else TextPrimary
                                            )
                                        }
                                    }

                                    // LFG Quick Action
                                    IconButton(
                                        onClick = { viewModel.navigateTo(AppScreen.LFG) },
                                        modifier = Modifier.testTag("top_bar_lfg_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd,
                                            contentDescription = "LFG",
                                            tint = if (currentScreen == AppScreen.LFG) NeonOrange else TextPrimary
                                        )
                                    }

                                    // Quick Discord Connect
                                    IconButton(
                                        onClick = { viewModel.showConnectDiscordDialog.value = true },
                                        modifier = Modifier.testTag("top_bar_connect_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Connect Discord",
                                            tint = if (currentUser.isConnected) NeonGreen else TextMuted
                                        )
                                    }

                                    // Quick Firebase Auth Dialog
                                    IconButton(
                                        onClick = { viewModel.showFirebaseAuthDialog.value = true },
                                        modifier = Modifier.testTag("top_bar_firebase_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Firebase Account",
                                            tint = if (firebaseUser != null) NeonOrange else TextMuted
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = DiscordDarker,
                                    titleContentColor = TextPrimary
                                )
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen) {
                                NavigationBar(
                                    containerColor = DiscordDarker,
                                    contentColor = TextPrimary,
                                    tonalElevation = 8.dp
                                ) {
                                    val navItems = listOf(
                                        AppScreen.DASHBOARD to Icons.Default.Dashboard,
                                        AppScreen.VOICE to Icons.Default.VolumeUp,
                                        AppScreen.CHAT to Icons.Default.Forum,
                                        AppScreen.EVENTS to Icons.Default.CalendarMonth,
                                        AppScreen.MEMBERS to Icons.Default.People,
                                        AppScreen.SETTINGS to Icons.Default.Settings
                                    )

                                    navItems.forEach { (screen, icon) ->
                                        val isSelected = currentScreen == screen
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { viewModel.navigateTo(screen) },
                                            icon = {
                                                if (screen == AppScreen.NOTIFICATIONS && unreadNotifsCount > 0) {
                                                    BadgedBox(badge = { Badge { Text("$unreadNotifsCount") } }) {
                                                        Icon(icon, contentDescription = screen.titleTh)
                                                    }
                                                } else {
                                                    Icon(icon, contentDescription = screen.titleTh)
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = screen.titleTh,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = NeonCyan,
                                                selectedTextColor = NeonCyan,
                                                indicatorColor = DiscordBlurpleDark,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted
                                            ),
                                            modifier = Modifier.testTag("nav_item_${screen.name}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Desktop / Wide Screen Persistent Left Sidebar (Requirement 16)
                            if (isWideScreen) {
                                DesktopNavigationSidebar(
                                    currentScreen = currentScreen,
                                    unreadNotifsCount = unreadNotifsCount,
                                    onSelectScreen = { viewModel.navigateTo(it) }
                                )
                            }

                            // Main Screen Content
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                when (currentScreen) {
                                    AppScreen.DASHBOARD -> DashboardScreen(
                                        guildInfo = guildInfo,
                                        currentUser = currentUser,
                                        firebaseUser = firebaseUser,
                                        members = rawMembers,
                                        voiceChannels = voiceChannels,
                                        upcomingEvents = gameEvents,
                                        onNavigate = { viewModel.navigateTo(it) },
                                        onJoinVoice = { viewModel.joinVoiceChannel(it) },
                                        onLeaveVoice = { viewModel.leaveVoiceChannel() },
                                        onRsvpEvent = { id, rsvp -> viewModel.rsvpEvent(id, rsvp) },
                                        onMemberClick = { viewModel.showMemberProfileDialog.value = it },
                                        onCreateEventClick = { viewModel.showCreateEventDialog.value = true },
                                        onCreateLfgClick = { viewModel.showCreateLfgDialog.value = true },
                                        onConnectDiscordClick = { viewModel.showConnectDiscordDialog.value = true },
                                        onOpenFirebaseAuthClick = { viewModel.showFirebaseAuthDialog.value = true },
                                        onTriggerSimVoice = { viewModel.triggerManualVoiceSim() },
                                        onTriggerSimChat = { viewModel.triggerManualChatSim() }
                                    )

                                    AppScreen.MEMBERS -> MembersScreen(
                                        members = members,
                                        searchQuery = memberSearchQuery,
                                        onSearchQueryChange = { viewModel.memberSearchQuery.value = it },
                                        currentFilter = memberFilter,
                                        onFilterChange = { viewModel.memberFilter.value = it },
                                        onMemberClick = { viewModel.showMemberProfileDialog.value = it }
                                    )

                                    AppScreen.EVENTS -> GameEventsScreen(
                                        events = gameEvents,
                                        onRsvpEvent = { id, rsvp -> viewModel.rsvpEvent(id, rsvp) },
                                        onCreateEventClick = { viewModel.showCreateEventDialog.value = true },
                                        onDeleteEvent = { viewModel.deleteEvent(it) }
                                    )

                                    AppScreen.LFG -> FindPlayersScreen(
                                        posts = lfgPosts,
                                        onToggleJoin = { viewModel.toggleJoinLfg(it) },
                                        onCreateLfgClick = { viewModel.showCreateLfgDialog.value = true }
                                    )

                                    AppScreen.VOICE -> VoiceChannelsScreen(
                                        voiceChannels = voiceChannels,
                                        allMembers = rawMembers,
                                        currentUser = currentUser,
                                        onJoinVoice = { viewModel.joinVoiceChannel(it) },
                                        onLeaveVoice = { viewModel.leaveVoiceChannel() },
                                        onSimulateVoiceHop = { viewModel.triggerManualVoiceSim() }
                                    )

                                    AppScreen.CHAT -> DiscordChatScreen(
                                        textChannels = textChannels,
                                        selectedChannelId = selectedChannelId,
                                        messages = messages,
                                        currentUser = currentUser,
                                        onSelectChannel = { viewModel.selectTextChannel(it) },
                                        onSendMessage = { viewModel.sendChatMessage(it) }
                                    )

                                    AppScreen.NOTIFICATIONS -> NotificationCenterScreen(
                                        notifications = notifications,
                                        unreadCount = unreadNotifsCount,
                                        selectedCategory = notificationFilter,
                                        onCategoryChange = { viewModel.notificationFilter.value = it },
                                        onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                        onClearAll = { viewModel.clearAllNotifications() },
                                        onNavigate = { viewModel.navigateTo(it) },
                                        onJoinVoice = { viewModel.joinVoiceChannel(it) }
                                    )

                                    AppScreen.BOT_DOCS -> BotDocsScreen()

                                    AppScreen.SETTINGS -> SettingsScreen(
                                        currentUser = currentUser,
                                        firebaseUser = firebaseUser,
                                        settings = userSettings,
                                        onSaveSettings = { viewModel.saveSettings(it) },
                                        onDisconnectDiscord = { viewModel.disconnectDiscord() },
                                        onOpenConnectDialog = { viewModel.showConnectDiscordDialog.value = true },
                                        onOpenFirebaseAuthDialog = { viewModel.showFirebaseAuthDialog.value = true },
                                        onLinkDiscordToFirebase = { viewModel.linkDiscordAccount() },
                                        onUnlinkDiscordFromFirebase = { viewModel.unlinkDiscordAccount() },
                                        onSignOutFirebase = { viewModel.signOutFirebase() }
                                    )
                                }
                            }

                            // Desktop Right Sidebar (Requirement 16)
                            if (isDesktopWide) {
                                DesktopRightSidebar(
                                    members = rawMembers,
                                    voiceChannels = voiceChannels,
                                    upcomingEvents = gameEvents,
                                    currentUser = currentUser,
                                    firebaseUser = firebaseUser,
                                    onOpenFirebaseAuth = { viewModel.showFirebaseAuthDialog.value = true },
                                    onMemberClick = { viewModel.showMemberProfileDialog.value = it },
                                    onJoinVoice = { viewModel.joinVoiceChannel(it) }
                                )
                            }
                        }
                    }
                }

                // Dialogs
                val voiceNames = remember(voiceChannels) { voiceChannels.map { it.name } }

                if (showCreateEvent) {
                    CreateEventDialog(
                        voiceChannelNames = voiceNames,
                        onDismiss = { viewModel.showCreateEventDialog.value = false },
                        onConfirm = { title, game, date, start, end, max, desc, voice ->
                            viewModel.createEvent(title, game, date, start, end, max, desc, voice)
                        }
                    )
                }

                if (showCreateLfg) {
                    CreateLfgDialog(
                        voiceChannelNames = voiceNames,
                        onDismiss = { viewModel.showCreateLfgDialog.value = false },
                        onConfirm = { game, needed, start, skill, voice, desc ->
                            viewModel.createLfg(game, needed, start, skill, voice, desc)
                        }
                    )
                }

                if (showConnectDiscord) {
                    ConnectDiscordDialog(
                        currentUsername = currentUser.username,
                        currentDisplayName = currentUser.displayName,
                        currentGuildId = guildInfo.id,
                        onDismiss = { viewModel.showConnectDiscordDialog.value = false },
                        onConfirm = { u, d, g ->
                            viewModel.connectDiscord(u, d, g)
                        },
                        onStartOAuthFlow = { clientId ->
                            viewModel.startDiscordOAuth(clientId)
                        },
                        onExchangeOAuthCode = { code, state, clientId ->
                            viewModel.handleDiscordOAuthCallback(code, state, clientId)
                        },
                        onSimulateOAuthSuccess = { u, d, g ->
                            viewModel.simulateDiscordOAuthSuccess(u, d, g)
                        }
                    )
                }

                if (showFirebaseAuth) {
                    FirebaseAuthDialog(
                        firebaseUser = firebaseUser,
                        currentUser = currentUser,
                        onDismiss = { viewModel.showFirebaseAuthDialog.value = false },
                        onSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
                        onSignUp = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
                        onAnonymousSignIn = { viewModel.signInAnonymously() },
                        onSignOut = { viewModel.signOutFirebase() },
                        onLinkDiscord = { viewModel.linkDiscordAccount() },
                        onUnlinkDiscord = { viewModel.unlinkDiscordAccount() },
                        onForgotPassword = { email -> viewModel.sendPasswordReset(email) }
                    )
                }

                memberProfileToView?.let { member ->
                    MemberProfileDialog(
                        member = member,
                        onDismiss = { viewModel.showMemberProfileDialog.value = null },
                        onSendMessage = { tag ->
                            viewModel.selectTextChannel("tc_general")
                            viewModel.navigateTo(AppScreen.CHAT)
                            viewModel.sendChatMessage(tag)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleOAuthDeepLink(intent)
    }

    private fun handleOAuthDeepLink(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "discordhub" && uri.host == "oauth") {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if (!code.isNullOrBlank() && !state.isNullOrBlank()) {
                viewModel.handleDiscordOAuthCallback(code, state)
            }
        }
    }
}

@Composable
fun DesktopNavigationSidebar(
    currentScreen: AppScreen,
    unreadNotifsCount: Int,
    onSelectScreen: (AppScreen) -> Unit
) {
    Row(modifier = Modifier.width(220.dp).fillMaxHeight()) {
        Surface(
            color = DiscordDarker,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "กลุ่มเล่นเกม",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                val screens = listOf(
                    AppScreen.DASHBOARD to Icons.Default.Dashboard,
                    AppScreen.MEMBERS to Icons.Default.People,
                    AppScreen.EVENTS to Icons.Default.CalendarMonth,
                    AppScreen.LFG to Icons.Default.GroupAdd,
                    AppScreen.VOICE to Icons.Default.VolumeUp,
                    AppScreen.CHAT to Icons.Default.Forum,
                    AppScreen.NOTIFICATIONS to Icons.Default.Notifications,
                    AppScreen.BOT_DOCS to Icons.Default.Code,
                    AppScreen.SETTINGS to Icons.Default.Settings
                )

                screens.forEach { (screen, icon) ->
                    val isSelected = currentScreen == screen
                    Surface(
                        color = if (isSelected) DiscordBlurpleDark else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreen(screen) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.titleTh,
                                tint = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = screen.titleTh,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (screen == AppScreen.NOTIFICATIONS && unreadNotifsCount > 0) {
                                Surface(
                                    color = NeonRed,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$unreadNotifsCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        VerticalDivider(
            modifier = Modifier.width(1.dp).fillMaxHeight(),
            color = BorderSubtle
        )
    }
}

@Composable
fun DesktopRightSidebar(
    members: List<MemberInfo>,
    voiceChannels: List<VoiceChannelInfo>,
    upcomingEvents: List<com.example.data.local.entities.GameEventEntity>,
    currentUser: com.example.data.model.CurrentUser? = null,
    firebaseUser: com.example.data.model.FirebaseAuthUserInfo? = null,
    onOpenFirebaseAuth: () -> Unit = {},
    onMemberClick: (MemberInfo) -> Unit,
    onJoinVoice: (String) -> Unit
) {
    Row(modifier = Modifier.width(260.dp).fillMaxHeight()) {
        VerticalDivider(
            modifier = Modifier.width(1.dp).fillMaxHeight(),
            color = BorderSubtle
        )
        Surface(
            color = DiscordDarker,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Firebase Account & Sync status pill for Desktop
                Surface(
                    color = DiscordDark,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentUser?.isFirebaseLinked == true) NeonGreen.copy(alpha = 0.5f) else NeonOrange.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFirebaseAuth() }
                        .testTag("desktop_sidebar_firebase_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (firebaseUser != null) "🔥" else "🔐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firebaseUser?.displayName ?: firebaseUser?.email ?: "Firebase Account",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentUser?.isFirebaseLinked == true) "Discord Linked 🔗" else if (firebaseUser != null) "Not Linked ⚠️" else "Sign In",
                                fontSize = 9.sp,
                                color = if (currentUser?.isFirebaseLinked == true) NeonGreen else if (firebaseUser != null) NeonYellow else NeonCyan
                            )
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                // Online Members
                Text(
                    text = "สมาชิกออนไลน์ — ${members.count { it.status != UserStatus.OFFLINE }}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    members.filter { it.status != UserStatus.OFFLINE }.take(5).forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMemberClick(m) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscordAvatar(imageUrl = m.avatarUrl, displayName = m.displayName, status = m.status, size = 26)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(m.displayName, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                if (!m.currentGame.isNullOrBlank()) {
                                    Text("🎮 ${m.currentGame}", fontSize = 10.sp, color = NeonCyan)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                // Voice Channels quick glance
                Text(
                    text = "ห้องเสียง ACTIVE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    voiceChannels.take(3).forEach { ch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJoinVoice(ch.id) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(ch.name, fontSize = 12.sp, color = TextPrimary)
                            }
                            Text("${ch.userIds.size} คน", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

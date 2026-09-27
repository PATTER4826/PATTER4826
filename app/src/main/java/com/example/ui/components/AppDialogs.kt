package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CreateEventDialog(
    voiceChannelNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        game: String,
        date: String,
        startTime: String,
        endTime: String,
        maxPlayers: Int,
        description: String,
        voiceChannel: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("นัดเล่น Valorant 5v5") }
    var game by remember { mutableStateOf("Valorant") }
    var date by remember { mutableStateOf("28 กันยายน 2026") }
    var startTime by remember { mutableStateOf("20:00") }
    var endTime by remember { mutableStateOf("23:00") }
    var maxPlayersStr by remember { mutableStateOf("10") }
    var description by remember { mutableStateOf("ซ้อมทีมแข่งขำๆ ซ้อมแมพ Haven และ Ascent") }
    var selectedVoice by remember { mutableStateOf(voiceChannelNames.firstOrNull() ?: "Gaming Room") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarker),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, DiscordBlurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "สร้างกิจกรรมนัดเล่นเกม",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ชื่อกิจกรรม") },
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it },
                    label = { Text("เกมที่เล่น") },
                    modifier = Modifier.fillMaxWidth().testTag("event_game_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("วันที่ (เช่น 28 กันยายน 2026)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("เวลาเริ่ม") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("เวลาสิ้นสุด") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = maxPlayersStr,
                    onValueChange = { maxPlayersStr = it },
                    label = { Text("จำนวนผู้เล่นสูงสุด") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Discord Voice Channel ที่จะใช้:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    voiceChannelNames.take(3).forEach { ch ->
                        FilterChip(
                            selected = selectedVoice == ch,
                            onClick = { selectedVoice = ch },
                            label = { Text(ch, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("รายละเอียด") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

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
                        Text("ยกเลิก")
                    }
                    Button(
                        onClick = {
                            val max = maxPlayersStr.toIntOrNull() ?: 10
                            onConfirm(title, game, date, startTime, endTime, max, description, selectedVoice)
                        },
                        modifier = Modifier.weight(1f).testTag("confirm_create_event_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
                    ) {
                        Text("สร้างกิจกรรม")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateLfgDialog(
    voiceChannelNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        game: String,
        neededPlayers: Int,
        startTime: String,
        skillLevel: String,
        voiceChannel: String,
        description: String
    ) -> Unit
) {
    var game by remember { mutableStateOf("Valorant") }
    var neededStr by remember { mutableStateOf("2") }
    var startTime by remember { mutableStateOf("20:00") }
    var skillLevel by remember { mutableStateOf("Gold / Platinum") }
    var voiceChannel by remember { mutableStateOf(voiceChannelNames.firstOrNull() ?: "Gaming Room") }
    var description by remember { mutableStateOf("ขาด 2 คนลง Competitive ด่วน ขอสื่อสารในไมค์ได้") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarker),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, NeonOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = NeonOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "โพสต์หาเพื่อนเล่นเกม (LFG)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it },
                    label = { Text("เกมที่เล่น") },
                    modifier = Modifier.fillMaxWidth().testTag("lfg_game_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = neededStr,
                        onValueChange = { neededStr = it },
                        label = { Text("ต้องการอีก (คน)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("เวลาเริ่ม") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = skillLevel,
                    onValueChange = { skillLevel = it },
                    label = { Text("ระดับการเล่น (เช่น Casual, Platinum+)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("คำอธิบายเพิ่มเติม") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

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
                        Text("ยกเลิก")
                    }
                    Button(
                        onClick = {
                            val needed = neededStr.toIntOrNull() ?: 2
                            onConfirm(game, needed, startTime, skillLevel, voiceChannel, description)
                        },
                        modifier = Modifier.weight(1f).testTag("confirm_create_lfg_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Text("โพสต์หาตี้")
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectDiscordDialog(
    currentUsername: String,
    currentDisplayName: String,
    currentGuildId: String,
    onDismiss: () -> Unit,
    onConfirm: (username: String, displayName: String, guildId: String) -> Unit,
    onStartOAuthFlow: (clientId: String) -> String = { "" },
    onExchangeOAuthCode: (code: String, state: String, clientId: String) -> Unit = { _, _, _ -> },
    onSimulateOAuthSuccess: (username: String, displayName: String, guildId: String) -> Unit = { _, _, _ -> }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0: OAuth2 PKCE, 1: Manual / Direct Config
    var username by remember { mutableStateOf(currentUsername) }
    var displayName by remember { mutableStateOf(currentDisplayName) }
    var guildId by remember { mutableStateOf(currentGuildId) }
    var clientId by remember { mutableStateOf("109823487123987123") }

    var generatedAuthUrl by remember { mutableStateOf<String?>(null) }
    var authCodeOrUrlInput by remember { mutableStateOf("") }
    var copiedToClipboard by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarker),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, DiscordBlurple, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DiscordBlurple.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = DiscordBlurple, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "เชื่อมต่อ Discord OAuth2",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Secure Token Exchange & Keystore Storage",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: OAuth2 PKCE vs Direct / Manual
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DiscordDark,
                    contentColor = NeonCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🌐 OAuth2 Flow (PKCE)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("⚙️ กำหนดบัญชีเอง", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Security Best Practices Info Box
                    Surface(
                        color = DiscordBlurpleDark.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DiscordBlurple.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Best Security Practices Implemented", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• RFC 7636 PKCE (S256 Code Challenge, Code Verifier)\n• Android Keystore Hardware AES-256 GCM Token Encryption\n• Anti-CSRF Random State & Single-use Replay Protection",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientId,
                        onValueChange = { clientId = it },
                        label = { Text("Discord Client ID") },
                        modifier = Modifier.fillMaxWidth().testTag("oauth_client_id_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 1: Generate Authorization URL
                    Button(
                        onClick = {
                            val url = onStartOAuthFlow(clientId)
                            generatedAuthUrl = url
                            copiedToClipboard = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                        modifier = Modifier.fillMaxWidth().testTag("btn_generate_oauth_url")
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1. สร้าง Discord Authorization URL (PKCE)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (generatedAuthUrl != null) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = DiscordDarkest,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Generated PKCE OAuth2 URL:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = generatedAuthUrl!!,
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedAuthUrl!!))
                                            copiedToClipboard = true
                                        },
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text(if (copiedToClipboard) "✓ คัดลอกแล้ว" else "📋 คัดลอก URL", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(generatedAuthUrl)
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("🌐 เปิดใน Browser", fontSize = 10.sp, color = DiscordDarkest, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Step 2: Code exchange
                        Text(
                            text = "2. แลกเปลี่ยน Token ด้วย Authorization Code:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = authCodeOrUrlInput,
                            onValueChange = { authCodeOrUrlInput = it },
                            placeholder = { Text("วาง code=... หรือ discordhub://oauth?code=...", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("oauth_code_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val input = authCodeOrUrlInput.trim()
                                val code = if (input.contains("code=")) {
                                    input.substringAfter("code=").substringBefore("&")
                                } else {
                                    input
                                }
                                val state = if (input.contains("state=")) {
                                    input.substringAfter("state=").substringBefore("&")
                                } else {
                                    ""
                                }
                                onExchangeOAuthCode(code, state, clientId)
                            },
                            enabled = authCodeOrUrlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_exchange_oauth_token")
                        ) {
                            Text("แลกเปลี่ยน Token & บันทึกลง Keystore", fontSize = 11.sp, color = DiscordDarkest, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                        Text(" หรือทดสอบใน Emulator ", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 6.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Instant OAuth Token exchange
                    OutlinedButton(
                        onClick = {
                            onSimulateOAuthSuccess(username, displayName, guildId)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonYellow),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_simulate_oauth_token")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ จำลอง OAuth Token Exchange & Keystore Storage ทันที", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                } else {
                    // Manual Direct Account Config
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Discord Username#Tag") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = guildId,
                        onValueChange = { guildId = it },
                        label = { Text("Discord Server (Guild) ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("ยกเลิก")
                        }
                        Button(
                            onClick = {
                                onConfirm(username, displayName, guildId)
                            },
                            modifier = Modifier.weight(1f).testTag("confirm_connect_discord_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
                        ) {
                            Text("บันทึก & ผูกบัญชี")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FirebaseAuthDialog(
    firebaseUser: com.example.data.model.FirebaseAuthUserInfo?,
    currentUser: com.example.data.model.CurrentUser,
    onDismiss: () -> Unit,
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (email: String, pass: String, displayName: String) -> Unit,
    onAnonymousSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onLinkDiscord: () -> Unit,
    onUnlinkDiscord: () -> Unit,
    onForgotPassword: (email: String) -> Unit
) {
    var selectedAuthTab by remember { mutableStateOf(0) } // 0: Sign In, 1: Sign Up
    var email by remember { mutableStateOf("gamer@discordhub.com") }
    var password by remember { mutableStateOf("gaming1234") }
    var displayName by remember { mutableStateOf(currentUser.displayName) }
    var showPassword by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DiscordDarker),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, NeonOrange.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonOrange.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🔥", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Firebase Authentication",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "จัดการบัญชีที่ผูกกับ Discord",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (firebaseUser != null) {
                    // Logged In Card State
                    Surface(
                        color = DiscordDark,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (firebaseUser.isAnonymous) NeonYellow.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (firebaseUser.isAnonymous) "👤" else "✓", fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = firebaseUser.displayName ?: firebaseUser.email ?: "Gamer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (firebaseUser.isAnonymous) "โหมดผู้เล่นชั่วคราว (Guest Session)" else (firebaseUser.email ?: ""),
                                        fontSize = 11.sp,
                                        color = if (firebaseUser.isAnonymous) NeonYellow else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Firebase UID: ${firebaseUser.uid}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Discord Account Link Section
                    Surface(
                        color = DiscordBlurpleDark.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentUser.isFirebaseLinked) NeonGreen else DiscordBlurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = if (currentUser.isFirebaseLinked) NeonGreen else NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "สถานะการผูก Discord Account",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    color = if (currentUser.isFirebaseLinked) NeonGreen.copy(alpha = 0.2f) else NeonYellow.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (currentUser.isFirebaseLinked) "ผูกแล้ว" else "ยังไม่ผูก",
                                        color = if (currentUser.isFirebaseLinked) NeonGreen else NeonYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Discord: ${currentUser.username} (${currentUser.displayName})",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "การผูกบัญชีช่วยให้สิทธิ์ Discord Guild, ตารางนัดเล่นเกม, และประวัติแชทถูกรักษาความปลอดภัยไว้บนคลาวด์ Firebase",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (!currentUser.isFirebaseLinked) {
                                Button(
                                    onClick = onLinkDiscord,
                                    colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_link_discord_to_firebase")
                                ) {
                                    Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ผูกบัญชี Discord นี้ทันที", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onUnlinkDiscord,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_unlink_discord_from_firebase")
                                ) {
                                    Text("ยกเลิกการผูกบัญชี Discord", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Out Button
                    OutlinedButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.fillMaxWidth().testTag("btn_firebase_sign_out")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ออกจากระบบ Firebase", fontSize = 12.sp)
                    }

                } else {
                    // Not Logged In: Sign In / Sign Up Forms
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = DiscordDark,
                        contentColor = NeonCyan
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0 },
                            text = { Text("เข้าสู่ระบบ (Sign In)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1 },
                            text = { Text("สมัครใหม่ (Sign Up)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedAuthTab == 1) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name / Gaming Tag") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("firebase_email_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (รหัสผ่าน)") },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("firebase_password_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedAuthTab == 0) {
                        Button(
                            onClick = { onSignIn(email, password) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_firebase_signin_submit")
                        ) {
                            Text("เข้าสู่ระบบด้วย Firebase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = { onForgotPassword(email) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("ลืมรหัสผ่าน?", fontSize = 11.sp, color = NeonCyan)
                        }
                    } else {
                        Button(
                            onClick = { onSignUp(email, password, displayName) },
                            colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple),
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_firebase_signup_submit")
                        ) {
                            Text("สร้างบัญชี Firebase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                        Text(" หรือ ", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 8.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Instant Anonymous Guest login
                    OutlinedButton(
                        onClick = onAnonymousSignIn,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonYellow),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_firebase_anonymous")
                    ) {
                        Text("⚡ เข้าสู่ระบบแบบ Guest ทันที (Anonymous)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

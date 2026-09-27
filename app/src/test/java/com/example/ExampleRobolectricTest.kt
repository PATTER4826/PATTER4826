package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserStatus
import com.example.data.repository.SeedData
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("กลุ่มเล่นเกม", appName)
    }

    @Test
    fun `verify seed data has members and channels`() {
        assertTrue(SeedData.defaultMembers.isNotEmpty())
        assertTrue(SeedData.defaultVoiceChannels.isNotEmpty())
        assertTrue(SeedData.defaultTextChannels.isNotEmpty())

        val onlineMember = SeedData.defaultMembers.firstOrNull { it.status == UserStatus.ONLINE }
        assertNotNull(onlineMember)
    }

    @Test
    fun `verify initial events and lfg posts exist`() {
        assertTrue(SeedData.initialEvents.isNotEmpty())
        assertTrue(SeedData.initialLfgPosts.isNotEmpty())
        val valorantEvent = SeedData.initialEvents.firstOrNull { it.game == "Valorant" }
        assertNotNull(valorantEvent)
    }

    @Test
    fun `verify SecureTokenStorage encrypts and retrieves DiscordOAuthToken`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = com.example.data.auth.oauth.SecureTokenStorage(context)

        storage.clearOAuthToken()
        org.junit.Assert.assertNull(storage.getOAuthToken())

        val token = com.example.data.auth.oauth.DiscordOAuthToken(
            accessToken = "discord_test_access_token_secret_999",
            tokenType = "Bearer",
            expiresAt = System.currentTimeMillis() + 86400_000L,
            refreshToken = "discord_test_refresh_token_secret_111",
            scope = "identify guilds email"
        )

        storage.saveOAuthToken(token)

        val retrieved = storage.getOAuthToken()
        assertNotNull(retrieved)
        assertEquals("discord_test_access_token_secret_999", retrieved?.accessToken)
        assertEquals("Bearer", retrieved?.tokenType)
        assertEquals("discord_test_refresh_token_secret_111", retrieved?.refreshToken)
        assertEquals("identify guilds email", retrieved?.scope)
        assertFalse(retrieved!!.isExpired)

        storage.clearOAuthToken()
        org.junit.Assert.assertNull(storage.getOAuthToken())
    }

    @Test
    fun `verify SecureTokenStorage pending state and verifier lifecycle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = com.example.data.auth.oauth.SecureTokenStorage(context)

        val state = "test_csrf_state_12345"
        val verifier = "test_code_verifier_67890"

        storage.savePendingOAuthState(state, verifier)

        // Consuming with wrong state fails
        val wrongAttempt = storage.consumePendingOAuthState("wrong_state")
        org.junit.Assert.assertNull(wrongAttempt)

        // Saving again
        storage.savePendingOAuthState(state, verifier)
        // Consuming with correct state returns verifier
        val successAttempt = storage.consumePendingOAuthState(state)
        assertEquals(verifier, successAttempt)

        // Replay attack is rejected because state was consumed and cleared
        val replayAttempt = storage.consumePendingOAuthState(state)
        org.junit.Assert.assertNull(replayAttempt)
    }

    @Test
    fun `verify DiscordOAuthService builds authorization URL with PKCE parameters`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = com.example.data.auth.oauth.DiscordOAuthService(context)

        val authUrl = service.initiateOAuthFlow(
            clientId = "test_client_id_001",
            redirectUri = "discordhub://oauth"
        )

        assertTrue(authUrl.startsWith("https://discord.com/oauth2/authorize"))
        assertTrue(authUrl.contains("response_type=code"))
        assertTrue(authUrl.contains("client_id=test_client_id_001"))
        assertTrue(authUrl.contains("redirect_uri=discordhub%3A%2F%2Foauth") || authUrl.contains("redirect_uri=discordhub://oauth"))
        assertTrue(authUrl.contains("code_challenge="))
        assertTrue(authUrl.contains("code_challenge_method=S256"))
        assertTrue(authUrl.contains("state="))
    }
}

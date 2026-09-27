package com.example

import com.example.data.local.entities.UserSettingsEntity
import com.example.data.model.CurrentUser
import com.example.data.model.FirebaseAuthUserInfo
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `verify FirebaseAuthUserInfo model holds user details`() {
        val user = FirebaseAuthUserInfo(
            uid = "firebase_gamer_uid_12345",
            email = "gamer@siamgaming.com",
            displayName = "SiamProPlayer",
            isAnonymous = false,
            isDiscordLinked = true,
            linkedDiscordUsername = "PlayerOne#0001"
        )

        assertEquals("firebase_gamer_uid_12345", user.uid)
        assertEquals("gamer@siamgaming.com", user.email)
        assertEquals("SiamProPlayer", user.displayName)
        assertFalse(user.isAnonymous)
        assertTrue(user.isDiscordLinked)
        assertEquals("PlayerOne#0001", user.linkedDiscordUsername)
    }

    @Test
    fun `verify CurrentUser links to Firebase`() {
        val defaultUser = CurrentUser()
        assertNull(defaultUser.firebaseUid)
        assertFalse(defaultUser.isFirebaseLinked)

        val linkedUser = defaultUser.copy(
            firebaseUid = "fb_123456",
            firebaseEmail = "gamer@discordhub.com",
            isFirebaseLinked = true
        )
        assertTrue(linkedUser.isFirebaseLinked)
        assertEquals("fb_123456", linkedUser.firebaseUid)
    }

    @Test
    fun `verify UserSettingsEntity stores Firebase configuration`() {
        val entity = UserSettingsEntity(
            discordUsername = "PlayerOne#0001",
            discordDisplayName = "PlayerOne",
            firebaseUid = "fb_test_uid",
            firebaseEmail = "user@example.com",
            firebaseLinkedDiscord = true
        )
        assertEquals("PlayerOne#0001", entity.discordUsername)
        assertEquals("fb_test_uid", entity.firebaseUid)
        assertTrue(entity.firebaseLinkedDiscord)
    }

    @Test
    fun `verify PKCE code_verifier meets RFC 7636 requirements`() {
        val verifier = com.example.data.auth.oauth.OAuthSecurityUtils.generateCodeVerifier(64)
        assertEquals(64, verifier.length)
        // Must contain only unreserved characters: [A-Z], [a-z], [0-9], "-", ".", "_", "~"
        assertTrue(verifier.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '-' || it == '.' || it == '_' || it == '~' })
    }

    @Test
    fun `verify PKCE code_challenge is reproducible and non-empty`() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val challenge1 = com.example.data.auth.oauth.OAuthSecurityUtils.generateCodeChallenge(verifier)
        val challenge2 = com.example.data.auth.oauth.OAuthSecurityUtils.generateCodeChallenge(verifier)

        assertTrue(challenge1.isNotBlank())
        assertEquals(challenge1, challenge2)
        // Base64URL without padding should not contain '=' or '+' or '/'
        assertFalse(challenge1.contains("="))
        assertFalse(challenge1.contains("+"))
        assertFalse(challenge1.contains("/"))
    }

    @Test
    fun `verify anti-CSRF state validation`() {
        val state = com.example.data.auth.oauth.OAuthSecurityUtils.generateState(32)
        assertTrue(state.isNotBlank())
        assertTrue(com.example.data.auth.oauth.OAuthSecurityUtils.validateState(state, state))
        assertFalse(com.example.data.auth.oauth.OAuthSecurityUtils.validateState(state, "tampered_state"))
        assertFalse(com.example.data.auth.oauth.OAuthSecurityUtils.validateState(null, state))
    }

    @Test
    fun `verify DiscordOAuthToken expiry check`() {
        val validToken = com.example.data.auth.oauth.DiscordOAuthToken(
            accessToken = "valid_token",
            expiresAt = System.currentTimeMillis() + 3600_000L
        )
        assertFalse(validToken.isExpired)

        val expiredToken = com.example.data.auth.oauth.DiscordOAuthToken(
            accessToken = "expired_token",
            expiresAt = System.currentTimeMillis() - 1000L
        )
        assertTrue(expiredToken.isExpired)
    }
}


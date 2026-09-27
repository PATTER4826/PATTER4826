package com.example.data.auth.oauth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object OAuthSecurityUtils {

    private val secureRandom = SecureRandom()
    private const val CODE_VERIFIER_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"

    /**
     * Generates a cryptographically secure random string for PKCE code_verifier.
     * RFC 7636 Section 4.1 specifies minimum 43 and maximum 128 characters.
     */
    fun generateCodeVerifier(length: Int = 64): String {
        val len = length.coerceIn(43, 128)
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            val randomIndex = secureRandom.nextInt(CODE_VERIFIER_CHARACTERS.length)
            sb.append(CODE_VERIFIER_CHARACTERS[randomIndex])
        }
        return sb.toString()
    }

    /**
     * Generates a code_challenge from code_verifier using SHA-256 and Base64 URL-safe encoding without padding.
     * RFC 7636 Section 4.2: code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return base64UrlEncode(digest)
    }

    /**
     * Generates a cryptographically secure random state parameter to mitigate CSRF attacks.
     */
    fun generateState(length: Int = 32): String {
        val randomBytes = ByteArray(length)
        secureRandom.nextBytes(randomBytes)
        return base64UrlEncode(randomBytes)
    }

    fun base64UrlEncode(bytes: ByteArray): String {
        return try {
            Base64.encodeToString(
                bytes,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            ).trim()
        } catch (t: Throwable) {
            java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).trim()
        }
    }

    /**
     * Validates that state matches the expected state received during OAuth initialization.
     */
    fun validateState(receivedState: String?, expectedState: String?): Boolean {
        if (receivedState.isNullOrBlank() || expectedState.isNullOrBlank()) return false
        return MessageDigest.isEqual(
            receivedState.toByteArray(Charsets.UTF_8),
            expectedState.toByteArray(Charsets.UTF_8)
        )
    }
}

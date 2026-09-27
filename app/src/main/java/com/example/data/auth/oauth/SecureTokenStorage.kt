package com.example.data.auth.oauth

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class DiscordOAuthToken(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresAt: Long,
    val refreshToken: String? = null,
    val scope: String = "identify guilds email"
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= (expiresAt - 60_000L) // 1 minute safety buffer
}

class SecureTokenStorage(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val secureRandom = SecureRandom()
    private val masterKey: SecretKey by lazy { getOrCreateMasterKey() }

    companion object {
        private const val PREFS_NAME = "secure_discord_oauth_prefs"
        private const val KEY_ALIAS = "discord_oauth_master_key_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

        private const val PREF_ENCRYPTED_TOKEN_DATA = "enc_oauth_token_data"
        private const val PREF_PENDING_STATE = "pending_oauth_state"
        private const val PREF_PENDING_VERIFIER = "pending_oauth_verifier"
        private const val PREF_PENDING_TIMESTAMP = "pending_oauth_timestamp"
        private const val STATE_VALIDITY_MS = 10 * 60 * 1000L // 10 minutes
    }

    private fun getOrCreateMasterKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            } else {
                keyStore.getKey(KEY_ALIAS, null) as SecretKey
            }
        } catch (e: Exception) {
            // Software fallback for JVM unit test environments (e.g. Robolectric)
            getFallbackSoftwareKey()
        }
    }

    private fun getFallbackSoftwareKey(): SecretKey {
        val fallbackAlias = "fb_aes_key"
        val existingHex = prefs.getString(fallbackAlias, null)
        return if (existingHex != null) {
            val keyBytes = Base64.decode(existingHex, Base64.NO_WRAP)
            SecretKeySpec(keyBytes, "AES")
        } else {
            val keyBytes = ByteArray(32)
            secureRandom.nextBytes(keyBytes)
            prefs.edit().putString(fallbackAlias, Base64.encodeToString(keyBytes, Base64.NO_WRAP)).apply()
            SecretKeySpec(keyBytes, "AES")
        }
    }

    /**
     * Encrypts plaintext bytes using AES-256 GCM with a fresh random 12-byte IV.
     */
    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine IV (12 bytes) + CipherText
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts combined IV + CipherText using AES-256 GCM.
     */
    private fun decrypt(encryptedBase64: String): String? {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) return null

            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Safely stores OAuth tokens encrypted with AES-256 GCM.
     */
    fun saveOAuthToken(token: DiscordOAuthToken) {
        val json = JSONObject().apply {
            put("access_token", token.accessToken)
            put("token_type", token.tokenType)
            put("expires_at", token.expiresAt)
            put("refresh_token", token.refreshToken ?: "")
            put("scope", token.scope)
        }
        val encrypted = encrypt(json.toString())
        prefs.edit().putString(PREF_ENCRYPTED_TOKEN_DATA, encrypted).apply()
    }

    /**
     * Retrieves and decrypts the stored OAuth token.
     */
    fun getOAuthToken(): DiscordOAuthToken? {
        val encrypted = prefs.getString(PREF_ENCRYPTED_TOKEN_DATA, null) ?: return null
        val decryptedJson = decrypt(encrypted) ?: return null
        return try {
            val json = JSONObject(decryptedJson)
            DiscordOAuthToken(
                accessToken = json.getString("access_token"),
                tokenType = json.optString("token_type", "Bearer"),
                expiresAt = json.getLong("expires_at"),
                refreshToken = json.optString("refresh_token").ifBlank { null },
                scope = json.optString("scope", "identify guilds email")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns the access token if available and not expired.
     */
    fun getValidAccessToken(): String? {
        val token = getOAuthToken() ?: return null
        return if (!token.isExpired) token.accessToken else null
    }

    /**
     * Clears all encrypted tokens upon logout or disconnect.
     */
    fun clearOAuthToken() {
        prefs.edit()
            .remove(PREF_ENCRYPTED_TOKEN_DATA)
            .remove(PREF_PENDING_STATE)
            .remove(PREF_PENDING_VERIFIER)
            .remove(PREF_PENDING_TIMESTAMP)
            .apply()
    }

    /**
     * Stores state and PKCE code_verifier for validating redirect callback and token exchange.
     */
    fun savePendingOAuthState(state: String, codeVerifier: String) {
        prefs.edit()
            .putString(PREF_PENDING_STATE, state)
            .putString(PREF_PENDING_VERIFIER, codeVerifier)
            .putLong(PREF_PENDING_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Verifies the state against stored state and returns codeVerifier.
     * Clears the pending state to prevent replay attacks.
     */
    fun consumePendingOAuthState(receivedState: String): String? {
        val savedState = prefs.getString(PREF_PENDING_STATE, null) ?: return null
        val savedVerifier = prefs.getString(PREF_PENDING_VERIFIER, null) ?: return null
        val savedTimestamp = prefs.getLong(PREF_PENDING_TIMESTAMP, 0L)

        // Clear immediately to prevent replay
        prefs.edit()
            .remove(PREF_PENDING_STATE)
            .remove(PREF_PENDING_VERIFIER)
            .remove(PREF_PENDING_TIMESTAMP)
            .apply()

        // Check expiration
        if (System.currentTimeMillis() - savedTimestamp > STATE_VALIDITY_MS) {
            return null
        }

        return if (OAuthSecurityUtils.validateState(receivedState, savedState)) {
            savedVerifier
        } else {
            null
        }
    }
}

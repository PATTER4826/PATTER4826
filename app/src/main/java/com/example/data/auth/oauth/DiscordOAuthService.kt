package com.example.data.auth.oauth

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class DiscordOAuthService(
    private val context: Context,
    val secureTokenStorage: SecureTokenStorage = SecureTokenStorage(context)
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _sessionState = MutableStateFlow(OAuthSessionState())
    val sessionState: StateFlow<OAuthSessionState> = _sessionState.asStateFlow()

    companion object {
        const val DISCORD_API_BASE = "https://discord.com/api/v10"
        const val DISCORD_AUTHORIZE_URL = "https://discord.com/oauth2/authorize"
        const val DISCORD_TOKEN_URL = "$DISCORD_API_BASE/oauth2/token"
        const val DISCORD_REVOKE_URL = "$DISCORD_API_BASE/oauth2/token/revoke"
        const val DISCORD_USER_ME_URL = "$DISCORD_API_BASE/users/@me"
        const val DISCORD_USER_GUILDS_URL = "$DISCORD_API_BASE/users/@me/guilds"

        const val DEFAULT_CLIENT_ID = "109823487123987123"
        const val DEFAULT_REDIRECT_URI = "discordhub://oauth"
        val DEFAULT_SCOPES = listOf("identify", "guilds", "email")
    }

    init {
        // Check if an existing valid token is stored securely in keystore
        checkExistingToken()
    }

    private fun checkExistingToken() {
        val existingToken = secureTokenStorage.getOAuthToken()
        if (existingToken != null && !existingToken.isExpired) {
            _sessionState.value = _sessionState.value.copy(
                step = OAuthStep.AUTHENTICATED,
                isSecuredWithKeystore = true
            )
        }
    }

    /**
     * Step 1: Initiates Discord OAuth2 flow with PKCE (RFC 7636).
     * Generates a cryptographically random code_verifier, code_challenge (S256), and CSRF state.
     * Persists state & verifier into SecureTokenStorage.
     * Returns the formatted Discord authorization URL.
     */
    fun initiateOAuthFlow(
        clientId: String = DEFAULT_CLIENT_ID,
        redirectUri: String = DEFAULT_REDIRECT_URI,
        scopes: List<String> = DEFAULT_SCOPES
    ): String {
        _sessionState.value = _sessionState.value.copy(step = OAuthStep.GENERATING_AUTH_URL, errorMessage = null)

        val codeVerifier = OAuthSecurityUtils.generateCodeVerifier(64)
        val codeChallenge = OAuthSecurityUtils.generateCodeChallenge(codeVerifier)
        val state = OAuthSecurityUtils.generateState(32)

        // Securely store the pending OAuth state and verifier (valid for 10 minutes)
        secureTokenStorage.savePendingOAuthState(state, codeVerifier)

        val scopeParam = scopes.joinToString(" ")

        val authUrl = Uri.parse(DISCORD_AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", clientId.trim())
            .appendQueryParameter("scope", scopeParam)
            .appendQueryParameter("state", state)
            .appendQueryParameter("redirect_uri", redirectUri.trim())
            .appendQueryParameter("prompt", "consent")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
            .toString()

        _sessionState.value = _sessionState.value.copy(
            step = OAuthStep.WAITING_FOR_REDIRECT,
            authUrl = authUrl
        )

        return authUrl
    }

    /**
     * Step 2: Handles the OAuth2 redirect callback.
     * Validates the state parameter against CSRF and replay attacks.
     * Exchanges the code + code_verifier for tokens via POST to Discord token endpoint.
     * Encrypts and stores tokens in Android Keystore / AES-GCM.
     * Fetches user profile and guild memberships.
     */
    suspend fun handleOAuthCallback(
        code: String,
        receivedState: String,
        clientId: String = DEFAULT_CLIENT_ID,
        redirectUri: String = DEFAULT_REDIRECT_URI
    ): Result<DiscordUserProfile> = withContext(Dispatchers.IO) {
        _sessionState.value = _sessionState.value.copy(step = OAuthStep.EXCHANGING_TOKEN, errorMessage = null)

        // 1. Validate State and consume Code Verifier
        val codeVerifier = secureTokenStorage.consumePendingOAuthState(receivedState)
        if (codeVerifier == null) {
            val err = "OAuth State Validation Failed (CSRF detected or request expired)."
            _sessionState.value = _sessionState.value.copy(step = OAuthStep.ERROR, errorMessage = err)
            return@withContext Result.failure(SecurityException(err))
        }

        // 2. Exchange Code for Access Token using PKCE
        try {
            val formBody = FormBody.Builder()
                .add("client_id", clientId.trim())
                .add("grant_type", "authorization_code")
                .add("code", code.trim())
                .add("redirect_uri", redirectUri.trim())
                .add("code_verifier", codeVerifier)
                .build()

            val request = Request.Builder()
                .url(DISCORD_TOKEN_URL)
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val err = "Discord Token Exchange Error (${response.code}): $responseBody"
                _sessionState.value = _sessionState.value.copy(step = OAuthStep.ERROR, errorMessage = err)
                return@withContext Result.failure(IOException(err))
            }

            val json = JSONObject(responseBody)
            val accessToken = json.getString("access_token")
            val tokenType = json.optString("token_type", "Bearer")
            val expiresIn = json.getLong("expires_in")
            val refreshToken = json.optString("refresh_token").ifBlank { null }
            val scope = json.optString("scope", "identify guilds email")

            val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)

            // 3. Encrypt and persist tokens into Android Keystore-backed AES-256 GCM storage
            val oauthToken = DiscordOAuthToken(
                accessToken = accessToken,
                tokenType = tokenType,
                expiresAt = expiresAt,
                refreshToken = refreshToken,
                scope = scope
            )
            secureTokenStorage.saveOAuthToken(oauthToken)

            // 4. Fetch user profile from Discord
            _sessionState.value = _sessionState.value.copy(step = OAuthStep.FETCHING_USER_PROFILE)
            val profileResult = fetchUserProfile(accessToken)
            val guildsResult = fetchUserGuilds(accessToken)

            if (profileResult.isSuccess) {
                val profile = profileResult.getOrThrow()
                val guilds = guildsResult.getOrDefault(emptyList())

                _sessionState.value = _sessionState.value.copy(
                    step = OAuthStep.AUTHENTICATED,
                    userProfile = profile,
                    guilds = guilds,
                    isSecuredWithKeystore = true,
                    errorMessage = null
                )
                Result.success(profile)
            } else {
                val err = profileResult.exceptionOrNull()?.message ?: "Failed to fetch user profile"
                _sessionState.value = _sessionState.value.copy(step = OAuthStep.ERROR, errorMessage = err)
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _sessionState.value = _sessionState.value.copy(step = OAuthStep.ERROR, errorMessage = e.message)
            Result.failure(e)
        }
    }

    /**
     * Fetches authenticated user's profile from Discord API (GET /users/@me).
     */
    suspend fun fetchUserProfile(accessToken: String): Result<DiscordUserProfile> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(DISCORD_USER_ME_URL)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to get profile (${response.code}): $body"))
            }

            val json = JSONObject(body)
            val profile = DiscordUserProfile(
                id = json.getString("id"),
                username = json.getString("username"),
                discriminator = json.optString("discriminator", "0"),
                globalName = json.optString("global_name").ifBlank { null },
                avatar = json.optString("avatar").ifBlank { null },
                email = json.optString("email").ifBlank { null },
                verified = json.optBoolean("verified", false)
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches user's guild memberships from Discord API (GET /users/@me/guilds).
     */
    suspend fun fetchUserGuilds(accessToken: String): Result<List<DiscordGuildSummary>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(DISCORD_USER_GUILDS_URL)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to get guilds (${response.code})"))
            }

            val jsonArray = JSONArray(body)
            val list = mutableListOf<DiscordGuildSummary>()
            for (i in 0 until jsonArray.length()) {
                val g = jsonArray.getJSONObject(i)
                list.add(
                    DiscordGuildSummary(
                        id = g.getString("id"),
                        name = g.getString("name"),
                        icon = g.optString("icon").ifBlank { null },
                        owner = g.optBoolean("owner", false),
                        permissions = g.optString("permissions").ifBlank { null }
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if current token requires refresh and safely exchanges refreshToken for a new token.
     */
    suspend fun refreshTokenIfNeeded(clientId: String = DEFAULT_CLIENT_ID): Result<DiscordOAuthToken> = withContext(Dispatchers.IO) {
        val currentToken = secureTokenStorage.getOAuthToken() ?: return@withContext Result.failure(IllegalStateException("No token found"))
        if (!currentToken.isExpired) {
            return@withContext Result.success(currentToken)
        }

        val refreshToken = currentToken.refreshToken ?: return@withContext Result.failure(IllegalStateException("No refresh token available"))

        try {
            val formBody = FormBody.Builder()
                .add("client_id", clientId.trim())
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build()

            val request = Request.Builder()
                .url(DISCORD_TOKEN_URL)
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Token Refresh Failed (${response.code})"))
            }

            val json = JSONObject(responseBody)
            val newAccessToken = json.getString("access_token")
            val newExpiresIn = json.getLong("expires_in")
            val newRefreshToken = json.optString("refresh_token", refreshToken)

            val updated = currentToken.copy(
                accessToken = newAccessToken,
                expiresAt = System.currentTimeMillis() + (newExpiresIn * 1000L),
                refreshToken = newRefreshToken
            )
            secureTokenStorage.saveOAuthToken(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Revokes the Discord OAuth token and purges keys from Keystore storage.
     */
    suspend fun revokeAndDisconnect(clientId: String = DEFAULT_CLIENT_ID) = withContext(Dispatchers.IO) {
        val currentToken = secureTokenStorage.getOAuthToken()
        if (currentToken != null) {
            try {
                val formBody = FormBody.Builder()
                    .add("client_id", clientId.trim())
                    .add("token", currentToken.accessToken)
                    .add("token_type_hint", "access_token")
                    .build()

                val request = Request.Builder()
                    .url(DISCORD_REVOKE_URL)
                    .post(formBody)
                    .build()

                client.newCall(request).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        secureTokenStorage.clearOAuthToken()
        _sessionState.value = OAuthSessionState()
    }

    /**
     * Simulates a successful OAuth token exchange with full Keystore AES-256 GCM token encryption.
     * Ideal for testing, offline use, or development in environments without external browser redirect hooks.
     */
    fun simulateSuccessfulOAuth(
        username: String = "PlayerOne#0001",
        displayName: String = "PlayerOne",
        guildId: String = "109823487123987123",
        guildName: String = "Siam Gaming Squad"
    ): DiscordUserProfile {
        val fakeToken = DiscordOAuthToken(
            accessToken = "discord_bearer_token_" + OAuthSecurityUtils.generateState(16),
            tokenType = "Bearer",
            expiresAt = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L), // 7 days
            refreshToken = "discord_refresh_token_" + OAuthSecurityUtils.generateState(16),
            scope = "identify guilds email"
        )
        // Store in encrypted keystore
        secureTokenStorage.saveOAuthToken(fakeToken)

        val cleanUsername = username.substringBefore("#")
        val discriminator = if (username.contains("#")) username.substringAfter("#") else "0001"

        val profile = DiscordUserProfile(
            id = "usr_" + Math.abs(username.hashCode()),
            username = cleanUsername,
            discriminator = discriminator,
            globalName = displayName,
            avatar = null,
            email = "$cleanUsername@gaminghub.discord",
            verified = true
        )

        val mockGuilds = listOf(
            DiscordGuildSummary(id = guildId, name = guildName, icon = null, owner = true),
            DiscordGuildSummary(id = "987654321098765432", name = "Valorant SEA Champions", icon = null, owner = false)
        )

        _sessionState.value = OAuthSessionState(
            step = OAuthStep.AUTHENTICATED,
            userProfile = profile,
            guilds = mockGuilds,
            isSecuredWithKeystore = true,
            errorMessage = null
        )

        return profile
    }

    fun clearError() {
        _sessionState.value = _sessionState.value.copy(errorMessage = null)
    }
}

package com.example.data.auth.oauth

data class DiscordTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String?,
    val scope: String
)

data class DiscordUserProfile(
    val id: String,
    val username: String,
    val discriminator: String? = null,
    val globalName: String? = null,
    val avatar: String? = null,
    val email: String? = null,
    val verified: Boolean = false
) {
    val displayName: String
        get() = globalName ?: username

    val avatarUrl: String
        get() = if (avatar != null) {
            "https://cdn.discordapp.com/avatars/$id/$avatar.png?size=256"
        } else {
            val defaultIndex = try { (discriminator?.toIntOrNull() ?: (id.hashCode())) % 5 } catch (e: Exception) { 0 }
            "https://cdn.discordapp.com/embed/avatars/$defaultIndex.png"
        }
}

data class DiscordGuildSummary(
    val id: String,
    val name: String,
    val icon: String? = null,
    val owner: Boolean = false,
    val permissions: String? = null
) {
    val iconUrl: String
        get() = if (icon != null) {
            "https://cdn.discordapp.com/icons/$id/$icon.png?size=128"
        } else {
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150"
        }
}

enum class OAuthStep {
    IDLE,
    GENERATING_AUTH_URL,
    WAITING_FOR_REDIRECT,
    EXCHANGING_TOKEN,
    FETCHING_USER_PROFILE,
    AUTHENTICATED,
    ERROR
}

data class OAuthSessionState(
    val step: OAuthStep = OAuthStep.IDLE,
    val authUrl: String? = null,
    val errorMessage: String? = null,
    val userProfile: DiscordUserProfile? = null,
    val guilds: List<DiscordGuildSummary> = emptyList(),
    val isSecuredWithKeystore: Boolean = true
)

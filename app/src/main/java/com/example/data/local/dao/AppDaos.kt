package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AppNotificationEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.GameEventEntity
import com.example.data.local.entities.LfgPostEntity
import com.example.data.local.entities.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameEventDao {
    @Query("SELECT * FROM game_events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<GameEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: GameEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<GameEventEntity>)

    @Update
    suspend fun updateEvent(event: GameEventEntity)

    @Query("UPDATE game_events SET userRsvp = :rsvp, currentPlayers = :newPlayerCount, participantsCsv = :participants WHERE id = :id")
    suspend fun updateRsvp(id: Long, rsvp: String, newPlayerCount: Int, participants: String)

    @Query("DELETE FROM game_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 30")
    fun getRecentMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotificationEntity>)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM app_notifications")
    suspend fun clearAll()
}

@Dao
interface LfgDao {
    @Query("SELECT * FROM lfg_posts ORDER BY timestamp DESC")
    fun getAllLfgPosts(): Flow<List<LfgPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLfgPost(post: LfgPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLfgPosts(posts: List<LfgPostEntity>)

    @Query("UPDATE lfg_posts SET hasJoined = :hasJoined, currentPlayers = :currentPlayers WHERE id = :id")
    suspend fun updateJoinStatus(id: Long, hasJoined: Boolean, currentPlayers: Int)

    @Query("DELETE FROM lfg_posts WHERE id = :id")
    suspend fun deleteLfgPost(id: Long)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettingsEntity)
}

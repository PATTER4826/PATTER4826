package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.GameEventDao
import com.example.data.local.dao.LfgDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entities.AppNotificationEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.GameEventEntity
import com.example.data.local.entities.LfgPostEntity
import com.example.data.local.entities.UserSettingsEntity

@Database(
    entities = [
        GameEventEntity::class,
        ChatMessageEntity::class,
        AppNotificationEntity::class,
        LfgPostEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameEventDao(): GameEventDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun lfgDao(): LfgDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "discord_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

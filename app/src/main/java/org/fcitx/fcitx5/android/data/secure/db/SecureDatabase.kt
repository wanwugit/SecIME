package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Friend::class, Channel::class, IndexNumber::class, DisguiseTemplate::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SecureDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun channelDao(): ChannelDao
    abstract fun indexNumberDao(): IndexNumberDao
    abstract fun disguiseTemplateDao(): DisguiseTemplateDao

    companion object {
        const val DB_NAME = "secure_database"
    }
}
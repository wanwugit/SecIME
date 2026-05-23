package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Channel.TABLE_NAME)
data class Channel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "PRIVATE")
    val channelType: String = "PRIVATE",
    @ColumnInfo(defaultValue = "1")
    val memberCount: Int = 1,
    val icon: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TABLE_NAME = "channel"
    }
}
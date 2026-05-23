package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Friend.TABLE_NAME)
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val remark: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    @ColumnInfo(defaultValue = "CODEBOOK")
    val keyMode: String = "CODEBOOK",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TABLE_NAME = "friend"
    }
}
package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = IndexNumber.TABLE_NAME)
data class IndexNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    @ColumnInfo(defaultValue = "FRIEND")
    val ownerType: String = "FRIEND",
    val label: String,
    val key: String,
    @ColumnInfo(defaultValue = "CODEBOOK")
    val mode: String = "CODEBOOK",
    @ColumnInfo(defaultValue = "1")
    val isVisible: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val isDefault: Boolean = false
) {
    companion object {
        const val TABLE_NAME = "index_number"
    }
}
package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChannelDao {
    @Insert
    suspend fun insert(channel: Channel): Long

    @Update
    suspend fun update(channel: Channel)

    @Delete
    suspend fun delete(channel: Channel)

    @Query("SELECT * FROM ${Channel.TABLE_NAME} WHERE id=:id")
    suspend fun getById(id: Long): Channel?

    @Query("SELECT * FROM ${Channel.TABLE_NAME} ORDER BY createdAt DESC")
    suspend fun getAll(): List<Channel>

    @Query("SELECT * FROM ${Channel.TABLE_NAME} WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchByName(query: String): List<Channel>

    @Query("SELECT COUNT(*) FROM ${Channel.TABLE_NAME}")
    suspend fun count(): Int
}
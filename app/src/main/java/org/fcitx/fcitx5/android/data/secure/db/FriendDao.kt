package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FriendDao {
    @Insert
    suspend fun insert(friend: Friend): Long

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(friend: Friend)

    @Query("SELECT * FROM ${Friend.TABLE_NAME} WHERE id=:id")
    suspend fun getById(id: Long): Friend?

    @Query("SELECT * FROM ${Friend.TABLE_NAME} ORDER BY createdAt DESC")
    suspend fun getAll(): List<Friend>

    @Query("SELECT * FROM ${Friend.TABLE_NAME} WHERE remark LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchByRemark(query: String): List<Friend>

    @Query("SELECT * FROM ${Friend.TABLE_NAME} WHERE userId=:userId LIMIT 1")
    suspend fun getByUserId(userId: String): Friend?

    @Query("SELECT COUNT(*) FROM ${Friend.TABLE_NAME}")
    suspend fun count(): Int
}
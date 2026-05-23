package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface IndexNumberDao {
    @Insert
    suspend fun insert(indexNumber: IndexNumber): Long

    @Update
    suspend fun update(indexNumber: IndexNumber)

    @Delete
    suspend fun delete(indexNumber: IndexNumber)

    @Query("SELECT * FROM ${IndexNumber.TABLE_NAME} WHERE id=:id")
    suspend fun getById(id: Long): IndexNumber?

    @Query("SELECT * FROM ${IndexNumber.TABLE_NAME} WHERE ownerId=:ownerId AND ownerType=:ownerType")
    suspend fun getByOwner(ownerId: Long, ownerType: String): List<IndexNumber>

    @Query("SELECT * FROM ${IndexNumber.TABLE_NAME} WHERE mode=:mode AND isVisible=1 ORDER BY isDefault DESC, label ASC")
    suspend fun getVisibleByMode(mode: String): List<IndexNumber>

    @Query("SELECT * FROM ${IndexNumber.TABLE_NAME} WHERE mode=:mode AND isDefault=1 AND ownerId=:ownerId AND ownerType=:ownerType LIMIT 1")
    suspend fun getDefaultForOwner(mode: String, ownerId: Long, ownerType: String): IndexNumber?

    @Query("UPDATE ${IndexNumber.TABLE_NAME} SET isDefault=0 WHERE mode=:mode AND ownerId=:ownerId AND ownerType=:ownerType")
    suspend fun clearDefaultForOwner(mode: String, ownerId: Long, ownerType: String)

    @Query("UPDATE ${IndexNumber.TABLE_NAME} SET isDefault=1 WHERE id=:id")
    suspend fun setDefault(id: Long)

    @Query("UPDATE ${IndexNumber.TABLE_NAME} SET isVisible=:visible WHERE id=:id")
    suspend fun setVisibility(id: Long, visible: Boolean)

    @Query("SELECT COUNT(*) FROM ${IndexNumber.TABLE_NAME}")
    suspend fun count(): Int
}
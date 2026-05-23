/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DisguiseTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: DisguiseTemplate): Long

    @Update
    suspend fun update(template: DisguiseTemplate)

    @Delete
    suspend fun delete(template: DisguiseTemplate)

    @Query("SELECT * FROM DisguiseTemplate WHERE id = :id")
    suspend fun getById(id: Long): DisguiseTemplate?

    @Query("SELECT * FROM DisguiseTemplate ORDER BY createdAt DESC")
    suspend fun getAll(): List<DisguiseTemplate>

    @Query("SELECT * FROM DisguiseTemplate WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchByName(query: String): List<DisguiseTemplate>

    @Query("SELECT COUNT(*) FROM DisguiseTemplate")
    suspend fun count(): Int
}

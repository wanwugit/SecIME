/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DisguiseTemplate(
    val name: String,
    val prefix: String,
    val suffix: String,
    val isBuiltin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.secure

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.data.secure.db.ChannelDao
import org.fcitx.fcitx5.android.data.secure.db.DisguiseTemplate
import org.fcitx.fcitx5.android.data.secure.db.DisguiseTemplateDao
import org.fcitx.fcitx5.android.data.secure.db.Friend
import org.fcitx.fcitx5.android.data.secure.db.FriendDao
import org.fcitx.fcitx5.android.data.secure.db.IndexNumberDao
import org.fcitx.fcitx5.android.data.secure.db.SecureDatabase

object SecureDataManager : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {

    private lateinit var db: SecureDatabase
    private lateinit var friendDao: FriendDao
    private lateinit var channelDao: ChannelDao
    private lateinit var indexNumberDao: IndexNumberDao
    private lateinit var templateDao: DisguiseTemplateDao

    fun init(context: Context) {
        db = Room
            .databaseBuilder(context, SecureDatabase::class.java, SecureDatabase.DB_NAME)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
        friendDao = db.friendDao()
        channelDao = db.channelDao()
        indexNumberDao = db.indexNumberDao()
        templateDao = db.disguiseTemplateDao()
        seedData()
    }

    fun getFriendDao(): FriendDao = friendDao
    fun getChannelDao(): ChannelDao = channelDao
    fun getIndexNumberDao(): IndexNumberDao = indexNumberDao
    fun getDisguiseTemplateDao(): DisguiseTemplateDao = templateDao

    private fun seedData() {
        launch {
            if (friendDao.count() == 0) {
                friendDao.insert(Friend(userId = "alice", remark = "Alice", phone = "13800001111"))
                friendDao.insert(Friend(userId = "bob", remark = "Bob", phone = "13800002222"))
                friendDao.insert(Friend(userId = "carol", remark = "Carol"))
            }
            if (templateDao.count() == 0) {
                templateDao.insert(DisguiseTemplate(name = "砍刀", prefix = "砍价群发！", suffix = "快来抢！", isBuiltin = true))
                templateDao.insert(DisguiseTemplate(name = "抽奖", prefix = "恭喜中奖！", suffix = "点击领取！", isBuiltin = true))
                templateDao.insert(DisguiseTemplate(name = "领券", prefix = "优惠券来了！", suffix = "限时领取！", isBuiltin = true))
                templateDao.insert(DisguiseTemplate(name = "游戏", prefix = "快来一起玩！", suffix = "等你加入！", isBuiltin = true))
                templateDao.insert(DisguiseTemplate(name = "红包", prefix = "红包来了！", suffix = "赶紧收！", isBuiltin = true))
                templateDao.insert(DisguiseTemplate(name = "链接", prefix = "快看这个！", suffix = "分享给你！", isBuiltin = true))
            }
        }
    }
}
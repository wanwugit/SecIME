---
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Friend.kt
  - app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Channel.kt
  - app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt
autonomous: true
---

# PLAN 01: Entity Definitions + DAO Interfaces

## Objective
Create Room Entity classes (Friend, Channel, IndexNumber) and DAO interfaces following the existing ClipboardDatabase pattern.

## Tasks

### Task 1: Create Friend Entity
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardEntry.kt — existing Entity pattern
- .planning/1-CONTEXT.md — D1 data model decisions
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Friend.kt`:

```kotlin
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
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Friend.kt` exists
- File contains `@Entity(tableName = Friend.TABLE_NAME)`
- File contains fields: id, userId, remark, phone, avatar, keyMode, createdAt
- keyMode has `defaultValue = "CODEBOOK"`
- TABLE_NAME = "friend"
</acceptance_criteria>

### Task 2: Create Channel Entity
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardEntry.kt — existing Entity pattern
- .planning/1-CONTEXT.md — D1 data model decisions
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Channel.kt`:

```kotlin
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
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Channel.kt` exists
- File contains `@Entity(tableName = Channel.TABLE_NAME)`
- Fields: id, name, channelType, memberCount, icon, createdAt
- channelType has `defaultValue = "PRIVATE"`
- TABLE_NAME = "channel"
</acceptance_criteria>

### Task 3: Create IndexNumber Entity
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardEntry.kt — existing Entity pattern
- .planning/1-CONTEXT.md — D1 data model decisions
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt`:

```kotlin
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
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt` exists
- File contains `@Entity(tableName = IndexNumber.TABLE_NAME)`
- Fields: id, ownerId, ownerType, label, key, mode, isVisible, isDefault
- ownerType has `defaultValue = "FRIEND"`
- mode has `defaultValue = "CODEBOOK"`
- TABLE_NAME = "index_number"
</acceptance_criteria>

### Task 4: Create FriendDao
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt — existing DAO pattern
- app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Friend.kt — just created Entity
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/FriendDao.kt`:

```kotlin
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
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/FriendDao.kt` exists
- Interface annotated with `@Dao`
- Methods: insert, update, delete, getById, getAll, searchByRemark, getByUserId, count
- searchByRemark uses LIKE with `:query` parameter
</acceptance_criteria>

### Task 5: Create ChannelDao
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt — existing DAO pattern
- app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Channel.kt — just created Entity
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/ChannelDao.kt`:

```kotlin
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
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/ChannelDao.kt` exists
- Interface annotated with `@Dao`
- Methods: insert, update, delete, getById, getAll, searchByName, count
</acceptance_criteria>

### Task 6: Create IndexNumberDao
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt — existing DAO pattern
- app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt — just created Entity
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumberDao.kt`:

```kotlin
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
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumberDao.kt` exists
- Interface annotated with `@Dao`
- Methods: insert, update, delete, getById, getByOwner, getVisibleByMode, getDefaultForOwner, clearDefaultForOwner, setDefault, setVisibility, count
- getVisibleByMode filters by `isVisible=1` and orders by `isDefault DESC, label ASC`
</acceptance_criteria>

## Verification
- All 6 files exist under `data/secure/db/`
- Each Entity has `@Entity`, `@PrimaryKey`, TABLE_NAME
- Each DAO has `@Dao`, CRUD operations, query methods
- Room annotations used consistently with existing ClipboardDatabase pattern

## must_haves
- Friend/Channel/IndexNumber Entity with all fields from 1-CONTEXT.md
- FriendDao/ChannelDao/IndexNumberDao with basic CRUD + search
- IndexNumberDao with owner-based queries (ownerId + ownerType) and default/visibility management
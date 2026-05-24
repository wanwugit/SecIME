---
wave: 2
depends_on:
  - 01-room
files_modified:
  - app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/SecureDatabase.kt
  - app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Converters.kt
autonomous: true
---

# PLAN 02: SecureDatabase + Converters

## Objective
Create the Room database class and type converters, following the ClipboardDatabase pattern.

## Tasks

### Task 1: Create Converters
<read_first>
- .planning/1-CONTEXT.md — D3 database isolation decision
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt — existing database pattern
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Converters.kt`:

```kotlin
package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.TypeConverter

class Converters {
    // Reserved for future complex type conversions
    // Currently all Entity fields use primitive types (String, Long, Int, Boolean)
    // that Room handles natively.
}
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Converters.kt` exists
- Class annotated with `@TypeConverter` methods (or reserved stub)
</acceptance_criteria>

### Task 2: Create SecureDatabase
<read_first>
- app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt — existing database pattern (CRITICAL reference)
- app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Friend.kt
- app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Channel.kt
- app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt
</read_first>

<action>
Create file `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/SecureDatabase.kt`:

```kotlin
package org.fcitx.fcitx5.android.data.secure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Friend::class, Channel::class, IndexNumber::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SecureDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun channelDao(): ChannelDao
    abstract fun indexNumberDao(): IndexNumberDao

    companion object {
        const val DB_NAME = "secure_database"
    }
}
```

</action>

<acceptance_criteria>
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/SecureDatabase.kt` exists
- `@Database` annotation lists all 3 entities, version = 1, exportSchema = true
- `@TypeConverters(Converters::class)` applied
- Abstract DAO methods: friendDao(), channelDao(), indexNumberDao()
- DB_NAME = "secure_database"
</acceptance_criteria>

## Verification
- SecureDatabase compiles with all 3 entities and 3 DAOs
- Schema export directory configured (uses existing `room.schemaLocation` from app/build.gradle.kts)

## must_haves
- SecureDatabase class with all Entity/DAO registrations
- DB_NAME constant for Room.databaseBuilder()
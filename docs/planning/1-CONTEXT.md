# Phase 1 CONTEXT — 数据层 Room 数据库 + 联系人模型

## Decisions

### D1: 数据模型 — 3个独立 Entity，按 spec 原样
- **Friend**: id (Long, PK autoGenerate), userId (String), remark (String?), phone (String?), avatar (String?), keyMode (String, default="CODEBOOK"), createdAt (Long)
- **Channel**: id (Long, PK autoGenerate), name (String), channelType (String, default="PRIVATE"), memberCount (Int, default=1), icon (String?), createdAt (Long)
- **IndexNumber**: id (Long, PK autoGenerate), ownerId (Long), ownerType (String, "FRIEND"|"CHANNEL"), label (String), key (String), mode (String, "CODEBOOK"|"EMOJI"), isVisible (Boolean, default=true), isDefault (Boolean, default=false)

**Why**: Spec 定义明确，3个独立概念。不加抽象层，KISS。
**How to apply**: Entity 定义与上述字段一一对应，ownerType 用 String 而非枚举（Room 兼容性）。

### D2: 模块位置 — fcitx5-android/app
- 包路径: `org.fcitx.fcitx5.android.data.secure`
- 文件位置: `app/src/main/java/org/fcitx/fcitx5/android/data/secure/`
- 不放在 secureime 模块（secureime 是纯 JVM 无 Room 依赖，且 Room 需要 Android context）

**Why**: secureime 模块设计为纯 Kotlin，不引入 Android 依赖。与 ClipboardDatabase 同级保持一致性。
**How to apply**: 在 `data/secure/` 下创建 db/ 子包，放置 Entity/DAO/Database 文件。

### D3: 数据库隔离 — 独立 SecureDatabase
- 数据库名: `secure_database`
- 版本: 1
- 包含 Entity: Friend, Channel, IndexNumber
- 与 ClipboardDatabase 完全独立，不影响现有功能

**Why**: 关注点分离，加密数据独立演进，避免修改 ClipboardDatabase 的迁移风险。
**How to apply**: 创建 `SecureDatabase.kt`，参照 `ClipboardDatabase.kt` 的 AutoMigration 模式。

### D4: 前向兼容性 — 预留模式字段
- Friend.keyMode: "CODEBOOK" | "SM9" | "SM4" — 标识默认加密模式
- Channel.channelType: "PRIVATE" | "GROUP" — 区分私聊/群聊频道
- IndexNumber.mode: "CODEBOOK" | "EMOJI" — 索引号适用的加密模式
- 字段用 String 而非枚举，便于 Room 持久化和后续扩展

**Why**: 后续 milestone 添加 SM9/SM4 时不需要数据库 migration（只需新增值）。String 比枚举更灵活。
**How to apply**: Entity 字段默认值覆盖当前密码本模式，后续添加新模式只需新增常量。

## Reference Pattern
- 现有 Room 模式参照: `data/clipboard/db/ClipboardDatabase.kt`
  - `@Database(entities = [...], version = 1, autoMigrations = [...], exportSchema = true)`
  - DAO 使用 `@Query`, `@Insert`, `@Update`, `@Delete`
  - `@TypeConverter` 用于复杂类型
  - PagingSource 支持
  - `room.schemaLocation = "$projectDir/schemas"` 已配置

## Assumptions
- Room 版本沿用 libs.versions.toml 中的 2.8.4
- 使用 AutoMigration（Room 自动生成迁移）
- 不需要外键约束（应用层保证一致性）
- IndexNumber 通过 ownerId + ownerType 关联 Friend/Channel（多态关联，无 FK）

## File Plan
```
app/src/main/java/org/fcitx/fcitx5/android/data/secure/
├── db/
│   ├── SecureDatabase.kt        — @Database, @TypeConverters
│   ├── Friend.kt                — @Entity
│   ├── Channel.kt               — @Entity
│   ├── IndexNumber.kt           — @Entity
│   ├── FriendDao.kt             — @Dao
│   ├── ChannelDao.kt            — @Dao
│   └── IndexNumberDao.kt        — @Dao
└── SecureRepository.kt          — facade (optional, depends on phase scope)
```

## Out of Scope
- UI 组件（Phase 3-4）
- 加密算法逻辑（Phase 2）
- 联系人选择器（Phase 4）
- Repository / UseCase 层（按需在后续 phase 添加）

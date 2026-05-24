---
plan: 01
phase: 01-room
status: complete
---

## Summary

Created 3 Room Entity classes and 3 DAO interfaces for the secure data layer.

### Key Files Created
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Friend.kt` — Friend entity (id, userId, remark, phone, avatar, keyMode, createdAt)
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/Channel.kt` — Channel entity (id, name, channelType, memberCount, icon, createdAt)
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt` — IndexNumber entity (id, ownerId, ownerType, label, key, mode, isVisible, isDefault)
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/FriendDao.kt` — CRUD + searchByRemark + getByUserId + count
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/ChannelDao.kt` — CRUD + searchByName + count
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumberDao.kt` — CRUD + getByOwner + getVisibleByMode + default/visibility management + count

### Decisions
- String types for mode fields (keyMode, channelType, ownerType, mode) for forward compatibility
- Default values match current codebook-only scope (CODEBOOK, PRIVATE, FRIEND)
- IndexNumber uses ownerId+ownerType polymorphic association (no FK constraint)

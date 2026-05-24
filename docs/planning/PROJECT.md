# Project: 隐言加密输入法 (SecureIME)

## Vision
在现有 fcitx5-android 输入法基础上，增加端到端加密通信能力。用户可以在输入法内直接加密文本，通过伪装模板隐藏加密内容，接收方剪贴板自动解密。

## Problem Statement
现有输入法不支持加密通信。用户需要切换到专用加密 App 完成加解密，体验割裂且无法伪装。隐言将加密能力内嵌到输入法中，让加密像输入一样自然。

## Target Users
- 需要私密通信的 Android 用户
- 对加密通信有伪装需求（隐蔽性）的用户
- 不希望切换 App 就能完成加密通信的用户

## Milestone 1: 密码本模式 + 基础框架

### In Scope
- **密码本加密模式** — 纯 Kotlin 实现，预置/自定义密码本映射表
- **伪装模板系统** — 天气/新闻/快递等模板外壳包装密文
- **剪贴板自动解密** — 监听剪贴板变化，自动识别加密内容并解密
- **联系人管理** — 好友/频道/索引号 CRUD，Room 数据库存储
- **加密工具栏** — 锁定/加密/选人/Buffer Bar UI 组件
- **双锁状态机** — 上锁/解锁/加密/解密状态转换

### Out of Scope (Future Milestones)
- SM9/SM4 加密模式（依赖 GmSSL JNI）
- KGC 服务端（SM9 密钥管理）
- Emoji 加密模式
- Windows 跨端互通
- 密码本在线同步/分享

## Architecture Constraints
- 加密逻辑独立于输入逻辑 — `secureime/` 模块
- 输入法进程可能被 Android 低内存杀死 — 加密状态必须持久化
- 不使用 Material Components — 使用原生 Android Widget
- 加密延迟 < 100ms — 密码本映射表需内存缓存
- UI 一致性 — 加密工具栏风格与现有键盘一致

## Success Criteria
1. 用户可以在密码本模式下选择联系人并加密文本
2. 加密后文本可套用伪装模板
3. 接收方复制密文后，剪贴板自动识别并解密
4. 加密/解密延迟 < 100ms
5. 联系人增删改查正常工作
6. 输入法进程被杀重启后，加密状态恢复正确

## Key References
- `SPECIFICATION.md` — 完整需求规格说明书
- `.planning/codebase/` — 代码库映射文档

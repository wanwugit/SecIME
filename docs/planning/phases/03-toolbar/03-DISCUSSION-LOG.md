# Phase 3: 加密工具栏 UI — Discussion Log

> **Audit trail only.** Decisions are captured in CONTEXT.md.

**Date:** 2026-05-23
**Phase:** 03-toolbar
**Areas discussed:** 工具栏位置, 双锁状态机, Buffer Bar, 状态持久化

---

## 工具栏位置

| Option | Description | Selected |
|--------|-------------|----------|
| KawaiiBar 下方 | 在候选栏和键盘之间，GONE/VISIBLE切换 | ✓ |
| preedit 上方 | 在输入预览之上 | |
| 替换 KawaiiBar | 替换候选栏 | |

**User's choice:** KawaiiBar 下方
**Notes:** 不干扰现有候选词功能，40dp高度与KawaiiBar一致。

---

## 双锁状态机

| Option | Description | Selected |
|--------|-------------|----------|
| EventStateMachine 模式 | 与KawaiiBarStateMachine一致 | ✓ |
| 简单 enum + when | 轻量但缺少转换校验 | |

**User's choice:** EventStateMachine 模式
**Notes:** 4状态 UNLOCKED/LOCKED/ENCRYPTING/ENCRYPTED。

---

## Buffer Bar

| Option | Description | Selected |
|--------|-------------|----------|
| Splitties DSL 构造 | 程序化水平LinearLayout | ✓ |
| EditText 可编辑 | 支持光标选区 | |

**User's choice:** Splitties DSL 构造
**Notes:** TextView预览 + 字符计数 + 删除按钮。加密态紫边，解密态绿边。

---

## 状态持久化

| Option | Description | Selected |
|--------|-------------|----------|
| AppPrefs 模式 | 新增 secure 命名空间 | ✓ |
| 独立 SecurePrefs | 单独文件 | |

**User's choice:** AppPrefs 模式
**Notes:** 与现有偏好集中管理一致。
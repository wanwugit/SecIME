# Phase 3: 加密工具栏 UI — 双锁状态机 + Buffer Bar - Context

**Gathered:** 2026-05-23
**Status:** Ready for planning

<domain>
## Phase Boundary

在键盘区域实现加密工具栏 UI：双锁状态机、Buffer Bar、选人插槽占位、状态持久化。不涉及加密算法调用（Phase 5），不涉及联系人选择弹窗（Phase 4），不涉及剪贴板（Phase 7）。

</domain>

<decisions>
## Implementation Decisions

### D1: 工具栏分两层 — 开关层 + 加密候选区
- **开关层**：加密锁、解密锁放在 KawaiiBar（候选栏）工具栏区域内，作为始终可见的图标按钮
- **加密候选区**：在 KawaiiBar 之下、键盘之上，包含 Buffer Bar + 选人插槽
- 加密候选区默认 GONE，点击加密/解密开关后自动 VISIBLE/隐藏
- 加密候选区高度 40dp

**Why**: 开关要始终可达（不需要先展开才能加密），候选区按需显示节省空间。
**How to apply**: KawaiiBar 中新增加密锁/解密锁按钮；加密候选区作为独立 View 在 InputView 中 below(kawaiiBar.view)。

### D2: 双锁状态机 — EventStateMachine 模式
- 采用与 KawaiiBarStateMachine 相同的 EventStateMachine 模式
- 4 个状态: UNLOCKED, LOCKED, ENCRYPTING, ENCRYPTED
- 状态转换事件: LockToggled, EncryptRequested, SendRequested
- UNLOCKED ↔ LOCKED 互斥切换
- LOCKED → ENCRYPTING → ENCRYPTED（加密完成）
- ENCRYPTED → LOCKED（点锁头回到可编辑）
- ENCRYPTED → UNLOCKED（发送后重置）

**Why**: 与现有代码一致，EventStateMachine 提供转换合法性校验和可测试性。
**How to apply**: 新建 EncryptionBarStateMachine.kt，放在 input/bar/ 包下，参照 KawaiiBarStateMachine 结构。

### D3: Buffer Bar — Splitties DSL 构造
- 用 Splitties DSL 程序化构造水平 LinearLayout
- 左侧：锁图标 + 文本预览区（TextView，可滚动）
- 右侧：字符计数 + 删除按钮
- 加密状态：紫边框 + 淡紫底
- 解密状态：绿边框 + 白底
- 文本颜色：加密=#7C4DFF，解密=#4CAF50

**Why**: 与 KawaiiBar 的 Splitties DSL 风格一致，轻量、无需 XML。
**How to apply**: BufferBarUi.kt 在 input/bar/ui/ 包下，参照 CandidateUi.kt / IdleUi.kt 的模式。

### D4: 状态持久化 — AppPrefs 模式
- 在 AppPrefs 中新增 secure 命名空间的 key
- 持久化字段：当前锁状态、加密模式、选中联系人ID列表、当前密码本ID、Buffer Bar内容
- 使用与 keyboard 等同样的 ManagedPreference 模式

**Why**: 与现有偏好设置一致，集中管理，无额外复杂度。
**How to apply**: 在 AppPrefs.kt 中新增 val secure = SecurePrefs() 内部类。

### D5: 选人插槽 — 占位实现
- 本 Phase 只做 4 个等宽插槽的 UI 占位（灰色虚线框 + "+"）
- 点击事件暂时不处理（Phase 4 实现弹窗）
- 插槽高度 16dp，文字 11sp，间距 4dp

**Why**: Phase 4 才实现联系人选择，本 Phase 只需 UI 骨架。
**How to apply**: 在工具栏 UI 中加入 4 个 SlotView，样式按 spec §8.4。

### Claude's Discretion
- 工具栏内部元素的具体排列细节（锁头、插槽、Buffer Bar 的水平布局）
- ENCRYPTING 状态的动画/进度显示方式
- 进程重启后 ENCRYPTED 状态的恢复策略

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### UI 规范
- `SPECIFICATION.md` §8.1 — 加密工具栏布局描述
- `SPECIFICATION.md` §8.2 — 双锁状态机定义和颜色
- `SPECIFICATION.md` §8.3 — Buffer Bar 状态和颜色
- `SPECIFICATION.md` §8.4 — 选人插槽三态设计
- `SPECIFICATION.md` §R1.1 — 工具栏布局要求（40dp高度）
- `SPECIFICATION.md` §R1.2 — 双锁状态机4个状态和转换
- `SPECIFICATION.md` §R1.3 — Buffer Bar 需求
- `SPECIFICATION.md` §R6.1-6.2 — 持久化字段和恢复逻辑

### 现有代码模式（MUST READ）
- `app/.../input/bar/KawaiiBarStateMachine.kt` — 状态机参考模式
- `app/.../input/bar/KawaiiBarComponent.kt` — 工具栏组件参考
- `app/.../input/bar/ui/IdleUi.kt` — Splitties DSL UI 参考
- `app/.../input/bar/ui/CandidateUi.kt` — Splitties DSL UI 参考
- `app/.../input/InputView.kt` — 布局约束参考
- `app/.../data/prefs/AppPrefs.kt` — 偏好设置参考

### Phase 2 上下文
- `.planning/phases/02-codebook/02-CONTEXT.md` — 加密引擎接口

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `EventStateMachine` — 通用状态机框架，KawaiiBarStateMachine 和 ClipboardStateMachine 均使用
- `KawaiiBarComponent` — 工具栏组件模式（UniqueViewComponent + DynamicScope 依赖注入）
- `AppPrefs` — 集中偏好管理，keyboard 子命名空间可直接参照
- `Splitties DSL` — 现有 UI 全部用 Splitties 程序化构造

### Established Patterns
- 组件注册：`scope += component` 在 InputView 中注册
- 状态机：enum State + enum TransitionEvent + BooleanKey
- 布局：constraintLayout + below/above 约束
- 偏好：ManagedPreference + registerOnChangeListener

### Integration Points
- InputView.kt 第 259 行：`add(kawaiiBar.view, ...)` — 加密工具栏需在此之后添加
- InputView.kt 第 273 行：`add(windowManager.view, ...)` — keyboard 需 below 加密工具栏
- KawaiiBarComponent：需要与之协调显示逻辑
- InputDecisionBus：加密状态下输入需拦截到 Buffer Bar

</code_context>

<specifics>
## Specific Ideas

- 工具栏 40dp 高度，与 KawaiiBar 视觉一致
- 双锁状态机：锁头点击 UNLOCKED↔LOCKED，加密/发送按钮触发 ENCRYPTING→ENCRYPTED→UNLOCKED
- Buffer Bar 加密态紫边、解密态绿边，按 spec §8.3
- 选人插槽 4 个等宽，按 spec §8.4 三态设计

</specifics>

<deferred>
## Deferred Ideas

- 联系人选择弹窗（Phase 4）
- 加密算法调用集成（Phase 5）
- 剪贴板自动解密（Phase 7）

</deferred>

---

*Phase: 03-toolbar*
*Context gathered: 2026-05-23*
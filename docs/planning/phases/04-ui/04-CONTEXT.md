# Phase 4: 联系人选择 UI — 选人插槽 + 联系人选择器 - Context

**Gathered:** 2026-05-23
**Status:** Ready for planning

<domain>
## Phase Boundary

实现工具栏选人插槽的数据绑定和联系人选择弹窗，以及好友管理独立设置页。不涉及加密算法调用（Phase 5），不涉及剪贴板（Phase 7）。

Phase 3 已完成：SlotBarUi 有 4 个 UI 占位插槽（Empty/Idle/Active 三态），EncryptionBarComponent 有状态机，SecurePrefs 有 selectedContactIds 持久化字段。

</domain>

<decisions>
## Implementation Decisions

### D1: 联系人选择器 = 轻量 AlertDialog 选人
- 点击 Empty/Active 插槽 → 弹出 AlertDialog，显示已配置的好友列表
- 选择器只负责选人绑定到插槽，不负责 CRUD
- 用 AlertDialog.Builder(service context) + recyclerView，参照 InputMethodPickerDialog 模式
- 列表项显示：备注名 + 加密模式图标

**Why**: IME 服务环境无法使用 BottomSheet/DialogFragment。AlertDialog 在 IME context 已有成功先例（InputMethodPickerDialog）。选人是轻量操作，不需要完整管理 UI。
**How to apply**: 新建 ContactPickerDialog.kt 在 input/dialog/ 包下，参照 InputMethodPickerDialog 结构。

### D2: 选择器布局 = 搜索框 + 列表，内存过滤
- 顶部搜索框（EditText），输入时实时过滤
- 下方 RecyclerView 显示好友列表
- 过滤逻辑：内存过滤（FriendDao.getAll() 一次加载，filter by remark.contains(query)）
- 联系人数量通常 < 100，无需分页

**Why**: 联系人数量少，内存过滤延迟 < 10ms，满足 NFR1（300ms 加载）。FriendDao 已有 searchByRemark() 但内存过滤更灵活（支持拼音首字母等未来扩展）。
**How to apply**: ContactPickerDialog 内部持有 List<Friend>，搜索时 filter 不重新查库。

### D3: 4 个插槽全用于联系人
- 保持 Phase 3 的 4 个等宽插槽，全部用于绑定联系人
- 最多可选 4 个联系人（每个插槽绑定一个 Friend）
- 密码本选择不在插槽中，由其他机制处理（Phase 5）

**Why**: 用户确认 4 个全用于联系人。Phase 3 已建 4 个插槽 UI，保持一致。
**How to apply**: SlotBarUi 已有 4 个 slot，只需绑定 Friend 数据。selectedContactIds 存储逗号分隔的 Friend.id。

### D4: 插槽状态流转 = Empty → Active 直接
- 点击 Empty 插槽 → 弹出选择器 → 选人后直接变 Active（紫色实线 + 备注名）
- 点击 Active 插槽 → 弹出选择器 → 可换人或点"清除"回到 Empty
- 不经过 Idle 中间态（Idle 预留给未来"已选但未激活"场景）

**Why**: 当前只有一个加密模式（密码本），选中即激活，无需 Idle 过渡。Idle 状态保留在 SlotBarUi 代码中供未来使用。
**How to apply**: 选中联系人后调用 slotBarUi.setSlotState(index, Active, friend.remark)。

### D5: 好友管理 = 独立设置页，集成现有 Settings 系统
- 新增 SettingsRoute.FriendManagement 路由
- 新增 FriendManagementFragment，使用 PreferenceFragment 或 BaseDynamicListUi
- CRUD 操作：添加好友（输入 userId + remark + phone）、编辑、删除
- 从 Settings 主页或加密工具栏入口进入

**Why**: 好友管理是独立功能，需要完整 CRUD，不属于键盘内轻量操作。现有 Settings 系统已有导航、Fragment 管理等基础设施。
**How to apply**: 在 SettingsRoute.kt 新增路由，新建 FriendManagementFragment，参照 AddonListFragment / ClipboardSettingsFragment 模式。

### D6: SecureDatabase 实例化 = SecureDataManager 单例
- 新建 SecureDataManager object，参照 ClipboardManager 模式
- Room.databaseBuilder 初始化 SecureDatabase
- 提供 friendDao/channelDao/indexNumberDao 访问
- 在 FcitxInputMethodService.onCreate() 中调用 init()

**Why**: ClipboardManager 是现有 Room 数据库管理的唯一参考模式。SecureDatabase 已定义但未实例化，Phase 4 需要实际查询 Friend 数据。
**How to apply**: 新建 SecureDataManager.kt 在 data/secure/ 包下，init() 中 Room.databaseBuilder + fallbackToDestructiveMigrationOnDowngrade。

### Claude's Discretion
- 选择器列表项的具体布局细节（图标位置、字体大小）
- 好友管理页的编辑表单字段排列
- 预置示例好友数据的具体内容

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### UI 规范
- `SPECIFICATION.md` §8.4 — 选人插槽三态设计
- `SPECIFICATION.md` §8.6 — 好友管理页描述

### 现有代码模式（MUST READ）
- `app/.../input/dialog/InputMethodPickerDialog.kt` — AlertDialog + RecyclerView 选人弹窗参考
- `app/.../ui/common/BaseDynamicListUi.kt` — 动态列表 CRUD UI 参考
- `app/.../data/clipboard/ClipboardManager.kt` — Room 数据库管理单例参考
- `app/.../data/secure/db/SecureDatabase.kt` — 已定义的 Room 数据库
- `app/.../data/secure/db/Friend.kt` + `FriendDao.kt` — 好友实体和 DAO
- `app/.../ui/main/settings/SettingsRoute.kt` — 设置路由系统
- `app/.../input/bar/ui/SlotBarUi.kt` — 现有插槽 UI（需绑定数据）
- `app/.../input/bar/EncryptionBarComponent.kt` — 加密栏组件（需集成选人）

### Phase 3 上下文
- `.planning/phases/03-toolbar/03-CONTEXT.md` — 工具栏 UI 架构

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `InputMethodPickerDialog` — AlertDialog + RecyclerView + Splitties DSL，直接参照
- `BaseDynamicListUi` — 带 FAB、编辑对话框、undo snackbar 的动态列表，可用于好友管理页
- `ClipboardManager` — Room 单例模式：object + init(context) + lateinit db/dao + CoroutineScope
- `SlotBarUi` — 4 个插槽 UI 已就绪，setSlotState(index, state, label) 方法已存在
- `EncryptionBarComponent` — 已有 SecurePrefs 引用，需添加选人逻辑
- `SecurePrefs.selectedContactIds` — 持久化字段已存在

### Established Patterns
- 设置页导航：SettingsRoute sealed class + Fragment 导航
- 列表管理：BaseDynamicListUi + Mode.FreeAdd / Mode.ChooseOne
- 数据库单例：object + init(context) + Room.databaseBuilder
- IME 对话框：AlertDialog.Builder(service context)

### Integration Points
- `SlotBarUi.onSlotClick` — 已定义但未实现，Phase 4 填充
- `EncryptionBarComponent` — 需添加选人方法和 Friend 数据绑定
- `SecurePrefs.selectedContactIds` — 需读写逻辑（逗号分隔 Friend.id）
- `SettingsRoute.kt` — 需新增 FriendManagement 路由
- `FcitxInputMethodService.onCreate()` — 需调用 SecureDataManager.init()

</code_context>

<specifics>
## Specific Ideas

- 联系人选择器：AlertDialog + 搜索框 + RecyclerView，内存过滤
- 插槽绑定：选中联系人 → setSlotState(index, Active, remark) → 持久化 selectedContactIds
- 好友管理页：BaseDynamicListUi<Friend> + Mode.FreeAdd，编辑对话框含 userId/remark/phone
- SecureDataManager：ClipboardManager 模式，Room 单例
- 4 插槽全用于联系人，Empty → Active 直接流转

</specifics>

<deferred>
## Deferred Ideas

- 联系人头像显示（当前只用文字备注名）
- 拼音首字母搜索（当前只用 remark 文本搜索）
- 频道（Channel）选择（当前只做好友）
- 索引号（IndexNumber）管理（Phase 5 加密时使用）
- 密码本选择 UI（Phase 5）

</deferred>

---

*Phase: 04-ui*
*Context gathered: 2026-05-23*

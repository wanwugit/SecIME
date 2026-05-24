# Requirements: Milestone 1 — 密码本模式 + 基础框架

## Source
Derived from `SPECIFICATION.md` with scope decisions:
- 密码本先行，跳过 SM9/SM4/Emoji 模式
- 跳过 KGC 服务端
- 仅 Android 端

---

## R1: 加密工具栏 (Encryption Toolbar)

### R1.1 工具栏布局
- 位于键盘候选栏上方，KawaiiBarComponent 下方
- 包含：锁定按钮、选人插槽（最多3个）、Buffer Bar、加密/发送按钮
- 高度：40dp，背景色与键盘主题一致

### R1.2 双锁状态机
- **未锁定** (UNLOCKED): 正常输入模式，工具栏隐藏或半透明
- **已锁定** (LOCKED): 加密输入模式，工具栏激活
- **加密中** (ENCRYPTING): 输入文本正在加密，显示进度
- **已加密** (ENCRYPTED): 文本已加密，可预览/编辑/发送
- 状态转换：
  - 点击锁头 → UNLOCKED ↔ LOCKED
  - LOCKED + 输入完成 + 点加密 → ENCRYPTING → ENCRYPTED
  - ENCRYPTED + 点发送 → commitText → UNLOCKED
  - ENCRYPTED + 点锁头 → 回到 LOCKED（可编辑）
- 状态持久化：SharedPreferences 存储当前锁状态、模式、选中联系人

### R1.3 Buffer Bar
- 显示当前待加密的明文预览
- 支持编辑（删除字符）
- 字符数计数器

---

## R2: 密码本加密模式

### R2.1 预置密码本
- 系统预置默认密码本（中文常用字→替换字映射）
- 密码本格式：`Map<String, String>` — 原文→密文映射
- 内存缓存：启动时加载到 HashMap，查找 O(1)

### R2.2 自定义密码本
- 用户可创建自定义密码本
- 支持 CSV 导入/导出
- 密码本列表管理（增删改查）

### R2.3 加密流程
1. 锁定 → 选择密码本 → 选择收件人 → 输入明文
2. 逐字查表替换 → 非密码本字符原样保留
3. 加密结果写入 Buffer Bar
4. 可选：套用伪装模板
5. commitText 输出

### R2.4 解密流程
1. 复制密文 → 剪贴板监听 → 检测加密标记
2. 自动识别密码本模式
3. 反向查表替换 → 显示解密结果
4. 解密结果通过 Toast/弹窗/状态栏显示

### R2.5 加密标记
- 密文前后添加标记符号（如 `⟦⟧`），用于剪贴板自动识别
- 标记格式：`⟦{mode}:{version}:{contactId}⟧密文⟦/⟧`
- 示例：`⟦CB:1:alice⟧你好世界⟦/⟧`

---

## R3: 伪装模板系统

### R3.1 模板定义
- 模板 = 外壳文本 + 嵌入位置标记
- 外壳：天气、新闻摘要、快递通知等常见文本样式
- 嵌入位置：密文替换外壳中的占位符

### R3.2 预置模板
- 天气预报模板：密文嵌入在天气描述中
- 新闻摘要模板：密文嵌入在新闻正文
- 快递通知模板：密文嵌入在取件码/地址

### R3.3 模板套用
- 加密后 → 选择模板 → 密文嵌入模板 → 输出伪装文本
- 伪装文本外观与正常消息无异
- 接收方需知道密文在模板中的嵌入位置

### R3.4 模板管理
- 用户可创建自定义模板
- 模板编辑器：外壳文本 + 占位符标记
- 模板增删改查

---

## R4: 剪贴板自动解密

### R4.1 剪贴板监听
- 监听系统剪贴板变化（ClipboardManager.OnPrimaryClipChangedListener）
- 检测加密标记（`⟦...⟧` 模式匹配）

### R4.2 自动解密
- 识别加密模式和版本
- 加载对应密码本
- 反向查表解密
- 显示解密结果（Toast/弹窗/状态栏通知）

### R4.3 去重（防"回魂"）
- 对剪贴板内容计算 hash 指纹
- 同一密文只解密一次，不重复提示
- hash 指纹有过期时间（默认5分钟）

### R4.4 手动解密入口
- 键盘工具栏提供"解密"按钮
- 用户可手动粘贴密文进行解密

---

## R5: 联系人管理

### R5.1 数据模型
- **好友 (Friend)**: id, name, avatar, defaultMode, createdAt
- **频道 (Channel)**: id, name, icon, memberCount, createdAt
- **索引号 (IndexNumber)**: id, friendId/channelId, label, value, mode

### R5.2 数据存储
- Room 数据库，三个 Entity + DAO
- 预置示例联系人用于演示

### R5.3 联系人选择 UI
- 选人插槽：工具栏上最多3个联系人头像
- 点击插槽 → 弹出联系人选择器
- 联系人选择器：列表 + 搜索 + 最近使用

### R5.4 联系人管理 UI
- 独立设置页面：联系人列表、增删改查
- 从设置入口或工具栏入口进入

---

## R6: 加密状态持久化

### R6.1 持久化字段
- 当前锁状态 (UNLOCKED/LOCKED/ENCRYPTED)
- 当前加密模式 (CODEBOOK)
- 选中的联系人 ID 列表
- 当前密码本 ID
- 当前模板 ID
- Buffer Bar 内容

### R6.2 恢复逻辑
- 输入法进程重启后读取 SharedPreferences
- 恢复到上次的加密状态
- 如果处于 ENCRYPTED 状态，保留加密结果

---

## Non-Functional Requirements

### NFR1: 性能
- 密码本加密/解密延迟 < 50ms（内存 HashMap 查表）
- 剪贴板检测延迟 < 200ms
- 联系人选择器加载 < 300ms

### NFR2: 安全
- 密码本内容不明文存储在 SharedPreferences
- 加密标记不可被第三方 App 轻易识别（但本版本不做反检测）
- 进程内存中的密码本在进程退出时清零

### NFR3: 可靠性
- 输入法进程被杀重启后状态恢复
- 密码本损坏时降级为空密码本，不崩溃
- 剪贴板监听权限被拒绝时，禁用自动解密，保留手动入口

### NFR4: 兼容性
- minSdk 23，targetSdk 36
- 不依赖 GmSSL 或其他 native 加密库
- 与现有 T9/QWERTY 输入无冲突

# Roadmap: Milestone 1 — 密码本模式 + 基础框架

---

## Phase 1: 数据层 — Room 数据库 + 联系人模型
**Goal**: 建立数据存储基础，为加密功能提供数据支撑

### Requirements
- R5.1 数据模型 (Friend, Channel, IndexNumber)
- R5.2 数据存储 (Room Entity + DAO + Database)

### Success Criteria
1. Room 数据库创建成功，包含 3 个 Entity 和对应的 DAO
2. DAO 的增删改查操作通过单元测试
3. 数据库 Migration 策略已定义
4. 预置示例数据可正常插入

### UI hint: no

---

## Phase 2: 密码本核心 — 加密/解密算法
**Goal**: 实现密码本加密解密的核心算法，不涉及 UI

### Requirements
- R2.1 预置密码本
- R2.2 自定义密码本
- R2.3 加密流程 (逐字查表)
- R2.4 解密流程 (反向查表)
- R2.5 加密标记 (协议标识头)
- NFR1 性能 (< 50ms)
- NFR2 安全 (内存清零)

### Success Criteria
1. 密码本加载到内存 HashMap，查表 O(1)
2. 中文逐字加密/解密正确
3. 非密码本字符原样保留
4. 加密标记格式正确（`⟦CB:1:{id}⟧...⟦/⟧`）
5. 加密/解密延迟 < 50ms
6. CSV 导入/导出正常工作
7. 单元测试覆盖核心加密/解密逻辑

### UI hint: no

---

## Phase 3: 加密工具栏 UI — 双锁状态机 + Buffer Bar
**Goal**: 在键盘区域实现加密工具栏，包括锁定按钮、状态机、Buffer Bar

### Requirements
- R1.1 工具栏布局
- R1.2 双锁状态机
- R1.3 Buffer Bar
- R6.1 持久化字段
- R6.2 恢复逻辑

### Success Criteria
1. 工具栏在键盘候选栏上方正确显示
2. 双锁状态机4个状态转换正确
3. Buffer Bar 显示待加密明文预览
4. 状态持久化到 SharedPreferences
5. 进程重启后状态恢复正确
6. 工具栏不干扰正常输入

### UI hint: yes

---

## Phase 4: 联系人选择 UI — 选人插槽 + 联系人选择器
**Goal**: 实现工具栏选人插槽和联系人选择弹窗

### Requirements
- R5.3 联系人选择 UI (选人插槽)
- R5.4 联系人管理 UI (设置页)

### Success Criteria
1. 工具栏显示最多3个联系人插槽
2. 点击插槽弹出联系人选择器
3. 选择器支持搜索和最近使用
4. 联系人设置页增删改查正常
5. 选中联系人持久化，重启恢复

### UI hint: yes

---

## Phase 5: 加密集成 — 输入拦截 + 密码本加密上屏
**Goal**: 将密码本加密集成到输入流程，实现锁定→加密→上屏完整流程

### Requirements
- R2.3 加密流程 (集成到输入)
- R1.2 双锁状态机 (ENCRYPTING/ENCRYPTED 状态)

### Success Criteria
1. 锁定状态下输入文本进入 Buffer Bar
2. 点击加密按钮执行密码本加密
3. 加密结果替换 Buffer Bar 内容
4. 点击发送将加密文本 commitText 上屏
5. 加密延迟 < 100ms (端到端)
6. 不影响未锁定状态下的正常输入

### UI hint: no

---

## Phase 6: 伪装模板系统 — 模板管理 + 套用
**Goal**: 实现伪装模板，让加密文本伪装成普通社交消息

### Requirements
- R3.1 模板定义
- R3.2 预置模板
- R3.3 模板套用
- R3.4 模板管理

### Success Criteria
1. 预置3个模板（天气/新闻/快递）可正常套用
2. 密文正确嵌入模板占位符位置
3. 伪装输出文本外观与正常消息无异
4. 用户可创建自定义模板
5. 模板增删改查正常
6. 模板选择状态持久化

### UI hint: yes

---

## Phase 7: 剪贴板自动解密 — 监听 + 解密 + 去重
**Goal**: 实现剪贴板自动解密，接收方体验无感化解密

### Requirements
- R4.1 剪贴板监听
- R4.2 自动解密
- R4.3 去重（防"回魂"）
- R4.4 手动解密入口

### Success Criteria
1. 复制加密文本后，自动检测加密标记
2. 自动识别密码本模式并解密
3. 解密结果通过弹窗/通知显示
4. 同一密文5分钟内不重复提示
5. 手动解密入口正常工作
6. 剪贴板权限被拒时降级处理

### UI hint: yes

---

## Phase 8: 集成测试 + 优化
**Goal**: 端到端集成测试，性能优化，边界情况处理

### Requirements
- NFR1 性能验证
- NFR2 安全验证
- NFR3 可靠性验证
- NFR4 兼容性验证

### Success Criteria
1. 完整加密→发送→接收→解密流程通过
2. 所有性能指标达标
3. 进程被杀重启后状态恢复正确
4. 与 T9/QWERTY 输入无冲突
5. 无崩溃、无 ANR

### UI hint: no

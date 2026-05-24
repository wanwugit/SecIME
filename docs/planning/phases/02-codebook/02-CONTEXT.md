# Phase 2: 密码本核心 — 加密/解密算法 - Context

**Gathered:** 2026-05-23
**Status:** Ready for planning

<domain>
## Phase Boundary

实现密码本加密/解密核心算法，纯 Kotlin，位于 secureime 模块（纯 JVM，无 Android 依赖）。不涉及 UI、不涉及输入拦截、不涉及剪贴板。

</domain>

<decisions>
## Implementation Decisions

### D1: 加密模型 — 分页字典坐标偏移（汉字等长替换）
- **字典**: codebook.json，149页×100字/页 = 14810字（最后一页10字）
- **字典结构**: `pages[0..148]` 每页100个汉字，按固定顺序排列
- **密钥**: 4位数字索引号（IndexNumber.key），前2位=KP（页偏移），后2位=KI（行偏移）
- **加密公式**: 字在(p,i)坐标 → `p' = (p + KP) mod 149, i' = (i + KI) mod 100` → pages[p'][i'] 即密文汉字
- **解密公式**: 密文汉字在(p',i')坐标 → `p = (p' - KP) mod 149, i = (i' - KI) mod 100` → pages[p][i] 即原文汉字
- **输出**: 1:1 等长汉字替换，密文与明文字数相同

**Why**: 用户提供的 codebook.json 已定义 149页×100字 分页结构，与 spec 2.4 公式完全一致。
**How to apply**: CodebookEngine 类加载 pages 二维数组，encrypt 逐字查找坐标+偏移取模→替换汉字，decrypt 反向。

### D2: 字符处理 — 仅字典内汉字偏移，其他原样
- 字典表中找到的字走坐标偏移加密/解密
- 数字、字母、符号原样保留，不参与加密
- 判断依据：字符在字典 index 中存在

**Why**: 简化实现，Milestone 1 KISS 原则。后续可扩展双轨（62字符混淆）。
**How to apply**: encrypt 逐字扫描，字典中有坐标就替换为偏移后的汉字，找不到原样保留。

### D3: 索引号复用 IndexNumber.key
- 4位索引号 = Phase 1 的 IndexNumber.key 字段
- KP = key.substring(0,2)，KI = key.substring(2,4)
- ownerId 关联到具体联系人（Friend）或频道（Channel）
- 加密时从数据库取对应联系人的 IndexNumber.key
- 解密时从加密标记的 contactId 找到对应 IndexNumber.key

**Why**: Phase 1 已建立 IndexNumber 数据模型，4位 key 字段正好对应 KP+KI。
**How to apply**: 加密标记中 contactId 使用 IndexNumber 的 label 作为标识。

### D4: 密码本数据结构 — 二维数组 + 索引 HashMap
- **加密方向**: `Map<Char, Pair<Int,Int>>` — 字 → (页码,行号) 坐标
- **解密方向**: 直接用 `pages[p][i]` 二维数组 — 坐标 → 字
- 149×100 二维数组用于解密查表 O(1)，HashMap 用于加密查坐标 O(1)

**Why**: 加密需要字→坐标，解密需要坐标→字。二维数组天然支持坐标→字，HashMap 支持字→坐标。
**How to apply**: CodebookTable 类加载 pages 数组 + 构建 charToCoord HashMap。

### D5: 字典存储 — JSON 文件打包在 APK assets
- codebook.json 直接放在 `app/src/main/assets/` 下
- 运行时 JSON 解析加载，构建二维数组 + HashMap
- 自定义密码本同样以 JSON 格式导入（结构一致）

**Why**: 用户已提供 codebook.json，直接沿用。JSON 格式比纯文本更适合分页结构。
**How to apply**: secureime 定义 CodebookSource 接口，app 层实现 AssetsCodebookSource（读取 assets JSON）。

### D6: 加密标记格式 — 沿用 Spec
- 标记格式: `⟦CB:1:{contactId}⟧密文⟦/⟧`
- CB = Codebook 模式标识
- 1 = 版本号
- contactId = IndexNumber 的 label（如 "alice"），用于接收方选择正确索引号
- 旧版兼容标识头 `⸝` (U+2E1D) 也识别

**Why**: Spec 定义明确，且解密需要 contactId 来选择正确的索引号。
**How to apply**: 加密输出拼接标记头+密文+标记尾，解密先检测标记提取 contactId。

### D7: 模块位置 — secureime 纯 JVM
- 加密/解密算法放在 secureime 模块
- 包路径: `org.secureime.sect9.crypto.codebook`
- 不引入 Android 依赖，纯 Kotlin + kotlinx-coroutines
- 字典加载接口由 Android app 层实现（assets 读取需要 Context）

**Why**: secureime 设计为纯 JVM，加密算法不应依赖 Android。与 Phase 1 D2 一致。
**How to apply**: secureime 定义 CodebookSource 接口，app 层实现 AssetsCodebookSource。

### D8: 内存清零 — clear() 手动清零
- CodebookTable 实现 Closeable
- close() 方法将 HashMap 所有 value 设为空再 clear map，二维数组置 null
- 不完美但实际有效（JVM GC 不可控，真安全清零需要 native 内存）

**Why**: NFR2 要求进程退出时清零。纯 JVM 无法 guarantee，手动清零是务实方案。
**How to apply**: 输入法 onDestroy 时调用 codebookTable.close()。

### Claude's Discretion
- CSV 导入/导出的具体格式细节（列定义、编码）
- 字典加载的错误处理和降级策略
- index 字段（codebook.json 有 index 键）的具体使用方式

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 加密算法（核心）
- `codebook.json` — 完整密码本数据（149页×100字，14810字，含 index 键）。MUST READ 全文结构。
- `SPECIFICATION.md` §2.4 — 密码本模式定义（坐标位移公式 `p'=(p+KP)mod149, i'=(i+KI)mod100`）
- `SPECIFICATION.md` §3.1-3.2 — 标识头定义和模式检测规则
- `SPECIFICATION.md` §6.1-6.4 — 索引号数据模型和密钥规则（KP/KI/KS）

### Phase 1 上下文
- `.planning/1-CONTEXT.md` — D1-D4 数据模型决策

### 现有代码
- `secureime/build.gradle.kts` — 纯 JVM 模块配置
- `secureime/src/main/kotlin/org/secureime/sect9/bus/CandidatePipeline.kt` — 现有 secureime 代码模式
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumber.kt` — 索引号 Entity（key 字段）
- `app/src/main/java/org/fcitx/fcitx5/android/data/secure/db/IndexNumberDao.kt` — 索引号 DAO

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `IndexNumber.key` — 4位数字密钥，KP=前2位, KI=后2位
- `IndexNumber.ownerId + ownerType` — 关联到联系人/频道
- `codebook.json` — 用户提供的完整密码本数据文件

### Established Patterns
- secureime 模块: 纯 JVM，kotlin("jvm")，无 Android 依赖
- 数据层: Room Entity + DAO 在 app 模块 `data.secure.db`
- kotlinx-coroutines: secureime 已依赖

### Integration Points
- 加密算法 → 输入拦截 (Phase 5): encrypt 输出写入 Buffer Bar
- 解密算法 → 剪贴板监听 (Phase 7): decrypt 从加密标记提取密文
- IndexNumber DAO → 加密时查询联系人的索引号
- codebook.json → app assets 加载 → CodebookTable 初始化

</code_context>

<specifics>
## Specific Ideas

- 用户提供 codebook.json（149页×100字=14810汉字），这是权威密码本数据源
- 加密模型：分页字典坐标偏移，不是输出数字索引号，而是输出汉字（1:1等长替换）
- 公式与 spec 2.4 完全一致：`p'=(p+KP)mod149, i'=(i+KI)mod100`

</specifics>

<deferred>
## Deferred Ideas

- 双轨加密（62字符混淆 for 数字/字母）— 后续 milestone 扩展
- Emoji 模式 — 后续 milestone
- SM9/SM4 模式 — 后续 milestone

</deferred>

---

*Phase: 02-codebook*
*Context gathered: 2026-05-23*
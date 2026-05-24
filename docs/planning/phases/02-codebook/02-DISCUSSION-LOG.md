# Phase 2: 密码本核心 — 加密/解密算法 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-23
**Phase:** 02-codebook
**Areas discussed:** 加密模型, 字典结构, 字符处理, 索引号复用, 加密标记, 字典存储, 内存清零

---

## 加密模型

| Option | Description | Selected |
|--------|-------------|----------|
| 字典序号+索引偏移→4位数字输出 | 每字输出4位数字索引号，密文为数字序列 | |
| 分页字典坐标偏移→汉字等长替换 | codebook.json 149×100 分页，坐标偏移取模后查表替换为汉字 | ✓ |

**User's choice:** 分页字典坐标偏移→汉字等长替换
**Notes:** 用户提供 codebook.json，结构为149页×100字/页=14810字。加密输出为汉字，1:1等长替换。公式与spec 2.4一致：`p'=(p+KP)mod149, i'=(i+KI)mod100`。

---

## 字典结构

| Option | Description | Selected |
|--------|-------------|----------|
| 纯文本每行一字 | 简单但无法表达分页结构 | |
| JSON 分页格式 (codebook.json) | 149页×100字/页，含 index 查找表 | ✓ |

**User's choice:** 直接使用 codebook.json
**Notes:** 文件含 pages 二维数组（解密用）和 index HashMap（加密用），数据已验证 0 不匹配。

---

## 字符处理

| Option | Description | Selected |
|--------|-------------|----------|
| 双轨（汉字偏移+62字符混淆） | spec 2.4 完整实现 | |
| 仅汉字偏移，其他原样 | 简化实现，KISS | ✓ |

**User's choice:** 仅汉字偏移
**Notes:** 数字/字母/符号原样保留。后续 milestone 可扩展双轨。

---

## 索引号复用

| Option | Description | Selected |
|--------|-------------|----------|
| 复用 IndexNumber.key | KP=前2位, KI=后2位，通过 ownerId 关联联系人 | ✓ |
| 独立概念 | 4位数字独立管理 | |

**User's choice:** 复用 IndexNumber.key
**Notes:** Phase 1 已建立数据模型，key 字段格式正好对应 KP+KI。

---

## 字典存储

| Option | Description | Selected |
|--------|-------------|----------|
| Assets 文本文件 | 每行一字 | |
| JSON in assets | 直接用 codebook.json，含完整分页和索引 | ✓ |
| Kotlin 常量 Map | 编译时内嵌 | |

**User's choice:** JSON in assets
**Notes:** codebook.json 已有完整数据结构，直接放入 app/src/main/assets/。

---

## 内存清零

| Option | Description | Selected |
|--------|-------------|----------|
| clear() 手动清零 | Closeable + clear values + null arrays | ✓ |
| DirectByteBuffer | native 内存，过度设计 | |
| 跳过 | 不做清零 | |

**User's choice:** clear() 手动清零
**Notes:** JVM GC 不可控，但清零+null 是务实方案。

---

## Claude's Discretion

- CSV 导入/导出格式细节
- 字典加载错误处理和降级策略
- index 字段的进一步使用
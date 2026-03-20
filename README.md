# es8-jsonl-gz-importer

一个基于 **Java 8 + Maven** 的 `.jsonl.gz -> Elasticsearch 8` 导入工具，重点解决：

- 多线程提速
- 批量写 ES8
- gzip 流式读取
- checkpoint / 断点续传
- 429 / 5xx 重试
- 死信输出
- 按 job 配置 comment / post / user / relation

## 核心思路

采用最直接有效的多线程模型：

- **读线程池**：一个文件一个线程，负责 gzip 解压、逐行读取、解析 JSON、组 batch
- **写线程池**：多个线程并发调用 ES Bulk API
- **阻塞队列**：连接读写两端，防止读太快把内存打满

## 推荐参数

- comment: 读线程 3，写线程 6，batch 3000 / 8MB
- post: 读线程 2，写线程 4，batch 1500 / 6MB
- user: 读线程 2，写线程 4，batch 3000 / 8MB
- relation: 读线程 2，写线程 4，batch 4000 / 8MB

## 目录结构

```text
src/main/java/com/github/lihaans/esimporter
├── checkpoint
├── config
├── deadletter
├── es
├── job
├── metrics
├── model
└── util
```

## 快速开始

### 1. 准备配置

复制 `config/*.json` 示例并修改：

- 输入目录
- 索引名
- ES 地址
- idField
- 并发参数

### 2. 打包

本项目按 Maven 标准结构组织。若本机安装 Maven：

```bash
mvn clean package
```

产物：

```bash
target/es8-jsonl-gz-importer-1.0.1-jar-with-dependencies.jar
```

### 3. 运行

```bash
java -jar target/es8-jsonl-gz-importer-1.0.1-jar-with-dependencies.jar config/comment-example.json
```

## 断点续传

checkpoint 使用 SQLite 本地文件记录，粒度到：

- 文件路径
- processed_lines
- success_lines
- failed_lines
- last_success_line
- 状态

gzip 文件恢复时采用现实做法：**重新从文件头读取，并跳过已成功行号之前的数据**。

## 死信

不可解析 JSON、不可重试的 ES 错误会写入：

```text
runtime/deadletter/<job>/<yyyyMMdd>/*.dlq.jsonl
```

## 注意

- 项目兼容“索引已创建”的前提，不依赖自动建索引
- 导入期间建议 ES 调大 `refresh_interval`
- 若允许，可临时将 `number_of_replicas` 设为 0 提高吞吐

详细设计见：

- `docs/design.md`
- `docs/checklist.md`

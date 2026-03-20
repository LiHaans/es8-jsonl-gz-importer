# 技术设计说明

## 目标

针对 `post_data / user_data / comment_data / relation_data` 目录下的大量 `.jsonl.gz` 文件，提供一个可直接落地的 Java 8 导入程序，重点关注：

- 多线程提速
- 稳定批量写入 ES8
- 断点续传
- 死信与重试

## 多线程模型

### 读线程池
- 一个文件一个任务
- 负责 gzip 解压、逐行读取、JSON 解析、batch 组装

### 写线程池
- 从阻塞队列消费 batch
- 并发调用 Bulk API
- 处理可重试错误

### 阻塞队列
- 用于读写分离
- 避免读取侧无限制堆积内存

## 数据流

```text
.jsonl.gz files
   -> FileScanner
   -> FileImportWorker(读线程池)
   -> ArrayBlockingQueue<BulkBatch>
   -> BulkWriterWorker(写线程池)
   -> Elasticsearch 8
```

## checkpoint

采用 SQLite 本地状态库，记录文件维度和行号维度进度。

gzip 场景下不追求压缩流 seek，而采用：
- 重新打开文件
- 重新解压
- 跳过 last_success_line 之前的数据

这是最稳的工程实现。

## 推荐参数

### comment
- fileParallelism=3
- writerParallelism=6
- batchDocLimit=3000
- batchBytesLimit=8MB

### post
- fileParallelism=2
- writerParallelism=4
- batchDocLimit=1500
- batchBytesLimit=6MB

### user
- fileParallelism=2
- writerParallelism=4
- batchDocLimit=3000
- batchBytesLimit=8MB

### relation
- fileParallelism=2
- writerParallelism=4
- batchDocLimit=4000
- batchBytesLimit=8MB

## 调优顺序

1. 先调 batch 大小
2. 再调 writerParallelism
3. 最后调 fileParallelism

原因：
- batch 太小，请求开销大
- writer 太少，ES 吞吐吃不满
- fileParallelism 太高，会先把 gzip 解压 CPU 打满

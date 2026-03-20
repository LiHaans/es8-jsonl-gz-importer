# 实施检查清单

## 开跑前
- [ ] ES 索引已提前创建
- [ ] 明确每类数据的 idField
- [ ] 先抽样校验 JSON 结构和 mapping
- [ ] 配置 checkpoint 路径和 deadletter 路径
- [ ] 确认磁盘空间足够写 SQLite 和死信

## 参数起步
- [ ] comment 先用 3 读 / 6 写 / 3000 / 8MB
- [ ] post 先用 2 读 / 4 写 / 1500 / 6MB
- [ ] user 先用 2 读 / 4 写 / 3000 / 8MB
- [ ] relation 先用 2 读 / 4 写 / 4000 / 8MB

## ES 侧建议
- [ ] 导入期间调大 refresh_interval
- [ ] 如允许，临时设置 replicas=0
- [ ] 观察 bulk latency、429、merge、磁盘 IO

## 调优顺序
- [ ] 先调 batch size
- [ ] 再调写线程数
- [ ] 最后调读线程数
- [ ] 每次只改一个参数

## 异常处理
- [ ] 解析失败进入 deadletter
- [ ] 429 / 5xx 走指数退避重试
- [ ] 导入中断后从 checkpoint 恢复

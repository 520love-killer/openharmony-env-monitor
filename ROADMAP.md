# Roadmap

## v1.0 基础展示版

- Web Dashboard 展示；
- 模拟数据 / 测试数据展示；
- 趋势图；
- 最近 50 条数据；
- 简单预测；
- 技术栈说明；
- 使用教程。

## v1.1 真实串口数据接入

目标：

- 禁用自动随机数据；
- 新增 dataSource 字段；
- 区分 MOCK 和 REAL_SERIAL；
- 新增 serial-bridge；
- 读取 COM21 串口；
- 解析 Hi3861 `[Sensor]` 日志；
- POST 到 Spring Boot 后端；
- 页面显示真实板端数据。

## v1.2 MQTT 联调

目标：

- Hi3861 通过 WiFi 连接 MQTT Broker；
- 通过 topic `mlp` 发布 JSON 数据；
- 后端订阅 MQTT；
- 页面显示 REAL_MQTT 数据。

## v1.3 数据库增强

目标：

- MySQL / H2 / SQLite 数据持久化；
- 预警记录管理；
- 数据导出；
- 历史查询。

## v2.0 AI Agent 智能分析

目标：

- 环境风险分析；
- 故障排查；
- 实验报告生成；
- Spring AI / LangChain4j 接入；
- RAG 问答。

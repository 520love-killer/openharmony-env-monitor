# OpenHarmony 环境监测预警系统 Web 平台

> 当前版本：v1.0 基础展示版  
> 本版本主要完成 Web 可视化平台、技术栈展示、使用教程和综合实训成果展示。  
> v1.0 默认使用模拟数据 / 后端测试数据进行页面演示；真实 Hi3861 串口数据接入将在 v1.1 版本实现。

## 项目背景

本项目用于 OpenHarmony / Hi3861 综合实训成果展示，围绕环境监测预警场景，提供一个基于 Spring Boot 和 Web Dashboard 的系统原型。

v1.0 版本重点展示系统的 Web 可视化效果、后端接口结构、数据表格、趋势图、安全状态判断和简单预测模块。当前版本主要用于综合实训成果展示和系统原型演示，不把模拟数据或后端测试数据包装成真实硬件数据。

后续真实 Hi3861 串口数据接入、MQTT 接入、数据库增强、鸿蒙手机端 APP 和 AI Agent 智能分析会在 v1.1 / v1.2 / v1.3 / v2.0 中继续开发。

## 版本说明

### v1.0 基础展示版

v1.0 是本项目的第一版，主要用于展示 OpenHarmony Hi3861 环境监测预警系统的整体架构和 Web 可视化效果。

已完成：

- Web Dashboard 页面；
- 设备 ID、温度、湿度、燃气浓度、安全状态展示；
- 温度、湿度、燃气浓度趋势图；
- 最近 50 条数据表格；
- SAFE / WARNING 状态判断；
- 简单预测模块；
- 技术栈说明；
- 使用教程；
- GitHub 项目结构整理。

注意：

当前 v1.0 版本的数据主要来自模拟数据或后端测试数据，用于系统展示和答辩演示。  
真实 Hi3861 板端数据接入将在后续 v1.1 版本中通过串口桥接或 MQTT 完成。

计划：

- v1.1：接入 Hi3861 COM 串口真实数据；
- v1.2：接入 MQTT；
- v1.3：接入数据库和数据持久化增强；
- v2.0：接入 AI Agent 智能分析和实验报告助手。

## 系统架构

```text
模拟数据 / 后端测试数据
  ↓
Spring Boot 后端服务
  ↓
H2 数据库存储
  ↓
REST API
  ↓
Web Dashboard 页面展示
```

后续可扩展为真实硬件链路：

```text
Hi3861 开发板 -> 串口桥接或 MQTT -> Spring Boot -> 数据库 -> Web Dashboard
```

## 数据来源说明

v1.0 默认使用 `MOCK` 模拟数据或通过后端接口写入的测试数据进行页面展示。

- `MOCK`：手动点击“生成模拟数据”或调用 `/api/sensor-data/mock` 产生的演示数据；
- `REAL_SERIAL`：预留给 v1.1 的 Hi3861 串口真实数据；
- `REAL_MQTT`：预留给 v1.2 的 MQTT 真实数据。

当前版本如果出现模拟数据，会明确标记为 `MOCK`，不代表真实 Hi3861 板端采集结果。

## 已完成内容

- GitHub 仓库基础结构；
- OpenHarmony / Hi3861 综合实训项目说明；
- Web 可视化页面；
- 温度、湿度、燃气浓度展示；
- 实时趋势图；
- 最近 50 条数据表格；
- SAFE / WARNING 状态显示；
- 简单预测模块；
- 技术栈说明；
- 使用教程；
- 视图演示说明；
- 后续可扩展 MQTT、真实串口数据、Agent、MySQL、鸿蒙手机端 APP。

## 技术栈

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Data JPA
- H2 Database
- HTML / CSS / JavaScript
- Chart.js

## 项目结构

```text
.
├── src/main/java/com/example/envmonitor
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── src/main/resources
│   ├── static
│   ├── application.yml
│   └── application-mysql.yml
├── docs
│   └── images
├── CHANGELOG.md
├── ROADMAP.md
├── RELEASE_NOTES_v1.0.md
├── pom.xml
├── start-local.ps1
└── stop-local.ps1
```

## 运行步骤

进入项目目录：

```powershell
cd D:\develop\openharmony-env-monitor
```

编译：

```powershell
mvn clean package
```

启动：

```powershell
mvn spring-boot:run
```

访问页面：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
```

H2 登录参数：

```text
JDBC URL: jdbc:h2:file:./data/env_monitor_db
User Name: sa
Password:
```

## API 列表

生成模拟数据：

```powershell
curl -X POST http://localhost:8080/api/sensor-data/mock
```

新增一条后端测试数据：

```powershell
curl -X POST http://localhost:8080/api/sensor-data `
  -H "Content-Type: application/json" `
  -d "{\"deviceId\":\"Hi3861-DeviceA\",\"temperature\":29.3,\"humidity\":35.2,\"gas\":18.7,\"dataSource\":\"MOCK\"}"
```

查询最新数据：

```powershell
curl "http://localhost:8080/api/sensor-data/latest?source=MOCK"
```

查询最近 N 条：

```powershell
curl "http://localhost:8080/api/sensor-data/recent?limit=50&source=MOCK"
```

查询预警记录：

```powershell
curl "http://localhost:8080/api/sensor-data/warnings?source=MOCK"
```

查询简单预测：

```powershell
curl "http://localhost:8080/api/sensor-data/prediction?source=MOCK"
```

## 预警规则

后端自动判断状态：

- `gas >= 50`：`WARNING`
- `temperature >= 40`：`WARNING`
- `humidity >= 80`：`WARNING`
- 其他情况：`SAFE`

## 视图演示说明

Web Dashboard 包含以下展示区域：

- 当前设备 ID、温度、湿度、燃气浓度；
- SAFE / WARNING 状态；
- 数据来源标记；
- 温度、湿度、燃气浓度趋势图；
- 简单预测模块；
- 最近 50 条数据表格；
- 预警记录查看入口。

如需添加截图，可将图片放入 `docs/images/` 目录，例如：

```md
![Web Dashboard](docs/images/dashboard.png)
```

## 后续扩展方向

- v1.1：接入 Hi3861 COM 串口真实数据；
- v1.2：接入 MQTT；
- v1.3：接入 MySQL / H2 / SQLite 数据持久化增强；
- v2.0：接入 AI Agent 智能分析、RAG 问答和实验报告助手；
- 后续可补充鸿蒙手机端 APP。

## 答辩说明

v1.0 版本可以作为综合实训第一版成果展示，用于说明系统架构、Web 可视化、预警判断、历史数据展示和简单预测能力。当前版本默认使用模拟数据或后端测试数据，不等同于真实 Hi3861 板端采集数据。

# OpenHarmony 环境监测预警系统 Web 平台

当前版本：v2.1 DeepSeek 真模型版

## v2.0 验证完成总结

### 当前状态
已成功将 openharmony-env-monitor 平台验证并更新至 v2.0 版本。系统目前运行稳定，核心功能已达到预期。

### 已完成内容
1. **应用启动验证**：确认 Spring Boot 后端能够使用 `local` 配置环境正常启动，并成功连接至本地 MySQL 数据库。
2. **API 接口验证**：对 `/api/agent/chat` 接口进行了完整测试，Agent 能够正确执行结构化 RAG 流程并调用 `getLatestSensorData`、`getAnalyticsSummary` 等工具。
3. **前端资源验证**：完成了前端静态资源的重新构建，Vue3 页面加载正常，UI 样式完成优化。
4. **文档同步**：已更新 `TASK_PROGRESS.md` 和 `CHECKPOINT.md`。
5. **Git 状态确认**：验证期间的修改已提交，工作区整洁。

### 下一阶段计划
1. **接入真实 DeepSeek LLM**：从 Mock 模式切换至真实 API。
2. **接入真实 MQTT 传感器数据**：对接实时数据流，实现多源数据融合。
3. **补充自动化测试**：提升 Agent 与 RAG 逻辑的稳定性。

---

本项目是基于 OpenHarmony Hi3861 的环境监测预警系统 Web 平台。
硬件端负责采集温度、湿度、燃气浓度；Spring Boot 后端负责通过串口实时读取、存储、查询、统计、预测、异常检测；Web Dashboard 用于实时展示和答辩演示。

本仓库当前只包含 Web 平台与 Spring Boot 后端改动，不包含 OpenHarmony 硬件端改动。

## 数据原则

- 默认优先读取 `REAL_SERIAL`，其次可手动选择 `REAL_MQTT`、`MOCK`、`ALL`。
- 页面刷新不会自动生成随机数据。
- `/api/sensor-data/mock` 仅用于手动演示，写入数据会明确标记为 `MOCK`。
- 统计、预测、异常检测默认不使用 `MOCK`。
- 真实数据不足时接口返回清晰提示，不会用随机数据冒充真实数据。
- 页面会用中文展示数据来源，例如“真实串口数据（REAL_SERIAL）”“真实 MQTT 数据（REAL_MQTT）”“模拟演示数据（MOCK）”。

## 串口实时接入

当前 Web 平台支持直接读取 Hi3861 串口输出，不修改硬件端代码。

本机已验证参数：

```text
port: COM21
baud-rate: 115200
data-source: REAL_SERIAL
```

板子当前输出格式示例：

```text
[Sensor] temperature=30.8C(AHT20), humidity=48.0%(AHT20), gas=46.6ppm(MQ2), status=SAFE
```

后端只会解析包含 `temperature`、`humidity`、`gas` 的传感器行，保存到 MySQL 的 `sensor_data` 表，并标记为 `REAL_SERIAL`。其他日志行只作为串口状态信息，不会当作传感器数据。

本地配置位于 `src/main/resources/application-local.yml`：

```yaml
app:
  serial:
    enabled: true
    port-name: COM21
    baud-rate: 115200
    reconnect-delay-ms: 3000
```

公开配置默认不强制启用串口，可通过环境变量开启：

```powershell
$env:SERIAL_ENABLED="true"
$env:SERIAL_PORT="COM21"
$env:SERIAL_BAUD_RATE="115200"
```

## MySQL 数据库配置

正式数据存储使用 MySQL，数据库名：

```text
openharmony_env_monitor
```

公开配置文件 `src/main/resources/application.yml` 使用环境变量占位，不保存真实密码：

```yaml
spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/openharmony_env_monitor?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:}
```

本地运行可使用：

```text
src/main/resources/application-local.yml
```

注意：`application-local.yml` 包含本机数据库密码，已加入 `.gitignore`，不应提交到 GitHub。

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS openharmony_env_monitor
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

本地启动：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

访问页面：

```text
http://localhost:8080
```

## Vue3 前端

v2.x 前端使用 Vue3 + Vite + TypeScript 重构，提供更现代的 AIoT Dashboard 和温度计 Agent 交互体验。

技术栈：
- Vue3
- Vite
- TypeScript
- Vue Router（Hash 模式）
- Pinia
- Axios
- ECharts
- Marked（Markdown 渲染）
- DeepSeek 流式输出
- localStorage 上下文记忆

开发启动：

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 http://localhost:5173，通过 Vite proxy 转发 `/api` 到 Spring Boot（http://localhost:8080）。

构建并集成到 Spring Boot：

```bash
cd frontend
npm run build
cp -r dist/* ../src/main/resources/static/
```

然后启动 Spring Boot，访问 http://localhost:8080 即可。

## 缓存技术栈

本项目使用 Spring Cache + Caffeine 作为默认缓存方案，用于提升 Dashboard、统计分析、预测分析、异常检测和 Agent 上下文接口的响应速度。

默认配置：

- provider: `caffeine`
- ttl: `30 seconds`
- max size: `1000`

缓存不会替代 MySQL，传感器原始数据仍然以 MySQL 为准。新增传感器数据、生成统计摘要、生成异常记录、执行数据清理后会清理相关缓存。

后续如需分布式部署，可切换到 Redis；当前默认不强制安装 Redis。

## 智能数据分析模块

v1.1 已加入以下能力：

- 最近 50 条数据统计；
- 平均值、最大值、最小值、样本标准差；
- 温度、湿度、燃气变化率；
- 数据波动程度；
- 温度短期预测；
- 异常检测；
- Agent 预留接口；
- 数据库定时汇总与定时清理；
- Caffeine 查询缓存。

## 算法说明

### 统计分析

平均值：`sum / n`

样本标准差：

```text
sqrt(sum((x - mean)^2) / (n - 1))
```

变化率：

```text
(last - first) / max(1, timeMinutes)
```

趋势判断：

- 温度：`abs(rate) < 0.02` 为稳定；
- 湿度：`abs(rate) < 0.05` 为稳定；
- 燃气：`abs(rate) < 0.1` 为稳定。

### 滑动平均

使用最近 5 条温度平均值平滑短期波动，再结合变化率推算未来 5 分钟和 10 分钟。

### 线性回归

使用序号 `0..n-1` 作为 x，温度作为 y，计算斜率和截距，判断整体上升或下降趋势。

### 指数平滑

使用 EWMA：

```text
S_t = alpha * X_t + (1 - alpha) * S_{t-1}
```

默认 `alpha = 0.3`，强调最近数据对预测的影响。

### 异常检测

包括：

- 阈值检测：温度、湿度、燃气超限；
- 突变检测：相邻数据变化过大；
- 连续升高检测：最近 5 条持续上升；
- 长期无变化检测：最近 10 条几乎不变。

## 数据清理策略

- 原始真实数据保留 30 天；
- MOCK 数据保留 7 天；
- 统计摘要保留 180 天；
- 异常记录保留 180 天；
- 每天凌晨 3:00 自动清理；
- 不删除最近 24 小时数据。

## v2.1 DeepSeek 真模型版

v2.1 在 v2.0 基础上将 Agent 从 Mock / 模板模式升级为真实 DeepSeek LLM 模式。

### 主要变化

1. Agent「温度计」在有 `DEEPSEEK_API_KEY` 时调用真实 DeepSeek LLM 生成自然语言回答
2. 无 API Key 时自动回退 Mock 模式，系统正常运行
3. API Key 通过环境变量读取，不写入代码/配置/文档
4. 请求失败时自动降级，接口不崩溃
5. 保留 7 种工具调用：getLatestSensorData、getRecent50Data、getAnalyticsSummary、getTemperatureForecast、getAnomalyDetection、getDatabaseStatus、generateReportSummary
6. 保留结构化 RAG + 上下文记忆 + 流式输出
7. 系统提示词优化，强调不编造数据、MOCK 数据明确标注
8. `AgentChatResponse` 新增 `mode`（deepseek/mock）和 `model` 字段

## v2.0 Agent 主页面版

v2.0 将系统从传统环境监测 Dashboard 升级为以 Agent 为核心的智能环境分析平台。

### 主要变化

1. Agent「温度计」成为默认主页面，用户访问 `http://localhost:8080` 直接看到 Agent 对话界面
2. 数据看板、统计分析、温度预测、异常检测调整为左侧导航次页面
3. Agent 可自动调用 7 种工具：getLatestSensorData、getRecent50Data、getAnalyticsSummary、getTemperatureForecast、getAnomalyDetection、getDatabaseStatus、generateReportSummary
4. Agent 可解释使用的算法和预测置信度
5. Agent 可生成实验报告摘要
6. 支持结构化 RAG + Tool Calling + DeepSeek LLM，Mock 模式不依赖外部 API Key
7. 新增会话持久化（MySQL），支持多轮对话和历史查询

### 页面结构

```
左侧导航栏：
├── 温度计 Agent（默认主页）
├── 数据看板
├── 统计分析
├── 温度预测
├── 异常检测
├── 最近数据
├── 系统状态
└── 关于项目
```

## Agent 扩展

v2.0 已将 Agent 升级为完整的结构化 RAG + Tool Calling 系统。

### Agent 身份

- 名字：**温度计**
- 角色：环境监测智能助手
- 能力：分析 Hi3861 采集的温度、湿度、燃气数据，回答环境状态、温度变化、异常风险、预测趋势、算法原理、实验报告和系统故障问题

### 当前实现

Mock Agent 基于真实后端接口结果生成回答，不编造数据：
- 用户问"最近温度" → 调用 getRecent50Data
- 用户问"趋势" → 调用 getAnalyticsSummary + getTemperatureForecast
- 用户问"异常" → 调用 getAnomalyDetection
- 用户问"算法" → 返回标准算法解释
- 用户问"报告" → 自动调用所有工具生成报告摘要

### 数据诚实规则

1. 没有 REAL_SERIAL / REAL_MQTT 数据时，Agent 必须说明
2. 只有 MOCK 数据时，必须说明"当前为模拟数据"
3. 真实数据不足 5 条时，说明"无法可靠分析"
4. 不说"精准预测"，只说"短期趋势估计"
5. 不编造数据库里不存在的数据

### 接入 DeepSeek LLM（可选）

当前 v2.1 支持两种模式，自动切换：
1. **DeepSeek 模式**（有 API Key 时自动启用）：调用真实 DeepSeek LLM 生成自然语言回答
2. **Mock Agent**（无 API Key 时自动回退）：基于后端工具调用结果生成结构化回答

配置 DeepSeek 环境变量：

DeepSeek 环境变量：
```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek API Key"
$env:DEEPSEEK_BASE_URL="https://api.deepseek.com"
$env:DEEPSEEK_MODEL="deepseek-chat"
```

通用环境变量（兼容旧配置）：
```powershell
$env:AGENT_PROVIDER="deepseek"
$env:AGENT_BASE_URL="https://api.deepseek.com"
$env:AGENT_MODEL="deepseek-chat"
$env:AGENT_API_KEY="你的 API Key"
```

也兼容 Kimi / Moonshot：
```powershell
$env:KIMI_API_KEY="你的 Kimi API Key"
$env:KIMI_BASE_URL="https://api.moonshot.cn"
$env:KIMI_MODEL="moonshot-v1-8k"
```

**安全注意事项：**
- API Key 只在服务端读取，不会传到前端
- 不要把真实 API Key 写入 `application.yml`、`application-local.yml` 或 Git 仓库
- 不要 `console.log` API Key
- 没有 API Key 时自动使用 Mock Agent，系统正常运行
- `.env.local` 和 `application-local.yml` 已加入 `.gitignore`

### 流式输出

v2.0 新增 `POST /api/agent/stream` 端点，使用 SSE (Server-Sent Events) 实现流式输出：
- 前端通过 `ReadableStream` 逐字/分段接收 Agent 回答
- 用户发送问题后立即显示气泡，内容逐步追加
- 支持"正在分析..."中间状态提示
- 输出结束后显示 usedTools、dataSource、confidence
- 发送中禁止重复发送
- DeepSeek 调用失败时自动降级为 Mock 流式输出

### 上下文记忆与多轮对话

v2.0 支持多轮对话上下文记忆：
- 会话数据保存在浏览器 `localStorage`（key: `hdt-study-agent-sessions`）
- 自动保存当前会话，刷新页面后保留
- 发送新问题时，自动将最近 10 条历史消息传给后端
- 单条消息超过 1000 字自动截断
- 历史会话超过 20 个时保留最近 20 个
- 支持新建会话、清空会话
- 后端同时持久化到 MySQL（AgentConversation / AgentMessage 表）
- API Key 不会写入 localStorage

## API 总览

传感器数据：

```text
GET  /api/sensor-data/latest?source=REAL_SERIAL
GET  /api/sensor-data/recent?limit=50&source=REAL_SERIAL
GET  /api/sensor-data
GET  /api/sensor-data/warnings?source=REAL_SERIAL
POST /api/sensor-data
POST /api/sensor-data/mock
```

智能分析：

```text
GET /api/analytics/summary?limit=50&source=REAL_SERIAL
GET /api/analytics/trend?limit=50&source=REAL_SERIAL
GET /api/forecast/temperature?limit=50&source=REAL_SERIAL
GET /api/anomaly/detect?limit=50&source=REAL_SERIAL
```

Agent（v2.0）：

```text
POST   /api/agent/chat
POST   /api/agent/stream            ← 新增：SSE 流式输出
GET    /api/agent/status            ← 新增：Agent 状态（DeepSeek/Mock）
GET    /api/agent/context?source=REAL_SERIAL
GET    /api/agent/sessions
GET    /api/agent/sessions/{sessionId}
DELETE /api/agent/sessions/{sessionId}
POST   /api/agent/report-summary
POST   /api/agent/explain-algorithm
```

系统状态：

```text
GET /api/system/database-status
GET /api/system/retention-policy
GET /api/system/cache-status
GET /api/system/serial-status
```

## 测试命令

编译：

```powershell
mvn clean package
```

启动 local 配置：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

写入一条明确标记为 `REAL_SERIAL` 的测试数据：

```powershell
curl -X POST http://localhost:8080/api/sensor-data `
  -H "Content-Type: application/json" `
  -d "{\"deviceId\":\"Hi3861-DeviceA\",\"temperature\":27.8,\"humidity\":60.4,\"gas\":26.2,\"status\":\"SAFE\",\"dataSource\":\"REAL_SERIAL\",\"rawMessage\":\"manual mysql test\"}"
```

测试分析接口：

```powershell
curl "http://localhost:8080/api/analytics/summary?limit=50&source=REAL_SERIAL"
curl "http://localhost:8080/api/forecast/temperature?limit=50&source=REAL_SERIAL"
curl "http://localhost:8080/api/anomaly/detect?limit=50&source=REAL_SERIAL"
curl "http://localhost:8080/api/agent/context?source=REAL_SERIAL"
curl -X POST http://localhost:8080/api/agent/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"查看最近50条温度\"}"
curl -X POST http://localhost:8080/api/agent/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"请分析当前温度趋势，并告诉我使用了什么算法\"}"
curl -X POST http://localhost:8080/api/agent/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"为什么系统报警？\"}"
curl -X POST http://localhost:8080/api/agent/report-summary ^
  -H "Content-Type: application/json" ^
  -d "{\"source\":\"REAL_SERIAL\"}"
curl "http://localhost:8080/api/agent/sessions"
curl "http://localhost:8080/api/system/database-status"
curl "http://localhost:8080/api/system/cache-status"
curl "http://localhost:8080/api/system/serial-status"
```

MySQL 验证：

```sql
USE openharmony_env_monitor;
SHOW TABLES;
SELECT * FROM sensor_data ORDER BY id DESC LIMIT 10;
SELECT * FROM analytics_summary ORDER BY id DESC LIMIT 10;
SELECT * FROM anomaly_record ORDER BY id DESC LIMIT 10;
```

## 不应提交的文件

- `.env`
- `*.env`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-secret.yml`
- `target/`
- `node_modules/`
- MySQL 本地数据文件
- 下载的本地工具和缓存文件

## 下一步

- [x] 串口实时读取已接入
- [x] Agent v2.0 结构化 RAG + Tool Calling
- [x] Agent v2.1 DeepSeek 真模型接入
- [x] 7 种 Agent 工具、会话持久化、多轮对话
- [x] DeepSeek 流式输出（SSE + ReadableStream）
- [x] 多轮对话上下文记忆（localStorage + MySQL）
- [x] Mock 模式伪流式降级
- [x] 左右两栏 Agent 工作台布局
- [ ] 接入 MQTT 数据通道
- [ ] 根据真实设备采样频率调优预测与异常检测阈值
- [ ] 添加向量数据库（Chroma / Milvus）升级为语义 RAG

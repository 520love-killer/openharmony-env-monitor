# OpenHarmony 环境监测预警系统 Web 平台

当前版本：v1.1 智能数据分析增强版

本项目是基于 OpenHarmony Hi3861 的环境监测预警系统 Web 平台。硬件端负责采集温度、湿度、燃气浓度；Spring Boot 后端负责接收、存储、查询、统计、预测、异常检测；Web Dashboard 用于实时展示和答辩演示。

本仓库当前只包含 Web 平台与 Spring Boot 后端改动，不包含 OpenHarmony 硬件端改动。

## 数据原则

- 默认优先读取 `REAL_SERIAL`，其次可手动选择 `REAL_MQTT`、`MOCK`、`ALL`。
- 页面刷新不会自动生成随机数据。
- `/api/sensor-data/mock` 仅用于手动演示，写入数据会明确标记为 `MOCK`。
- 统计、预测、异常检测默认不使用 `MOCK`。
- 真实数据不足时接口返回清晰提示，不会用随机数据冒充真实数据。

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

## Agent 扩展

当前使用 Mock Agent，不接入外部大模型 API，不需要 API Key。Mock Agent 会读取统计分析、温度预测和异常检测结果生成解释，不编造数据。

后续可替换为 Spring AI、LangChain4j 或大模型 API，并复用 `/api/agent/context` 作为环境分析上下文。

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

Agent：

```text
GET  /api/agent/context?source=REAL_SERIAL
POST /api/agent/analyze
```

系统状态：

```text
GET /api/system/database-status
GET /api/system/retention-policy
GET /api/system/cache-status
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
curl "http://localhost:8080/api/system/database-status"
curl "http://localhost:8080/api/system/cache-status"
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

- 接入真实串口桥接程序，将 Hi3861 串口数据写入 `/api/sensor-data`，数据源标记为 `REAL_SERIAL`；
- 接入 MQTT 数据通道，数据源标记为 `REAL_MQTT`；
- 为 Agent 接入 Spring AI 或 LangChain4j；
- 根据真实设备采样频率继续调优预测与异常检测阈值。

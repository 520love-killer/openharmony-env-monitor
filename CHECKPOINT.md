# CHECKPOINT

更新时间：2026-05-19 09:45 Asia/Shanghai

## 当前任务目标

完成 Web 平台 v1.1 智能数据分析增强版：

- 接入本地 MySQL；
- 禁止随机数据冒充真实数据；
- 支持 `REAL_SERIAL` / `REAL_MQTT` / `MOCK` / `ALL` 数据源；
- 保留手动 MOCK，但必须明确标记；
- 增加 Caffeine 缓存、统计分析、温度预测、异常检测、定时汇总、定时清理；
- 优化前端 Dashboard；
- 为后续 Agent 预留接口和页面区域；
- 不修改 OpenHarmony Hi3861 硬件端代码。

## 已完成步骤

- 已确认当前项目根目录为 `D:\develop\openharmony-env-monitor`。
- 已确认 MySQL 8.0.43 可用，服务 `MySQL80` 正在运行。
- 已创建数据库 `openharmony_env_monitor`。
- 已将 Spring Boot 默认公开配置切换为 MySQL 环境变量占位写法。
- 已创建本地 `src/main/resources/application-local.yml`，该文件被 `.gitignore` 忽略，不会提交。
- 已加入 Spring Cache + Caffeine 缓存依赖与配置。
- 已启用 `@EnableCaching` 和 `@EnableScheduling`。
- 已新增 `analytics_summary` 和 `anomaly_record` 对应实体与 Repository。
- 已扩展 `sensor_data`，保留 `raw_message` 文本字段。
- 已实现统计分析、趋势判断、温度预测、异常检测、Agent 上下文、Mock Agent、系统状态接口。
- 已实现统计摘要定时任务、异常检测定时任务、数据保留清理定时任务。
- 已把新增数据后的相关缓存清理接入写入流程。
- 已将前端默认数据源改为 `REAL_SERIAL`。
- 已移除页面刷新自动生成 MOCK 的行为。
- 已重做 Web Dashboard：实时数据、趋势图、智能统计、预测、异常、Agent、最近 50 条、数据库状态、缓存状态、清理策略。
- 已更新 README.md 到 v1.1 说明。
- 已添加 `.playwright-cli/`、本地密钥配置、日志、target 等忽略规则。
- 已接入 Hi3861 当前串口 `COM21 / 115200`，实时解析 `[Sensor] temperature=... humidity=... gas=...` 行并保存为 `REAL_SERIAL`。
- 前端数据源已改为中文展示：真实串口数据、真实 MQTT 数据、模拟演示数据、全部数据来源。

## 已修改文件

主要修改：

- `.gitignore`
- `README.md`
- `pom.xml`
- `src/main/java/com/example/envmonitor/EnvMonitorApplication.java`
- `src/main/java/com/example/envmonitor/controller/SensorDataController.java`
- `src/main/java/com/example/envmonitor/entity/SensorData.java`
- `src/main/java/com/example/envmonitor/repository/SensorDataRepository.java`
- `src/main/java/com/example/envmonitor/service/SensorDataService.java`
- `src/main/java/com/example/envmonitor/service/PredictionService.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-mysql.yml`
- `src/main/resources/static/index.html`
- `src/main/resources/static/app.js`
- `src/main/resources/static/style.css`

新增后端文件：

- `src/main/java/com/example/envmonitor/config/CacheConfig.java`
- `src/main/java/com/example/envmonitor/controller/AgentController.java`
- `src/main/java/com/example/envmonitor/controller/AnalyticsController.java`
- `src/main/java/com/example/envmonitor/controller/AnomalyController.java`
- `src/main/java/com/example/envmonitor/controller/ForecastController.java`
- `src/main/java/com/example/envmonitor/controller/SystemStatusController.java`
- `src/main/java/com/example/envmonitor/dto/AgentAnalyzeRequest.java`
- `src/main/java/com/example/envmonitor/dto/AgentAnalyzeResponse.java`
- `src/main/java/com/example/envmonitor/dto/AnomalyItem.java`
- `src/main/java/com/example/envmonitor/dto/AnomalyResponse.java`
- `src/main/java/com/example/envmonitor/dto/ForecastResponse.java`
- `src/main/java/com/example/envmonitor/dto/StatisticsSummaryResponse.java`
- `src/main/java/com/example/envmonitor/dto/TrendSummaryResponse.java`
- `src/main/java/com/example/envmonitor/entity/AnalyticsSummary.java`
- `src/main/java/com/example/envmonitor/entity/AnomalyRecord.java`
- `src/main/java/com/example/envmonitor/repository/AnalyticsSummaryRepository.java`
- `src/main/java/com/example/envmonitor/repository/AnomalyRecordRepository.java`
- `src/main/java/com/example/envmonitor/service/AgentContextService.java`
- `src/main/java/com/example/envmonitor/service/AnalyticsScheduleService.java`
- `src/main/java/com/example/envmonitor/service/AnalyticsService.java`
- `src/main/java/com/example/envmonitor/service/AnomalyScheduleService.java`
- `src/main/java/com/example/envmonitor/service/AnomalyService.java`
- `src/main/java/com/example/envmonitor/service/CacheInvalidationService.java`
- `src/main/java/com/example/envmonitor/service/DataRetentionService.java`
- `src/main/java/com/example/envmonitor/service/ForecastService.java`
- `src/main/java/com/example/envmonitor/service/MockAgentService.java`
- `src/main/java/com/example/envmonitor/service/SystemStatusService.java`
- `src/main/java/com/example/envmonitor/util/DataSourceUtils.java`

本地未提交文件：

- `src/main/resources/application-local.yml`，包含本地数据库密码，已被 `.gitignore` 忽略。

## 测试通过的功能

- `mvn clean package`：成功。
- `node --check src/main/resources/static/app.js`：成功。
- `mvn spring-boot:run -Dspring-boot.run.profiles=local`：成功启动。
- MySQL 连接：HikariPool 成功建立连接。
- JPA 自动建表：`sensor_data`、`analytics_summary`、`anomaly_record` 已创建。
- 写入测试数据：通过 `/api/sensor-data` 写入 6 条 `REAL_SERIAL` 接口测试数据。
- `GET /api/sensor-data/latest?source=REAL_SERIAL`：返回最新数据。
- `GET /api/analytics/summary?limit=50&source=REAL_SERIAL`：返回统计摘要。
- `GET /api/forecast/temperature?limit=50&source=REAL_SERIAL`：返回温度预测。
- `GET /api/anomaly/detect?limit=50&source=REAL_SERIAL`：检测到 GAS_THRESHOLD、TEMPERATURE_RISING、GAS_RISING。
- `GET /api/agent/context?source=REAL_SERIAL`：返回 latest/statistics/forecast/anomalies/dataQuality。
- `POST /api/agent/analyze`：Mock Agent 返回基于真实接口结果的解释。
- `GET /api/system/database-status`：返回数据库名 `openharmony_env_monitor`、计数和最近任务时间。
- `GET /api/system/retention-policy`：返回 30/7/180/180 天策略。
- `GET /api/system/cache-status`：返回 Caffeine、TTL 30 秒、max size 1000、缓存名列表。
- 空数据源 `REAL_MQTT`：统计、预测、异常接口返回“真实数据不足”，没有报 500。
- 浏览器验证：`http://localhost:8080` 页面加载成功，控制台 0 error / 0 warning。
- 串口验证：`GET /api/system/serial-status` 显示 `enabled=true`、`connected=true`、`portName=COM21`、`baudRate=115200`。
- 实时数据验证：`GET /api/sensor-data/latest?source=REAL_SERIAL` 返回板子实时数据，例如温度约 30.8℃、湿度约 48.0%、燃气约 46.6ppm。

## 当前是否能编译

能。最近一次命令：

```powershell
mvn clean package
```

结果：`BUILD SUCCESS`。

## 当前是否能启动

能。当前服务已用 local profile 启动在：

```text
http://localhost:8080
```

当前监听进程：Java / Maven Spring Boot，端口 `8080`。

## 当前是否有报错

没有阻塞性错误。

已修复的问题：

- 数据库状态接口曾错误解析 `serverTimezone=Asia/Shanghai`，已修复为正确数据库名 `openharmony_env_monitor`。
- 浏览器曾有 `/favicon.ico` 404，已添加空 favicon，控制台已无错误。
- Hibernate `MySQL8Dialect` 弃用提示已改为 `MySQLDialect`。

## 当前 MySQL 验证结果

已存在表：

- `sensor_data`
- `analytics_summary`
- `anomaly_record`

最近测试数据：

- `sensor_data`：6 条 `REAL_SERIAL` 接口测试数据，最后一条 gas=55.2，status=`WARNING`。
- `analytics_summary`：定时任务已写入统计摘要，source=`REAL_SERIAL`，sampleCount=6。
- `anomaly_record`：已写入 3 条异常记录。

注意：这些 `REAL_SERIAL` 数据是接口测试数据，`raw_message` 为 `codex manual api test...`，不是 Hi3861 硬件真实采集。

## 还没有完成

- 已接入当前 Hi3861 串口实时读取；如果换 USB 口或串口号变化，需要更新 `app.serial.port-name`。
- 未接入真实 MQTT。
- 未接入真实大模型 Agent。
- 未新增自动化单元测试类。
- 本地 Git 提交已完成。
- GitHub 推送已尝试 2 次，但网络连接 GitHub 443 端口失败，需要下次网络恢复后重试。

## 下一步应该从哪里继续

如果用户明天发送“重新开始”，应从当前项目根目录继续：

```powershell
cd D:\develop\openharmony-env-monitor
git status
mvn clean package
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

然后优先继续：

1. 如本轮尚未提交，先检查 `.gitignore` 和 `application-local.yml` 是否未被跟踪。
2. 如果本轮提交不存在，再执行 Git 提交：

```powershell
git add .
git commit -m "feat: add MySQL cache analytics forecast anomaly detection and agent preparation"
```

3. 检查远程：

```powershell
git remote -v
```

4. 当前远程是用户指定仓库，但本轮推送失败。网络恢复后重试：

```powershell
git push
```

5. 下一阶段如果串口号变化，先执行 `[System.IO.Ports.SerialPort]::GetPortNames()`，再更新 `app.serial.port-name`。

## 需要执行的常用命令

```powershell
cd D:\develop\openharmony-env-monitor
mvn clean package
mvn spring-boot:run -Dspring-boot.run.profiles=local
Invoke-RestMethod 'http://localhost:8080/api/system/database-status'
Invoke-RestMethod 'http://localhost:8080/api/system/cache-status'
```

MySQL 验证：

```powershell
& 'D:\tool\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -p openharmony_env_monitor
```

SQL：

```sql
SHOW TABLES;
SELECT * FROM sensor_data ORDER BY id DESC LIMIT 10;
SELECT * FROM analytics_summary ORDER BY id DESC LIMIT 10;
SELECT * FROM anomaly_record ORDER BY id DESC LIMIT 10;
```

## 需要注意的坑

- 不要把 `src/main/resources/application-local.yml` 提交到 GitHub。
- 不要把 MySQL 密码写入 `application.yml`、README 或提交记录。
- 不要把 MOCK 当作真实数据。
- 页面默认数据源必须保持 `REAL_SERIAL`。
- 真实数据不足时要返回提示，不要自动生成随机数据。
- 新增数据后必须清理统计、预测、异常、Agent、数据库状态相关缓存。
- 统计、预测、异常接口的 cache key 必须包含 `source` 和 `limit`。
- 串口读取只解析含 `temperature`、`humidity`、`gas` 的传感器行，其他板子日志不要写入传感器表。
- 不要修改 OpenHarmony 硬件端代码。
- 不要使用 MaQueOS、`./run.sh`、`gdb.sh`、QEMU、A18 烧录工具。

## 不能上传 GitHub 的文件

- `.env`
- `*.env`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-secret.yml`
- `target/`
- `logs/`
- `.playwright-cli/`
- `node_modules/`
- MySQL 本地数据文件
- `D:\tool` 下载工具或安装包

## 当前 Git 状态

本轮已完成本地提交，提交信息为：

```text
feat: add MySQL cache analytics forecast anomaly detection and agent preparation
```

本轮推送结果：

```text
git push
fatal: unable to access 'https://github.com/520love-killer/openharmony-env-monitor.git/': Recv failure: Connection was reset

git push origin main
fatal: unable to access 'https://github.com/520love-killer/openharmony-env-monitor.git/': Failed to connect to github.com port 443
```

提交前状态记录为：

```text
## main...origin/main
 M .gitignore
 M README.md
 M pom.xml
 M src/main/java/com/example/envmonitor/EnvMonitorApplication.java
 M src/main/java/com/example/envmonitor/controller/SensorDataController.java
 M src/main/java/com/example/envmonitor/entity/SensorData.java
 M src/main/java/com/example/envmonitor/repository/SensorDataRepository.java
 M src/main/java/com/example/envmonitor/service/PredictionService.java
 M src/main/java/com/example/envmonitor/service/SensorDataService.java
 M src/main/resources/application-mysql.yml
 M src/main/resources/application.yml
 M src/main/resources/static/app.js
 M src/main/resources/static/index.html
 M src/main/resources/static/style.css
?? src/main/java/com/example/envmonitor/config/
?? src/main/java/com/example/envmonitor/controller/AgentController.java
?? src/main/java/com/example/envmonitor/controller/AnalyticsController.java
?? src/main/java/com/example/envmonitor/controller/AnomalyController.java
?? src/main/java/com/example/envmonitor/controller/ForecastController.java
?? src/main/java/com/example/envmonitor/controller/SystemStatusController.java
?? src/main/java/com/example/envmonitor/dto/
?? src/main/java/com/example/envmonitor/entity/AnalyticsSummary.java
?? src/main/java/com/example/envmonitor/entity/AnomalyRecord.java
?? src/main/java/com/example/envmonitor/repository/AnalyticsSummaryRepository.java
?? src/main/java/com/example/envmonitor/repository/AnomalyRecordRepository.java
?? src/main/java/com/example/envmonitor/service/AgentContextService.java
?? src/main/java/com/example/envmonitor/service/AnalyticsScheduleService.java
?? src/main/java/com/example/envmonitor/service/AnalyticsService.java
?? src/main/java/com/example/envmonitor/service/AnomalyScheduleService.java
?? src/main/java/com/example/envmonitor/service/AnomalyService.java
?? src/main/java/com/example/envmonitor/service/CacheInvalidationService.java
?? src/main/java/com/example/envmonitor/service/DataRetentionService.java
?? src/main/java/com/example/envmonitor/service/ForecastService.java
?? src/main/java/com/example/envmonitor/service/MockAgentService.java
?? src/main/java/com/example/envmonitor/service/SystemStatusService.java
?? src/main/java/com/example/envmonitor/util/
```

## 如果用户明天发送“重新开始”

应该先输出：

```text
已读取检查点，准备从上次中断处继续。
```

然后执行：

1. `cd D:\develop\openharmony-env-monitor`
2. 读取 `CHECKPOINT.md`
3. 读取 `TASK_PROGRESS.md`
4. 执行 `git status`
5. 如果本轮已提交，继续真实串口桥接或真实 Agent 接入设计。
6. 如果本轮未提交，先重新执行 `mvn clean package`，确认通过后提交。

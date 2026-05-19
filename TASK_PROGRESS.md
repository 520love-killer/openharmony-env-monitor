# TASK_PROGRESS

## 已完成

- [x] 确认项目根目录与 Spring Boot 结构
- [x] 检查 MySQL 客户端与 MySQL80 服务
- [x] 创建 `openharmony_env_monitor` 数据库
- [x] 配置公开 `application.yml` 使用环境变量占位
- [x] 创建本地 `application-local.yml`
- [x] 确认 `application-local.yml` 被 `.gitignore` 忽略
- [x] 添加 MySQL / JPA / Cache / Caffeine 依赖
- [x] 启用缓存与定时任务
- [x] 新增 Caffeine 缓存配置
- [x] 新增统一缓存清理服务
- [x] 改造数据源规范，默认查询 `REAL_SERIAL`
- [x] 保留手动 MOCK，且写入 `dataSource=MOCK`
- [x] 新增 `AnalyticsSummary` 实体
- [x] 新增 `AnomalyRecord` 实体
- [x] 新增统计分析接口
- [x] 新增趋势判断接口
- [x] 新增温度预测接口
- [x] 新增异常检测接口
- [x] 新增 Agent 上下文接口
- [x] 新增 Mock Agent 分析接口
- [x] 新增数据库状态接口
- [x] 新增缓存状态接口
- [x] 新增数据保留策略接口
- [x] 新增统计摘要定时任务
- [x] 新增异常检测定时任务
- [x] 新增数据保留清理定时任务
- [x] 前端默认数据源改为 `REAL_SERIAL`
- [x] 前端新增统计、预测、异常、Agent、数据库、缓存、清理策略区域
- [x] README 更新为 v1.1
- [x] `mvn clean package` 通过
- [x] Spring Boot local profile 启动成功
- [x] MySQL 表创建成功
- [x] 手动写入 6 条 `REAL_SERIAL` 接口测试数据
- [x] 统计、预测、异常、Agent、系统状态接口测试通过
- [x] 浏览器页面验证通过，控制台 0 error / 0 warning
- [x] 创建检查点文件

## 待完成

- [x] Git 提交本轮稳定版本
- [ ] 根据远程仓库情况推送 GitHub
- [ ] 接入真实 Hi3861 串口数据桥接
- [ ] 接入真实 MQTT
- [ ] 接入真实大模型 Agent
- [ ] 增加自动化测试类

## 未测试

- [ ] Redis 可选配置
- [ ] 大量历史数据下的查询性能
- [ ] 真实串口高频写入下的缓存清理表现
- [ ] 生产部署环境变量配置

## 有问题需要修复

- [ ] GitHub 推送失败：连接 github.com:443 超时或被重置，网络恢复后执行 `git push origin main`

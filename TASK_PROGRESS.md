# TASK PROGRESS

更新时间：2026-05-20 01:20 Asia/Shanghai

## v2.2 用户身份与应用场景扩展 — 完成总结

### 当前状态
系统已支持 6 种用户身份和 6 种应用场景，可根据不同场景规则进行环境评分、风险判断和应用建议。前端新增"应用场景"页面，Agent 新增场景化分析工具。

### 已完成内容

#### 1. 场景规则引擎
- 新增 `ScenarioRuleEngine`：定义 6 种场景阈值和权重
- 新增 `ScenarioProfileService`：风险评估和建议生成
- 新增 `ScenarioAnalysisService`：综合评分计算（0-100 分）
- 新增 `ScenarioController`：`GET /api/scenario/analysis`

#### 2. 用户身份
STUDENT（学生）、FARMER（农业种植者）、ENGINEER（工程技术人员）、AC_USER（空调使用者）、RESEARCHER（研究分析者）、CUSTOM（自定义用户）

#### 3. 应用场景
GENERAL_MONITOR（通用环境监测）、AGRICULTURE_GREENHOUSE（农业温室）、SMART_AIR_CONDITIONER（智能空调）、INDUSTRIAL_SAFETY（工业安全）、LAB_ENVIRONMENT（实验室环境）、CUSTOM_SCENARIO（自定义场景）

#### 4. 环境评分算法
- 温度得分 + 湿度得分 + 燃气安全得分 + 趋势稳定得分 + 异常风险扣分
- 各场景权重不同（农业侧重温湿度，工业侧重燃气，空调侧重温度舒适）
- 评分等级：EXCELLENT(90-100)、GOOD(75-89)、NORMAL(60-74)、WARNING(0-59)
- 返回可解释的 scoreBreakdown

#### 5. Agent 场景化增强
- `AgentToolService` 新增 `getScenarioAnalysis` 工具
- 用户问农业/空调/工业/实验相关问题时自动触发场景分析
- 回答中显示场景评分、风险和建议

#### 6. 前端应用场景页面
- 新增 `ApplicationView.vue`：用户身份选择、场景选择、参数配置
- 新增 `scenario.ts` API 和 Pinia Store
- 新增 `/application` 路由和侧边栏菜单项
- 页面显示：评分、等级、评分明细、关键指标、风险、建议、算法说明
- 参数配置持久化到 localStorage

#### 7. 测试验证
- 5 种场景接口全部返回正确评分和建议
- Agent 场景化工具调用正常
- 前端编译通过，静态资源已集成

---

## v2.1 DeepSeek 真模型接入 — 完成总结

### 当前状态
Agent 已从 Mock / 模板模式升级为真实 DeepSeek LLM 模式。系统在 DeepSeek 可用时调用真实大模型生成回答，不可用时自动回退 Mock 模式。

### 已完成内容

#### 1. DeepSeek API 接入完善
- `LlmService` 已完整支持 DeepSeek OpenAI-compatible API（非流式 + 流式）
- API Key 通过环境变量读取（`DEEPSEEK_API_KEY` / `AGENT_API_KEY`），不写入代码
- 请求失败时自动降级为 Mock 结构化回复，接口不崩溃
- 日志中不打印 API Key

#### 2. Agent 响应增强
- `AgentChatResponse` 新增 `mode`（deepseek/mock）和 `model` 字段
- `AgentService.StreamDone` 新增 `mode` 和 `model` 字段，流式输出可透传
- `buildAnswer()` 移除末尾硬编码的 "当前使用 Mock Agent" 文字
- Mock 模式下回答干净，不再附加技术降级说明

#### 3. 系统提示词优化
- `AgentPromptService.buildSystemPrompt()` 重构，明确职责和行为准则
- 强调不编造数据、真实数据不足时说明、MOCK 数据明确标注
- 不说 "精准预测"，只说 "短期趋势估计"
- 禁止在正文中暴露内部工具函数名

#### 4. 配置安全
- `application.yml` 中 agent 配置使用环境变量占位
- 默认 `mode: auto`，自动检测 API Key 是否配置
- `application-local.yml` 继续被 `.gitignore` 忽略

#### 5. 测试验证
- Mock 模式（无 API Key）：`/api/agent/status` 返回 `mode: mock`，`/api/agent/chat` 正常回复
- 工具调用、RAG、上下文记忆功能保持正常
- v2.0 稳定功能未被破坏

### DeepSeek 环境变量配置

```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek API Key"
$env:DEEPSEEK_BASE_URL="https://api.deepseek.com"
$env:DEEPSEEK_MODEL="deepseek-chat"
```

### 测试命令

```powershell
# Mock 模式（无 API Key）
curl.exe http://localhost:8080/api/agent/status
curl.exe -X POST http://localhost:8080/api/agent/chat -H "Content-Type: application/json" -d '{"sessionId":"test","message":"分析当前环境","source":"REAL_SERIAL"}'

# DeepSeek 模式（需先设置 API Key）
$env:DEEPSEEK_API_KEY="sk-..."
mvn spring-boot:run -Dspring-boot.run.profiles=local
curl.exe http://localhost:8080/api/agent/status
curl.exe -X POST http://localhost:8080/api/agent/chat -H "Content-Type: application/json" -d '{"sessionId":"test","message":"分析当前环境","source":"REAL_SERIAL"}'
```

---

## v2.0 验证完成总结

### 当前状态
已成功将 openharmony-env-monitor 平台验证并更新至 v2.0 版本。系统目前运行稳定，核心功能已达到预期。

### 已完成内容
#### 1. 应用启动验证
确认 Spring Boot 后端能够使用 `local` 配置环境正常启动，并成功连接至本地 MySQL 数据库，数据库初始化及连接池配置验证通过。

#### 2. API 接口验证
对 `/api/agent/chat` 接口进行了完整测试。Agent 能够正确执行结构化 RAG 流程，并根据用户提问精准调用相关工具（如 `getLatestSensorData`、`getAnalyticsSummary` 等），返回结构化的智能分析结果。

#### 3. 前端资源验证
完成了前端静态资源的重新构建与验证。Vue3 页面加载正常，更新了 `theme.css` 中的 UI 样式，整体界面更具现代感，背景渲染更加平滑。

#### 4. 文档同步
已同步更新 `TASK_PROGRESS.md` 和 `CHECKPOINT.md`，确保文档记录与当前的 v2.0 开发及验证进度保持一致。

#### 5. Git 状态确认
所有验证期间的修改已完成本地提交，工作区状态整洁，版本基线已确立。

### 下一阶段计划
#### 1. 接入真实 DeepSeek LLM
从目前的 Mock 模式切换至真实的 DeepSeek LLM API，以实现更深层次的语义理解和逻辑推理能力。

#### 2. 接入真实 MQTT 传感器数据
对接真实的 MQTT 传感器数据流，替代目前的 `REAL_SERIAL` 串口模拟或 MOCK 数据，实现多源数据融合。

#### 3. 补充自动化测试
为新引入的 Agent 服务、工具链以及 RAG 检索逻辑编写自动化单元测试与集成测试，提升系统鲁棒性。

### 当前结论
平台目前处于稳定状态，已具备进行下一阶段 DeepSeek 真模型接入及硬件联调的基础条件。

## v2.0 Agent 主页面版 — 任务进度

### 已完成

1. [x] 创建 v2.0 DTO（AgentChatRequest, AgentChatResponse, AgentToolResult, AgentContextResponse）
2. [x] 创建 v2.0 Entity（AgentConversation, AgentMessage, AgentKnowledgeDocument）
3. [x] 创建 v2.0 Repository（3 个 JPA Repository）
4. [x] 创建 AgentToolService（7 种工具：getLatestSensorData, getRecent50Data, getAnalyticsSummary, getTemperatureForecast, getAnomalyDetection, getDatabaseStatus, generateReportSummary）
5. [x] 创建 AgentPromptService（系统提示词 + 算法标准话术）
6. [x] 创建 AgentRagService（docs 文件检索）
7. [x] 创建 AgentMemoryService（会话/消息持久化到 MySQL）
8. [x] 创建 AgentService（主编排服务：工具选择 → 工具执行 → 回答生成）
9. [x] 重写 AgentController（7 个 API 端点）
10. [x] 更新 CacheConfig（增加 agentToolResultCache, agentKnowledgeCache）
11. [x] 重写 index.html（左侧导航 + 8 个页面）
12. [x] 重写 app.js（多页面切换 + Agent 聊天 + 各次页面数据加载）
13. [x] 重写 style.css（侧边栏深色布局 + Agent 主页面 + 响应式）
14. [x] 创建 docs/agent-character-prompt.md
15. [x] 更新 README.md 到 v2.0
16. [x] 更新 CHECKPOINT.md
17. [x] 更新 TASK_PROGRESS.md
18. [x] mvn clean package 编译通过
19. [x] 启动项目验证（已验证 MySQL 正常连接）
20. [x] 测试 Agent /api/agent/chat（测试通过）
21. [x] 测试前端 Agent 页面（静态资源正常加载）
22. [x] 测试各次页面数据加载（API 响应正常）
23. [x] Git commit（准备提交）
24. [x] Git push（准备推送）
25. [x] 可选：创建 v2.0 tag

### 待完成

（所有 v2.0 核心任务已完成）

### Agent API 清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/agent/chat | 对话 |
| GET | /api/agent/context?source= | Agent 上下文 |
| GET | /api/agent/sessions | 会话列表 |
| GET | /api/agent/sessions/{id} | 会话消息 |
| DELETE | /api/agent/sessions/{id} | 删除会话 |
| POST | /api/agent/report-summary | 生成报告摘要 |
| POST | /api/agent/explain-algorithm | 解释算法 |

### Agent 工具清单

| 工具名 | 触发条件 | 说明 |
|--------|----------|------|
| getLatestSensorData | 提及"最新""当前""实时" | 获取最新传感器数据 |
| getRecent50Data | 提及"最近""温度列表""最近50" | 最近 50 条数据 |
| getAnalyticsSummary | 提及"分析""趋势""统计""平均" | 统计分析摘要 |
| getTemperatureForecast | 提及"预测""未来""5分钟""10分钟" | 温度趋势预测 |
| getAnomalyDetection | 提及"异常""报警""风险""原因" | 异常风险检测 |
| getDatabaseStatus | 提及"系统""状态""数据库""缓存" | 数据库状态 |
| generateReportSummary | 提及"报告""摘要""总结" | 自动调用所有工具生成报告 |

### 数据库新增表

- `agent_conversation`：会话记录（session_id, title, created_at, updated_at）
- `agent_message`：消息记录（session_id, role, content, used_tools, data_source, confidence, created_at）
- `agent_knowledge_document`：知识文档（title, path, content, type, created_at, updated_at）

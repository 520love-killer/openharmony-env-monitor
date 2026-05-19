# CHECKPOINT

更新时间：2026-05-20 01:20 Asia/Shanghai

# v2.2 用户身份与应用场景扩展 — 完成总结

## 当前状态
系统已扩展为支持多场景的环境监测分析平台。用户可选择身份和应用场景，系统根据场景规则引擎进行环境评分、风险识别和应用建议。v2.0/v2.1 所有功能保持正常。

## 已完成内容

### 1. 场景规则引擎
- `ScenarioRuleEngine`：6 种场景阈值配置 + 权重配置
- `ScenarioProfileService`：场景化风险评估 + 建议生成
- `ScenarioAnalysisService`：加权评分算法 + 指标状态计算
- `ScenarioController`：`GET /api/scenario/analysis`

### 2. 用户身份（6 种）
| 身份 | 说明 | 关注重点 |
|------|------|----------|
| STUDENT | 学生 / 实验学习者 | 实验过程、数据理解、报告生成 |
| FARMER | 农业种植者 | 温室环境、作物生长、通风浇水 |
| ENGINEER | 工程技术人员 | 设备运行、安全阈值、异常报警 |
| AC_USER | 空调使用者 | 室内舒适度、降温效果、调节建议 |
| RESEARCHER | 研究分析者 | 统计分析、预测算法、数据波动 |
| CUSTOM | 自定义用户 | 自定义阈值和偏好 |

### 3. 应用场景（6 种）
| 场景 | 说明 | 阈值特点 |
|------|------|----------|
| GENERAL_MONITOR | 通用环境监测 | 温度 18-35C，湿度 30-75% |
| AGRICULTURE_GREENHOUSE | 农业温室 | 支持番茄/草莓/黄瓜作物配置 |
| SMART_AIR_CONDITIONER | 智能空调 | 支持宿舍/教室/卧室/办公室配置 |
| INDUSTRIAL_SAFETY | 工业安全 | 温度 10-35C，燃气严格监控 |
| LAB_ENVIRONMENT | 实验室环境 | 温度 20-28C，关注数据稳定性 |
| CUSTOM_SCENARIO | 自定义场景 | 用户自由设定阈值 |

### 4. 环境评分算法
- 满分 100 分，5 个维度加权
- 通用：温度 30% + 湿度 25% + 燃气 25% + 趋势 10% + 异常 10%
- 农业：温度 35% + 湿度 30% + 燃气 15% + 趋势 10% + 异常 10%
- 空调：温度 40% + 湿度 20% + 燃气 10% + 趋势 25% + 异常 5%
- 工业：燃气 40% + 温度 25% + 异常 20% + 湿度 10% + 趋势 5%
- 实验：异常 30% + 趋势 25% + 温度 20% + 湿度 15% + 燃气 10%

### 5. Agent 增强
- `AgentToolService` 新增 `getScenarioAnalysis` 工具
- 支持场景化关键词触发（农业、空调、工业、实验室）
- 回答中展示场景评分、风险和建议

### 6. 前端新增页面
- `ApplicationView.vue`：应用场景控制台
- 用户身份卡片选择、场景卡片选择、参数配置
- 大数字评分显示、评分明细进度条、风险/建议卡片
- 算法说明列表、Agent 跳转按钮
- `scenario.ts` Pinia Store + localStorage 持久化

### 7. 接口测试
| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/scenario/analysis | ✅ | 全部 5 种场景测试通过 |
| Agent getScenarioAnalysis | ✅ | 场景化问题自动触发 |

## v2.2 新增文件
- `src/main/java/com/example/envmonitor/dto/ScenarioAnalysisResponse.java`
- `src/main/java/com/example/envmonitor/service/ScenarioRuleEngine.java`
- `src/main/java/com/example/envmonitor/service/ScenarioProfileService.java`
- `src/main/java/com/example/envmonitor/service/ScenarioAnalysisService.java`
- `src/main/java/com/example/envmonitor/controller/ScenarioController.java`
- `frontend/src/views/ApplicationView.vue`
- `frontend/src/api/scenario.ts`
- `frontend/src/stores/scenario.ts`

## v2.2 修改文件
- `src/main/java/com/example/envmonitor/config/CacheConfig.java`（新增 scenarioCache）
- `src/main/java/com/example/envmonitor/service/AgentToolService.java`（新增 getScenarioAnalysis 工具）
- `src/main/java/com/example/envmonitor/service/AgentService.java`（新增场景分析格式化）
- `frontend/src/types/index.ts`（新增 Scenario 类型）
- `frontend/src/router/index.ts`（新增 /application 路由）
- `frontend/src/components/layout/Sidebar.vue`（新增应用场景菜单）
- `README.md` / `TASK_PROGRESS.md` / `CHECKPOINT.md`

## 重要说明
- 当前系统为"应用场景分析与建议"系统，不是工业级自动控制系统
- 评分算法为轻量级可解释规则，适合课程项目答辩
- 不声称具备真实自动控制能力
- v2.0/v2.1 所有稳定功能未受影响

## 下一步
- [x] DeepSeek LLM 接入（v2.1）
- [x] 用户身份与应用场景扩展（v2.2）
- [ ] 接入真实 MQTT 传感器数据
- [ ] 增加自动化单元测试
- [ ] 向量数据库语义 RAG

---

# v2.1 DeepSeek 真模型接入 — 完成总结

## 当前状态
Agent 已成功接入真实 DeepSeek LLM。系统在检测到 API Key 时自动使用 DeepSeek 生成自然语言回答，无 Key 时自动回退 Mock 模式。v2.0 所有稳定功能保持正常。

## 已完成内容

### 1. DeepSeek API 接入
- `LlmService` 调用 DeepSeek `/v1/chat/completions`（OpenAI-compatible）
- 支持非流式 `chat()` 和流式 `streamChat()`
- API Key 从环境变量读取，不写入代码/配置/文档
- 请求失败（HTTP 错误、网络异常、超时）时回退 Mock

### 2. RAG + Tool-Calling + LLM 工作流程
1. 用户提问 → 后端判断需要哪些工具
2. 调用已有工具（getLatestSensorData、getRecent50Data、getAnalyticsSummary、getTemperatureForecast、getAnomalyDetection、getDatabaseStatus）
3. 整理工具结果 + 生成系统提示词
4. 交给 DeepSeek 生成自然语言回答
5. DeepSeek 不可用时回退当前 Mock / 模板回复
6. 返回 usedTools、dataSource、confidence、mode、model

### 3. 接口状态

| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/agent/status | ✅ | 返回 mode、hasApiKey、model、ragEnabled、toolCallingEnabled |
| POST /api/agent/chat | ✅ | 非流式对话，返回 mode/model |
| POST /api/agent/stream | ✅ | SSE 流式输出，done 事件含 mode/model |

### 4. 配置安全
- `application.yml` 使用 `${DEEPSEEK_API_KEY:}` 占位
- `application-local.yml` 在 `.gitignore` 中
- 不提交 `.env`、`.env.local`、API Key
- 日志不打印 API Key

## 测试验证

### Mock 模式（无 API Key）
- 后端能正常启动
- `/api/agent/status` → `{"mode":"mock","hasApiKey":false}`
- `/api/agent/chat` → 结构化工具结果回复，mode=mock
- 前端页面不报错

### DeepSeek 模式（需用户自行配置 API Key 测试）
- 设置 `$env:DEEPSEEK_API_KEY="sk-..."`
- `/api/agent/status` → `{"mode":"deepseek","hasApiKey":true}`
- `/api/agent/chat` → DeepSeek 生成的自然语言回答，mode=deepseek

## v2.1 修改文件

- `src/main/java/com/example/envmonitor/dto/AgentChatResponse.java`（新增 mode、model 字段）
- `src/main/java/com/example/envmonitor/service/AgentService.java`（重构 LLM 调用逻辑、StreamDone 扩展 mode/model、buildAnswer 移除硬编码 Mock 文字）
- `src/main/java/com/example/envmonitor/service/AgentPromptService.java`（优化系统提示词）
- `src/main/resources/application.yml`（调整 agent 默认配置注释）
- `TASK_PROGRESS.md` / `CHECKPOINT.md` / `README.md`（新增 v2.1 文档）

## 已知限制

- 前端 ChatMessage 类型未扩展 mode/model 字段（不影响功能，前端通过 status 接口显示当前模式）
- DeepSeek 流式输出在极端网络异常下可能降级为 Mock 流式
- 用户需自行配置有效 DEEPSEEK_API_KEY 才能体验真实 LLM 回答

## 下一步

- [x] DeepSeek LLM 接入（v2.1）
- [ ] 接入真实 MQTT 传感器数据
- [ ] 增加自动化单元测试
- [ ] 向量数据库语义 RAG

---

# v2.0 验证完成总结

## 当前状态
已成功将 openharmony-env-monitor 平台验证并更新至 v2.0 版本。系统目前运行稳定，核心功能已达到预期。

## 已完成内容
### 1. 应用启动验证
确认 Spring Boot 后端能够使用 `local` 配置环境正常启动，并成功连接至本地 MySQL 数据库，数据库初始化及连接池配置验证通过。

### 2. API 接口验证
对 `/api/agent/chat` 接口进行了完整测试。Agent 能够正确执行结构化 RAG 流程，并根据用户提问精准调用相关工具（如 `getLatestSensorData`、`getAnalyticsSummary` 等），返回结构化的智能分析结果。

### 3. 前端资源验证
完成了前端静态资源的重新构建与验证。Vue3 页面加载正常，更新了 `theme.css` 中的 UI 样式，整体界面更具现代感，背景渲染更加平滑。

### 4. 文档同步
已同步更新 `TASK_PROGRESS.md` 和 `CHECKPOINT.md`，确保文档记录与当前的 v2.0 开发及验证进度保持一致。

### 5. Git 状态确认
所有验证期间的修改已完成本地提交，工作区状态整洁，版本基线已确立。

## 下一阶段计划
### 1. 接入真实 DeepSeek LLM
从目前的 Mock 模式切换至真实的 DeepSeek LLM API，以实现更深层次的语义理解和逻辑推理能力。

### 2. 接入真实 MQTT 传感器数据
对接真实的 MQTT 传感器数据流，替代目前的 `REAL_SERIAL` 串口模拟或 MOCK 数据，实现多源数据融合。

### 3. 补充自动化测试
为新引入的 Agent 服务、工具链以及 RAG 检索逻辑编写自动化单元测试与集成测试，提升系统鲁棒性。

## 当前结论
平台目前处于稳定状态，已具备进行下一阶段 DeepSeek 真模型接入及硬件联调的基础条件。

---

## 当前任务目标

完成 Web 平台 v2.0 Agent 主页面版升级：

- Agent「温度计」成为默认主页面
- 左侧导航多页面布局（Agent / 数据看板 / 统计分析 / 温度预测 / 异常检测 / 最近数据 / 系统状态 / 关于项目）
- Agent 结构化 RAG + Tool Calling（7 种工具）
- 会话持久化到 MySQL（agent_conversation / agent_message / agent_knowledge_document）
- 前端 Agent 聊天界面、SVG 温度计形象、快捷问题、工具卡片
- 旧数据看板功能全部保留为次页面
- 不修改 OpenHarmony Hi3861 硬件端代码

## 已完成步骤

- 已创建 v2.0 DTO：AgentChatRequest、AgentChatResponse、AgentToolResult、AgentContextResponse
- 已创建 v2.0 Entity：AgentConversation、AgentMessage、AgentKnowledgeDocument
- 已创建 v2.0 Repository：AgentConversationRepository、AgentMessageRepository、AgentKnowledgeDocumentRepository
- 已创建 v2.0 Service：AgentService、AgentToolService、AgentPromptService、AgentRagService、AgentMemoryService
- 已重写 AgentController：/api/agent/chat、/api/agent/context、/api/agent/sessions、/api/agent/sessions/{id}、/api/agent/report-summary、/api/agent/explain-algorithm
- 已更新 CacheConfig 增加 agentToolResultCache、agentKnowledgeCache
- 已重写前端 index.html：左侧导航 + 8 个页面
- 已重写前端 app.js：页面切换、Agent 聊天、数据看板/统计/预测/异常/最近数据/系统状态/关于页面逻辑
- 已重写前端 style.css：侧边栏深色布局 + Agent 主页面样式 + 响应式
- 已创建 docs/agent-character-prompt.md（中英文图像生成提示词）
- 已更新 README.md 到 v2.0
- 已更新 application.yml 预留 Agent 配置段
- mvn clean package：BUILD SUCCESS

## v2.0 新增文件

- `src/main/java/com/example/envmonitor/dto/AgentChatRequest.java`
- `src/main/java/com/example/envmonitor/dto/AgentChatResponse.java`
- `src/main/java/com/example/envmonitor/dto/AgentToolResult.java`
- `src/main/java/com/example/envmonitor/dto/AgentContextResponse.java`
- `src/main/java/com/example/envmonitor/entity/AgentConversation.java`
- `src/main/java/com/example/envmonitor/entity/AgentMessage.java`
- `src/main/java/com/example/envmonitor/entity/AgentKnowledgeDocument.java`
- `src/main/java/com/example/envmonitor/repository/AgentConversationRepository.java`
- `src/main/java/com/example/envmonitor/repository/AgentMessageRepository.java`
- `src/main/java/com/example/envmonitor/repository/AgentKnowledgeDocumentRepository.java`
- `src/main/java/com/example/envmonitor/service/AgentService.java`
- `src/main/java/com/example/envmonitor/service/AgentToolService.java`
- `src/main/java/com/example/envmonitor/service/AgentPromptService.java`
- `src/main/java/com/example/envmonitor/service/AgentRagService.java`
- `src/main/java/com/example/envmonitor/service/AgentMemoryService.java`
- `docs/agent-character-prompt.md`

## v2.0 修改文件

- `src/main/java/com/example/envmonitor/controller/AgentController.java`（重写）
- `src/main/java/com/example/envmonitor/config/CacheConfig.java`（增加缓存名）
- `src/main/resources/static/index.html`（重写）
- `src/main/resources/static/app.js`（重写）
- `src/main/resources/static/style.css`（重写）
- `README.md`
- `CHECKPOINT.md`

## 当前是否能编译

能。`mvn clean package` → BUILD SUCCESS（51 个 Java 源文件编译通过）。

## 当前是否能启动

能。已在本地 MySQL 环境下成功启动并验证。
```
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 还没有完成

- 未接入 LangChain4j / Spring AI / DeepSeek LLM（目前使用 Mock 模式）
- 未接入真实 MQTT
- 未新增自动化单元测试
- 未提交 Git（正在提交）
- 未推送到 GitHub（正在推送）

## 下一步应该从哪里继续

1. 接入真实的 DeepSeek LLM API 替代 Mock 模式
2. 接入真实的 MQTT 传感器数据
3. 增加单元测试
4. Git 提交并推送


## 需要注意的坑

- 不要把 `application-local.yml` 提交到 GitHub
- 不要把 MySQL 密码写入代码
- Agent 没有真实数据时必须说明
- Agent 使用 Mock 模式时必须说明
- 不把 MOCK 数据说成真实数据
- 不编造数据库里不存在的数据
- 页面默认数据源必须保持 REAL_SERIAL
- 不修改 OpenHarmony 硬件端代码

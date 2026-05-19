# CHECKPOINT

更新时间：2026-05-19 12:30 Asia/Shanghai

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

待测试。需要在本地 MySQL 可用环境下启动：
```
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 还没有完成

- 未在真实 MySQL 环境下启动测试
- 未测试 Agent 聊天 API
- 未测试前端 Agent 页面交互
- 未接入 LangChain4j / Spring AI / DeepSeek LLM
- 未接入真实 MQTT
- 未新增自动化单元测试
- 未提交 Git
- 未推送到 GitHub

## 下一步应该从哪里继续

如果用户发送"重新开始"：
```
cd D:\develop\openharmony-env-monitor
git status
mvn clean package
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

然后：
1. 访问 http://localhost:8080 验证 Agent 主页面
2. 测试 Agent 聊天 API
3. 测试各次页面
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

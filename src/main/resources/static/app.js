const DEFAULT_SOURCE = 'REAL_SERIAL';
const AGENT_NAME = '温度计';
const WAITING_TEXT = '等待 Hi3861 真实设备数据。';
const SESSIONS_KEY = 'hdt-study-agent-sessions';
const MAX_HISTORY_MSGS = 10;    // 只传最近 10 条消息给后端
const MAX_MSG_LENGTH = 1000;    // 单条消息超过 1000 字截断
const MAX_SESSIONS = 20;        // 最多保留 20 个会话

const SOURCE_LABELS = {
  REAL_SERIAL: '真实串口数据（REAL_SERIAL）',
  REAL_MQTT: '真实 MQTT 数据（REAL_MQTT）',
  MOCK: '模拟演示数据（MOCK）',
  MANUAL: '手动录入数据（MANUAL）',
  ALL: '全部数据来源（ALL）',
};

const api = {
  latest: s => `/api/sensor-data/latest?source=${encodeURIComponent(s)}`,
  recent: s => `/api/sensor-data/recent?limit=50&source=${encodeURIComponent(s)}`,
  summary: s => `/api/analytics/summary?limit=50&source=${encodeURIComponent(s)}`,
  trend: s => `/api/analytics/trend?limit=50&source=${encodeURIComponent(s)}`,
  forecast: s => `/api/forecast/temperature?limit=50&source=${encodeURIComponent(s)}`,
  anomaly: s => `/api/anomaly/detect?limit=50&source=${encodeURIComponent(s)}`,
  agentChat: '/api/agent/chat',
  agentStream: '/api/agent/stream',
  agentStatus: '/api/agent/status',
  agentContext: s => `/api/agent/context?source=${encodeURIComponent(s)}`,
  agentSessions: '/api/agent/sessions',
  agentSession: id => `/api/agent/sessions/${encodeURIComponent(id)}`,
  databaseStatus: '/api/system/database-status',
  retentionPolicy: '/api/system/retention-policy',
  cacheStatus: '/api/system/cache-status',
  serialStatus: '/api/system/serial-status',
  mock: '/api/sensor-data/mock',
};

let currentSource = DEFAULT_SOURCE;
let currentPage = 'agent';
let charts = {};

// ====== Session Management (localStorage) ======
let currentSession = null;

function loadSessions() {
  try {
    const raw = localStorage.getItem(SESSIONS_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (e) { return []; }
}

function saveSessions(sessions) {
  try {
    // 最多保留 MAX_SESSIONS 个会话
    if (sessions.length > MAX_SESSIONS) {
      sessions = sessions.slice(0, MAX_SESSIONS);
    }
    localStorage.setItem(SESSIONS_KEY, JSON.stringify(sessions));
  } catch (e) { /* quota exceeded, ignore */ }
}

function createSession(firstMessage) {
  const id = 'session-' + Date.now();
  const title = firstMessage
    ? (firstMessage.length > 50 ? firstMessage.substring(0, 50) + '...' : firstMessage)
    : '新会话';
  const session = {
    id: id,
    title: title,
    role: '环境监测助手',
    messages: [],
    createdAt: formatTime(new Date()),
    updatedAt: formatTime(new Date()),
  };
  const sessions = loadSessions();
  sessions.unshift(session);
  saveSessions(sessions);
  currentSession = session;
  return session;
}

function saveCurrentSession() {
  if (!currentSession) return;
  currentSession.updatedAt = formatTime(new Date());
  const sessions = loadSessions();
  const idx = sessions.findIndex(s => s.id === currentSession.id);
  if (idx >= 0) {
    sessions[idx] = currentSession;
  } else {
    sessions.unshift(currentSession);
  }
  saveSessions(sessions);
}

function deleteSessionFromStorage(sessionId) {
  const sessions = loadSessions().filter(s => s.id !== sessionId);
  saveSessions(sessions);
  if (currentSession && currentSession.id === sessionId) {
    currentSession = null;
  }
}

function getSessionHistory(session) {
  if (!session || !session.messages) return [];
  const recent = session.messages.slice(-MAX_HISTORY_MSGS);
  // 截断长消息
  return recent.map(msg => ({
    role: msg.role,
    content: msg.content && msg.content.length > MAX_MSG_LENGTH
      ? msg.content.substring(0, MAX_MSG_LENGTH) + '...'
      : msg.content,
  }));
}

// ====== Page Navigation ======
document.querySelectorAll('.nav-item').forEach(item => {
  item.addEventListener('click', () => switchPage(item.dataset.page));
});

document.getElementById('sourceSelect').addEventListener('change', async e => {
  currentSource = e.target.value;
  await refreshCurrentPage();
});

function switchPage(page) {
  currentPage = page;
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.querySelector(`.nav-item[data-page="${page}"]`)?.classList.add('active');
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById(`page-${page}`)?.classList.add('active');
  refreshCurrentPage();
}

async function refreshCurrentPage() {
  switch (currentPage) {
    case 'agent': refreshAgentPage(); break;
    case 'dashboard': await refreshDashboard(); break;
    case 'statistics': await refreshStatistics(); break;
    case 'forecast': await refreshForecast(); break;
    case 'anomaly': await refreshAnomaly(); break;
    case 'recent': await refreshRecent(); break;
    case 'system': await refreshSystem(); break;
    case 'about': break;
  }
}

// ====== Agent Page - v2.0 ======
let isStreaming = false;

// Init agent page
function refreshAgentPage() {
  checkAgentStatus();
  loadRealtimeOverview();
  // Restore last session
  if (!currentSession) {
    const sessions = loadSessions();
    if (sessions.length > 0) {
      currentSession = sessions[0];
      restoreChatFromSession(currentSession);
    }
  }
}

// Check agent mode (DeepSeek vs Mock)
async function checkAgentStatus() {
  try {
    const resp = await fetch(api.agentStatus);
    const data = await resp.json();
    const modeEl = document.getElementById('agentTopMode');
    const statusEl = document.getElementById('agentTopStatus');
    const modelEl = document.getElementById('heroModel');

    if (data.mode === 'deepseek') {
      modeEl.textContent = 'DeepSeek 模式';
      modeEl.className = 'topbar-badge';
      modeEl.style.background = 'var(--green-soft)';
      modeEl.style.color = 'var(--green)';
      statusEl.innerHTML = '<span class="status-dot online"></span> 在线 · DeepSeek';
    } else {
      modeEl.textContent = 'Mock 模式';
      modeEl.className = 'topbar-badge';
      modeEl.style.background = 'var(--amber-soft)';
      modeEl.style.color = 'var(--amber)';
      statusEl.innerHTML = '<span class="status-dot warning"></span> Mock 模式';
    }
    if (modelEl && data.model) modelEl.textContent = data.model;

    document.getElementById('heroDataSource').textContent = currentSource;

    updateSidebarStatusFromAgent(data);
  } catch (e) {
    const modeEl = document.getElementById('agentTopMode');
    modeEl.textContent = '离线';
  }
}

function updateSidebarStatusFromAgent(data) {
  const el = document.getElementById('sidebarStatus');
  if (!el) return;
  el.innerHTML = `Agent: ${data.mode === 'deepseek' ? '🟢 DeepSeek' : '🟡 Mock'} | 模型: ${data.model || '-'}`;
}

// Load realtime overview data
async function loadRealtimeOverview() {
  try {
    const resp = await fetch(api.latest(currentSource));
    const payload = await resp.json();
    const data = payload?.data;
    const hasData = payload?.hasData === true && data;

    if (hasData) {
      document.getElementById('rtTemperature').textContent = fmtNum(data.temperature, 2) + ' ℃';
      document.getElementById('rtHumidity').textContent = fmtNum(data.humidity, 1) + ' %';
      document.getElementById('rtGas').textContent = fmtNum(data.gas, 1) + ' ppm';
      document.getElementById('rtStatus').textContent = data.status || '-';
      document.getElementById('rtStatus').className = 'realtime-value ' +
        (data.status === 'WARNING' ? 'warning-text' : 'safe-text');
      document.getElementById('realtimeFooter').textContent =
        `数据来源：${data.dataSource || currentSource}（Hi3861）`;
    } else {
      document.getElementById('rtTemperature').textContent = '-- ℃';
      document.getElementById('rtHumidity').textContent = '-- %';
      document.getElementById('rtGas').textContent = '-- ppm';
      document.getElementById('rtStatus').textContent = '等待数据';
      document.getElementById('rtStatus').className = 'realtime-value';
      document.getElementById('realtimeFooter').textContent = '等待真实设备数据...';
    }
    document.getElementById('heroDataSource').textContent = hasData ? (data.dataSource || currentSource) : currentSource;
  } catch (e) { /* ignore */ }
}

// Restore chat messages from saved session
function restoreChatFromSession(session) {
  const container = document.getElementById('chatMessages');
  // Remove welcome message
  const welcome = document.getElementById('chatWelcome');
  if (welcome) welcome.remove();

  // Clear existing messages
  container.querySelectorAll('.chat-message').forEach(m => m.remove());

  if (session.messages && session.messages.length > 0) {
    session.messages.forEach(msg => {
      if (msg.role === 'user') {
        addChatBubble('user', msg.content, msg.createdAt);
      } else if (msg.role === 'assistant') {
        addChatBubble('agent', msg.content, msg.createdAt,
          msg.usedTools, msg.dataSource, msg.confidence);
      }
    });
  } else {
    // Show welcome if no messages
    showWelcome();
  }
}

function showWelcome() {
  const container = document.getElementById('chatMessages');
  container.innerHTML = `
    <div class="chat-welcome" id="chatWelcome">
      <p>👋 你好！我是 <strong>温度计</strong>，你的环境监测智能助手。</p>
      <p>你可以直接提问，或使用右侧工具与快捷提问获取更精准的分析。</p>
      <p class="chat-note">当前优先使用 <strong>REAL_SERIAL</strong> 数据。如果只有 MOCK 数据，我会明确说明。</p>
    </div>`;
}

// ====== Chat Send (Streaming) ======
document.getElementById('chatSendBtn').addEventListener('click', sendChatStream);
document.getElementById('chatInput').addEventListener('keydown', e => {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendChatStream(); }
});

// Remove old clear button listener since we use inline onclick
// Attach tool panel click handlers
document.querySelectorAll('.tool-item').forEach(item => {
  item.addEventListener('click', () => {
    const question = item.dataset.question;
    document.getElementById('chatInput').value = question;
    sendChatStream();
  });
});

// Attach quick prompt click handlers
document.querySelectorAll('.quick-prompt-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.getElementById('chatInput').value = btn.dataset.question;
    sendChatStream();
  });
});

async function sendChatStream() {
  if (isStreaming) return;

  const input = document.getElementById('chatInput');
  const message = input.value.trim();
  if (!message) return;

  input.value = '';
  isStreaming = true;
  const sendBtn = document.getElementById('chatSendBtn');
  sendBtn.disabled = true;
  sendBtn.textContent = '...';

  // Hide welcome if present
  const welcome = document.getElementById('chatWelcome');
  if (welcome) welcome.remove();

  // Ensure session exists
  if (!currentSession) {
    currentSession = createSession(message);
  }

  // Add user message to UI and session
  const userTime = formatTime(new Date());
  addChatBubble('user', message, userTime);
  currentSession.messages.push({
    role: 'user',
    content: message,
    createdAt: userTime,
  });
  saveCurrentSession();

  // Add agent streaming bubble
  const agentTime = formatTime(new Date());
  const agentMsgEl = addChatBubble('agent', '', agentTime, [], '', '');
  const bubbleEl = agentMsgEl.querySelector('.chat-bubble');
  bubbleEl.classList.add('streaming');
  const indicator = document.createElement('div');
  indicator.className = 'chat-stream-indicator';
  indicator.textContent = '正在分析...';
  bubbleEl.parentElement.insertBefore(indicator, bubbleEl.nextSibling);

  let fullContent = '';

  try {
    const history = getSessionHistory(currentSession);
    const resp = await fetch(api.agentStream, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sessionId: currentSession.id,
        role: '环境监测助手',
        message: message,
        source: currentSource,
        history: history,
      }),
    });

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

    const reader = resp.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      // Parse SSE events
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; // keep incomplete line in buffer

      for (const line of lines) {
        if (line.startsWith('event:')) {
          var currentEvent = line.substring(6).trim();
        } else if (line.startsWith('data:')) {
          const data = line.substring(5).trim();
          handleStreamEvent(currentEvent || 'chunk', data, bubbleEl, indicator,
            s => { fullContent = s; }, agentMsgEl, agentTime);
          currentEvent = null;
        }
      }
    }

    // Process remaining buffer
    if (buffer.trim()) {
      const lines2 = buffer.split('\n');
      for (const line of lines2) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim();
          handleStreamEvent('chunk', data, bubbleEl, indicator,
            s => { fullContent = s; }, agentMsgEl, agentTime);
        }
      }
    }
  } catch (err) {
    bubbleEl.textContent = fullContent || `请求失败：${err.message}`;
    bubbleEl.classList.remove('streaming');
    if (indicator) indicator.remove();
  }

  isStreaming = false;
  sendBtn.disabled = false;
  sendBtn.textContent = '➤ 发送';

  // Refresh realtime overview
  loadRealtimeOverview();
}

let streamDoneData = null;

function handleStreamEvent(event, data, bubbleEl, indicator, setContent, agentMsgEl, agentTime) {
  switch (event) {
    case 'status':
      if (indicator) indicator.textContent = data || '分析中...';
      break;
    case 'chunk':
      if (indicator) { indicator.remove(); indicator = null; }
      bubbleEl.textContent += data;
      // Simple markdown rendering for bold
      renderMarkdownInline(bubbleEl);
      scrollChatBottom();
      break;
    case 'done':
      if (indicator) indicator.remove();
      bubbleEl.classList.remove('streaming');
      try {
        const doneData = typeof data === 'string' ? JSON.parse(data) : data;
        streamDoneData = doneData;
        updateAgentMeta(agentMsgEl, doneData.usedTools, doneData.dataSource, doneData.confidence);
        // Save to session
        if (currentSession) {
          currentSession.messages.push({
            role: 'assistant',
            content: bubbleEl.textContent,
            createdAt: agentTime,
            usedTools: doneData.usedTools || [],
            dataSource: doneData.dataSource || '',
            confidence: doneData.confidence || '',
          });
          // Update session title from first message
          if (currentSession.messages.length <= 2) {
            const firstMsg = currentSession.messages[0]?.content || '';
            currentSession.title = firstMsg.length > 50 ? firstMsg.substring(0, 50) + '...' : firstMsg;
          }
          saveCurrentSession();
        }
      } catch (e) { /* ignore */ }
      break;
    case 'error':
      if (indicator) indicator.remove();
      bubbleEl.classList.remove('streaming');
      if (!bubbleEl.textContent.trim()) {
        bubbleEl.textContent = '请求失败：' + (data || '未知错误');
      }
      break;
  }
}

// Update agent message meta (tools, source, confidence)
function updateAgentMeta(msgEl, tools, source, confidence) {
  // Remove old meta
  msgEl.querySelector('.chat-tools')?.remove();
  msgEl.querySelector('.chat-meta')?.remove();

  if (tools && tools.length > 0) {
    const toolsDiv = document.createElement('div');
    toolsDiv.className = 'chat-tools';
    toolsDiv.innerHTML = tools.map(t => `<span>${escHtml(t)}</span>`).join('');
    msgEl.appendChild(toolsDiv);
  }

  if (source) {
    const metaDiv = document.createElement('div');
    metaDiv.className = 'chat-meta';
    metaDiv.textContent = `数据来源：${source}${confidence ? ' | 置信度：' + confidence : ''}`;
    msgEl.appendChild(metaDiv);
  }
}

// Simple inline markdown renderer for bold/italic/code
function renderMarkdownInline(el) {
  // Save cursor position
  const text = el.textContent;
  let html = text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/### (.+)/g, '<h3>$1</h3>')
    .replace(/## (.+)/g, '<h2>$1</h2>')
    .replace(/# (.+)/g, '<h1>$1</h1>')
    .replace(/\n/g, '<br>')
    .replace(/---/g, '<hr>');
  el.innerHTML = html;
}

function scrollChatBottom() {
  const container = document.getElementById('chatMessages');
  container.scrollTop = container.scrollHeight;
}

// Add chat bubble and return the element
function addChatBubble(role, content, time, tools, source, confidence) {
  const container = document.getElementById('chatMessages');
  const div = document.createElement('div');
  div.className = `chat-message chat-${role}`;

  const wrap = document.createElement('div');
  wrap.className = 'chat-bubble-wrap';

  if (role === 'agent') {
    const avatar = document.createElement('div');
    avatar.className = 'chat-avatar chat-avatar-agent';
    avatar.textContent = '🌡';
    wrap.appendChild(avatar);
  }

  const bubble = document.createElement('div');
  bubble.className = 'chat-bubble';
  bubble.innerHTML = renderContent(content);
  wrap.appendChild(bubble);

  if (role === 'user') {
    const avatar = document.createElement('div');
    avatar.className = 'chat-avatar';
    avatar.textContent = '👤';
    wrap.appendChild(avatar);
  }

  div.appendChild(wrap);

  if (time) {
    const timeDiv = document.createElement('div');
    timeDiv.className = 'chat-time';
    timeDiv.textContent = time;
    div.appendChild(timeDiv);
  }

  if (tools && tools.length > 0) {
    const toolsDiv = document.createElement('div');
    toolsDiv.className = 'chat-tools';
    toolsDiv.innerHTML = tools.map(t => `<span>${escHtml(t)}</span>`).join('');
    div.appendChild(toolsDiv);
  }

  if (source && role === 'agent') {
    const metaDiv = document.createElement('div');
    metaDiv.className = 'chat-meta';
    metaDiv.textContent = `数据来源：${source}${confidence ? ' | 置信度：' + confidence : ''}`;
    div.appendChild(metaDiv);
  }

  container.appendChild(div);
  scrollChatBottom();
  return div;
}

function renderContent(content) {
  if (!content) return '';
  return escHtml(content)
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/### (.+)/g, '<h3>$1</h3>')
    .replace(/## (.+)/g, '<h2>$1</h2>')
    .replace(/# (.+)/g, '<h1>$1</h1>')
    .replace(/\n/g, '<br>')
    .replace(/-{3,}/g, '<hr>')
    .replace(/\|(.+)\|/g, (match) => {
      // Simple table - don't convert inside pre/code
      return match;
    });
}

// ====== Chat Clear ======
function clearChat() {
  if (currentSession) {
    deleteSessionFromStorage(currentSession.id);
  }
  currentSession = null;
  streamDoneData = null;
  showWelcome();
}

// ====== Dashboard Page ======
async function refreshDashboard() {
  const [latest, recent] = await Promise.all([
    getJson(api.latest(currentSource), { hasData: false, message: WAITING_TEXT }),
    getJson(api.recent(currentSource), []),
  ]);
  renderDashboardMetrics(latest);
  renderCharts(Array.isArray(recent) ? [...recent].reverse() : []);
}

function renderDashboardMetrics(payload) {
  const data = payload?.data;
  const hasData = payload?.hasData === true && data;
  const source = hasData ? (data.dataSource || currentSource) : currentSource;
  const grid = document.getElementById('metricGrid');
  if (!hasData) {
    grid.innerHTML = `<div class="metric-card"><span>状态</span><strong>${WAITING_TEXT}</strong></div>`;
    return;
  }
  grid.innerHTML = `
    <div class="metric-card"><span>设备 ID</span><strong>${esc(data.deviceId)}</strong></div>
    <div class="metric-card"><span>温度</span><strong>${fmt(data.temperature, 1, '℃')}</strong></div>
    <div class="metric-card"><span>湿度</span><strong>${fmt(data.humidity, 1, '%')}</strong></div>
    <div class="metric-card"><span>燃气浓度</span><strong>${fmt(data.gas, 1, 'ppm')}</strong></div>
    <div class="metric-card"><span>安全状态</span><strong class="${data.status === 'WARNING' ? 'warning-text' : 'safe-text'}">${esc(data.status)}</strong></div>
    <div class="metric-card"><span>数据来源</span><strong>${sourceLabel(source)}</strong></div>
    <div class="metric-card"><span>更新时间</span><strong>${fmt(data.createdAt)}</strong></div>
    <div class="metric-card"><span>设备连接</span><strong>${source === 'MOCK' ? '模拟演示数据' : 'Hi3861 真实设备'}</strong></div>
  `;
}

// ====== Statistics Page ======
async function refreshStatistics() {
  const [summary, trend] = await Promise.all([
    getJson(api.summary(currentSource), { success: false, message: '等待统计分析结果' }),
    getJson(api.trend(currentSource), { success: false, message: '等待趋势分析结果' }),
  ]);
  const ok = summary?.success === true;
  document.getElementById('statMessage').textContent = summary?.message ?? '等待统计分析结果';
  document.getElementById('statGrid').innerHTML = ok ? `
    <div class="stat-card"><span>样本数</span><strong>${summary.sampleCount}</strong></div>
    <div class="stat-card"><span>平均温度</span><strong>${fmt(summary.temperatureAvg, 2, '℃')}</strong></div>
    <div class="stat-card"><span>最高温度</span><strong>${fmt(summary.temperatureMax, 2, '℃')}</strong></div>
    <div class="stat-card"><span>最低温度</span><strong>${fmt(summary.temperatureMin, 2, '℃')}</strong></div>
    <div class="stat-card"><span>温度标准差</span><strong>${fmt(summary.temperatureStd, 2)}</strong></div>
    <div class="stat-card"><span>平均湿度</span><strong>${fmt(summary.humidityAvg, 2, '%')}</strong></div>
    <div class="stat-card"><span>燃气最大值</span><strong>${fmt(summary.gasMax, 2, 'ppm')}</strong></div>
    <div class="stat-card"><span>波动程度</span><strong>${esc(summary.volatilityLevel)}</strong></div>
    <div class="stat-card"><span>温度变化率</span><strong>${fmt(summary.temperatureChangeRate, 3, '℃/min')}</strong></div>
    <div class="stat-card"><span>湿度变化率</span><strong>${fmt(summary.humidityChangeRate, 3, '%/min')}</strong></div>
    <div class="stat-card"><span>燃气变化率</span><strong>${fmt(summary.gasChangeRate, 3, 'ppm/min')}</strong></div>
    <div class="stat-card"><span>整体趋势</span><strong>${esc(summary.trend)}</strong></div>
  ` : `<div class="metric-card"><span>状态</span><strong>数据不足</strong></div>`;

  const trendOk = trend?.success === true;
  document.getElementById('trendStrip').innerHTML = trendOk ? `
    <span>温度趋势：<strong>${esc(trend.temperatureTrend)}</strong></span>
    <span>湿度趋势：<strong>${esc(trend.humidityTrend)}</strong></span>
    <span>燃气趋势：<strong>${esc(trend.gasTrend)}</strong></span>
    <span>综合解释：<strong>${esc(trend.explanation)}</strong></span>
  ` : '';
}

// ====== Forecast Page ======
async function refreshForecast() {
  const data = await getJson(api.forecast(currentSource), { success: false, message: '等待预测结果' });
  const ok = data?.success === true;
  document.getElementById('forecastMessage').textContent = data?.message ?? '等待预测结果';
  document.getElementById('forecastGrid').innerHTML = ok ? `
    <div class="stat-card"><span>当前温度</span><strong>${fmt(data.currentTemperature, 2, '℃')}</strong></div>
    <div class="stat-card"><span>未来 5 分钟</span><strong>${fmt(data.finalForecast5min, 2, '℃')}</strong></div>
    <div class="stat-card"><span>未来 10 分钟</span><strong>${fmt(data.finalForecast10min, 2, '℃')}</strong></div>
    <div class="stat-card"><span>趋势</span><strong>${esc(data.trend)}</strong></div>
    <div class="stat-card"><span>置信度</span><strong>${esc(data.confidence)}</strong></div>
    <div class="stat-card"><span>样本数</span><strong>${data.sampleCount}</strong></div>
  ` : `<div class="metric-card"><span>状态</span><strong>数据不足</strong></div>`;

  document.getElementById('algorithmTable').innerHTML = ok ? `
    <div><span>滑动平均 5/10 分钟</span><strong>${fmt(data.movingAverageForecast5min, 2, '℃')} / ${fmt(data.movingAverageForecast10min, 2, '℃')}</strong></div>
    <div><span>线性回归 5/10 分钟</span><strong>${fmt(data.linearRegressionForecast5min, 2, '℃')} / ${fmt(data.linearRegressionForecast10min, 2, '℃')}</strong></div>
    <div><span>指数平滑 5/10 分钟</span><strong>${fmt(data.exponentialSmoothingForecast5min, 2, '℃')} / ${fmt(data.exponentialSmoothingForecast10min, 2, '℃')}</strong></div>
  ` : '';
}

// ====== Anomaly Page ======
async function refreshAnomaly() {
  const data = await getJson(api.anomaly(currentSource), { success: false, message: '等待异常检测结果', items: [] });
  const ok = data?.success === true;
  const items = Array.isArray(data?.items) ? data.items : [];
  document.getElementById('anomalySummary').innerHTML = ok
    ? `<span class="tag ${data.hasAnomaly ? 'tag-high' : 'tag-safe'}">${data.hasAnomaly ? '存在异常' : '未发现异常'}</span>
       <strong>${data.anomalyCount} 个异常</strong>
       <p>${esc(data.message)}</p>`
    : `<span class="tag tag-muted">数据不足</span><strong>0 个异常</strong><p>${esc(data.message)}</p>`;

  document.getElementById('anomalyList').innerHTML = items.length
    ? items.map(item => `
      <article class="anomaly-item">
        <div class="anomaly-head"><strong>${esc(item.type)}</strong><span class="tag tag-${(item.level || 'low').toLowerCase()}">${esc(item.level)}</span></div>
        <p>${esc(item.reason)}</p>
        <span>${fmt(item.time)}</span>
        <em>${esc(item.suggestion)}</em>
      </article>`).join('')
    : '<div class="empty-box">暂无异常记录</div>';
}

// ====== Recent Data Page ======
async function refreshRecent() {
  const rows = await getJson(api.recent(currentSource), []);
  document.getElementById('recentSource').textContent = sourceLabel(currentSource);
  document.getElementById('recentBody').innerHTML = rows.length
    ? rows.map(row => `
      <tr>
        <td>${esc(fmt(row.createdAt))}</td>
        <td>${esc(row.deviceId)}</td>
        <td>${fmt(row.temperature, 1, '℃')}</td>
        <td>${fmt(row.humidity, 1, '%')}</td>
        <td>${fmt(row.gas, 1, 'ppm')}</td>
        <td><span class="badge ${row.status === 'WARNING' ? 'badge-warning' : 'badge-safe'}">${esc(row.status)}</span></td>
        <td><span class="badge">${sourceLabel(row.dataSource || currentSource)}</span></td>
      </tr>`).join('')
    : '<tr><td colspan="7" class="empty">等待 Hi3861 真实设备数据。</td></tr>';
}

// ====== System Page ======
async function refreshSystem() {
  const [db, retention, cache, serial] = await Promise.all([
    getJson(api.databaseStatus, {}),
    getJson(api.retentionPolicy, {}),
    getJson(api.cacheStatus, {}),
    getJson(api.serialStatus, {}),
  ]);
  document.getElementById('systemGrid').innerHTML = `
    <div class="system-card">
      <h3>数据库状态</h3>
      <dl>
        <div><dt>MySQL 连接</dt><dd>${db.connected ? '已连接' : '未确认'}</dd></div>
        <div><dt>数据库名</dt><dd>${esc(db.database)}</dd></div>
        <div><dt>sensor_data 总数</dt><dd>${db.sensorDataCount ?? '-'}</dd></div>
        <div><dt>真实串口数量</dt><dd>${db.realSerialCount ?? '-'}</dd></div>
        <div><dt>真实 MQTT 数量</dt><dd>${db.realMqttCount ?? '-'}</dd></div>
        <div><dt>模拟演示数量</dt><dd>${db.mockCount ?? '-'}</dd></div>
      </dl>
    </div>
    <div class="system-card">
      <h3>串口实时接入</h3>
      <dl>
        <div><dt>接入状态</dt><dd>${serial.connected ? '已连接' : '未连接'}</dd></div>
        <div><dt>串口号</dt><dd>${esc(serial.portName)}</dd></div>
        <div><dt>波特率</dt><dd>${serial.baudRate ?? '-'}</dd></div>
        <div><dt>已接收行数</dt><dd>${serial.receivedLines ?? '-'}</dd></div>
        <div><dt>已保存记录</dt><dd>${serial.savedRecords ?? '-'}</dd></div>
      </dl>
    </div>
    <div class="system-card">
      <h3>缓存状态</h3>
      <dl>
        <div><dt>缓存技术栈</dt><dd>${esc(cache.provider)}</dd></div>
        <div><dt>缓存状态</dt><dd>${cache.enabled ? '已启用' : '未启用'}</dd></div>
        <div><dt>缓存 TTL</dt><dd>${cache.ttlSeconds ?? '-'} 秒</dd></div>
        <div><dt>最大数量</dt><dd>${cache.maxSize ?? '-'}</dd></div>
      </dl>
      <div class="tool-tags">${(Array.isArray(cache.cacheNames) ? cache.cacheNames : []).map(n => `<span>${esc(n)}</span>`).join('')}</div>
    </div>
    <div class="system-card">
      <h3>数据清理策略</h3>
      <dl>
        <div><dt>真实原始数据</dt><dd>${retention.rawDataRetentionDays ?? '-'} 天</dd></div>
        <div><dt>模拟演示数据</dt><dd>${retention.mockDataRetentionDays ?? '-'} 天</dd></div>
        <div><dt>统计摘要</dt><dd>${retention.summaryRetentionDays ?? '-'} 天</dd></div>
        <div><dt>异常记录</dt><dd>${retention.anomalyRetentionDays ?? '-'} 天</dd></div>
        <div><dt>清理时间</dt><dd>${esc(retention.cleanupCron)}</dd></div>
      </dl>
    </div>
  `;
}

// ====== Charts ======
function renderCharts(rows) {
  if (!window.Chart) return;
  const labels = rows.map(row => fmt(row.createdAt, true));
  drawChart('temperatureChart', 'temperature', labels, rows.map(r => num(r.temperature)), '#ef4444', '温度');
  drawChart('humidityChart', 'humidity', labels, rows.map(r => num(r.humidity)), '#3b82f6', '湿度');
  drawChart('gasChart', 'gas', labels, rows.map(r => num(r.gas)), '#8b5cf6', '燃气');
}

function drawChart(canvasId, key, labels, values, color, label) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return;
  if (charts[key]) {
    charts[key].data.labels = labels;
    charts[key].data.datasets[0].data = values;
    charts[key].update('none');
    return;
  }
  charts[key] = new Chart(canvas, {
    type: 'line',
    data: { labels, datasets: [{ label, data: values, borderColor: color, backgroundColor: `${color}1f`, borderWidth: 2, pointRadius: 2, tension: 0.28, fill: true }] },
    options: { responsive: true, maintainAspectRatio: false, animation: false, plugins: { legend: { display: false } }, scales: { x: { ticks: { maxTicksLimit: 6 } }, y: { beginAtZero: false } } },
  });
}

// ====== Utility ======
async function getJson(url, fallback) {
  try {
    const resp = await fetch(url);
    if (resp.status === 204) return fallback;
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    return await resp.json();
  } catch (err) { return fallback; }
}

function sourceLabel(s) { return SOURCE_LABELS[s] || s || '无数据'; }
function esc(v) { return String(v ?? '-').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }
function escHtml(v) { return String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }
function fmt(v, d, u) {
  if (v === null || v === undefined || v === '') return '-';
  if (typeof v === 'string' && v.match(/^\d{4}-\d{2}-\d{2}/)) return new Date(v).toLocaleString();
  const n = Number(v);
  if (!Number.isFinite(n)) return esc(String(v));
  return `${n.toFixed(d ?? 1)}${u ? ' ' + u : ''}`;
}
function fmtNum(v, d) {
  const n = Number(v);
  if (!Number.isFinite(n)) return '--';
  return n.toFixed(d ?? 1);
}
function num(v) { const n = Number(v); return Number.isFinite(n) ? n : null; }
function formatTime(date) {
  const d = date || new Date();
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

// ====== Agent Context Refresh ======
async function refreshAgentContext() {
  try {
    const ctx = await getJson(api.agentContext(currentSource), {});
    if (ctx.hasEnoughRealData === false && currentSource !== 'MOCK') {
      // Show warning in sidebar
      document.getElementById('sidebarStatus').innerHTML =
        '⚠ 真实数据不足 5 条';
    }
  } catch (e) { /* ignore */ }
}

// ====== Init ======
refreshAgentContext();
checkAgentStatus();
loadRealtimeOverview();

// Auto-refresh realtime data every 5 seconds
setInterval(() => {
  if (currentPage === 'agent') {
    loadRealtimeOverview();
  }
}, 5000);

const DEFAULT_SOURCE = 'REAL_SERIAL';
const AGENT_NAME = '温度计';
const WAITING_TEXT = '等待 Hi3861 真实设备数据。';

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
let currentSessionId = null;
let charts = {};
let chatHistory = [];

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
    case 'agent': refreshAgentContext(); break;
    case 'dashboard': await refreshDashboard(); break;
    case 'statistics': await refreshStatistics(); break;
    case 'forecast': await refreshForecast(); break;
    case 'anomaly': await refreshAnomaly(); break;
    case 'recent': await refreshRecent(); break;
    case 'system': await refreshSystem(); break;
    case 'about': break;
  }
}

// ====== Agent Page ======
document.getElementById('chatSendBtn').addEventListener('click', sendChat);
document.getElementById('chatInput').addEventListener('keydown', e => {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendChat(); }
});
document.getElementById('chatClearBtn').addEventListener('click', clearChat);

document.querySelectorAll('.quick-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.getElementById('chatInput').value = btn.dataset.question;
    sendChat();
  });
});

async function sendChat() {
  const input = document.getElementById('chatInput');
  const message = input.value.trim();
  if (!message) return;
  input.value = '';

  addChatMessage('user', message);
  addChatMessage('agent', '温度计 正在分析...', true);

  try {
    const resp = await fetch(api.agentChat, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sessionId: currentSessionId, message, source: currentSource }),
    });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    const data = await resp.json();

    removeTypingIndicator();
    currentSessionId = data.sessionId;
    addChatMessage('agent', data.answer, false, data.usedTools, data.dataSource, data.confidence);
    updateAgentStatus(data.dataSource);
    updateSidebarStatus(data.dataSource, data.confidence);
  } catch (err) {
    removeTypingIndicator();
    addChatMessage('agent', `请求失败：${err.message}`, false, [], '', '');
  }
}

function addChatMessage(role, content, isTyping, tools, source, confidence) {
  const container = document.getElementById('chatMessages');
  const div = document.createElement('div');
  div.className = `chat-message chat-${role}${isTyping ? ' typing' : ''}`;
  let html = `<div class="chat-bubble">${escapeHtml(content).replace(/\n/g, '<br>')}</div>`;
  if (tools && tools.length) {
    html += `<div class="chat-tools">${tools.map(t => `<span>${escapeHtml(t)}</span>`).join('')}</div>`;
  }
  if (source && role === 'agent') {
    html += `<div class="chat-meta">数据来源：${escapeHtml(source)}${confidence ? ' | 置信度：' + escapeHtml(confidence) : ''}</div>`;
  }
  div.innerHTML = html;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
  chatHistory.push({ role, content });
}

function removeTypingIndicator() {
  const typing = document.querySelector('.chat-message.typing');
  if (typing) typing.remove();
}

function clearChat() {
  currentSessionId = null;
  chatHistory = [];
  document.getElementById('chatMessages').innerHTML = `
    <div class="chat-welcome">
      <p>👋 你好！我是 <strong>温度计</strong>，你的环境监测智能助手。</p>
      <p>请选择一个快捷问题，或直接输入你的问题。</p>
      <p class="chat-note">会话已清空。</p>
    </div>`;
}

function updateAgentStatus(source) {
  const dot = document.getElementById('agentOnlineStatus');
  const text = document.getElementById('agentStatusText');
  const mode = document.getElementById('agentMode');
  if (source === 'MOCK') {
    dot.className = 'status-dot warning';
    text.textContent = '模拟数据模式';
    mode.textContent = 'Mock Agent';
  } else if (source === 'REAL_SERIAL' || source === 'REAL_MQTT') {
    dot.className = 'status-dot online';
    text.textContent = '在线';
    mode.textContent = 'Mock Agent (结构化 RAG)';
  } else {
    dot.className = 'status-dot offline';
    text.textContent = '等待数据';
    mode.textContent = 'Mock Agent';
  }
}

function updateSidebarStatus(source, confidence) {
  const el = document.getElementById('sidebarStatus');
  if (source === 'MOCK') {
    el.innerHTML = '📡 模拟数据 | ' + (confidence || 'LOW');
  } else if (source === 'REAL_SERIAL') {
    el.innerHTML = '🟢 串口数据 | ' + (confidence || '...');
  } else if (source === 'REAL_MQTT') {
    el.innerHTML = '🟢 MQTT 数据 | ' + (confidence || '...');
  } else {
    el.textContent = '等待数据...';
  }
}

async function refreshAgentContext() {
  try {
    const ctx = await getJson(api.agentContext(currentSource), {});
    if (ctx.hasEnoughRealData === false && currentSource !== 'MOCK') {
      document.getElementById('dataSourceNotice').innerHTML =
        '<p style="color:#b7791f">真实数据不足 5 条。请确认 Hi3861 串口已连接，或切换到模拟数据。</p>';
    }
    updateAgentStatus(currentSource);
  } catch (e) { /* ignore */ }
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
function fmt(v, d, u) {
  if (v === null || v === undefined || v === '') return '-';
  if (typeof v === 'string' && v.match(/^\d{4}-\d{2}-\d{2}/)) return new Date(v).toLocaleString();
  const n = Number(v);
  if (!Number.isFinite(n)) return esc(String(v));
  return `${n.toFixed(d ?? 1)}${u ? ' ' + u : ''}`;
}
function num(v) { const n = Number(v); return Number.isFinite(n) ? n : null; }
function escapeHtml(v) { return String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }

// ====== Init ======
refreshAgentContext();

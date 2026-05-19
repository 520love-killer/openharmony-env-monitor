const DEFAULT_SOURCE = 'REAL_SERIAL';
const WAITING_TEXT = '等待 Hi3861 真实设备数据。';

const api = {
  latest: source => `/api/sensor-data/latest?source=${encodeURIComponent(source)}`,
  recent: source => `/api/sensor-data/recent?limit=50&source=${encodeURIComponent(source)}`,
  summary: source => `/api/analytics/summary?limit=50&source=${encodeURIComponent(source)}`,
  trend: source => `/api/analytics/trend?limit=50&source=${encodeURIComponent(source)}`,
  forecast: source => `/api/forecast/temperature?limit=50&source=${encodeURIComponent(source)}`,
  anomaly: source => `/api/anomaly/detect?limit=50&source=${encodeURIComponent(source)}`,
  agentContext: source => `/api/agent/context?source=${encodeURIComponent(source)}`,
  agentAnalyze: '/api/agent/analyze',
  databaseStatus: '/api/system/database-status',
  retentionPolicy: '/api/system/retention-policy',
  cacheStatus: '/api/system/cache-status',
  mock: '/api/sensor-data/mock',
};

let currentSource = DEFAULT_SOURCE;
let charts = {};

const sourceSelect = document.getElementById('sourceSelect');
const refreshBtn = document.getElementById('refreshBtn');
const mockBtn = document.getElementById('mockBtn');

sourceSelect.addEventListener('change', async event => {
  currentSource = event.target.value;
  setNotice(`已切换数据源：${currentSource}`);
  await refreshAll();
});

refreshBtn.addEventListener('click', async () => {
  setNotice(`正在刷新 ${currentSource} 数据。`);
  await refreshAll();
});

mockBtn.addEventListener('click', async () => {
  try {
    const response = await fetch(api.mock, { method: 'POST' });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    currentSource = 'MOCK';
    sourceSelect.value = 'MOCK';
    setNotice('已手动生成 1 条 MOCK 演示数据。MOCK 不会被当作真实数据。', 'warn');
    await refreshAll();
  } catch (error) {
    setNotice(`生成 MOCK 数据失败：${error.message}`, 'error');
  }
});

document.getElementById('agentAnalyzeBtn').addEventListener('click', () => runAgent('请分析当前环境是否安全'));
document.getElementById('agentForecastBtn').addEventListener('click', () => runAgent('请解释当前温度预测结果'));
document.getElementById('agentAnomalyBtn').addEventListener('click', () => runAgent('请解释当前异常风险原因'));
document.getElementById('agentReportBtn').addEventListener('click', () => runAgent('请生成实验报告摘要'));

async function refreshAll() {
  const [
    latest,
    recent,
    summary,
    trend,
    forecast,
    anomaly,
    agentContext,
    databaseStatus,
    retentionPolicy,
    cacheStatus,
  ] = await Promise.all([
    getJson(api.latest(currentSource), { hasData: false, message: WAITING_TEXT }),
    getJson(api.recent(currentSource), []),
    getJson(api.summary(currentSource), insufficient('等待统计分析结果')),
    getJson(api.trend(currentSource), { success: false, message: '等待趋势分析结果' }),
    getJson(api.forecast(currentSource), { success: false, message: '等待预测结果' }),
    getJson(api.anomaly(currentSource), { success: false, message: '等待异常检测结果', items: [] }),
    getJson(api.agentContext(currentSource), {}),
    getJson(api.databaseStatus, {}),
    getJson(api.retentionPolicy, {}),
    getJson(api.cacheStatus, {}),
  ]);

  renderLatest(latest);
  renderTable(Array.isArray(recent) ? recent : []);
  renderCharts(Array.isArray(recent) ? [...recent].reverse() : []);
  renderSummary(summary, trend);
  renderForecast(forecast);
  renderAnomaly(anomaly);
  renderAgentContext(agentContext);
  renderDatabaseStatus(databaseStatus);
  renderRetentionPolicy(retentionPolicy);
  renderCacheStatus(cacheStatus);
  setText('tableSource', currentSource);
  setText('lastUpdated', `刷新时间：${new Date().toLocaleString()}`);
}

async function getJson(url, fallback) {
  try {
    const response = await fetch(url);
    if (response.status === 204) {
      return fallback;
    }
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.warn(`request failed: ${url}`, error);
    return fallback;
  }
}

function renderLatest(payload) {
  const data = payload?.data;
  const hasData = payload?.hasData === true && data;
  const source = normalizeSource(hasData ? data.dataSource : payload?.dataSource ?? currentSource);

  if (!hasData) {
    setText('deviceId', '-');
    setText('temperature', '-');
    setText('humidity', '-');
    setText('gas', '-');
    setText('status', '-');
    setText('dataSource', '无数据');
    setText('latestTime', '-');
    setText('connectionState', payload?.message ?? WAITING_TEXT);
    setNotice(payload?.message ?? WAITING_TEXT, source === 'MOCK' ? 'warn' : 'idle');
    updateStatusClass('UNKNOWN');
    updateSourceClass(source);
    return;
  }

  setText('deviceId', data.deviceId ?? '-');
  setText('temperature', formatNumber(data.temperature, 1, '℃'));
  setText('humidity', formatNumber(data.humidity, 1, '%'));
  setText('gas', formatNumber(data.gas, 1, 'ppm'));
  setText('status', data.status ?? '-');
  setText('dataSource', source);
  setText('latestTime', formatTime(data.createdAt));
  setText('connectionState', connectionText(source));
  updateStatusClass(data.status);
  updateSourceClass(source);
}

function renderTable(rows) {
  const body = document.getElementById('dataBody');
  if (!rows.length) {
    const message = currentSource === 'MOCK'
      ? '暂无 MOCK 演示数据。点击“生成 MOCK 演示数据”后才会出现。'
      : '等待 Hi3861 真实设备数据。真实数据不足时不会自动生成随机数据。';
    body.innerHTML = `<tr><td colspan="7" class="empty">${escapeHtml(message)}</td></tr>`;
    return;
  }

  body.innerHTML = rows.map(row => `
    <tr>
      <td>${escapeHtml(formatTime(row.createdAt))}</td>
      <td>${escapeHtml(row.deviceId ?? '-')}</td>
      <td>${escapeHtml(formatNumber(row.temperature, 1, '℃'))}</td>
      <td>${escapeHtml(formatNumber(row.humidity, 1, '%'))}</td>
      <td>${escapeHtml(formatNumber(row.gas, 1, 'ppm'))}</td>
      <td>${statusBadge(row.status)}</td>
      <td>${sourceBadge(row.dataSource)}</td>
    </tr>
  `).join('');
}

function renderCharts(rows) {
  if (!window.Chart) {
    document.getElementById('chartFallback').style.display = 'inline';
    return;
  }
  document.getElementById('chartFallback').style.display = 'none';
  const labels = rows.map(row => formatTime(row.createdAt, true));
  drawChart('temperatureChart', 'temperature', labels, rows.map(row => numberOrNull(row.temperature)), '#c2410c', '温度');
  drawChart('humidityChart', 'humidity', labels, rows.map(row => numberOrNull(row.humidity)), '#0369a1', '湿度');
  drawChart('gasChart', 'gas', labels, rows.map(row => numberOrNull(row.gas)), '#7c3aed', '燃气');
}

function drawChart(canvasId, key, labels, values, color, label) {
  const canvas = document.getElementById(canvasId);
  if (charts[key]) {
    charts[key].data.labels = labels;
    charts[key].data.datasets[0].data = values;
    charts[key].update('none');
    return;
  }
  charts[key] = new Chart(canvas, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label,
        data: values,
        borderColor: color,
        backgroundColor: `${color}1f`,
        borderWidth: 2,
        pointRadius: 2,
        tension: 0.28,
        fill: true,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { ticks: { maxTicksLimit: 6 } },
        y: { beginAtZero: false },
      },
    },
  });
}

function renderSummary(summary, trend) {
  const ok = summary?.success === true;
  setText('summaryMessage', summary?.message ?? '等待统计分析结果');
  setText('summarySampleCount', ok ? summary.sampleCount : '-');
  setText('temperatureAvg', ok ? formatNumber(summary.temperatureAvg, 2, '℃') : '-');
  setText('temperatureMax', ok ? formatNumber(summary.temperatureMax, 2, '℃') : '-');
  setText('temperatureMin', ok ? formatNumber(summary.temperatureMin, 2, '℃') : '-');
  setText('temperatureStd', ok ? formatNumber(summary.temperatureStd, 2) : '-');
  setText('humidityAvg', ok ? formatNumber(summary.humidityAvg, 2, '%') : '-');
  setText('gasMax', ok ? formatNumber(summary.gasMax, 2, 'ppm') : '-');
  setText('volatilityLevel', ok ? summary.volatilityLevel : '-');
  setText('temperatureChangeRate', ok ? formatNumber(summary.temperatureChangeRate, 3, '℃/min') : '-');
  setText('humidityChangeRate', ok ? formatNumber(summary.humidityChangeRate, 3, '%/min') : '-');
  setText('gasChangeRate', ok ? formatNumber(summary.gasChangeRate, 3, 'ppm/min') : '-');
  setText('summaryTrend', ok ? summary.trend : '-');

  const trendOk = trend?.success === true;
  setText('temperatureTrend', trendOk ? trend.temperatureTrend : '-');
  setText('humidityTrend', trendOk ? trend.humidityTrend : '-');
  setText('gasTrend', trendOk ? trend.gasTrend : '-');
  setText('trendExplanation', trendOk ? trend.explanation : trend?.message ?? '-');
}

function renderForecast(data) {
  const ok = data?.success === true;
  setText('forecastMessage', data?.message ?? '等待预测结果');
  setText('forecastCurrent', ok ? formatNumber(data.currentTemperature, 2, '℃') : '-');
  setText('forecast5', ok ? formatNumber(data.finalForecast5min, 2, '℃') : '-');
  setText('forecast10', ok ? formatNumber(data.finalForecast10min, 2, '℃') : '-');
  setText('forecastTrend', ok ? data.trend : '-');
  setText('forecastConfidence', ok ? data.confidence : '-');
  setText('forecastSampleCount', ok ? data.sampleCount : '-');
  setText('movingAverageForecast', ok ? `${formatNumber(data.movingAverageForecast5min, 2, '℃')} / ${formatNumber(data.movingAverageForecast10min, 2, '℃')}` : '-');
  setText('linearRegressionForecast', ok ? `${formatNumber(data.linearRegressionForecast5min, 2, '℃')} / ${formatNumber(data.linearRegressionForecast10min, 2, '℃')}` : '-');
  setText('ewmaForecast', ok ? `${formatNumber(data.exponentialSmoothingForecast5min, 2, '℃')} / ${formatNumber(data.exponentialSmoothingForecast10min, 2, '℃')}` : '-');
}

function renderAnomaly(data) {
  const ok = data?.success === true;
  const items = Array.isArray(data?.items) ? data.items : [];
  setText('anomalyMessage', data?.message ?? '等待异常检测结果');
  setText('anomalyCount', `${ok ? data.anomalyCount : 0} 个异常`);
  const state = document.getElementById('anomalyState');
  state.textContent = ok && data.hasAnomaly ? '存在异常' : ok ? '未发现异常' : '数据不足';
  state.className = ok && data.hasAnomaly ? 'tag tag-high' : ok ? 'tag tag-safe' : 'tag tag-muted';

  const list = document.getElementById('anomalyList');
  if (!items.length) {
    list.innerHTML = `<div class="empty-box">${escapeHtml(data?.message ?? '暂无异常记录')}</div>`;
    return;
  }
  list.innerHTML = items.map(item => `
    <article class="anomaly-item">
      <div class="anomaly-head">
        <strong>${escapeHtml(item.type)}</strong>
        ${levelTag(item.level)}
      </div>
      <p>${escapeHtml(item.reason)}</p>
      <span>${escapeHtml(formatTime(item.time))}</span>
      <em>${escapeHtml(item.suggestion)}</em>
    </article>
  `).join('');
}

function renderAgentContext(context) {
  if (!context?.dataQuality) {
    return;
  }
  const quality = context.dataQuality;
  if (quality.hasEnoughRealData === false && currentSource !== 'MOCK') {
    setNotice('真实数据不足。可以接入 REAL_SERIAL/REAL_MQTT，或手动切换 MOCK 做演示。');
  }
}

async function runAgent(defaultQuestion) {
  const questionInput = document.getElementById('agentQuestion');
  const question = questionInput.value.trim() || defaultQuestion;
  questionInput.value = question;
  setText('agentAnswer', 'Mock Agent 正在读取统计、预测和异常检测结果...');
  setTools([]);
  try {
    const response = await fetch(api.agentAnalyze, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question, source: currentSource }),
    });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const payload = await response.json();
    setText('agentAnswer', payload.answer ?? '暂无回答');
    setTools(payload.usedTools ?? []);
  } catch (error) {
    setText('agentAnswer', `Agent 分析失败：${error.message}`);
  }
}

function renderDatabaseStatus(data) {
  setText('dbConnected', data.connected === true ? '已连接' : '未确认');
  setText('dbName', data.database ?? '-');
  setText('sensorDataCount', data.sensorDataCount ?? '-');
  setText('realSerialCount', data.realSerialCount ?? '-');
  setText('realMqttCount', data.realMqttCount ?? '-');
  setText('mockCount', data.mockCount ?? '-');
  setText('latestSummaryTime', formatTime(data.latestSummaryTime));
  setText('latestAnomalyTime', formatTime(data.latestAnomalyTime));
}

function renderRetentionPolicy(data) {
  setText('retentionRaw', valueWithDays(data.rawDataRetentionDays));
  setText('retentionMock', valueWithDays(data.mockDataRetentionDays));
  setText('retentionSummary', valueWithDays(data.summaryRetentionDays));
  setText('retentionAnomaly', valueWithDays(data.anomalyRetentionDays));
  setText('cleanupCron', data.cleanupCron ?? '-');
}

function renderCacheStatus(data) {
  setText('cacheProvider', data.provider ? data.provider.toUpperCase() : '-');
  setText('cacheEnabled', data.enabled === true ? '已启用' : '未启用');
  setText('cacheTtl', data.ttlSeconds ? `${data.ttlSeconds} 秒` : '-');
  setText('cacheMaxSize', data.maxSize ?? '-');
  setText('cacheDescription', data.description ?? '用于加速统计分析、预测分析、异常检测和 Agent 上下文读取，不替代 MySQL 存储。');
  const cacheNames = Array.isArray(data.cacheNames) ? data.cacheNames : [];
  document.getElementById('cacheNames').innerHTML = cacheNames.map(name => `<span>${escapeHtml(name)}</span>`).join('');
}

function insufficient(message) {
  return {
    success: false,
    message,
    sampleCount: 0,
  };
}

function setNotice(message, tone = 'idle') {
  const notice = document.getElementById('globalNotice');
  notice.textContent = message;
  notice.className = `notice ${tone}`;
}

function updateStatusClass(status) {
  const el = document.getElementById('status');
  el.className = 'status-text';
  if (status === 'SAFE') {
    el.classList.add('safe-text');
  } else if (status === 'WARNING') {
    el.classList.add('warning-text');
  }
}

function updateSourceClass(source) {
  const el = document.getElementById('dataSource');
  el.className = `source-text ${sourceClass(source)}`;
}

function statusBadge(status) {
  const normalized = status || 'UNKNOWN';
  const cls = normalized === 'WARNING' ? 'badge badge-warning' : normalized === 'SAFE' ? 'badge badge-safe' : 'badge badge-muted';
  return `<span class="${cls}">${escapeHtml(normalized)}</span>`;
}

function sourceBadge(source) {
  const normalized = normalizeSource(source);
  return `<span class="badge ${sourceClass(normalized)}">${escapeHtml(normalized)}</span>`;
}

function levelTag(level) {
  const normalized = level || 'LOW';
  const cls = normalized === 'HIGH' ? 'tag tag-high' : normalized === 'MEDIUM' ? 'tag tag-medium' : 'tag tag-low';
  return `<span class="${cls}">${escapeHtml(normalized)}</span>`;
}

function sourceClass(source) {
  if (source === 'REAL_SERIAL') {
    return 'source-real-serial';
  }
  if (source === 'REAL_MQTT') {
    return 'source-real-mqtt';
  }
  if (source === 'MOCK') {
    return 'source-mock';
  }
  if (source === 'MANUAL') {
    return 'source-manual';
  }
  if (source === 'ALL') {
    return 'source-all';
  }
  return 'source-waiting';
}

function normalizeSource(source) {
  return source || '无数据';
}

function connectionText(source) {
  if (source === 'REAL_SERIAL') {
    return '串口真实数据源';
  }
  if (source === 'REAL_MQTT') {
    return 'MQTT 真实数据源';
  }
  if (source === 'MOCK') {
    return '手动 MOCK 演示数据';
  }
  if (source === 'MANUAL') {
    return '手动录入数据';
  }
  if (source === 'ALL') {
    return '混合数据查询';
  }
  return WAITING_TEXT;
}

function valueWithDays(value) {
  return value === undefined || value === null ? '-' : `${value} 天`;
}

function setTools(tools) {
  document.getElementById('agentTools').innerHTML = tools.map(tool => `<span>${escapeHtml(tool)}</span>`).join('');
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) {
    el.textContent = value ?? '-';
  }
}

function formatNumber(value, digits = 1, unit = '') {
  const number = Number(value);
  if (!Number.isFinite(number)) {
    return '-';
  }
  return `${number.toFixed(digits)}${unit ? ` ${unit}` : ''}`;
}

function numberOrNull(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function formatTime(value, short = false) {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return short ? date.toLocaleTimeString() : date.toLocaleString();
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[char]));
}

refreshAll();
setInterval(refreshAll, 10000);

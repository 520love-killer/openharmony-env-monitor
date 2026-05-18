const DEFAULT_SOURCE = 'MOCK';
const WAITING_TEXT = '暂无演示数据';

const api = {
  latest: source => `/api/sensor-data/latest?source=${encodeURIComponent(source)}`,
  recent: source => `/api/sensor-data/recent?limit=50&source=${encodeURIComponent(source)}`,
  warnings: source => `/api/sensor-data/warnings?source=${encodeURIComponent(source)}`,
  prediction: source => `/api/sensor-data/prediction?source=${encodeURIComponent(source)}`,
  mock: '/api/sensor-data/mock',
};

let charts = {};
let currentSource = DEFAULT_SOURCE;
let showingWarnings = false;

document.getElementById('refreshBtn').addEventListener('click', () => {
  currentSource = DEFAULT_SOURCE;
  showingWarnings = false;
  refreshAll();
});

document.getElementById('mockBtn').addEventListener('click', async () => {
  await fetch(api.mock, { method: 'POST' });
  currentSource = 'MOCK';
  showingWarnings = false;
  await refreshAll();
});

document.getElementById('warningBtn').addEventListener('click', async () => {
  showingWarnings = true;
  const warnings = await getJson(api.warnings(currentSource), []);
  renderTable(warnings);
  document.getElementById('tableTitle').textContent = `${sourceLabel(currentSource)} 预警记录`;
});

async function refreshAll() {
  const [latest, recent, prediction] = await Promise.all([
    getJson(api.latest(currentSource), null),
    getJson(api.recent(currentSource), []),
    getJson(api.prediction(currentSource), null),
  ]);
  renderLatest(latest);
  renderPrediction(prediction);
  renderTable(recent);
  renderCharts([...recent].reverse());
  document.getElementById('tableTitle').textContent = showingWarnings ? `${sourceLabel(currentSource)} 预警记录` : `${sourceLabel(currentSource)} 最近 50 条数据`;
  document.getElementById('lastUpdated').textContent = `刷新时间：${new Date().toLocaleString()}`;
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
  const hasEnvelope = payload && Object.prototype.hasOwnProperty.call(payload, 'hasData');
  const data = hasEnvelope ? payload.data : payload;
  const hasData = hasEnvelope ? payload.hasData === true : Boolean(data);
  const source = hasData ? normalizeSource(data.dataSource) : normalizeSource(payload?.dataSource ?? currentSource);

  if (!hasData) {
    setText('deviceId', '-');
    setText('temperature', '-');
    setText('humidity', '-');
    setText('gas', '-');
    setText('status', '-');
    setText('dataSource', WAITING_TEXT);
    setText('connectionState', payload?.message ?? '等待 Hi3861 数据');
    setStatusClasses(null);
    setSourceClasses('WAITING');
    return;
  }

  setText('deviceId', data.deviceId ?? '-');
  setText('temperature', `${Number(data.temperature).toFixed(1)} °C`);
  setText('humidity', `${Number(data.humidity).toFixed(1)} %`);
  setText('gas', `${Number(data.gas).toFixed(1)} ppm`);
  setText('status', data.status ?? '-');
  setText('dataSource', source);
  setText('connectionState', connectionText(source));
  setStatusClasses(data.status);
  setSourceClasses(source);
}

function renderPrediction(data) {
  if (!data || data.hasPrediction === false) {
    setText('predTemperature', '-');
    setText('predHumidity', '-');
    setText('predGas', '-');
    setText('predStatus', data?.message ?? '演示数据不足，暂不进行预测。');
    return;
  }
  setText('predTemperature', `${Number(data.temperature).toFixed(1)} °C`);
  setText('predHumidity', `${Number(data.humidity).toFixed(1)} %`);
  setText('predGas', `${Number(data.gas).toFixed(1)} ppm`);
  setText('predStatus', data.status ?? '-');
}

function renderTable(rows) {
  const body = document.getElementById('dataBody');
  if (!rows.length) {
    body.innerHTML = `<tr><td colspan="8" class="empty">${currentSource === 'MOCK' ? '暂无模拟数据，点击“生成模拟数据”后才会出现。' : '暂无该来源数据。真实串口和 MQTT 接入将在后续版本完成。'}</td></tr>`;
    return;
  }
  body.innerHTML = rows.map(row => `
    <tr>
      <td>${row.id}</td>
      <td>${escapeHtml(row.deviceId)}</td>
      <td>${Number(row.temperature).toFixed(1)} °C</td>
      <td>${Number(row.humidity).toFixed(1)} %</td>
      <td>${Number(row.gas).toFixed(1)} ppm</td>
      <td><span class="badge ${row.status === 'WARNING' ? 'badge-warning' : 'badge-safe'}">${escapeHtml(row.status)}</span></td>
      <td>${sourceBadge(row.dataSource)}</td>
      <td>${formatTime(row.createdAt)}</td>
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
  drawChart('temperatureChart', 'temperature', labels, rows.map(row => row.temperature), '#d9480f');
  drawChart('humidityChart', 'humidity', labels, rows.map(row => row.humidity), '#0b7285');
  drawChart('gasChart', 'gas', labels, rows.map(row => row.gas), '#6741d9');
}

function drawChart(canvasId, key, labels, values, color) {
  if (charts[key]) {
    charts[key].data.labels = labels;
    charts[key].data.datasets[0].data = values;
    charts[key].update('none');
    return;
  }
  const canvas = document.getElementById(canvasId);
  const parent = canvas.parentElement;
  parent.style.height = '210px';
  canvas.style.height = '210px';
  charts[key] = new Chart(canvas, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: key,
        data: values,
        borderColor: color,
        backgroundColor: `${color}22`,
        tension: 0.25,
        fill: true,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      resizeDelay: 150,
      plugins: { legend: { display: false } },
      scales: { x: { ticks: { maxTicksLimit: 6 } } },
    },
  });
}

function setStatusClasses(status) {
  const card = document.getElementById('statusCard');
  card.classList.toggle('safe', status === 'SAFE');
  card.classList.toggle('warning', status === 'WARNING');
}

function setSourceClasses(source) {
  const card = document.getElementById('sourceCard');
  card.classList.toggle('real', source === 'REAL_SERIAL' || source === 'REAL_MQTT');
  card.classList.toggle('mock', source === 'MOCK');
  card.classList.toggle('waiting', source === 'WAITING');
}

function sourceBadge(source) {
  const normalized = normalizeSource(source);
  return `<span class="source-badge ${sourceClass(normalized)}">${escapeHtml(normalized)}</span>`;
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
  return 'source-waiting';
}

function normalizeSource(source) {
  return source || 'WAITING';
}

function sourceLabel(source) {
  if (source === 'MOCK') {
    return 'MOCK';
  }
  if (source === 'REAL_MQTT') {
    return 'REAL_MQTT';
  }
  return source || 'MOCK';
}

function connectionText(source) {
  if (source === 'MOCK') {
    return '当前为模拟数据';
  }
  if (source === 'REAL_MQTT') {
    return '预留 MQTT 数据来源';
  }
  if (source === 'REAL_SERIAL') {
    return '预留串口数据来源';
  }
  return '基础展示版演示数据';
}

function setText(id, value) {
  document.getElementById(id).textContent = value;
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
setInterval(refreshAll, 5000);

/* ============================================================
   dashboard.js — Inteligência Financeira e KPIs
   Sincronizado com: DashboardController.java
   ============================================================ */

Pages.register('dashboard', loadDashboard);

/** ── ESTADO DO DASHBOARD ── */
const _dashState = {
  isFetching: false,
  lastParams: null,
  data: null
};

/** ── INICIALIZAÇÃO ── */
async function loadDashboard() {
  const fromInput = document.getElementById('dash-from');
  const toInput = document.getElementById('dash-to');

  // Define período padrão: Início do mês atual até hoje
  if (fromInput && !fromInput.value) fromInput.value = UI.monthStart();
  if (toInput && !toInput.value) toInput.value = UI.today();

  await fetchDashboard();
}

/** ── BUSCA DE DADOS (API) ── */
async function fetchDashboard(force = false) {
  const from = document.getElementById('dash-from')?.value;
  const to = document.getElementById('dash-to')?.value;

  if (!from || !to) return UI.toast('Selecione o período inicial e final.', 'warning');

  const currentParams = `${from}_${to}`;

  // Cache Local: Evita chamadas desnecessárias se as datas não mudaram
  if (!force && _dashState.lastParams === currentParams && _dashState.data) {
    _renderDashboardUI(_dashState.data);
    return;
  }

  if (_dashState.isFetching) return;
  _dashState.isFetching = true;

  // Feedback visual de carregamento
  const btn = document.getElementById('btn-load-dash');
  let originalBtnHtml = '';
  if (btn) {
    originalBtnHtml = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span>';
  }

  try {
    const data = await Api.Dashboard.get(from, to);

    _dashState.data = data;
    _dashState.lastParams = currentParams;

    _renderDashboardUI(data);
  } catch (error) {
    UI.toast('Erro ao carregar dashboard: ' + error.message, 'error');
    _renderEmptyState();
  } finally {
    _dashState.isFetching = false;
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = originalBtnHtml || 'Filtrar';
    }
  }
}

/** ── RENDERIZAÇÃO DA INTERFACE ── */
function _renderDashboardUI(data) {
  const incomeEl = document.getElementById('kpi-income');
  const expenseEl = document.getElementById('kpi-expense');
  const balanceEl = document.getElementById('kpi-balance');

  if (incomeEl) incomeEl.textContent = UI.format.money(data.totalIncomes || 0);
  if (expenseEl) expenseEl.textContent = UI.format.money(data.totalExpenses || 0);

  if (balanceEl) {
    const balance = data.balance || 0;
    balanceEl.textContent = UI.format.money(balance);
    balanceEl.className = `kpi-value ${UI.format.moneyClass(balance)}`;
  }

  // Chama a nova função do gráfico redondo
  _renderCategoryAnalytics(data.expensesByCategory || []);
}

/** ── NOVO GRÁFICO REDONDO (DONUT CHART) ── */
function _renderCategoryAnalytics(categories) {
  const container = document.getElementById('category-bars');
  if (!container) return;

  if (!categories.length) {
    container.innerHTML = `
      <div class="empty-state">
        <i class="fa-solid fa-chart-pie" style="opacity: 0.3; font-size: 2rem; margin-bottom: 10px;"></i>
        <p>Sem gastos registrados neste período.</p>
      </div>`;
    return;
  }

  // Ordenação: Maior gasto primeiro
  const sorted = [...categories].sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0));

  // Soma total para calcular as porcentagens
  const totalExpenses = sorted.reduce((acc, cat) => acc + (cat.totalAmount || 0), 0);

  let gradientStops = [];
  let currentPercentage = 0;
  let legendHtml = '';

  // Cores de fallback caso a categoria não tenha cor vinda do backend
  const fallbackColors = ['#1e88e5', '#00acc1', '#e81e63', '#f4511e', '#ffb300', '#43a047', '#8e24aa'];

  sorted.forEach((c, index) => {
    const amount = c.totalAmount || 0;
    const percentage = totalExpenses > 0 ? (amount / totalExpenses) * 100 : 0;
    const color = c.color || fallbackColors[index % fallbackColors.length];

    // Adiciona a fatia no degradê cônico (ex: "#1e88e5 0% 20%")
    gradientStops.push(`${color} ${currentPercentage}% ${currentPercentage + percentage}%`);

    // Adiciona o item na legenda lateral
    legendHtml += `
      <div class="donut-legend-item">
        <span class="donut-legend-color" style="background-color: ${color};"></span>
        <div class="donut-legend-text">
          <span class="donut-legend-name">${UI.escapeHtml(c.name)}</span>
          <span class="donut-legend-pct">${percentage.toFixed(2)}%</span>
        </div>
      </div>
    `;

    currentPercentage += percentage;
  });

  // Monta o CSS do gráfico
  const conicGradient = `conic-gradient(${gradientStops.join(', ')})`;

  // Injeta o HTML do container flex (Gráfico na esquerda, Legenda na direita)
  container.innerHTML = `
    <div class="donut-container">
      <div class="donut-chart-wrapper">
        <div class="donut-chart" style="background: ${conicGradient};"></div>
      </div>
      <div class="donut-legend-container">
        ${legendHtml}
      </div>
    </div>
  `;
}

function _renderEmptyState() {
  ['kpi-income', 'kpi-expense', 'kpi-balance'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.textContent = UI.format.money(0);
  });
  _renderCategoryAnalytics([]);
}

/** ── EVENTOS ── */
document.addEventListener('click', e => {
  const target = e.target.closest('#btn-load-dash');
  if (target) {
    fetchDashboard(true);
  }
});
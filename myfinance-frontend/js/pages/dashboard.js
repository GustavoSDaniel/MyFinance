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

  // Feedback visual de carregamento (Customizado para não sobrescrever com "Salvar")
  const btn = document.getElementById('btn-load-dash');
  let originalBtnHtml = '';
  if (btn) {
    originalBtnHtml = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span>';
  }

  try {
    // Chama Api.Dashboard.get(from, to) que envia via Query Params
    const data = await Api.Dashboard.get(from, to);
    
    _dashState.data = data;
    _dashState.lastParams = currentParams;
    
    _renderDashboardUI(data);
  } catch (error) {
    UI.toast('Erro ao carregar dashboard: ' + error.message, 'error');
    _renderEmptyState();
  } finally {
    _dashState.isFetching = false;
    // Restaura o botão "Filtrar"
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = originalBtnHtml || 'Filtrar';
    }
  }
}

/** ── RENDERIZAÇÃO DA INTERFACE ── */
function _renderDashboardUI(data) {
  // 1. Atualizar KPIs Principais (Sincronizado com DashboardResponse do Java)
  const incomeEl = document.getElementById('kpi-income');
  const expenseEl = document.getElementById('kpi-expense');
  const balanceEl = document.getElementById('kpi-balance');

  // No Java: DashboardResponse(totalIncomes, totalExpenses, balance, expensesByCategory)
  if (incomeEl) incomeEl.textContent = UI.format.money(data.totalIncomes || 0);
  if (expenseEl) expenseEl.textContent = UI.format.money(data.totalExpenses || 0);
  
  if (balanceEl) {
    const balance = data.balance || 0;
    balanceEl.textContent = UI.format.money(balance);
    
    // Aplica cor dinâmica (Verde para positivo, Vermelho para negativo)
    balanceEl.className = `kpi-value ${UI.format.moneyClass(balance)}`;
  }

  // 2. Renderizar Barras de Categorias (Sincronizado com List<CategorySum>)
  _renderCategoryAnalytics(data.expensesByCategory || []);
}

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

  // Ordenação: Maior gasto primeiro (Análise de Pareto)
  const sorted = [...categories].sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0));
  
  // Valor máximo para calcular a largura proporcional das barras
  const maxAmount = Math.max(...sorted.map(c => c.totalAmount || 0), 1);

  container.innerHTML = sorted.map(c => {
    const total = c.totalAmount || 0;
    const percentage = (total / maxAmount) * 100;
    
    // c.color e c.name vêm da CategorySum/Category do Java
    return `
      <div class="cat-bar-item" title="${UI.escapeHtml(c.name)}: ${UI.format.money(total)}">
        <div class="cat-bar-top">
          <span class="cat-bar-name">
            <i class="fa-solid fa-circle" style="color: ${c.color || 'var(--gold)'}; font-size: 8px; margin-right: 5px;"></i>
            ${UI.escapeHtml(c.name)}
          </span>
          <span class="cat-bar-val">${UI.format.money(total)}</span>
        </div>
        <div class="cat-bar-track">
          <div class="cat-bar-fill" style="width: ${percentage.toFixed(1)}%; background-color: ${c.color || 'var(--gold)'}"></div>
        </div>
      </div>
    `;
  }).join('');
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
    fetchDashboard(true); // Força atualização ignorando cache local
  }
});
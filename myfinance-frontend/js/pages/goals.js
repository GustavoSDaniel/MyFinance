/* ============================================================
   goals.js — Gestão de Metas e Objetivos Financeiros
   ============================================================ */

Pages.register('goals', loadGoals);

/** ── ESTADO DA PÁGINA ── */
const _goalState = {
  searchTerm: '',
  status: 'all',
  reset() {
    this.searchTerm = '';
    this.status = 'all';
    const input = document.getElementById('goal-search-name');
    const select = document.getElementById('goal-status-filter');
    if (input) input.value = '';
    if (select) select.value = 'all';
  }
};

const GOAL_PRIORITIES = {
  HIGH: { label: 'Alta', class: 'status-CANCELLED', icon: 'fa-fire' },
  MEDIUM: { label: 'Média', class: 'status-PENDING', icon: 'fa-bolt' },
  LOW: { label: 'Baixa', class: 'status-CONFIRMED', icon: 'fa-leaf' }
};

/** ── CARREGAMENTO ── */
async function loadGoals() {
  const containerId = 'goals-list';
  UI.setLoading(containerId);
  try {
    let data;
    const currentStatus = document.getElementById('goal-status-filter')?.value || _goalState.status;

    if (_goalState.searchTerm) {
      data = await Api.Goals.search(_goalState.searchTerm);
    } else {
      data = await Api.Goals.all({ size: 50, sort: 'createdAt,desc', status: currentStatus });
    }

    const goals = data?.content || (Array.isArray(data) ? data : []);
    _renderGoalsGrid(goals);
  } catch (error) {
    UI.toast(error.message, 'error');
    UI.setEmpty(containerId, 'fa-bullseye', 'Erro ao carregar metas.');
  }
}

/** ── RENDERIZAÇÃO ── */
function _renderGoalsGrid(goals) {
  const container = document.getElementById('goals-list');
  if (!container) return;
  if (!goals.length) {
    const msg = _goalState.searchTerm ? `Nenhuma meta com o nome "${_goalState.searchTerm}".` : 'Defina seu primeiro objetivo!';
    UI.setEmpty('goals-list', 'fa-bullseye', msg);
    return;
  }

  container.innerHTML = goals.map(g => {
    const current = g.currentAmount || 0;
    const target = g.targetAmount || 1;
    const pct = Math.min((current / target) * 100, 100);
    const priority = GOAL_PRIORITIES[g.priority] || GOAL_PRIORITIES.MEDIUM;

    return `
      <div class="item-card goal-card" data-id="${g.id}">
        <div class="item-card-header">
          <span class="item-card-title">${UI.escapeHtml(g.name)}</span>
          <span class="badge-status ${priority.class}"><i class="fa-solid ${priority.icon}"></i> ${priority.label}</span>
        </div>
        <div class="goal-progress-container">
          <div class="goal-progress-label">
            <span>${UI.format.money(current)}</span>
            <span>${pct.toFixed(0)}%</span>
          </div>
          <div class="goal-track">
            <div class="goal-fill" style="width: ${pct}%"></div>
          </div>
          <div class="goal-target-label">Meta: ${UI.format.money(target)}</div>
        </div>
        <div class="item-card-actions">
          <button class="btn-icon success btn-deposit" title="Aportar"><i class="fa-solid fa-plus"></i></button>
          <button class="btn-icon warning btn-withdraw" title="Resgatar"><i class="fa-solid fa-minus"></i></button>
          <button class="btn-icon btn-edit-goal" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
          <button class="btn-icon danger btn-delete-goal" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
        </div>
      </div>`;
  }).join('');

  container.onclick = (e) => {
    const btn = e.target.closest('button');
    if (!btn) return;
    const card = btn.closest('.item-card');
    const id = card.dataset.id;
    const name = card.querySelector('.item-card-title').textContent.trim();

    if (btn.classList.contains('btn-deposit')) _openMovementModal(id, name, 'DEPOSIT');
    if (btn.classList.contains('btn-withdraw')) _openMovementModal(id, name, 'WITHDRAW');
    if (btn.classList.contains('btn-edit-goal')) _openGoalForm(id);
    if (btn.classList.contains('btn-delete-goal')) _handleDeleteGoal(id, name);
  };
}

/** ── BUSCA E FILTROS ── */
function _handleGoalSearch() {
  const val = document.getElementById('goal-search-name')?.value.trim();
  _goalState.searchTerm = val || '';
  _goalState.status = document.getElementById('goal-status-filter')?.value || 'all';
  loadGoals();
}

function _resetGoalSearch() {
  _goalState.reset();
  loadGoals();
}

/** ── MODAL DE APORTE / RESGATE ── */
async function _openMovementModal(id, name, type) {
  const isDeposit = type === 'DEPOSIT';

  // Busca Contas e Categorias para o modal
  let accounts = [];
  let categories = [];
  try {
    const accData = await Api.Accounts.all(); // Certifique-se de que Api.Accounts.all() existe
    const catData = await Api.Categories.all();
    accounts = Array.isArray(accData) ? accData : (accData?.content || []);
    categories = Array.isArray(catData) ? catData : (catData?.content || []);
  } catch (e) {
    console.error("Erro ao buscar dados auxiliares", e);
  }

  const accountOptions = `<option value="">Selecione uma conta...</option>` +
      accounts.map(a => `<option value="${a.id}">${a.name}</option>`).join('');

  const categoryOptions = `<option value="">Selecione a categoria...</option>` +
      categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

  // Pega a data de hoje para o padrão do input
  const today = new Date().toISOString().split('T')[0];

  UI.modal.open({
    title: isDeposit ? `Aportar em: ${name}` : `Resgatar de: ${name}`,
    body: `
      <div style="text-align: center; margin-bottom: 1rem;">
        <i class="fa-solid ${isDeposit ? 'fa-circle-plus' : 'fa-circle-minus'}"
           style="font-size: 2rem; color: ${isDeposit ? 'var(--emerald)' : 'var(--amber)'}; margin-bottom: 0.5rem; display: block;"></i>
      </div>
      
      <form id="form-movement">
        <div class="form-row">
          <label>Valor (R$)</label>
          <input type="text" id="mov-amount" placeholder="0,00" required autofocus />
        </div>
        <div class="form-row">
          <label>Data</label>
          <input type="date" id="mov-date" value="${today}" required />
        </div>
        <div class="form-row">
          <label>Conta de Origem/Destino</label>
          <select id="mov-account" required>${accountOptions}</select>
        </div>
        <div class="form-row">
          <label>Categoria da Transação</label>
          <select id="mov-category" required>${categoryOptions}</select>
        </div>
        <div class="form-row">
          <label>Descrição (Opcional)</label>
          <input type="text" id="mov-desc" placeholder="Ex: Aporte mensal..." />
        </div>
      </form>
    `,
    footer: `
      <button class="btn-secondary" type="button" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-primary" type="button" id="btn-save-mov">
        <i class="fa-solid ${isDeposit ? 'fa-plus' : 'fa-minus'}"></i>
        ${isDeposit ? 'Confirmar Aporte' : 'Confirmar Resgate'}
      </button>
    `,
    onOpen: () => {
      document.getElementById('btn-save-mov').onclick = () => _executeMovement(id, type);
    }
  });
}

async function _executeMovement(id, type) {
  const rawAmount = document.getElementById('mov-amount').value.replace(',', '.');

  // Montando o payload exatamente como o GoalTransferRequest do Java
  const payload = {
    idempotencyKey: crypto.randomUUID(), // Gera um UUID único no navegador
    accountId:      document.getElementById('mov-account').value,
    categoryId:     document.getElementById('mov-category').value,
    amount:         parseFloat(rawAmount) || 0,
    date:           document.getElementById('mov-date').value,
    description:    document.getElementById('mov-desc').value.trim(),
    isRecurring:    false, // Padrão
    recurrenceType: null   // Padrão
  };

  // Validações locais
  if (payload.amount <= 0)  return UI.toast('Informe um valor válido maior que zero.', 'warning');
  if (!payload.accountId)   return UI.toast('Selecione uma conta.', 'warning');
  if (!payload.categoryId)  return UI.toast('Selecione uma categoria.', 'warning');
  if (!payload.date)        return UI.toast('A data é obrigatória.', 'warning');

  UI.modal.setLoading(true);
  try {
    if (type === 'DEPOSIT') {
      await Api.Goals.deposit(id, payload);
    } else {
      await Api.Goals.withdraw(id, payload);
    }
    UI.toast(type === 'DEPOSIT' ? 'Aporte realizado!' : 'Resgate realizado!', 'success');
    UI.modal.close();
    loadGoals(); // Recarrega a tela para atualizar a barra de progresso
  } catch (error) {
    UI.modal.setLoading(false);
    UI.toast(error.message, 'error');
  }
}

/** ── MODAL DE CRIAÇÃO / EDIÇÃO DE META ── */
async function _openGoalForm(id = null) {
  let goal = { name: '', targetAmount: '', priority: 'MEDIUM', deadLine: '', description: '', categoryId: '' };
  const isEdit = !!id;

  // Busca Categorias para preencher o select de categoryId
  let categories = [];
  try {
    const catData = await Api.Categories.all(); // Usando a API de categorias
    categories = Array.isArray(catData) ? catData : (catData?.content || []);
  } catch (e) {
    console.error("Erro ao buscar categorias", e);
  }

  if (isEdit) {
    UI.toast('Buscando dados...', 'info');
    try {
      goal = await Api.Goals.byId(id);
    } catch (e) {
      return UI.toast('Erro ao buscar meta.', 'error');
    }
  }

  const priorityOptions = [
    { value: 'HIGH',   label: '🔥 Alta' },
    { value: 'MEDIUM', label: '⚡ Média' },
    { value: 'LOW',    label: '🍃 Baixa' }
  ].map(p => `<option value="${p.value}" ${goal.priority === p.value ? 'selected' : ''}>${p.label}</option>`).join('');

  // Monta as opções de categoria
  const categoryOptions = `<option value="">Selecione uma categoria...</option>` +
      categories.map(c => `<option value="${c.id}" ${goal.categoryId === c.id ? 'selected' : ''}>${c.name}</option>`).join('');

  // Extrai data formato YYYY-MM-DD
  const deadlineVal = goal.deadLine ? goal.deadLine.split('T')[0] : '';

  UI.modal.open({
    title: isEdit ? 'Editar Meta' : 'Nova Meta',
    body: `
      <form id="form-goal">
        <div class="form-row">
          <label>Nome da Meta</label>
          <input type="text" id="goal-name" value="${UI.escapeHtml(goal.name || '')}" placeholder="Ex: Viagem, Reserva de emergência..." required />
        </div>
        <div class="form-row">
          <label>Categoria</label>
          <select id="goal-category" required>${categoryOptions}</select>
        </div>
        <div class="form-row">
          <label>Valor Alvo (R$)</label>
          <input type="text" id="goal-target" value="${goal.targetAmount || ''}" placeholder="0,00" ${isEdit ? 'disabled title="O valor alvo não pode ser editado após a criação"' : 'required'} />
        </div>
        <div class="form-row">
          <label>Prioridade</label>
          <select id="goal-priority">${priorityOptions}</select>
        </div>
        <div class="form-row">
          <label>Prazo</label>
          <input type="date" id="goal-deadline" value="${deadlineVal}" required />
        </div>
        <div class="form-row">
          <label>Descrição</label>
          <textarea id="goal-desc" placeholder="Detalhes da sua meta...">${UI.escapeHtml(goal.description || '')}</textarea>
        </div>
      </form>
    `,
    footer: `
      <button class="btn-secondary" type="button" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-primary" type="button" id="btn-save-goal"><i class="fa-solid fa-check"></i> Salvar Meta</button>
    `,
    onOpen: () => {
      document.getElementById('btn-save-goal').onclick = () => _saveGoal(id);
    }
  });
}

async function _saveGoal(id) {
  const isEdit = !!id;

  // O payload base bate exatamente com o GoalRequestUpdate do Java
  const basePayload = {
    categoryId:   document.getElementById('goal-category').value,
    name:         document.getElementById('goal-name').value.trim(),
    description:  document.getElementById('goal-desc').value.trim(),
    deadLine:     document.getElementById('goal-deadline').value, // <--- C maiúsculo na key!
    priority:     document.getElementById('goal-priority').value
  };

  // Validações locais referentes ao GoalRequestUpdate
  if (!basePayload.name)       return UI.toast('O nome da meta é obrigatório.', 'warning');
  if (!basePayload.categoryId) return UI.toast('Selecione uma categoria.', 'warning');
  if (!basePayload.deadLine)   return UI.toast('O prazo da meta é obrigatório.', 'warning');

  // Lógica separada para criação (que exige o targetAmount)
  let createPayload = { ...basePayload };
  if (!isEdit) {
    const targetRaw = document.getElementById('goal-target').value.replace(',', '.');
    createPayload.targetAmount = parseFloat(targetRaw) || 0;
    
    if (createPayload.targetAmount <= 0) {
      return UI.toast('Informe um valor alvo maior que zero.', 'warning');
    }
  }

  UI.modal.setLoading(true);
  try {
    if (isEdit) {
      // Envia apenas os dados compatíveis com GoalRequestUpdate
      await Api.Goals.update(id, basePayload);
    } else {
      // Envia o payload completo, incluindo targetAmount
      await Api.Goals.create(createPayload);
    }
    UI.toast(isEdit ? 'Meta atualizada!' : 'Meta criada!', 'success');
    UI.modal.close();
    loadGoals();
  } catch (error) {
    UI.modal.setLoading(false);
    UI.toast(error.message, 'error');
  }
}

/** ── EXCLUIR META ── */
function _handleDeleteGoal(id, name) {
  UI.modal.open({
    title: 'Excluir Meta',
    body: `
      <div style="text-align: center; padding: 1rem;">
        <i class="fa-solid fa-triangle-exclamation" style="font-size: 2.5rem; color: var(--crimson); margin-bottom: 1rem; display: block;"></i>
        <p>Deseja realmente excluir a meta <strong>${UI.escapeHtml(name)}</strong>?</p>
        <p style="font-size: 0.82rem; color: var(--text-3); margin-top: 0.5rem;">
          Todo o histórico de aportes e resgates será removido permanentemente.
        </p>
      </div>
    `,
    footer: `
      <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-danger" id="btn-confirm-del-goal">Excluir</button>
    `,
    onOpen: () => {
      document.getElementById('btn-confirm-del-goal').onclick = async () => {
        UI.modal.setLoading(true, '.btn-danger');
        try {
          await Api.Goals.delete(id);
          UI.modal.close();
          UI.toast('Meta excluída.', 'success');
          loadGoals();
        } catch (e) {
          UI.modal.setLoading(false, '.btn-danger');
          UI.toast(e.message, 'error');
        }
      };
    }
  });
}

/** ── LISTENER GLOBAIS ── */
document.addEventListener('click', e => {
  const btn = e.target.closest('button');
  if (!btn) return;

  if (btn.id === 'btn-new-goal') _openGoalForm();
  if (btn.id === 'btn-search-goal') _handleGoalSearch();
  if (btn.id === 'btn-reset-goal') _resetGoalSearch();
});

document.getElementById('goal-status-filter')?.addEventListener('change', _handleGoalSearch);
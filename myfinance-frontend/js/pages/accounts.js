/* ============================================================
   accounts.js — Gestão de Contas Bancárias e Carteiras
   Sincronizado com: AccountController.java
   ============================================================ */

Pages.register('accounts', loadAccounts);

/** ── ESTADO DA PÁGINA ── */
const _accState = {
  searchTerm: '',
  reset() {
    this.searchTerm = '';
    const input = document.getElementById('acc-search-name');
    if (input) input.value = '';
  }
};

/** ── CONFIGURAÇÕES (Chaves batendo com o Enum Java) ── */
const ACCOUNT_TYPES = {
  CORRENTE: { label: 'Conta Corrente', icon: 'fa-building-columns' },
  POUPANCA: { label: 'Poupança', icon: 'fa-piggy-bank' },
  INVESTMENT: { label: 'Investimento', icon: 'fa-chart-pie' },
  WALLET: { label: 'Dinheiro em Espécie', icon: 'fa-money-bill-1' },
  CREDIT_CARD: { label: 'Cartão de Crédito', icon: 'fa-credit-card' }
};

/** ── CARREGAMENTO (Com suporte a Busca) ── */
async function loadAccounts() {
  const containerId = 'accounts-list';
  UI.setLoading(containerId);

  try {
    let data;
    
    // Se tiver um termo de busca, chama o endpoint de search, senão busca todas
    if (_accState.searchTerm) {
        data = await Api.Accounts.search(_accState.searchTerm);
    } else {
        data = await Api.Accounts.all({ status: 'all' });
    }

    const accounts = Array.isArray(data) ? data : (data?.content || []);
    _renderAccountsGrid(accounts);
  } catch (error) {
    UI.toast(error.message, 'error');
    UI.setEmpty(containerId, 'fa-triangle-exclamation', 'Erro ao carregar contas.');
  }
}

/** ── RENDERIZAÇÃO ── */
function _renderAccountsGrid(accounts) {
  const container = document.getElementById('accounts-list');
  if (!container) return;

  if (!accounts.length) {
    const msg = _accState.searchTerm ? `Nenhuma conta encontrada com o nome "${_accState.searchTerm}".` : 'Você ainda não cadastrou nenhuma conta.';
    UI.setEmpty('accounts-list', 'fa-wallet', msg);
    return;
  }

  container.innerHTML = accounts.map(a => {
    const typeInfo = ACCOUNT_TYPES[a.type] || { label: a.type, icon: 'fa-wallet' };
    const isActive = a.active ?? a.isActive ?? true; 
    
    return `
      <div class="item-card ${isActive ? '' : 'is-inactive'}" data-id="${a.id}">
        <div class="item-card-header">
          <span class="item-card-title">
            <i class="fa-solid ${typeInfo.icon}" style="margin-right: 8px; color: var(--gold);"></i>
            ${UI.escapeHtml(a.name)}
          </span>
          <span class="badge-status ${isActive ? 'status-CONFIRMED' : 'status-CANCELLED'}">
            ${isActive ? 'Ativa' : 'Inativa'}
          </span>
        </div>
        
        <div class="item-card-value ${UI.format.moneyClass(a.currentBalance)}">
          ${UI.format.money(a.currentBalance)}
        </div>

        <div class="item-card-meta">
          <p>${typeInfo.label}</p>
          ${a.description ? `<p class="text-truncate">${UI.escapeHtml(a.description)}</p>` : ''}
        </div>

        <div class="item-card-actions">
          <button class="btn-icon btn-edit" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
          <button class="btn-icon btn-toggle" title="${isActive ? 'Desativar' : 'Ativar'}">
            <i class="fa-solid ${isActive ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
          </button>
          <button class="btn-icon danger btn-delete" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
        </div>
      </div>
    `;
  }).join('');

  // Delegação de eventos dos cards
  container.onclick = (e) => {
    const btn = e.target.closest('button');
    if (!btn) return;
    
    const card = btn.closest('.item-card');
    const id = card.dataset.id;
    const name = card.querySelector('.item-card-title').textContent.trim();
    const isCurrentlyActive = !card.classList.contains('is-inactive');

    if (btn.classList.contains('btn-edit')) _openAccountModal(id);
    if (btn.classList.contains('btn-toggle')) _toggleAccountStatus(id, isCurrentlyActive);
    if (btn.classList.contains('btn-delete')) _deleteAccountConfirm(id, name);
  };
}

/** ── BUSCA E FILTROS (Nomes Corrigidos) ── */
function _handleAccountSearch() {
  const val = document.getElementById('acc-search-name')?.value.trim();
  if (!val) return UI.toast('Digite o nome da conta para buscar.', 'warning');
  
  _accState.searchTerm = val;
  loadAccounts();
}

function _resetAccountSearch() {
  _accState.reset();
  loadAccounts();
}

/** ── FORMULÁRIO (Criação/Edição) ── */
async function _openAccountModal(id = null) {
  let acc = { name: '', type: 'CORRENTE', description: '', currentBalance: 0 };
  const isEdit = !!id;

  if (isEdit) {
    UI.toast('Buscando dados...', 'info');
    try {
        acc = await Api.Accounts.byId(id);
    } catch (e) {
        return UI.toast('Erro ao buscar conta', 'error');
    }
  }

  const typeOptions = Object.entries(ACCOUNT_TYPES)
    .map(([key, obj]) => `<option value="${key}" ${acc.type === key ? 'selected' : ''}>${obj.label}</option>`)
    .join('');

  UI.modal.open({
    title: isEdit ? 'Editar Conta' : 'Nova Conta',
    body: `
      <form id="form-account">
        <div class="form-row">
          <label>Nome da Conta</label>
          <input type="text" id="acc-name" value="${UI.escapeHtml(acc.name)}" placeholder="Ex: Itaú, Nubank..." required />
        </div>
        ${!isEdit ? `
        <div class="form-row">
          <label>Saldo Inicial (R$)</label>
          <input type="text" id="acc-balance" placeholder="0,00" />
        </div>` : ''}
        <div class="form-row">
          <label>Tipo de Conta</label>
          <select id="acc-type">${typeOptions}</select>
        </div>
        <div class="form-row">
          <label>Observações</label>
          <textarea id="acc-description" placeholder="Opcional">${UI.escapeHtml(acc.description || '')}</textarea>
        </div>
      </form>
    `,
    footer: `
      <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-primary" id="btn-save-account">Salvar Conta</button>
    `,
    onOpen: () => {
      document.getElementById('btn-save-account').onclick = () => _saveAccount(id);
    }
  });
}

/** ── AÇÕES API ── */
async function _saveAccount(id) {
  const isEdit = !!id;
  
  const payload = {
    name: document.getElementById('acc-name').value.trim(),
    type: document.getElementById('acc-type').value,
    description: document.getElementById('acc-description').value.trim()
  };

  if (!payload.name) return UI.toast('O nome da conta é obrigatório.', 'warning');

  UI.modal.setLoading(true);
  try {
    if (isEdit) {
      await Api.Accounts.update(id, payload);
    } else {
      const balanceRaw = document.getElementById('acc-balance').value.replace(',', '.');
      // Correção aqui: enviando initialBalance conforme o DTO do backend
      payload.initialBalance = parseFloat(balanceRaw) || 0; 
      await Api.Accounts.create(payload);
    }
    
    UI.toast(isEdit ? 'Conta atualizada!' : 'Conta criada!', 'success');
    UI.modal.close();
    loadAccounts();
  } catch (error) {
    UI.modal.setLoading(false);
    UI.toast(error.message, 'error');
  }
}

async function _toggleAccountStatus(id, currentlyActive) {
  try {
    if (currentlyActive) {
        await Api.Accounts.deactivate(id);
    } else {
        await Api.Accounts.activate(id);
    }
    
    UI.toast(`Conta ${currentlyActive ? 'desativada' : 'ativada'} com sucesso.`);
    loadAccounts();
  } catch (error) {
    UI.toast(error.message, 'error');
  }
}

function _deleteAccountConfirm(id, name) {
  UI.modal.open({
    title: 'Excluir Conta',
    body: `<p>Deseja realmente excluir <strong>${name}</strong>?</p><p class="text-danger">Atenção: Isso removerá permanentemente a conta do sistema.</p>`,
    footer: `
      <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-danger" id="btn-confirm-del">Excluir</button>
    `,
    onOpen: () => {
      document.getElementById('btn-confirm-del').onclick = async () => {
        UI.modal.setLoading(true);
        try {
            await Api.Accounts.delete(id);
            UI.modal.close();
            loadAccounts();
        } catch (e) {
            UI.modal.setLoading(false);
            UI.toast(e.message, 'error');
        }
      };
    }
  });
}

/** ── LISTENER GLOBAL (Com os nomes corretos) ── */
document.addEventListener('click', e => {
  const btn = e.target.closest('button');
  if (!btn) return;

  if (btn.id === 'btn-new-account') _openAccountModal();
  if (btn.id === 'btn-search-acc') _handleAccountSearch();
  if (btn.id === 'btn-reset-acc') _resetAccountSearch();
});
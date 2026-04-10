/* ============================================================
   transactions.js — Gestão de Movimentações Financeiras
   Sincronizado com: TransactionController.java
   ============================================================ */

Pages.register('transactions', () => loadTransactions(0));

const _txState = {
  page: 0,
  totalPages: 0,
  accounts: [],
  categories: [],
  lastFetch: 0,
  CACHE_DURATION: 30000
};

/** ── MAPEAMENTO DE INTERFACE (Sincronizado com Enums Java) ── */
const TX_UI = {
  types: {
    RECEITA: { label: 'Receita', class: 'text-success', sign: '+' },
    DESPESA: { label: 'Despesa', class: 'text-danger', sign: '-' }
  },
  status: {
    PENDENTE: { label: 'Pendente', class: 'status-PENDING' },
    CONFIRMADA: { label: 'Confirmada', class: 'status-CONFIRMED' },
    CANCELADA: { label: 'Cancelada', class: 'status-CANCELLED' }
  }
};

/** ── CARREGAMENTO DE METADADOS ── */
async function _ensureMetadata() {
  const now = Date.now();
  if (_txState.accounts.length && (now - _txState.lastFetch < _txState.CACHE_DURATION)) return;
  try {
    const [accs, cats] = await Promise.all([
      Api.Accounts.all({ status: 'all' }),
      Api.Categories.all({ status: 'all' })
    ]);
    _txState.accounts = accs?.content || (Array.isArray(accs) ? accs : []);
    _txState.categories = cats?.content || (Array.isArray(cats) ? cats : []);
    _txState.lastFetch = now;
    _updateFilterBar();
  } catch (e) { console.error('[TX] Erro metadados:', e); }
}

/** ── BUSCA (Usa LocalDateTime conforme TransactionSearchFilter) ── */
async function loadTransactions(page = 0) {
  const tbody = document.getElementById('tx-tbody');
  if (!tbody) return;
  _txState.page = page;
  await _ensureMetadata();

  const rawFrom = document.getElementById('tx-from')?.value || UI.monthStart();
  const rawTo = document.getElementById('tx-to')?.value || UI.today();

  // Parâmetros para o @ParameterObject TransactionSearchFilter
  const params = {
    page,
    size: 10,
    startDate: `${rawFrom}T00:00:00`,
    endDate: `${rawTo}T23:59:59`,
    type: document.getElementById('tx-type-filter')?.value || null,
    accountId: document.getElementById('tx-account-filter')?.value || null,
    categoryId: document.getElementById('tx-category-filter')?.value || null,
    status: document.getElementById('tx-status-filter')?.value || null
  };

  try {
    const data = await Api.Transactions.all(params);
    const items = data?.content || [];
    _txState.totalPages = data?.totalPages || 1;
    _renderTable(items);
    if (UI.renderPagination) UI.renderPagination('tx-pagination', _txState.page, _txState.totalPages, loadTransactions);
  } catch (e) {
    UI.toast(e.message, 'error');
    tbody.innerHTML = `<tr><td colspan="7" class="table-empty">Nenhuma transação encontrada.</td></tr>`;
  }
}

/** ── RENDERIZAÇÃO DA TABELA ── */
function _renderTable(items) {
  const tbody = document.getElementById('tx-tbody');
  tbody.innerHTML = items.map(tx => {
    const typeCfg = TX_UI.types[tx.type] || TX_UI.types.DESPESA;
    const statusCfg = TX_UI.status[tx.status] || { label: tx.status, class: '' };

    // Correção AQUI: Acessando o objeto "category" aninhado que vem do Java (CategoryResponse)
    const categoryLabel = tx.category?.name || 'Transferência';

    return `
      <tr class="tx-row" data-id="${tx.id}">
        <td class="text-muted">${UI.format.date(tx.dateTime)}</td>
        <td class="fw-bold">${UI.escapeHtml(tx.description)}</td>
        <td><span class="badge-cat">${UI.escapeHtml(categoryLabel)}</span></td>
        <td><i class="fa-solid fa-wallet sm"></i> ${UI.escapeHtml(tx.accountName)}</td>
        <td><span class="badge-status ${statusCfg.class}">${statusCfg.label}</span></td>
        <td class="text-right ${typeCfg.class}">
          ${typeCfg.sign} ${UI.format.money(tx.amount)}
        </td>
        <td class="text-center">
          <div class="tx-actions">
            ${tx.status === 'PENDENTE' ? `
              <button class="btn-icon success btn-confirm" title="Confirmar"><i class="fa-solid fa-check"></i></button>
              <button class="btn-icon warning btn-cancel" title="Cancelar"><i class="fa-solid fa-xmark"></i></button>
            ` : ''}
            <button class="btn-icon danger btn-delete" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
          </div>
        </td>
      </tr>`;
  }).join('');

  tbody.onclick = (e) => {
    const btn = e.target.closest('button');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.classList.contains('btn-confirm')) _updateStatus(id, 'confirm');
    if (btn.classList.contains('btn-cancel')) _updateStatus(id, 'cancel');
    if (btn.classList.contains('btn-delete')) _deleteTx(id);
  };
}

/** ── MODAL DE CRIAÇÃO / TRANSFERÊNCIA ── */
async function openTransactionModal(type = 'COMMON') {
  await _ensureMetadata();
  const isTransfer = type === 'TRANSFER';
  const accOptions = _txState.accounts.map(a => `<option value="${a.id}">${a.name}</option>`).join('');
  const catOptions = _txState.categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

  UI.modal.open({
    title: isTransfer ? 'Nova Transferência Entre Contas' : 'Nova Transação',
    body: `
      <form id="form-tx">
        <div class="form-row">
            <label>Descrição</label>
            <input type="text" id="f-desc" placeholder="Ex: Conta de Luz, Salário..." required />
        </div>
        <div class="form-grid-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
          <div class="form-row">
            <label>Valor (R$)</label>
            <input type="text" id="f-amount" placeholder="0,00" required />
          </div>
          <div class="form-row">
            <label>Data</label>
            <input type="date" id="f-date" value="${UI.today()}" required />
          </div>
        </div>
        ${isTransfer ? `
          <div class="form-row"><label>Conta Origem (Sai o dinheiro)</label><select id="f-from-acc">${accOptions}</select></div>
          <div class="form-row"><label>Conta Destino (Entra o dinheiro)</label><select id="f-to-acc">${accOptions}</select></div>
          ` : `
          <div class="form-row"><label>Tipo de Movimentação</label>
            <select id="f-type">
                <option value="DESPESA">Despesa (Saída)</option>
                <option value="RECEITA">Receita (Entrada)</option>
            </select>
          </div>
          <div class="form-row"><label>Conta</label><select id="f-acc">${accOptions}</select></div>
          <div class="form-row"><label>Categoria</label><select id="f-cat">${catOptions}</select></div>
        `}
      </form>`,
    footer: `
        <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
        <button class="btn-primary" id="btn-save-tx">Salvar Transação</button>
    `,
    onOpen: () => { document.getElementById('btn-save-tx').onclick = () => _processSave(isTransfer); }
  });
}

/** ── SALVAMENTO ── */
async function _processSave(isTransfer) {
  const payload = {
    idempotencyKey: crypto.randomUUID(),
    description: document.getElementById('f-desc').value.trim(),
    amount: parseFloat(document.getElementById('f-amount').value.replace(',', '.')) || 0,
    date: document.getElementById('f-date').value
  };

  if (payload.amount <= 0 || !payload.description) {
    return UI.toast('Preencha os campos obrigatórios com valores válidos.', 'warning');
  }

  UI.modal.setLoading(true);
  try {
    if (isTransfer) {
      // Sincronizado com TransferRequest.java
      payload.fromAccountId = document.getElementById('f-from-acc').value;
      payload.toAccountId = document.getElementById('f-to-acc').value;

      if (payload.fromAccountId === payload.toAccountId) throw new Error("A conta de origem e destino não podem ser a mesma!");

      await Api.Transactions.transfer(payload); // Chama @PostMapping("/transfer")
    } else {
      // Sincronizado com TransactionRequest.java
      payload.type = document.getElementById('f-type').value;
      payload.accountId = document.getElementById('f-acc').value;
      payload.categoryId = document.getElementById('f-cat').value;

      await Api.Transactions.create(payload); // Chama @PostMapping
    }
    UI.toast('Transação salva!', 'success');
    UI.modal.close();
    loadTransactions(0);
  } catch (e) {
    UI.modal.setLoading(false);
    UI.toast(e.message, 'error');
  }
}

async function _updateStatus(id, action) {
  try {
    // Chama @PatchMapping("/{id}/confirm") ou /cancel
    await Api.Transactions[action](id);
    UI.toast('Status atualizado!');
    loadTransactions(_txState.page);
  } catch (e) { UI.toast(e.message, 'error'); }
}

async function _deleteTx(id) {
  if(!confirm("Deseja excluir esta transação? Apenas transações pendentes podem ser excluídas.")) return;
  try {
    await Api.Transactions.delete(id);
    UI.toast('Removida!');
    loadTransactions(_txState.page);
  } catch (e) { UI.toast(e.message, 'error'); }
}

function _updateFilterBar() {
  const accSel = document.getElementById('tx-account-filter');
  const catSel = document.getElementById('tx-category-filter');
  if (accSel) accSel.innerHTML = '<option value="">Todas Contas</option>' + _txState.accounts.map(a => `<option value="${a.id}">${a.name}</option>`).join('');
  if (catSel) catSel.innerHTML = '<option value="">Todas Categorias</option>' + _txState.categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

/** ── LISTENER GLOBAIS ── */
document.addEventListener('click', e => {
  const btn = e.target.closest('#btn-new-transaction, #btn-new-transfer, #btn-filter-tx');
  if (!btn) return;

  if (btn.id === 'btn-filter-tx') loadTransactions(0);
  if (btn.id === 'btn-new-transaction') openTransactionModal('COMMON');
  if (btn.id === 'btn-new-transfer') openTransactionModal('TRANSFER');
});
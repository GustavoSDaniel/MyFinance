/* ============================================================
   categories.js — Organização de Receitas e Despesas
   ============================================================ */

Pages.register('categories', loadCategories);

/** ── ESTADO DA PÁGINA ── */
const _catState = {
  searchTerm: '',
  status: 'all',
  reset() {
    this.searchTerm = '';
    this.status = 'all';
    const input = document.getElementById('cat-search-name');
    const select = document.getElementById('cat-status-filter');
    if (input) input.value = '';
    if (select) select.value = 'all';
  }
};

const CATEGORY_UI = {
  RECEITA: { label: 'Receita', class: 'badge-income', icon: 'fa-arrow-trend-up' },
  DESPESA: { label: 'Despesa', class: 'badge-expense', icon: 'fa-arrow-trend-down' },
  TRANSFERENCIA: { label: 'Transferência', class: 'badge-transfer', icon: 'fa-right-left' }
};

/** ── CARREGAMENTO ── */
async function loadCategories() {
  const containerId = 'categories-list';
  UI.setLoading(containerId);

  try {
    let data;
    // Pega o status do select (se ele existir)
    const currentStatus = document.getElementById('cat-status-filter')?.value || _catState.status;

    // Se tiver nome, usa endpoint de search, senão busca por status
    if (_catState.searchTerm) {
        // PRECISA GARANTIR QUE O api.js TEM: search: (name) => request('GET', '/categories/search', null, { name })
        data = await Api.Categories.search(_catState.searchTerm);
    } else {
        data = await Api.Categories.all({ status: currentStatus });
    }

    const categories = Array.isArray(data) ? data : (data?.content || []);
    categories.sort((a, b) => a.name.localeCompare(b.name));
    
    _renderCategoriesGrid(categories);
  } catch (error) {
    UI.toast(error.message, 'error');
    UI.setEmpty(containerId, 'fa-tags', 'Erro ao carregar categorias.');
  }
}

/** ── RENDERIZAÇÃO ── */
function _renderCategoriesGrid(categories) {
  const container = document.getElementById('categories-list');
  if (!container) return;

  if (!categories.length) {
    const msg = _catState.searchTerm ? `Nenhuma categoria encontrada com "${_catState.searchTerm}".` : 'Nenhuma categoria encontrada.';
    UI.setEmpty('categories-list', 'fa-tags', msg);
    return;
  }

  container.innerHTML = categories.map(c => {
    const config = CATEGORY_UI[c.type] || CATEGORY_UI.DESPESA;
    const isActive = c.active ?? c.isActive ?? true; 

    return `
      <div class="item-card ${isActive ? '' : 'is-inactive'}" data-id="${c.id}">
        <div class="item-card-header">
          <span class="item-card-title">
            <i class="fa-solid ${c.icon || 'fa-tag'}" style="margin-right: 8px; color: ${c.color || 'var(--gold)'}"></i>
            ${UI.escapeHtml(c.name)}
          </span>
          <span class="badge-status ${config.class}">${config.label}</span>
        </div>

        <div class="item-card-meta">
          <span class="badge-status ${isActive ? 'status-CONFIRMED' : 'status-CANCELLED'}">
            ${isActive ? 'Ativa' : 'Inativa'}
          </span>
          ${c.description ? `<p class="text-truncate" style="margin-top: 8px; font-size: 0.8rem;">${UI.escapeHtml(c.description)}</p>` : ''}
        </div>

        <div class="item-card-actions">
          <button class="btn-icon btn-edit-cat" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
          <button class="btn-icon btn-toggle-cat" title="${isActive ? 'Desativar' : 'Ativar'}">
            <i class="fa-solid ${isActive ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
          </button>
          <button class="btn-icon danger btn-delete-cat" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
        </div>
      </div>
    `;
  }).join('');

  container.onclick = (e) => {
    const btn = e.target.closest('button');
    if (!btn) return;

    const card = btn.closest('.item-card');
    const id = card.dataset.id;
    const name = card.querySelector('.item-card-title').textContent.trim();
    const isCurrentlyActive = !card.classList.contains('is-inactive');

    if (btn.classList.contains('btn-edit-cat')) _openCategoryModal(id);
    if (btn.classList.contains('btn-toggle-cat')) _handleToggle(id, isCurrentlyActive);
    if (btn.classList.contains('btn-delete-cat')) _handleDelete(id, name);
  };
}

/** ── BUSCA E FILTROS ── */
function _handleCatSearch() {
  const val = document.getElementById('cat-search-name')?.value.trim();
  _catState.searchTerm = val || '';
  _catState.status = document.getElementById('cat-status-filter')?.value || 'all';
  loadCategories();
}

function _resetCatSearch() {
  _catState.reset();
  loadCategories();
}

/** ── MODAL FORM ── */
async function _openCategoryModal(id = null) {
  let cat = { name: '', type: 'DESPESA', icon: 'fa-tag', color: '#d4af37', description: '' };
  const isEdit = !!id;

  if (isEdit) {
    UI.toast('Buscando dados...', 'info');
    try {
      cat = await Api.Categories.getById(id);
    } catch (e) {
      return UI.toast('Erro ao buscar categoria.', 'error');
    }
  }

  const typeOptions = [
    { value: 'RECEITA',       label: 'Receita' },
    { value: 'DESPESA',       label: 'Despesa' },
    { value: 'TRANSFERENCIA', label: 'Transferência' }
  ].map(t => `<option value="${t.value}" ${cat.type === t.value ? 'selected' : ''}>${t.label}</option>`).join('');

  UI.modal.open({
    title: isEdit ? 'Editar Categoria' : 'Nova Categoria',
    body: `
      <form id="form-cat">
        <div class="form-row">
          <label>Nome da Categoria</label>
          <input type="text" id="cat-name" value="${UI.escapeHtml(cat.name || '')}" placeholder="Ex: Alimentação, Salário..." required />
        </div>
        <div class="form-row">
          <label>Tipo</label>
          <select id="cat-type">${typeOptions}</select>
        </div>
        <div class="form-row">
          <label>Ícone (Font Awesome)</label>
          <input type="text" id="cat-icon" value="${UI.escapeHtml(cat.icon || 'fa-tag')}" placeholder="Ex: fa-tag, fa-home, fa-car" />
        </div>
        <div class="form-row">
          <label>Cor</label>
          <input type="color" id="cat-color" value="${cat.color || '#d4af37'}" style="height: 42px; padding: 4px 6px; cursor: pointer;" />
        </div>
        <div class="form-row">
          <label>Descrição (opcional)</label>
          <textarea id="cat-desc" placeholder="Descrição da categoria...">${UI.escapeHtml(cat.description || '')}</textarea>
        </div>
      </form>
    `,
    footer: `
      <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-primary" id="btn-save-cat"><i class="fa-solid fa-check"></i> Salvar</button>
    `,
    onOpen: () => {
      document.getElementById('btn-save-cat').onclick = () => _saveCategory(id);
    }
  });
}

/** ── SALVAR CATEGORIA ── */
async function _saveCategory(id) {
  const isEdit = !!id;
  const payload = {
    name:        document.getElementById('cat-name').value.trim(),
    type:        document.getElementById('cat-type').value,
    icon:        document.getElementById('cat-icon').value.trim() || 'fa-tag',
    color:       document.getElementById('cat-color').value,
    description: document.getElementById('cat-desc').value.trim()
  };

  if (!payload.name) return UI.toast('O nome da categoria é obrigatório.', 'warning');

  UI.modal.setLoading(true);
  try {
    if (isEdit) {
      await Api.Categories.update(id, payload);
    } else {
      await Api.Categories.create(payload);
    }
    UI.toast(isEdit ? 'Categoria atualizada!' : 'Categoria criada!', 'success');
    UI.modal.close();
    loadCategories();
  } catch (error) {
    UI.modal.setLoading(false);
    UI.toast(error.message, 'error');
  }
}

/** ── TOGGLE ATIVO/INATIVO ── */
async function _handleToggle(id, isCurrentlyActive) {
  try {
    if (isCurrentlyActive) {
      await Api.Categories.deactivate(id);
    } else {
      await Api.Categories.activate(id);
    }
    UI.toast(`Categoria ${isCurrentlyActive ? 'desativada' : 'ativada'} com sucesso.`);
    loadCategories();
  } catch (error) {
    UI.toast(error.message, 'error');
  }
}

/** ── EXCLUIR CATEGORIA ── */
function _handleDelete(id, name) {
  UI.modal.open({
    title: 'Excluir Categoria',
    body: `
      <div style="text-align: center; padding: 1rem;">
        <i class="fa-solid fa-triangle-exclamation" style="font-size: 2.5rem; color: var(--crimson); margin-bottom: 1rem; display: block;"></i>
        <p>Deseja realmente excluir <strong>${UI.escapeHtml(name)}</strong>?</p>
        <p style="font-size: 0.82rem; color: var(--text-3); margin-top: 0.5rem;">
          Transações vinculadas a esta categoria não serão excluídas.
        </p>
      </div>
    `,
    footer: `
      <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-danger" id="btn-confirm-del-cat">Excluir</button>
    `,
    onOpen: () => {
      document.getElementById('btn-confirm-del-cat').onclick = async () => {
        UI.modal.setLoading(true, '.btn-danger');
        try {
          await Api.Categories.delete(id);
          UI.modal.close();
          UI.toast('Categoria excluída.', 'success');
          loadCategories();
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
  
  if (btn.id === 'btn-new-category') _openCategoryModal();
  if (btn.id === 'btn-search-cat') _handleCatSearch();
  if (btn.id === 'btn-reset-cat') _resetCatSearch();
});

// Listener para o Select de Status (filtra ao trocar a opção)
document.getElementById('cat-status-filter')?.addEventListener('change', _handleCatSearch);
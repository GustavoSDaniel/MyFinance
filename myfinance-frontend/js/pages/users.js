/* ============================================================
   users.js — Perfil do Usuário e Gestão Administrativa (Dual-Mode)
   Sincronizado com: UserController.java e UserService.java
   ============================================================ */

Pages.register('users', () => {
  _usersState.reset();
  loadUsers();
});

const _usersState = {
  page: 0,
  totalPages: 0,
  searchType: 'all',
  searchValue: '',
  reset() {
    this.page = 0;
    this.searchType = 'all';
    this.searchValue = '';
  }
};

/** ── CARREGAMENTO DE DADOS (INTELIGENTE E PROFISSIONAL) ── */
async function loadUsers(page = 0) {
  const listContainer = 'section-users-list'; 
  UI.setLoading(listContainer);
  _usersState.page = page;

  try {
    // 1. Puxa os dados do Spring Boot e do Token local
    const currentUserInfo = await Api.Users.me();
    const localUser = Auth.getUser() || {};
    
    let isAdmin = false;
    
    // 2. Coleta roles de todas as fontes possíveis (API e Keycloak)
    const allRoles = [];
    
    // Do Back-end (campos role, roles ou authorities)
    if (currentUserInfo.role) allRoles.push(...(Array.isArray(currentUserInfo.role) ? currentUserInfo.role : [currentUserInfo.role]));
    if (currentUserInfo.roles) allRoles.push(...(Array.isArray(currentUserInfo.roles) ? currentUserInfo.roles : [currentUserInfo.roles]));
    if (currentUserInfo.authorities) allRoles.push(...(Array.isArray(currentUserInfo.authorities) ? currentUserInfo.authorities : [currentUserInfo.authorities]));
    
    // Do Keycloak (Realm e Client access)
    if (localUser.realm_access?.roles) allRoles.push(...localUser.realm_access.roles);
    if (localUser.resource_access?.['my-finance-app']?.roles) allRoles.push(...localUser.resource_access['my-finance-app'].roles);

    // 3. Normaliza para maiúsculo e verifica se é ADMIN
    const upperRoles = allRoles.map(r => String(r).toUpperCase());
    isAdmin = upperRoles.includes('ADMIN') || upperRoles.includes('ROLE_ADMIN');

    // Seleciona elementos da interface
    const filterBar = document.querySelector('#section-users .card'); 
    const titleEl = document.querySelector('#section-users .section-title');
    const subTitleEl = document.querySelector('#section-users .section-sub');

    // ==========================================
    // FLUXO 1: VISÃO DE USUÁRIO COMUM (MEU PERFIL)
    // ==========================================
    if (!isAdmin) {
        if (filterBar) filterBar.style.display = 'none'; 
        if (titleEl) titleEl.textContent = 'Meu Perfil';
        if (subTitleEl) subTitleEl.textContent = 'Gerencie as informações da sua conta';
        
        const paginationEl = document.getElementById('users-pagination');
        if (paginationEl) paginationEl.innerHTML = '';

        _renderUsersTable([currentUserInfo], false);
        return;
    }

    // ==========================================
    // FLUXO 2: VISÃO DE ADMINISTRADOR
    // ==========================================
    if (filterBar) filterBar.style.display = 'block';
    if (titleEl) titleEl.textContent = 'Administração';
    if (subTitleEl) subTitleEl.textContent = 'Gestão de usuários do sistema';

    let response;
    if (_usersState.searchType === 'email' && _usersState.searchValue) {
      const user = await Api.Users.byEmail(_usersState.searchValue);
      response = user ? { content: [user], totalPages: 1 } : { content: [], totalPages: 0 };
    } 
    else if (_usersState.searchType === 'id' && _usersState.searchValue) {
      const user = await Api.Users.getUserById(_usersState.searchValue);
      response = user ? { content: [user], totalPages: 1 } : { content: [], totalPages: 0 };
    } 
    else {
      response = await Api.Users.all(page);
    }

    const users = response?.content || [];
    _usersState.totalPages = response?.totalPages || 0;

    _renderUsersTable(users, true);
    
    // Paginação apenas para a lista completa ('all')
    const paginationEl = document.getElementById('users-pagination');
    if (paginationEl) {
        if (_usersState.searchType === 'all' && _usersState.totalPages > 1) {
            UI.renderPagination('users-pagination', _usersState.page, _usersState.totalPages, loadUsers);
        } else {
            paginationEl.innerHTML = '';
        }
    }

  } catch (error) {
    UI.toast(error.message, 'error');
    UI.setEmpty(listContainer, 'fa-users-slash', 'Erro ao processar dados de usuário.');
  }
}

/** ── RENDERIZAÇÃO ── */
function _renderUsersTable(users, isAdminView) {
  const container = document.getElementById('section-users-list');
  if (!container) return;

  if (!users.length) {
    UI.setEmpty('section-users-list', 'fa-user-magnifying-glass', 'Nenhum usuário encontrado.');
    return;
  }

  container.innerHTML = users.map(u => {
    const userRoleStr = String(u.role || 'USER').toUpperCase();
    const isUserAdmin = userRoleStr === 'ADMIN' || userRoleStr === 'ROLE_ADMIN';

    return `
    <div class="item-card user-admin-card" data-id="${u.id || ''}">
      <div class="item-card-header">
        <span class="item-card-title">${UI.escapeHtml(u.name)}</span>
        <span class="badge-status ${isUserAdmin ? 'status-CONFIRMED' : 'status-PENDING'}">
          ${userRoleStr}
        </span>
      </div>
      <div class="item-card-meta">
        <p><i class="fa-regular fa-envelope" style="margin-right: 5px;"></i> ${UI.escapeHtml(u.email)}</p>
        <p style="font-family: var(--font-mono); font-size: 0.7rem; margin-top: 8px; opacity: 0.6;">UUID: ${u.id || 'N/A'}</p>
      </div>
      <div class="item-card-actions">
        <button class="btn-icon danger btn-delete-user" data-id="${u.id}" data-name="${UI.escapeHtml(u.name)}" title="${isAdminView ? 'Excluir Usuário' : 'Deletar Minha Conta'}">
          <i class="fa-solid fa-trash-can"></i>
        </button>
      </div>
    </div>
  `}).join('');

  container.onclick = (e) => {
    const btn = e.target.closest('.btn-delete-user');
    if (!btn) return;
    _handleDeletePrompt(btn.dataset.id, btn.dataset.name, isAdminView);
  };
}

/** ── AÇÕES DE EXCLUSÃO ── */
function _handleDeletePrompt(id, name, isAdminView) {
  const title = isAdminView ? 'Excluir Usuário' : 'Deletar Minha Conta';
  const text = isAdminView 
    ? `Confirmar a exclusão permanente de <strong>${name}</strong>?` 
    : `Atenção! Você está prestes a deletar a sua própria conta.`;

  UI.modal.open({
    title: title,
    body: `
      <div style="text-align: center; padding: 1rem;">
        <i class="fa-solid fa-triangle-exclamation" style="font-size: 2.5rem; color: var(--crimson); margin-bottom: 1rem;"></i>
        <p>${text}</p>
        <p style="font-size: 0.8rem; color: var(--text-3); margin-top: 10px;">
          Todas as contas, transações e metas vinculadas serão removidas para sempre.
        </p>
      </div>
    `,
    footer: `
      <button class="btn-secondary" onclick="UI.modal.close()">Cancelar</button>
      <button class="btn-danger" id="confirm-user-delete-btn">${isAdminView ? 'Excluir Agora' : 'Deletar Minha Conta'}</button>
    `,
    onOpen: () => {
      document.getElementById('confirm-user-delete-btn').onclick = () => _executeUserDeletion(id, isAdminView);
    }
  });
}

async function _executeUserDeletion(id, isAdminView) {
  UI.modal.setLoading(true);
  try {
    await Api.Users.delete(id);
    UI.modal.close();

    const currentUser = Auth.getUser();
    // Se o usuário deletou a própria conta (ID do token igual ao ID deletado)
    if (!isAdminView || (currentUser && (currentUser.sub === id || currentUser.id === id))) {
       UI.toast('Sua conta foi excluída. Encerrando sessão...', 'info');
       setTimeout(() => Auth.logout(), 2000);
    } else {
       UI.toast('Usuário removido com sucesso.', 'success');
       _resetUsersView();
    }
  } catch (err) {
    UI.modal.setLoading(false);
    UI.toast(err.message, 'error');
  }
}

/** ── FILTROS E BUSCA ── */
function _handleSearch(type) {
  const val = type === 'email' 
    ? document.getElementById('user-search-email')?.value.trim() 
    : document.getElementById('user-search-id')?.value.trim();

  if (!val) return UI.toast('Digite um valor para buscar.', 'warning');

  _usersState.searchType = type;
  _usersState.searchValue = val;
  loadUsers(0);
}

function _resetUsersView() {
  const emailInput = document.getElementById('user-search-email');
  const idInput = document.getElementById('user-search-id');
  if (emailInput) emailInput.value = '';
  if (idInput) idInput.value = '';
  
  _usersState.reset();
  loadUsers(0);
}

/** ── LISTENERS GLOBAIS ── */
document.addEventListener('click', e => {
  const btn = e.target.closest('button');
  if (!btn) return;
  
  if (btn.id === 'btn-search-email') _handleSearch('email');
  if (btn.id === 'btn-search-id') _handleSearch('id');
  if (btn.id === 'btn-reset-users') _resetUsersView();
});
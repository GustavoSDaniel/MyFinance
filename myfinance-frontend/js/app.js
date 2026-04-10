/* ============================================================
   app.js — Inicialização do Sistema
   ============================================================ */
document.addEventListener('DOMContentLoaded', async () => {
  console.log('[MyFinance] Inicializando...');

  // 1. Vincular botões (Login e Logout)
  document.getElementById('btn-login-home')?.addEventListener('click', () => Auth.login());
  document.getElementById('btn-logout')?.addEventListener('click', () => Auth.logout());

  // 2. Vincular Navegação do Menu Lateral
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', () => {
        UI.navigate(item.dataset.section);
        document.getElementById('sidebar')?.classList.remove('mobile-open'); // Fecha menu no celular
    });
  });

// 2.1. Tornar o Card do Usuário clicável para abrir o Perfil/Admin
  const userProfileCard = document.querySelector('.sidebar-user');
  if (userProfileCard) {
      userProfileCard.title = 'Acessar Meu Perfil';
      
      // Ao clicar, navega para a aba 'users'
      userProfileCard.addEventListener('click', () => {
          UI.navigate('users');
          document.getElementById('sidebar')?.classList.remove('mobile-open');
      });
  }

  // 3. Inicializar Autenticação
  try {
    const isAuth = await Auth.init();
    
    if (isAuth) {
      document.getElementById('page-home').classList.remove('active');
      document.getElementById('page-app').classList.add('active');

      // ==========================================
      // ATUALIZAÇÃO VISUAL DO USUÁRIO NA SIDEBAR
      // ==========================================
      const user = Auth.getUser(); 
      
      if (user && user.name) {
        const initial = user.name.charAt(0).toUpperCase();
        const avatarEl = document.getElementById('user-avatar-initial');
        const nameEl = document.getElementById('user-name-sidebar');
        const greetingEl = document.getElementById('topbar-greeting');
        const roleEl = document.getElementById('user-role-sidebar');

        if (avatarEl) avatarEl.textContent = initial;
        if (nameEl) nameEl.textContent = user.name;
        if (greetingEl) greetingEl.textContent = `Olá, ${user.name.split(' ')[0]}`;
        if (roleEl) roleEl.textContent = user.isAdmin ? 'Administrador' : 'Usuário';
      }

      // Vai para o Dashboard inicial
      UI.navigate('dashboard');
    } else {
      document.getElementById('page-home').classList.add('active');
      document.getElementById('page-app').classList.remove('active');
    }
  } catch (e) {
    console.error("Erro no bootstrap:", e);
    UI.toast('Erro ao iniciar aplicação.', 'error');
  }
});
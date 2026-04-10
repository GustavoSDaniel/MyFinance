const UI = (() => {
  const navigate = async (sectionId) => {
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

    const targetSection = document.getElementById(`section-${sectionId}`);
    const targetNav = document.querySelector(`[data-section="${sectionId}"]`);

    if (targetSection) targetSection.classList.add('active');
    if (targetNav) targetNav.classList.add('active');

    const titleMap = { 
        dashboard: 'Dashboard', accounts: 'Contas', 
        transactions: 'Transações', categories: 'Categorias', 
        goals: 'Metas', users: 'Administração' 
    };
    document.getElementById('topbar-title').textContent = titleMap[sectionId] || 'MyFinance';

    const loader = Pages.getLoader(sectionId);
    if (loader) await loader();
  };

  const format = {
    money: (v) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v || 0),
    moneyClass: (v) => v >= 0 ? 'val-positive' : 'val-negative',
    date: (d) => {
        if (!d) return '--';
        const date = new Date(d);
        return date.toLocaleDateString('pt-BR', { timeZone: 'UTC' });
    }
  };

  const modal = {
    open: ({ title, body, footer, onOpen }) => {
      document.getElementById('modal-title').textContent = title;
      document.getElementById('modal-body').innerHTML = body;
      document.getElementById('modal-footer').innerHTML = footer || '';
      document.getElementById('modal-overlay').classList.remove('hidden');
      if (onOpen) onOpen();
    },
    close: () => document.getElementById('modal-overlay').classList.add('hidden'),
    setLoading: (isLoading, btnSelector = '.btn-primary') => {
      const btn = document.querySelector(btnSelector);
      if (btn) {
        btn.disabled = isLoading;
        btn.innerHTML = isLoading ? `<span class="spinner"></span>` : `<i class="fa-solid fa-check"></i> Salvar`;
      }
    }
  };

  return {
    navigate, format, 
    today: () => new Date().toISOString().split('T')[0],
    monthStart: () => {
        const d = new Date();
        return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0];
    },
    setLoading: (id, load = true) => {
        const el = document.getElementById(id);
        if (el && load) el.innerHTML = '<div class="spinner-container"><div class="spinner"></div></div>';
    },
    setEmpty: (id, icon, msg) => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = `<div class="empty-state"><i class="fa-solid ${icon}"></i><p>${msg}</p></div>`;
    },
    toast: (msg, type = 'info') => {
        const cont = document.getElementById('toast-container');
        const t = document.createElement('div');
        t.className = `toast ${type}`;
        t.textContent = msg;
        cont.appendChild(t);
        setTimeout(() => t.remove(), 4000);
    },
    modal,
    escapeHtml: (s) => {
        const t = document.createElement('div');
        t.textContent = s;
        return t.innerHTML;
    }
  };
})();

const Pages = (() => {
    const _reg = {};
    return { 
        register: (n, l) => _reg[n] = l, 
        getLoader: (n) => _reg[n] 
    };
})();
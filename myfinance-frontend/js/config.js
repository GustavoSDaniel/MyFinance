// Verifica se está rodando no seu computador local
const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

// Configura as URLs automaticamente
const ENV = {
  KEYCLOAK_BASE: isLocalhost 
    ? 'http://localhost:5053/realms/my-finance-app/protocol/openid-connect'
    : 'https://URL_DO_SEU_KEYCLOAK_EM_PRODUCAO/realms/my-finance-app/protocol/openid-connect',
  
  API_BASE: isLocalhost
    ? 'http://localhost:5050/api/v1'
    : 'https://URL_DA_SUA_API_SPRING_EM_PRODUCAO/api/v1',
  
  CLIENT_ID: 'my-finance-app'
};
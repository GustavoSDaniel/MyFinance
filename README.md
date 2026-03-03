# 💰 MyFinance API

API para **gerenciamento de finanças pessoais**, desenvolvida com **Java + Spring Boot**, focada em **segurança**, **observabilidade** e **arquitetura profissional**, utilizando **OAuth2 (Google)**, **PostgreSQL**, **DragonflyDB (Redis-compatible)**, **Docker**, **Prometheus** e **Grafana**.

---

## 📌 Visão Geral

O **MyFinance** permite que usuários gerenciem:

* 👤 Usuários e perfis
* 🏦 Contas financeiras
* 💸 Transações
* 🗂️ Categorias
* 🎯 Metas financeiras
* 📊 Dashboards de controle

A autenticação é feita **exclusivamente via OAuth2 (Google)**, garantindo segurança e praticidade.

---

## 🧱 Arquitetura

O projeto segue uma arquitetura, preparada para ambientes reais de produção:

```text
┌───────────────┐
│   Frontend    │ (futuro)
└───────▲───────┘
        │
┌───────┴───────┐
│ MyFinance API │  ← Spring Boot
└───────▲───────┘
        │
┌───────┴───────────────┐
│ PostgreSQL | Dragonfly │
└───────────────────────┘
```

E com observabilidade:

```text
MyFinance API
     │
     ▼
Prometheus → Grafana
```

---

## 🛠️ Tecnologias Utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Security
* OAuth2 Client (Google)
* Spring Data JPA
* Spring Actuator

### Banco de Dados

* PostgreSQL 16
* DragonflyDB (compatível com Redis)

### Infraestrutura

* Docker
* Docker Compose
* Nginx (proxy reverso)
* Prometheus
* Grafana

### Documentação

* Swagger / OpenAPI 3

---

## 🔐 Segurança

* Autenticação via **OAuth2 (Google)**
* Controle de acesso por **roles (ADMIN / USER)**
* Sessões gerenciadas pelo Spring Security
* Endpoints sensíveis protegidos
* Actuator exposto apenas para métricas

---

## 🔑 Autenticação OAuth2

O login é feito **somente via Google**.

Após autenticação bem-sucedida:

* O usuário é criado ou atualizado no banco
* O sistema redireciona para:

```http
GET /api/v1/auth/user
```

Configuração feita via `OAuth2LoginSuccessHandler`.

---

## 📄 Documentação da API (Swagger)

Disponível em:

```http
http://localhost:8080/swagger-ui.html
```

> ⚠️ O Swagger **não realiza login OAuth2 automaticamente**.
> Para testar endpoints protegidos:

* Faça login pelo navegador via Google
* Use a mesma sessão para testar os endpoints

---

## 📊 Monitoramento

### Actuator

```http
/actuator/health
/actuator/prometheus
```

### Prometheus

Coleta métricas da aplicação.

### Grafana

Dashboards com métricas como:

* Uso de memória JVM
* Threads
* Tempo de resposta
* Requests HTTP
* Erros
* GC

---

## 🐳 Docker — Estrutura

O projeto pode ser separado em **3 ambientes (VMs)**:

```text
📁 app/
📁 db/
📁 monitoring/
```

Mas também funciona localmente com um único `docker-compose`.

---

## ▶️ Como Rodar o Projeto (Passo a Passo)

### 1️⃣ Clonar o repositório

```bash
git clone https://github.com/GustavoSDaniel/MyFinance.git
cd MyFinance
```

---

### 2️⃣ Criar arquivo `.env`

```env
POSTGRES_DB=myfinance
POSTGRES_USER=myfinance
POSTGRES_PASSWORD=senha_segura

CLIENT_ID=seu_client_id_google
CLIENT_SECRET=seu_client_secret_google
ADMIN_EMAILS=admin@email.com
```

---

### 3️⃣ Subir a aplicação

```bash
docker compose up -d
```

---

### 4️⃣ Verificar serviços

* API: `http://localhost:5050`
* Swagger: `http://localhost:5050/swagger-ui.html`
* Prometheus: `http://localhost:5054`
* Grafana: `http://localhost:5055`

---

## 🧠 Boas Práticas Aplicadas

* Clean Architecture
* DTOs e Mappers
* Separation of Concerns
* Healthchecks no Docker
* Observabilidade nativa
* Configuração via variáveis de ambiente
* Pronto para CI/CD

---

## 🚀 Próximos Passos

* Frontend (React / React Native)

---

## 👨‍💻 Autor

**Gustavo Silva Daniel**

* Backend Developer (Java / Spring Boot)
* Estudante de Desenvolvimento de Software (FATEC)
* Foco em arquitetura, cloud e microsserviços

🔗 GitHub: https://github.com/GustavoSDaniel

---

## ⭐ Contribuições

Contribuições são bem-vindas!
Sinta-se à vontade para abrir issues ou pull requests.

---

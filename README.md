# 💰 MyFinance API

API para **gerenciamento de finanças pessoais**, desenvolvida com **Java + Spring Boot**, focada em **segurança**, **testes unitarios**, **observabilidade** e **arquitetura profissional**.

O projeto simula um ambiente real de produção utilizando **OAuth2 (Google)**, **PostgreSQL**, **DragonflyDB**, **Docker**, **Prometheus** e **Grafana**.

---

## 📌 Visão Geral

O **MyFinance** permite que usuários gerenciem:

* 👤 Usuários e perfis
* 🏦 Contas financeiras
* 💸 Transações
* 🗂️ Categorias
* 🎯 Metas financeiras
* 📊 Dashboards de controle

A autenticação é feita exclusivamente via **OAuth2 (Google)**.

---

## 🧱 Arquitetura

Arquitetura preparada para ambientes reais de produção:

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

Observabilidade:

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
* DragonflyDB (Redis-compatible)

### Infraestrutura

* Docker
* Docker Compose
* Nginx (proxy reverso)
* Prometheus
* Grafana

### Testes

* JUnit 5
* Mockito

### Documentação

* Swagger / OpenAPI 3

---

## 🔐 Segurança

* Autenticação via OAuth2 (Google)
* Controle de acesso por roles (ADMIN / USER)
* Proteção de endpoints sensíveis
* Sessões gerenciadas pelo Spring Security
* Actuator exposto apenas para métricas

---

## 🧪 Testes Unitários

A camada de **Service** possui testes unitários garantindo:

* ✔ Validação das regras de negócio
* ✔ Testes de cenários de sucesso
* ✔ Testes de exceções
* ✔ Mock das dependências com Mockito

Os testes aumentam a confiabilidade da aplicação e seguem boas práticas de desenvolvimento backend.

Para rodar os testes:

```bash
mvn test
```

---

## 🔑 Autenticação OAuth2

Login realizado exclusivamente via Google.

Após autenticação:

* O usuário é criado ou atualizado no banco
* O sistema redireciona para:

```http
GET /api/v1/auth/user
```

Configuração realizada via `OAuth2LoginSuccessHandler`.

---

## 📄 Documentação da API

Swagger disponível em:

```
http://localhost:5050/swagger-ui.html
```

> ⚠️ O Swagger não realiza o fluxo OAuth2 automaticamente.
> Para testar endpoints protegidos:
>
> * Faça login via navegador
> * Utilize a mesma sessão

---

## 📊 Monitoramento

### Actuator

```
/actuator/health
/actuator/prometheus
```

### Prometheus

Coleta métricas da aplicação.

### Grafana

Dashboards monitorando:

* Uso de memória JVM
* Threads
* Tempo de resposta
* Requests HTTP
* Erros
* Garbage Collector

---

## 🐳 Executando com Docker

### 1️⃣ Clonar o projeto

```bash
git clone https://github.com/GustavoSDaniel/MyFinance.git
cd MyFinance
```

### 2️⃣ Criar arquivo `.env`

```env
POSTGRES_DB=myfinance
POSTGRES_USER=myfinance
POSTGRES_PASSWORD=senha_segura

CLIENT_ID=seu_client_id_google
CLIENT_SECRET=seu_client_secret_google
ADMIN_EMAILS=admin@email.com
```

### 3️⃣ Subir containers

```bash
docker compose up -d
```

---

## 🌍 Endpoints Locais

* API: http://localhost:5050
* Swagger: http://localhost:5050/swagger-ui.html
* Prometheus: http://localhost:5054
* Grafana: http://localhost:5055

---

## 🧠 Boas Práticas Aplicadas

* Clean Architecture
* DTO Pattern
* Mapper Pattern
* Separation of Concerns
* Testes unitários na camada de Service
* Configuração via variáveis de ambiente
* Healthchecks no Docker
* Observabilidade integrada
* Estrutura preparada para CI/CD

---

## 🚀 Próximos Passos

* Frontend (React ou React Native)

---

## 👨‍💻 Autor

**Gustavo Silva Daniel**

Backend Developer (Java / Spring Boot)
Estudante de Desenvolvimento de Software – FATEC

GitHub: https://github.com/GustavoSDaniel

---

## ⭐ Contribuições

Contribuições são bem-vindas.
Sinta-se à vontade para abrir issues ou pull requests.

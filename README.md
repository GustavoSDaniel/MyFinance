# 💰 My Finance API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white)

## 📘 Contexto

Desenvolvimento de uma API RESTful em Java com Spring Boot para gerenciar finanças pessoais. O sistema permite o controle de contas, transações, categorias e metas financeiras, com autenticação robusta via **Keycloak**, cache de alta performance com **DragonflyDB** e suporte a observabilidade completa (Prometheus/Grafana).

---

## 🧩 Arquitetura

```mermaid
flowchart LR
    Client -->|Auth| Keycloak
    Client -->|JWT| API
    API -->|Validate Token| Keycloak
    API --> PostgreSQL
    API --> DragonflyDB
    API --> Prometheus
    Prometheus --> Grafana

## ✨ Funcionalidades

### 🔐 Segurança e Usuários
* **Keycloak (OIDC):** Autenticação e autorização centralizada.
* **RBAC:** Controle de acesso baseado em roles (`ADMIN`, `USER`).
* **Endpoint /me:** Recuperação de dados do usuário autenticado via JWT.

### 💸 Gestão Financeira
* **Contas & Categorias:** CRUD completo com ativação/desativação lógica.
* **Transações:** Registro de entradas, saídas e transferências entre contas.
* **Metas (Goals):** Controle de economia com funções de depósito e saque.
* **Dashboard:** Métricas consolidadas de gastos por categoria.

### ⚙️ Infraestrutura e Boas Práticas
* **Flyway:** Evolução controlada do schema do banco de dados.
* **DragonflyDB:** Cache compatível com Redis para alta performance.
* **AOP:** Log e tratamento de aspectos transversais.
* **Watchtower:** Atualização automática de containers em produção.
* **Observabilidade:** Métricas via Actuator expostas para Prometheus e Grafana.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 4.0.1
* **Banco de Dados:** PostgreSQL 16
* **Cache:** DragonflyDB v1.37
* **Segurança:** Keycloak 23.0 & Spring Security (OAuth2 Resource Server)
* **Documentação:** Swagger / OpenAPI 3.0
* **Monitoramento:** Micrometer, Prometheus v2.52, Grafana 10.4
* **Cloud:** Oracle Cloud Infrastructure (OCI)

---

## 🐳 Serviços Docker (Infrastructure)

| Serviço | Imagem | Porta | Descrição |
| :--- | :--- | :--- | :--- |
| `my-finance-app` | Build local | `5050` | API Principal |
| `keycloak` | `keycloak:23.0` | `5053` | Identity Provider |
| `postgres` | `postgres:16-alpine` | `5051` | DB Aplicação |
| `dragonfly` | `dragonfly:v1.37.0` | `5052` | Cache Layer |
| `prometheus` | `prom/prometheus` | `5054` | Coleta de Métricas |
| `grafana` | `grafana/grafana` | `5055` | Dashboards |

---

## 🌐 Principais Endpoints

Base URL: `http://localhost:5050` | 🔒 Requer JWT (Exceto `/erros` e `/health`)

### Transações (`/api/v1/transactions`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| POST | `/` | Registrar nova transação |
| POST | `/transfer` | Transferência entre contas |
| PATCH | `/{id}/confirm` | Confirmar transação pendente |
| GET | `/search` | Listagem com filtros avançados |

### Contas e Metas
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | `/api/v1/accounts` | Listar todas as contas do usuário |
| POST | `/api/v1/goals/{id}/deposit` | Adicionar valor a uma meta |
| GET | `/api/v1/dashboards` | Resumo de gastos por categoria |

---

## ⚙️ Configuração do Ambiente

1. Renomeie o arquivo `.env.example` para `.env`.
2. Configure as variáveis conforme o exemplo abaixo:

```env
# Database
POSTGRES_DB=myfinance_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=secret

# Keycloak
CLIENT_ID=my-finance-app
CLIENT_SECRET=sua_secret_aqui
ISSUER_URI=http://localhost:5053/realms/app
ADMIN_EMAILS=seuemail@exemplo.com

# Monitoring
GF_SECURITY_ADMIN_PASSWORD=admin_grafana
```

> ⚠️ O arquivo `.env` real já está no `.gitignore`. **Jamais commite suas credenciais reais no GitHub!**

---

## 🚀 Como Executar

#### 1. Usando Docker Compose (Recomendado)

Esta é a forma mais simples e garante que todos os serviços (aplicação, banco, Keycloak, DragonflyDB, Prometheus e Grafana) subam juntos.

1. Certifique-se de ter o **Docker** e o **Docker Compose** instalados.
2. Configure o arquivo `.env` como descrito acima.
3. Abra um terminal na raiz do projeto e execute:
    ```bash
    docker compose up -d --build
    ```
4. A aplicação estará disponível em `http://localhost:5050`.

#### 2. Localmente (IDE + Docker para Serviços)

Ideal para desenvolvimento e depuração.

1. Certifique-se de ter o **Docker** e o **Docker Compose** instalados.
2. Configure o arquivo `.env`.
3. Suba apenas os serviços de infraestrutura:
    ```bash
    docker compose up -d postgres postgres-keycloak keycloak dragonfly
    ```
4. Configure sua IDE (IntelliJ, VS Code):
    * Instale o plugin `EnvFile`.
    * Vá em `Run -> Edit Configurations...`.
    * Selecione sua aplicação Spring Boot.
    * Na aba `EnvFile`, adicione o arquivo `.env` do projeto.
    * No campo **Active profiles**, digite `local`.
5. Execute a aplicação (`Application.java`) a partir da sua IDE.
6. A aplicação estará disponível em `http://localhost:5050`.

---

## 🔗 Acesso à Aplicação

| Serviço | URL | Credenciais |
| :--- | :--- | :--- |
| **API Base URL** | `http://localhost:5050` | — |
| **Swagger UI** | `http://localhost:5050/swagger-ui.html` | — |
| **Keycloak** | `http://localhost:5053` | Definidas no `.env` |
| **DragonflyDB** | `localhost:5052` | — |
| **Prometheus** | `http://localhost:5054` | — |
| **Grafana** | `http://localhost:5055` | `admin` / definido no `.env` |

---

## 🏅 Diferenciais do Projeto

* 🧭 Documentação Swagger / OpenAPI
* 🔐 Autenticação enterprise com **Keycloak** (OAuth2 / OpenID Connect)
* 🐳 Orquestração completa com Docker Compose (8 serviços)
* ⚡ Cache de alta performance com **DragonflyDB**
* 📈 Monitoramento completo (Actuator, Prometheus, Grafana)
* 🔄 Deploy contínuo com **Watchtower** (atualização automática dos contêineres)
* ☁️ Deploy em **Oracle Cloud**
* 📋 Catálogo de erros padronizados para integração com Front-end
* 🔀 AOP para separação de responsabilidades transversais

---

## 🛠️ Tags

`#Java` `#SpringBoot` `#Backend` `#FinancasPessoais`
`#API` `#RestAPI` `#Docker` `#PostgreSQL` `#JPA` `#Flyway`
`#Swagger` `#Keycloak` `#OAuth2` `#SpringSecurity`
`#DragonflyDB` `#Cache` `#Actuator` `#Prometheus` `#Grafana`
`#Watchtower` `#OracleCloud` `#CleanCode` `#SoftwareEngineering`
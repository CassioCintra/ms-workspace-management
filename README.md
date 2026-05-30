# ms-users-management

Microserviço responsável pela gestão de workspaces, RBAC, convites por e-mail e tokens de API do ecossistema **Switchboard**. Faz parte da **Fase 2** da plataforma.

> **Keycloak gerencia:** autenticação (JWT, refresh token, sessão, login, logout).  
> **Este serviço gerencia:** o que o Keycloak não cobre — workspaces, papéis por workspace, convites e tokens de API.

---

## Stack

| Componente        | Tecnologia                        |
|-------------------|-----------------------------------|
| Linguagem         | Java 25                           |
| Framework         | Spring Boot 4.0.6                 |
| Banco de dados    | PostgreSQL 16                     |
| Migrations        | Flyway                            |
| Event Bus         | Kafka 4.0 (KRaft, sem ZooKeeper)  |
| Autenticação      | Keycloak (OAuth2 Resource Server) |
| Testes            | JUnit 5 · Mockito · Testcontainers|

---

## Arquitetura

O serviço segue **arquitetura hexagonal** com as seguintes camadas:

```
domain/                         ← entidades, enums, exceções (JDK puro)
application/
  TenantContext.java            ← contexto de workspace e usuário por request (ThreadLocal)
  port/in/                      ← interfaces de casos de uso
  port/out/                     ← interfaces de repositórios e publishers
  service/                      ← orquestração dos casos de uso
adapter/
  in/web/                       ← controllers REST, filtros, exception handler
  out/persistence/              ← JPA entities, Spring Data repositories
  out/messaging/                ← produtores Kafka
  out/mail/                     ← envio de e-mail via JavaMailSender
```

### Regra de dependências

```
adapter/in/web  →  port/in  →  service  →  port/out  →  adapter/out/*
domain          ←  nunca importa camadas acima
```

---

## Multi-tenancy: schema compartilhado

Todas as tabelas vivem no schema `public`. O isolamento por workspace é feito via coluna `workspace_id` em cada tabela, filtrada pelos adapters de persistência com base no `TenantContext`.

### Schemas

| Schema    | Tabelas                                                                |
|-----------|------------------------------------------------------------------------|
| `public`  | `workspaces`, `workspace_members`, `invites`, `api_tokens`, `invite_tokens` |

### Como o contexto de workspace funciona

```
Request HTTP  →  BearerTokenAuthenticationFilter  (valida JWT com Keycloak)
              →  TenantExtractorFilter             (lê claims do JWT)
                   TenantContext.setWorkspaceId("uuid")
                   TenantContext.setUserId("sub")
                   TenantContext.setUserEmail("email")
                   TenantContext.setUserName("name")
              →  Adapters de persistência
                   workspaceId = UUID.fromString(TenantContext.getWorkspaceId())
                   → todas as queries filtram por workspace_id automaticamente
              →  finally: TenantContext.clear()
```

Endpoints públicos que precisam do workspace (ex: aceitar convite) resolvem o `workspaceId` a partir da tabela `invite_tokens` e setam o `TenantContext` manualmente antes das operações de persistência.

---

## Endpoints

Base path: `/users/v1`

### Workspaces e membros

| Método   | Rota                                          | Descrição              | Auth         |
|----------|-----------------------------------------------|------------------------|--------------|
| `POST`   | `/workspaces`                                 | Cria workspace         | JWT          |
| `GET`    | `/workspaces/{id}/members`                    | Lista membros          | JWT          |
| `PATCH`  | `/workspaces/{id}/members/{userId}/role`      | Altera papel do membro | JWT (admin)  |
| `DELETE` | `/workspaces/{id}/members/{userId}`           | Remove membro          | JWT (admin)  |

### Convites

| Método  | Rota                          | Descrição                               | Auth         |
|---------|-------------------------------|-----------------------------------------|--------------|
| `POST`  | `/workspaces/{id}/invites`    | Convida por e-mail                      | JWT (admin)  |
| `GET`   | `/invites/{token}/info`       | Retorna metadados do convite            | público      |
| `POST`  | `/invites/accept`             | Aceita o convite e entra no workspace   | JWT          |

### Tokens de API

| Método   | Rota              | Descrição                          | Auth |
|----------|-------------------|------------------------------------|------|
| `GET`    | `/tokens`         | Lista tokens do workspace          | JWT  |
| `POST`   | `/tokens`         | Cria token (retorna plain uma vez) | JWT  |
| `DELETE` | `/tokens/{id}`    | Revoga token                       | JWT  |

> Endpoints de autenticação (`/auth/*`) são responsabilidade do **Keycloak**, roteados diretamente pelo API Gateway.

### RBAC

| Papel    | Permissões                                                    |
|----------|---------------------------------------------------------------|
| `ADMIN`  | Acesso total: membros, convites, tokens, configurações        |
| `EDITOR` | Cria e edita flags; não gerencia membros nem tokens           |
| `VIEWER` | Leitura apenas                                                |

---

## Tokens de API

Tokens são gerados com `SecureRandom` (32 bytes → Base64url). Apenas o hash SHA-256 é armazenado — o valor original é retornado **uma única vez** na criação e nunca mais recuperado.

```json
// POST /tokens → 201 Created
{
  "id": "...",
  "name": "ci-token",
  "plainToken": "xK9mP2...",   ← presente apenas na criação
  "createdAt": "..."
}

// GET /tokens → 200 OK
[
  {
    "id": "...",
    "name": "ci-token",
    "lastUsedAt": null,         ← plainToken ausente (JsonInclude.NON_NULL)
    "createdAt": "..."
  }
]
```

---

## Convites

Ao convidar um usuário por e-mail:
- Um token UUID é gerado, salvo em `invites` (tenant) e em `invite_tokens` (public, com vínculo ao workspace)
- O link de aceite é enviado por e-mail: `{base-url}/invites/{token}/info`
- O convite expira em **72 horas**
- Não é possível ter dois convites `PENDING` para o mesmo e-mail no mesmo workspace (HTTP 409)

Fluxo de aceite:
1. `GET /invites/{token}/info` — qualquer pessoa com o link vê os metadados antes de autenticar
2. `POST /invites/accept` — usuário autenticado aceita; o serviço valida token, expiração e correspondência de e-mail com o JWT, adiciona o membro e invalida o token

---

## Eventos Kafka

| Tópico          | Produzido por             | Consumido por | Payload principal                                          |
|-----------------|---------------------------|---------------|------------------------------------------------------------|
| `user.invited`  | `InviteEventKafkaAdapter` | ms-audit      | `workspaceId`, `inviteeEmail`, `role`, `invitedBy`        |
| `token.revoked` | `TokenEventKafkaAdapter`  | ms-audit      | `workspaceId`, `tokenId`, `tokenName`, `revokedBy`        |

Ambos os eventos seguem o formato:
```json
{
  "action": "INVITED | REVOKED",
  "workspaceId": "uuid",
  "...",
  "invitedAt | revokedAt": "2026-05-29T..."
}
```

---

## Migrations Flyway

Todas as migrations ficam em `db/migration/` e são executadas pelo Flyway na inicialização da aplicação.

| Versão | Arquivo                                  | Conteúdo                                              |
|--------|------------------------------------------|-------------------------------------------------------|
| V1     | `V1__create_workspaces.sql`              | Tabela `workspaces`                                   |
| V2     | `V2__create_invite_tokens.sql`           | Tabela `invite_tokens` (mapa token → workspace)       |
| V3     | `V3__create_workspace_scoped_tables.sql` | Tabelas `workspace_members`, `invites`, `api_tokens`  |

---

## Configuração

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ms_users_management
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ms-users-management
  kafka:
    bootstrap-servers: localhost:9092
  mail:
    host: localhost
    port: 1025

users:
  kafka:
    topics:
      user-invited: user.invited
      token-revoked: token.revoked
  invite:
    base-url: http://localhost:8082/users/v1
    expiration-hours: 72

server:
  port: 8082
  servlet:
    context-path: /users/v1
```

---

## Testes

```bash
./mvnw test
```

| Tipo                  | Ferramentas                               | Exemplo                              |
|-----------------------|-------------------------------------------|--------------------------------------|
| Serviço (unit)        | JUnit 5 + Mockito                         | `WorkspaceServiceTest`               |
| Persistence (integr.) | Testcontainers (PostgreSQL real)          | `WorkspaceMemberPersistenceAdapterTest` |
| Messaging (unit)      | Mockito + `ReflectionTestUtils`           | `InviteEventKafkaAdapterTest`        |
| Web (slice)           | `@WebMvcTest` + `@WithMockUser`           | `WorkspaceControllerTest`            |
| Smoke test            | Testcontainers (PostgreSQL + Kafka)       | `MsUsersManagementApplicationTests` |

> Nunca usa H2 ou bancos in-memory — todos os testes de persistência rodam contra PostgreSQL real via Testcontainers.

---

## Contexto no ecossistema Switchboard

```
Fase 1 — ms-feature-flags     ✅ concluída
Fase 2 — ms-users-management  ✅ este serviço
Fase 3 — Spring Cloud Gateway  ⏳ próximo
Fase 4 — ms-audit              ⏳
Fase 5 — ms-dashboard (BFF)    ⏳
```

Documentação completa da plataforma: [`platform-ops/ARCHITECTURE.md`](../platform-ops/ARCHITECTURE.md)

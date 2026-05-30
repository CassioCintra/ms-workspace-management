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
| E-mail            | Resend (SMTP) / MailHog (dev)     |
| Testes            | JUnit 5 · Mockito · Testcontainers|

---

## Arquitetura

O serviço segue **arquitetura hexagonal** com pacotes organizados por feature dentro de cada camada:

```
domain/
  workspace/    ← Workspace, WorkspaceMember, WorkspaceRole
  invite/       ← Invite, InviteStatus
  token/        ← ApiToken
  exception/    ← exceções de domínio

application/
  TenantContext.java   ← contexto de workspace e usuário por request (ThreadLocal)
  port/in/             ← interfaces de casos de uso
  port/out/            ← interfaces de repositórios e publishers
  service/             ← orquestração dos casos de uso

adapter/
  in/web/
    workspace/  ← WorkspaceController, InviteController
    invite/     ← InviteAcceptController
    token/      ← ApiTokenController
    config/     ← SecurityConfig
    filter/     ← TenantExtractorFilter, CorrelatorFilter
    request/    ← DTOs de entrada
    response/   ← DTOs de saída
  out/
    persistence/
      workspace/ ← adapters e repositórios JPA de workspace
      invite/    ← adapters e repositórios JPA de invite
      token/     ← adapters e repositórios JPA de token
      entity/    ← entidades JPA
    messaging/  ← produtores Kafka
    mail/       ← envio de e-mail via Resend/JavaMailSender
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

Controllers com `{id}` no path (ex: `/workspaces/{id}/invites`) setam o `TenantContext` diretamente a partir do path variable, eliminando a dependência do claim `workspace_id` no JWT para rotas workspace-scoped.

```
Request HTTP  →  BearerTokenAuthenticationFilter  (valida JWT com Keycloak)
              →  TenantExtractorFilter             (lê claims do JWT)
                   TenantContext.setUserId("sub")
                   TenantContext.setUserEmail("email")
                   TenantContext.setUserName("name")
                   TenantContext.setWorkspaceId("uuid")  ← JWT ou path variable
              →  Adapters de persistência
                   workspaceId = UUID.fromString(TenantContext.getWorkspaceId())
                   → todas as queries filtram por workspace_id automaticamente
              →  finally: TenantContext.clear()
```

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

| Método  | Rota                             | Descrição                               | Auth         |
|---------|----------------------------------|-----------------------------------------|--------------|
| `POST`  | `/workspaces/{id}/invites`       | Convida por e-mail                      | JWT (admin)  |
| `GET`   | `/invites/{token}/info`          | Retorna metadados do convite            | público      |
| `POST`  | `/invites/accept`                | Aceita o convite e entra no workspace   | JWT          |

### Tokens de API

| Método   | Rota                              | Descrição                          | Auth |
|----------|-----------------------------------|------------------------------------|------|
| `GET`    | `/workspaces/{id}/tokens`         | Lista tokens do workspace          | JWT  |
| `POST`   | `/workspaces/{id}/tokens`         | Cria token (retorna plain uma vez) | JWT  |
| `DELETE` | `/workspaces/{id}/tokens/{tokenId}` | Revoga token                     | JWT  |

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
// POST /workspaces/{id}/tokens → 201 Created
{
  "id": "...",
  "name": "ci-token",
  "plainToken": "xK9mP2...",   ← presente apenas na criação
  "createdAt": "..."
}

// GET /workspaces/{id}/tokens → 200 OK
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

Fluxo completo de convite via e-mail:

1. **Admin envia convite** → `POST /workspaces/{id}/invites`
   - Token UUID gerado, salvo em `invites` e `invite_tokens`
   - E-mail HTML enviado via Resend com link para o frontend: `{frontend-url}/invites/{token}`
   - Evento `user.invited` publicado no Kafka
   - Convite expira em **72 horas**; duplicata PENDING para mesmo e-mail retorna HTTP 409

2. **Convidado abre o link** → `GET /invites/{token}/info` (público, sem autenticação)
   - Frontend exibe workspace, papel e expiração

3. **Convidado se registra/autentica** no Keycloak
   - Auto-registro habilitado no realm (`registrationAllowed=true`)

4. **Convidado aceita** → `POST /invites/accept`
   - Valida: token existente, status PENDING, não expirado, e-mail do JWT == e-mail do convite
   - Adiciona membro ao workspace, marca convite como ACCEPTED, invalida o token

---

## E-mail de convite

Template HTML responsivo em `src/main/resources/templates/email/invite.html`, renderizado com substituição de placeholders em `InviteMailAdapter`. Exibe workspace, papel, expiração e botão de aceite apontando para o frontend.

Configuração:
- **Desenvolvimento:** MailHog (`localhost:1025`) — captura e-mails localmente em `http://localhost:8025`
- **Produção:** Resend (`smtp.resend.com:465`) via `RESEND_API_KEY`

---

## Eventos Kafka

| Tópico          | Produzido por             | Consumido por | Payload principal                                          |
|-----------------|---------------------------|---------------|------------------------------------------------------------|
| `user.invited`  | `InviteEventKafkaAdapter` | ms-audit      | `workspaceId`, `inviteeEmail`, `role`, `invitedBy`        |
| `token.revoked` | `TokenEventKafkaAdapter`  | ms-audit      | `workspaceId`, `tokenId`, `tokenName`, `revokedBy`        |

---

## Migrations Flyway

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
    bootstrap-servers: localhost:9094   # porta externa do container
  mail:
    host: smtp.resend.com               # ou localhost para dev (MailHog)
    port: 465
    username: resend
    password: ${RESEND_API_KEY}

users:
  kafka:
    topics:
      user-invited: user.invited
      token-revoked: token.revoked
  invite:
    base-url: http://localhost:8082/users/v1
    frontend-url: http://app.switchboard.io  # URL do frontend para links do e-mail
    expiration-hours: 72
  mail:
    from: "Switchboard <onboarding@resend.dev>"

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

# caderneta-user-services

Microserviço para gerenciamento de usuários (criação, consulta, atualização parcial, roles e upload de avatar).

Status: badge de CI (se aplicável) — ver arquivo original do repositório para badges.

Sumário
- Visão geral
- Arquitetura
- Endpoints REST
- Modelos e DTOs
- Mapeamento (MapStruct)
- Autenticação e autorização
- Cache
- Configuração (variáveis e perfis)
- Scripts de banco de dados
- Build e execução local
- Docker / docker-compose
- Testes
- Logging / monitoring
- Próximos passos


Visão geral
---------
Este projeto é um microserviço em Java (Spring Boot) responsável por gerenciar usuários da aplicação. Ele oferece endpoints para criação de usuários, consulta por e-mail ou id, login (consulta) e atualização parcial do usuário (nome, senha e foto). Também integra com provedor de armazenamento em nuvem para avatar e usa cache Redis para acelerar consultas.

Stack principal
- Java 17
- Spring Boot (versão definida em `pom.xml`)
- Spring Data JPA (PostgreSQL)
- Redis para cache (configurado via properties)
- MapStruct para mapeamento entidade <-> DTO
- Lombok para reduzir boilerplate
- SpringDoc (OpenAPI/Swagger) para documentação
- Dependências internas: `com.br.azevedo.*` (cache, security, utils)

Arquitetura
---------
Componentes principais:
- Controller (`br.com.user.controller`): camada de API REST.
- Service (`br.com.user.service` / `service.impl`): regras de negócio e integração com storage/caches.
- Repository (`br.com.user.repository`): interface JPA para persistência (Postgres).
- Model (`br.com.user.model`) e DTOs (`br.com.user.model.dto`): entidades e objetos de transferência.
- Mapper (`br.com.user.mapper`): MapStruct para conversões entre entidades e DTOs.
- Configurações: `DatabaseConfig`, `ApiConfig`, `application.yml` e `infra/`.

Fluxo típico de requisição
1. Requisição HTTP chega ao `UserController`.
2. Controller delega para `UserServiceImpl`.
3. Service aplica regras (ex.: validações, encoding de senha, upload de foto) e persiste via `IUserRepository`.
4. Service atualiza/evicta caches conforme necessário.
5. Response é retornada (DTOs sem campo `password`).

Endpoints REST
---------
Base path: `/v1/user`

1) Create User
- POST /v1/user
- Request body: `UserDTO` (ex.: name, email, password)
- Response: 200 OK (void)
- Observações: validação via `@Valid`; se o e-mail já existir, retorna erro (lança `UserException`).

2) Login (consulta por email com dados de login)
- GET /v1/user/login/{email}
- Response: `UserDTO` (método `login` mapeia para `toDTOLogin` — pode conter menos campos sensíveis)
- Cache: `user_services_cliente_login_email`

3) Find by email
- GET /v1/user/{email}
- Response: `UserDTO` (mapeado por `toDTO` — senha é ignorada nas respostas)
- Cache: `user_services_cliente_por_email`

4) Find by id
- GET /v1/user/code/{id}
- Response: `UserDTO`
- Cache: `user_services_cliente_por_id`

5) Find all
- GET /v1/user
- Response: List<UserDTO>

6) Update (parcial)
- PATCH /v1/user/{email}
- Request body: `UserDTO` parcial — apenas `name`, `password` e campos de foto são aplicados; campos `email`, `status` e `perfis/roles` são preservados e não podem ser alterados por este endpoint.
- Response: `UserDTO` atualizado
- Cache: o método `update` faz evict em caches:
  - `user_services_cliente_por_id` (key = result.id)
  - `user_services_cliente_por_email` (key = result.email)
  - `user_services_cliente_login_email` (key = result.email)

Observações sobre endpoints
- Os DTOs exigem `email` e `password` em `UserDTO` para criação; no `toDTO` MapStruct existe mapeamento que ignora `password` ao retornar os dados.
- Swagger/OpenAPI está disponível via SpringDoc (dependência em `pom.xml`). Em tempo de execução, a UI costuma ficar em `/swagger-ui/index.html` e a spec em `/v3/api-docs`.

Modelos e DTOs
---------
Principais classes:
- `br.com.user.model.User` — entidade JPA mapeada para tabela `TB_USER`:
  - Campos relevantes: `id`, `name`, `email`, `password` (coluna `PWD`), `status`, `createdAt` (DT_CADASTRO), `updatedAt` (DT_UPDATE), `photo` e `roles` (ManyToMany com `Role`).
  - Observação: `createdAt` e `updatedAt` anotados para gerenciar timestamps.

- `br.com.user.model.Role` — enum `PerfilEnum` usado para roles: `ROLE_ADMIN`, `ROLE_USER`, `ROLE_APPLICATION`.

- `br.com.user.model.dto.UserDTO` — DTO para entrada/saída:
  - Campos: `id`, `name`, `email`, `password`, `status`, `perfis`, `photo`, `photoUploaded`, `photoUpdate`.
  - Anotações de validação (`@NotEmpty`, `@Email`) se aplicam em criação.

Mapeamento (MapStruct)
---------
- `UserMapper` usa MapStruct para converter entre `User` e `UserDTO`.
- Regras importantes:
  - Ao converter `User` -> `UserDTO`, o campo `password` é ignorado (`@Mapping(target="password", ignore = true)`), portanto respostas não incluem senha.
  - `perfis` (List<PerfilEnum>) é resolvido a partir de `roles` na entidade.
  - `toEntity` mapeia `name` e `email` a partir do DTO (usado durante criação).

Autenticação e autorização
---------
- O projeto injeta habilitações via `@EnableSecurity` (pacote `com.br.azevedo.security`), configurado em `Application.java`.
- Detalhes da implementação de segurança (JWT, OAuth2, filtro HTTP) estão abstraídos pela dependência `commons-security` / `com.br.azevedo` e não estão todos expostos no código-fonte visível.
- Em `application.yml` existe `security.enabled` que pode ser `true/false`.

Cache
---------
- Redis é usado como provedor de cache (configurado em `application.yml`).
- Caches configurados em `application.yml`:
  - `user_services_cliente_por_email` (ex.: TTL `${CACHE_DURATION_CONSULTA_PESSOA_EMAIL:PT72H}`)
  - `user_services_cliente_login_email` (ex.: TTL `${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT72H}`)
  - `user_services_cliente_por_id` (ex.: TTL `${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT168H}`)
- Métodos anotados com `@Cacheable`:
  - `findByEmail` → cache por e-mail
  - `findById` → cache por id
  - `login` → cache por login e-mail
- Invalidação: o `update` do usuário usa `@Caching(evict = {...})` para remover entradas por id e por email (tanto consulta padrão quanto login) quando um usuário é atualizado.

Configuração (application.yml e variáveis de ambiente)
---------
As propriedades principais (em `src/main/resources/application.yml`):
- Server:
  - `server.port` (padrão 8080)
- Database (chaves usadas pelo `DatabaseConfig`):
  - `database.url` (ex.: `jdbc:postgresql://localhost:5432/usuario_db`)
  - `database.driver-class-name`
  - `database.user`
  - `database.pass`
  - `database.default_schema`
- Security:
  - `security.enabled` (controla habilitação de segurança)
  - Configurações de Vault (se usadas) para segredos
- Cloud/AWS S3:
  - `cloud.aws.credentials.access-key`
  - `cloud.aws.credentials.secret-key`
  - `cloud.aws.region.static`
  - `cloud.aws.s3.bucket`
- Cache/Redis:
  - `cache.redis.config.host`/`port`/`database`/`password`
  - `cache.redis.config.caches` — lista com nomes e `expiration`

Exemplo de variáveis de ambiente (usadas por Docker/composer):
- DATABASE_URL, DATABASE_USER, DATABASE_PASS, REDIS_HOST, REDIS_PORT, REDIS_PASSWORD, AWS_BUCKET_NAME, PORT, SECURITY_ENABLE

Scripts de banco de dados
---------
- `database/V01__CREATE_TABLES.sql` — cria sequências, tipos (`DOMINIO_STATUS`), tabelas `TB_USER`, `TB_ROLE` e `TB_USER_ROLE` e insere roles e um usuário inicial.
- `database/V02__DADOS.sql` — (quando presente) contém dados iniciais adicionais.
- Observação: não há Flyway/Liquibase configurado explicitamente no `pom.xml` — os scripts podem ser executados manualmente ou adicionar Flyway para migrações automatizadas.

Build e execução local
---------
Pré-requisitos
- JDK 17
- Maven (ou usar `./mvnw` wrapper presente no projeto)

Build
```
./mvnw -DskipTests package
```
OU
```
mvn -DskipTests package
```

Executar JAR
```
java -jar target/user-services-0.0.1-SNAPSHOT.jar
```
Passar profile / variáveis
```
java -jar -Dspring.profiles.active=dev -Ddatabase.url=jdbc:... target/...jar
```

Swagger / OpenAPI
- Após iniciar a aplicação, acessar `/swagger-ui/index.html` ou `/v3/api-docs` para a spec OpenAPI.

Docker / docker-compose
---------
Dockerfile (resumo): copia JAR e roda `java -jar /app/app.jar`.

Build da imagem local:
```
docker build -t user-services:local .
```
Run com Docker (exemplo simplificado):
```
docker run -e DATABASE_URL=jdbc:postgresql://host:5432/db -p 8080:8080 user-services:local
```

Usando docker-compose (arquivo `docker-compose.yml`):
- O compose define um serviço `user-services` (imagem `sirondba/user-services:latest`) e network `automacao-net`.
- Para desenvolvimento local, você pode descomentar a seção `build` no `docker-compose.yml` e executar:
```
docker-compose up --build
```

Testes
---------
- Rodar testes unitários/integrados:
```
./mvnw test
```
- Há um profile `test` e arquivo `src/test/resources/application-test.properties` usado pelos testes.

Logging e monitoramento
---------
- `logback.xml` está presente em `src/main/resources` para configurar logs.
- Actuator está nas dependências (`spring-boot-starter-actuator`) — exponha endpoints conforme configuração de segurança e propriedades.

Próximos passos e melhorias sugeridas
---------
- Documentar claramente o fluxo de autenticação (token, introspecção) e exemplos de como consumir endpoints protegidos.
- Adicionar migração automatizada (Flyway) para versionamento dos scripts SQL.
- Completar cobertura de testes (unitários e de integração) para `UserServiceImpl` e controller.
- Hardening de segurança: checar políticas de CORS, rate-limiting, validação de senhas.
- Incluir exemplos de requests/responses reais no README (ex.: curl) e coleções Postman / Insomnia.
- Automatizar build e deploy via CI (GitHub Actions / GitLab CI) usando o `Dockerfile`.

Referências rápidas e caminhos
- Código fonte: `src/main/java/br/com/user`
- Controllers: `src/main/java/br/com/user/controller/UserController.java`
- Services: `src/main/java/br/com/user/service` e `service/impl/UserServiceImpl.java`
- Repositories: `src/main/java/br/com/user/repository`
- Model/DTO: `src/main/java/br/com/user/model` e `src/main/java/br/com/user/model/dto`
- Mappers: `src/main/java/br/com/user/mapper/UserMapper.java`
- Config: `src/main/java/br/com/user/config` e `src/main/resources/application.yml`
- DB scripts: `database/V01__CREATE_TABLES.sql`, `database/V02__DADOS.sql`
- Docker: `Dockerfile`, `docker-compose.yml`

---

Se quiser, eu:
- incluo exemplos JSON para cada endpoint (requests e responses),
- gero uma imagem PlantUML com diagrama da arquitetura e coloco em `docs/`,
- e/ou abro PR com README.md atualizado (neste repositório já apliquei a alteração localmente).

Diga qual desses extras prefere que eu adicione em seguida.


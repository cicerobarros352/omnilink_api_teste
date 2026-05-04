# Omnilink API

API REST para gerenciamento de **veículos** e **clientes**, com autenticação JWT, cache, Swagger e testes.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.5 |
| Spring Security | 6.x |
| Hibernate / JPA | 6.x |
| MySQL | 8.x |
| JJWT | 0.12.3 |
| springdoc-openapi | 2.8.6 |
| Lombok | latest |

## Funcionalidades

- **CRUD completo** de Veículos e Clientes
- **Autenticação e Autorização** via JWT
  - Role `USER`: criar, ler e atualizar
  - Role `ADMIN`: também pode deletar
- **Validações** de entrada (CPF, placa, email, ano, etc.)
- **Cache em memória** para leituras com Spring Cache
- **Documentação** interativa via Swagger UI
- **Logging** com SLF4J/Logback
- **Controle de transações** (`@Transactional`)
- **Testes unitários** (Mockito) e de camada web (MockMvc)

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- MySQL 8.x em execução

---

## Configuração do Banco de Dados

1. Crie o banco ou deixe o Spring criar automaticamente (`createDatabaseIfNotExist=true`):

```sql
CREATE DATABASE IF NOT EXISTS omnilink_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

> O DDL completo está em [`schema.sql`](./schema.sql) na raiz do projeto.

2. Edite as credenciais em `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/omnilink_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: SEU_USUARIO
    password: SUA_SENHA
```

---

## Executando o Projeto

### Com Maven

```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou gerar o JAR e executar
./mvnw clean package -DskipTests
java -jar target/omnilink-api-1.0.0.jar
```

A API estará disponível em: `http://localhost:8080`

---

## Documentação Interativa (Swagger UI)

Acesse após iniciar a aplicação:

```
http://localhost:8080/swagger-ui.html
```

---

## Autenticação

### 1. Registrar usuário

```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "joao",
  "email": "joao@email.com",
  "password": "senha123"
}
```

### 2. Fazer login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "joao",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "joao",
  "email": "joao@email.com",
  "role": "USER"
}
```

### 3. Usar o token nas requisições

```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Endpoints

### Veículos (`/api/vehicles`)

| Método | Endpoint | Descrição | Role mínima |
|--------|----------|-----------|-------------|
| `POST` | `/api/vehicles` | Criar veículo | USER |
| `GET` | `/api/vehicles` | Listar (paginado) | USER |
| `GET` | `/api/vehicles/{id}` | Buscar por ID | USER |
| `PUT` | `/api/vehicles/{id}` | Atualizar | USER |
| `DELETE` | `/api/vehicles/{id}` | Remover | ADMIN |

**Exemplo de criação:**
```json
{
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2023,
  "plate": "ABC1D23",
  "color": "Prata",
  "price": 120000.00,
  "status": "AVAILABLE"
}
```

**Status disponíveis:** `AVAILABLE`, `RESERVED`, `SOLD`

**Formato de placa aceito:**
- Padrão antigo: `ABC1234`
- Padrão Mercosul: `ABC1D23`

### Clientes (`/api/customers`)

| Método | Endpoint | Descrição | Role mínima |
|--------|----------|-----------|-------------|
| `POST` | `/api/customers` | Criar cliente | USER |
| `GET` | `/api/customers` | Listar (paginado) | USER |
| `GET` | `/api/customers/{id}` | Buscar por ID | USER |
| `PUT` | `/api/customers/{id}` | Atualizar | USER |
| `DELETE` | `/api/customers/{id}` | Remover | ADMIN |

**Exemplo de criação:**
```json
{
  "name": "Maria Souza",
  "cpf": "529.982.247-25",
  "email": "maria.souza@email.com",
  "phone": "(11) 99999-9999",
  "address": "Rua das Flores, 100 - São Paulo/SP"
}
```

### Paginação

Todos os endpoints de listagem suportam paginação via query params:

```
GET /api/vehicles?page=0&size=10&sort=brand,asc
GET /api/customers?page=0&size=20&sort=name,desc
```

---

## Rodando os Testes

```bash
./mvnw test
```

### Cobertura dos testes

- **Unitários:** `VehicleServiceTest`, `CustomerServiceTest`, `CpfValidatorTest`
- **Web (MockMvc):** `VehicleControllerTest`, `AuthControllerTest`

---

## DDL — Tabelas do Banco de Dados

O arquivo [`schema.sql`](./schema.sql) na raiz do projeto contém os comandos DDL completos para:

- `users` — usuários do sistema
- `vehicles` — veículos
- `customers` — clientes

> Em desenvolvimento, o Hibernate pode gerenciar o schema automaticamente via `ddl-auto: update`. Para produção, use `ddl-auto: validate` e aplique o `schema.sql` manualmente.

---

## Variáveis de Ambiente (Produção)

Para produção, externalizar via variáveis de ambiente:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://seu-host:3306/omnilink_db
export SPRING_DATASOURCE_USERNAME=usuario
export SPRING_DATASOURCE_PASSWORD=senha_segura
export JWT_SECRET=seu-secret-base64-de-pelo-menos-32-bytes
```

---

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/cicerobarros/omnilink_api_teste/
│   │   ├── config/          # SecurityConfig, SwaggerConfig, CacheConfig
│   │   ├── controller/      # AuthController, VehicleController, CustomerController
│   │   ├── dto/
│   │   │   ├── request/     # LoginRequest, RegisterRequest, VehicleRequest, CustomerRequest
│   │   │   └── response/    # AuthResponse, VehicleResponse, CustomerResponse
│   │   ├── entity/          # User, Vehicle, Customer
│   │   │   └── enums/       # Role, VehicleStatus
│   │   ├── exception/       # GlobalExceptionHandler, ErrorResponse, exceptions
│   │   ├── repository/      # UserRepository, VehicleRepository, CustomerRepository
│   │   ├── security/        # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
│   │   ├── service/         # AuthService, VehicleService, CustomerService
│   │   └── validation/      # ValidCpf, CpfValidator
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/cicerobarros/omnilink_api_teste/
        ├── controller/      # AuthControllerTest, VehicleControllerTest
        ├── service/         # VehicleServiceTest, CustomerServiceTest
        └── validation/      # CpfValidatorTest
```

# 🔐 Sistema de Autenticação de Usuários

API REST desenvolvida com Java e Spring Boot para cadastro, autenticação e gerenciamento de usuários. O objetivo é aplicar conceitos de segurança, persistência de dados.

## Ferramentas Utilizadas
- **Backend:** Java 21, Spring Boot, Spring Security, Bean Validation, Lombok
- **Segurança:** JWT, BCrypt
- **Persistência:** PostgreSQL e Spring Data JPA
- **Comunicação:** Java Mail Sender
- **Desenvolvimento e documentação:** Maven, Swagger/OpenAPI
- **Testes:** JUnit + Mockito
- **Infraestrutura:** Docker

---

## Funcionalidades

## ✨ Funcionalidades

- **Cadastro**: validação e normalização de dados, e-mails duplicados bloqueados, senha protegida com BCrypt
- **Verificação de conta**: código de 6 dígitos enviado por e-mail, obrigatório antes do primeiro login
- **Login**: autenticação via JWT, endpoints protegidos, bloqueio temporário após tentativas incorretas
- **Recuperação de senha**: código temporário por e-mail, com expiração e proteção contra múltiplas tentativas
- **Tratamento de erros**: respostas padronizadas via `GlobalExceptionHandler`, sem expor detalhes internos


## Fluxo do Sistema
Diagrama dos fluxos de login, cadastro e recuperação de senha
[Login](docs/images/fluxo_login.drawio.png) <br>
[Cadastro](docs/images/fluxo_cadastro.drawio.png) <br>
[Recuperação de Senha](docs/images/fluxo_recuperacao_senha.drawio.png)

---

## Endpoints
| Método | Rota                        | Autenticação | Descrição |
|---|-----------------------------|---|---|
| `POST` | `/usuarios/cadastro`        | Pública | Cadastra um novo usuário e envia código de verificação por e-mail |
| `POST` | `/usuarios/verificar-conta` | Pública | Confirma a conta a partir do código enviado por e-mail |
| `POST` | `/usuarios/login`           | Pública | Autentica o usuário e retorna o token JWT |
| `POST` | `/usuarios/recuperar-senha` | Pública | Solicita o código de recuperação de senha por e-mail |
| `POST` | `/usuarios/confirmar-senha` | Pública | Valida o código de recuperação e define a nova senha |
| `POST` | `/usuarios/sair`            | Protegida | Realiza o logout |
| `GET` | `/usuarios/perfil`          | Protegida | Retorna os dados do usuário autenticado (endpoint de demonstração do JWT) |

> Rotas protegidas exigem o header `Authorization: Bearer <token>` obtido no login
---
## Arquitetura e Design
A aplicação utiliza a **Arquitetura em Camadas**, separando as responsabilidades entre exposição da API, regras de negócio, persistência e segurança.
```text
src/main/java/com/c4amila/LoginAuthentication
│
├── config/
│   └── configurações do Spring Security e da aplicação
│
├── controller/
│   └── endpoints REST
│
├── dto/
│   └── objetos de entrada e saída
│
├── exception/
│   ├── exceções personalizadas
│   └── tratamento global das exceções
│
├── model/
│   └── entidades persistidas
│
├── repository/
│   └── acesso ao banco de dados
│
├── security/
│   ├── filtro JWT
│   ├── geração e validação de tokens
│   ├── integração com UserDetails
│   └── tratamento de autenticação
│
└── service/
    └── regras de negócio
```

**Decisões de design:**
- DTOs próprios de entrada/saída — a entidade `Usuario` nunca é exposta diretamente na API
- Autenticação **stateless**: nenhuma sessão é mantida no servidor, apenas validação do JWT a cada requisição
- Senhas e códigos temporários nunca armazenados em texto puro (BCrypt)
- Bloqueio automático após tentativas inválidas em login, verificação e recuperação de senha

---

## Melhorias futuras
- Refresh token
- Rate limiting
- Revogação de token no logout
- Roles/perfis de acesso
- Maior expansão nos testes unitários e de integração
- Observabilidade e logging

## ⚙️ Como executar o projeto

### Pré-requisitos

- Java 21
- Maven
- PostgreSQL (local ou via Docker)
- Uma conta de teste em algum serviço SMTP ([Mailtrap](https://mailtrap.io)) para o envio dos e-mails de verificação e recuperação

### Passo a passo

1. Clone o repositório:
```bash
   git clone https://github.com/c4amila/LoginAuthentication.git
   cd LoginAuthentication
```

2. Configure as variáveis de ambiente. Copie o `.env.example` para um novo arquivo `.env` e preencha com seus próprios valores:

   | Variável | Descrição |
      |---|---|
   | `DB_URL` | URL de conexão com o PostgreSQL |
   | `DB_USERNAME` | Usuário do banco de dados |
   | `DB_PASSWORD` | Senha do banco de dados |
   | `MAIL_USERNAME` | Usuário/token do serviço SMTP |
   | `MAIL_PASSWORD` | Senha/token do serviço SMTP |
   | `JWT_SECRET` | Chave secreta usada para assinar os tokens JWT (mínimo 256 bits) |

> No IntelliJ, essas variáveis precisam ser adicionadas manualmente em **Run/Debug Configurations → Environment Variables**, já que o projeto não carrega o `.env` automaticamente fora do Docker.

3. Execute a aplicação:
```bash
   ./mvnw spring-boot:run
```

4. Acesse a documentação interativa da API:
http://localhost:8080/swagger-ui/index.html

### Executando com Docker (opcional)

O projeto também inclui `Dockerfile` e `docker-compose.yml`, subindo a aplicação já conectada a um container PostgreSQL:

```bash
docker-compose up --build
```

## 👩‍💻 Autora
Projeto desenvolvido por [Camila Ferreira](https://github.com/c4amila)


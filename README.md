# API de Consulta de CEP

API REST desenvolvida em Java com Spring Boot para consulta de endereços brasileiros através do CEP (Código de Endereçamento Postal). Integra-se com a API ViaCEP para fornecer dados de logradouro, bairro, cidade, UF e informações adicionais de forma simples e eficiente.

## Key Features

- **Consulta de CEP** — Busca endereços completos a partir de qualquer CEP brasileiro válido
- **Validação Inteligente** — Aceita CEPs com ou sem hífen, com validação automática de formato
- **Tratamento de Erros Robusto** — Respostas padronizadas para CEPs inválidos, não encontrados ou erros de conexão
- **SSL Flexível** — Configuração SSL permissiva para ambiente de desenvolvimento
- **Testes Automatizados** — Script bash completo para validação de todos os cenários de uso
- **Arquitetura Limpa** — Separação clara entre controllers, services, DTOs e tratamento de exceções

---

## Tech Stack

- **Linguagem**: Java 25
- **Framework**: Spring Boot 4.0.5
- **Build Tool**: Gradle 8.x
- **HTTP Client**: Apache HttpClient 5
- **JSON**: Jackson (via Spring Boot)
- **Utils**: Project Lombok
- **Testes**: JUnit 5 (Jupiter)

---

## Prerequisites

Antes de iniciar, certifique-se de ter instalado:

- **Java JDK 25** ou superior ([Download Oracle](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://openjdk.org/))
- **Gradle** 8.x (opcional — o wrapper está incluído)
- **cURL** (para execução dos testes via script)
- **Git** (para clonar o repositório)

---

## Getting Started

### 1. Clone o Repositório

```bash
git clone git@github.com:caarlosandree/consultaCep.git
cd consultaCep
```

### 2. Compile o Projeto

Usando o Gradle Wrapper (recomendado):

```bash
./gradlew build
```

No Windows:

```bash
gradlew.bat build
```

### 3. Execute a Aplicação

```bash
./gradlew bootRun
```

A API estará disponível em `http://localhost:8080`

### 4. Teste a API

Execute o script de testes automatizados:

```bash
chmod +x test-api.sh
./test-api.sh
```

Ou consulte manualmente:

```bash
curl http://localhost:8080/consulta/01001000
```

---

## API Endpoints

### GET /consulta/{cep}

Consulta endereço pelo CEP.

#### Parâmetros

| Parâmetro | Tipo   | Descrição                    | Exemplo   |
|-----------|--------|------------------------------|-----------|
| cep       | String | CEP brasileiro (8 dígitos)   | 01001000  |

#### Sucesso — HTTP 200

```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "uf": "SP",
  "codigoIbge": "3550308",
  "gia": "1004",
  "ddd": "11",
  "siafi": "7107"
}
```

#### Erros

| Código HTTP | Situação                              | Exemplo de Resposta |
|-------------|---------------------------------------|---------------------|
| 400         | CEP inválido (formato incorreto)      | `{"errorCode":"CEP_INVALIDO","message":"O CEP '123' informado é inválido...","status":400}` |
| 404         | CEP não encontrado na base ViaCEP     | `{"errorCode":"CEP_NAO_ENCONTRADO","message":"O CEP '00000000' não foi encontrado...","status":404}` |
| 405         | Método HTTP não permitido             | `{"errorCode":"METODO_NAO_PERMITIDO","message":"Método HTTP 'POST' não é suportado...","status":405}` |
| 503         | Serviço ViaCEP indisponível           | `{"errorCode":"API_EXTERNA_INDISPONIVEL","message":"Serviço de consulta de CEP indisponível...","status":503}` |
| 500         | Erro interno inesperado               | `{"errorCode":"ERRO_INTERNO","message":"Ocorreu um erro interno no servidor.","status":500}` |

---

## Architecture

### Estrutura de Diretórios

```
src/
├── main/
│   ├── java/com/api/cep/
│   │   ├── CepApplication.java              # Entry point Spring Boot
│   │   ├── exceptions/                      # Exceções customizadas e handler global
│   │   │   ├── CepApiException.java         # Exceção base
│   │   │   ├── CepInvalidoException.java   # CEP formato inválido
│   │   │   ├── CepNaoEncontradoException.java # CEP não existe na ViaCEP
│   │   │   ├── CepApiExternaException.java  # Erro de conexão ViaCEP
│   │   │   ├── ErrorResponse.java           # DTO de resposta de erro
│   │   │   └── GlobalExceptionHandler.java  # @RestControllerAdvice centralizado
│   │   └── modules/
│   │       ├── config/
│   │       │   └── RestTemplateConfig.java  # Configuração SSL/HttpClient
│   │       ├── controllers/
│   │       │   └── cepController.java       # REST Controller
│   │       ├── dto/
│   │       │   └── cep.java                 # DTO de resposta ViaCEP
│   │       └── services/
│   │           └── cepRequest.java          # Lógica de negócio + integração
│   └── resources/
│       └── application.properties           # Configurações da aplicação
└── test/
    └── java/com/api/cep/
        └── CepApplicationTests.java         # Testes de contexto Spring
```

### Fluxo de Requisição

```
Requisição HTTP
      ↓
@RestController (cepController)
      ↓
@Validação automática / cepRequest.getCep()
      ↓
RestTemplate → ViaCEP API (viacep.com.br)
      ↓
DTO cep populado ou Exceção lançada
      ↓
@RestControllerAdvice (GlobalExceptionHandler)
      ↓
Resposta JSON padronizada
```

### Componentes Principais

#### Controller (`cepController.java`)

Expõe o endpoint `/consulta/{cep}` com suporte a GET. Validações básicas e delegação para service.

#### Service (`cepRequest.java`)

- **Validação**: Remove caracteres não numéricos e verifica 8 dígitos
- **Integração**: Consome API ViaCEP via `RestTemplate`
- **Mapeamento**: Converte JSON ViaCEP para DTO interno
- **Tratamento**: Converte exceções HTTP em exceções de negócio

#### Exception Handler (`GlobalExceptionHandler.java`)

Handler centralizado que captura todas as exceções e retorna `ErrorResponse` com:
- Código de erro (errorCode)
- Mensagem legível
- Path da requisição
- Timestamp
- Status HTTP

#### Configuração SSL (`RestTemplateConfig.java`)

Configura `RestTemplate` com `HttpClient 5` aceitando qualquer certificado SSL — útil para desenvolvimento e ambientes com certificados autoassinados.

---

## Scripts de Teste

### test-api.sh

Script bash completo para validação da API:

```bash
./test-api.sh [API_URL]
```

| Variável   | Padrão                | Descrição                    |
|------------|-----------------------|------------------------------|
| API_URL    | http://localhost:8080 | URL base da API de consulta  |

**Cenários testados:**
- ✅ CEPs válidos (SP, RJ, BH)
- ✅ CEP com hífen
- ❌ CEP vazio
- ❌ CEP muito curto/longo
- ❌ CEP com letras
- ❌ CEP não encontrado (404)
- ❌ Métodos HTTP não permitidos (POST, PUT, DELETE)

**Exemplo de saída:**

```
========================================
  Testes da API de Consulta de CEP
  URL Base: http://localhost:8080
========================================

[INFO] === TESTES DE SUCESSO ===
[INFO] Testando: CEP válido - São Paulo
  Endpoint: GET /consulta/01001000
[INFO]   Esperado: HTTP 200
[PASS]   HTTP 200 (esperado: 200)
...
========================================
  RESUMO DOS TESTES
========================================
  Passaram: 11
  Falharam: 0
  Total: 11
========================================
```

---

## Development

### Comandos Úteis

| Comando                              | Descrição                              |
|--------------------------------------|----------------------------------------|
| `./gradlew build`                    | Compila e executa testes               |
| `./gradlew bootRun`                  | Inicia aplicação em modo desenvolvimento |
| `./gradlew test`                     | Executa apenas testes unitários          |
| `./gradlew clean`                    | Limpa build/                         |
| `./gradlew dependencies`             | Lista dependências                     |

### Dependências Principais

```groovy
// Spring Boot Starters
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-actuator'

// Apache HttpClient 5
implementation 'org.apache.httpcomponents.client5:httpclient5'
implementation 'org.apache.httpcomponents.core5:httpcore5'

// Lombok
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

---

## Production Deployment

### Docker (Recomendado)

```dockerfile
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build e execução:

```bash
./gradlew bootJar
docker build -t cep-api .
docker run -p 8080:8080 cep-api
```

### Health Check

O endpoint Actuator está disponível para monitoramento:

```bash
curl http://localhost:8080/actuator/health
```

### Variáveis de Ambiente

| Variável       | Descrição                    | Padrão |
|----------------|------------------------------|--------|
| SERVER_PORT    | Porta da aplicação           | 8080   |
| LOGGING_LEVEL  | Nível de log (DEBUG/INFO)    | INFO   |

---

## Troubleshooting

### Erro: `Connection refused`

```bash
curl: (7) Failed to connect to localhost port 8080: Connection refused
```

**Solução:** A aplicação não está rodando. Execute `./gradlew bootRun`.

### Erro: `SSLHandshakeException`

Se ocorrer em produção, atualize `RestTemplateConfig` para usar truststore válido em vez de `TrustAllCerts`.

### Erro: `CepInvalidoException` com CEPs válidos

Verifique se não há espaços em branco ou caracteres especiais. O serviço faz trim automático, mas CEPs com letras ou menos/more que 8 dígitos são rejeitados.

---

## Licença

MIT — use, modifique e distribua livremente.

## Contato

Para dúvidas ou sugestões, abra uma issue no repositório.

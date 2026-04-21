#!/usr/bin/env bash
set -euo pipefail

readonly SCRIPT_NAME=$(basename "$0")
readonly BASE_URL="${API_URL:-http://localhost:8080}"
readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m'

PASS=0
FAIL=0

log_info() { echo -e "${YELLOW}[INFO]${NC} $*"; }
log_pass() { echo -e "${GREEN}[PASS]${NC} $*"; }
log_fail() { echo -e "${RED}[FAIL]${NC} $*"; }

request() {
    local method=$1
    local endpoint=$2
    local expected_status=$3
    local description=$4

    log_info "Testando: $description"
    log_info "  Endpoint: $method $endpoint"
    log_info "  Esperado: HTTP $expected_status"

    local url="${BASE_URL}${endpoint}"
    local http_code
    local response

    local curl_opts=(-s -w "\n%{http_code}")
    if [[ "$method" != "GET" ]]; then
        curl_opts+=(-X "$method")
    fi

    if ! response=$(curl "${curl_opts[@]}" "${url}" 2>/dev/null); then
        log_fail "  Erro: Falha na conexão"
        ((FAIL++)) || true
        return
    fi

    http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    if [[ "$http_code" == "$expected_status" ]]; then
        log_pass "  HTTP $http_code (esperado: $expected_status)"
        ((PASS++)) || true
    else
        log_fail "  HTTP $http_code (esperado: $expected_status)"
        ((FAIL++)) || true
    fi

    if [[ -n "$body" ]]; then
        echo "  Resposta: $body" | head -c 200
        echo
    fi
    echo
}

main() {
    echo "========================================"
    echo "  Testes da API de Consulta de CEP"
    echo "  URL Base: $BASE_URL"
    echo "========================================"
    echo

    # Testes de Sucesso
    log_info "=== TESTES DE SUCESSO ==="
    request "GET" "/consulta/01001000" "200" "CEP válido - São Paulo"
    request "GET" "/consulta/20040002" "200" "CEP válido - Rio de Janeiro"
    request "GET" "/consulta/30140071" "200" "CEP válido - Belo Horizonte"
    request "GET" "/consulta/01001-000" "200" "CEP válido com hífen"

    echo
    log_info "=== TESTES DE ERRO - CEP INVÁLIDO (400) ==="
    request "GET" "/consulta/" "400" "CEP vazio"
    request "GET" "/consulta/123" "400" "CEP muito curto (3 dígitos)"
    request "GET" "/consulta/123456789" "400" "CEP muito longo (9 dígitos)"
    request "GET" "/consulta/abcdefgh" "400" "CEP com letras"
    request "GET" "/consulta/0100100a" "400" "CEP com caractere não numérico"

    echo
    log_info "=== TESTES DE ERRO - CEP NÃO ENCONTRADO (404) ==="
    request "GET" "/consulta/00000000" "404" "CEP inexistente"
    request "GET" "/consulta/99999999" "404" "CEP não cadastrado"

    echo
    log_info "=== TESTES DE ERRO - MÉTODO NÃO PERMITIDO ==="
    request "POST" "/consulta/01001000" "405" "POST não permitido"
    request "PUT" "/consulta/01001000" "405" "PUT não permitido"
    request "DELETE" "/consulta/01001000" "405" "DELETE não permitido"

    echo
    echo "========================================"
    echo "  RESUMO DOS TESTES"
    echo "========================================"
    echo -e "  ${GREEN}Passaram: $PASS${NC}"
    echo -e "  ${RED}Falharam: $FAIL${NC}"
    echo -e "  Total: $((PASS + FAIL))"
    echo "========================================"

    if [[ $FAIL -gt 0 ]]; then
        exit 1
    fi
}

main "$@"

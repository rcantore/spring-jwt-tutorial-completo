#!/bin/bash

##############################################################################
# Script de Prueba de Endpoints JWT Demo
# 
# Este script prueba automáticamente todos los endpoints de la API JWT Demo
# para verificar que la autenticación, autorización y funcionalidades
# básicas estén funcionando correctamente.
#
# CONCEPTOS EDUCATIVOS:
#
# 1. AUTOMATIZACIÓN DE PRUEBAS:
#    - Validación automática de APIs
#    - Verificación de respuestas HTTP
#    - Testing de flujos completos de autenticación
#
# 2. BASH SCRIPTING:
#    - Uso de curl para peticiones HTTP
#    - Procesamiento de JSON con jq
#    - Manejo de variables y funciones
#    - Control de flujo y validaciones
#
# 3. TESTING DE APIS REST:
#    - Verificación de códigos de estado HTTP
#    - Validación de estructura de respuestas JSON
#    - Testing de casos de éxito y error
#    - Pruebas de autorización y permisos
#
# PREREQUISITOS:
# - curl: Para realizar peticiones HTTP
# - jq: Para procesar respuestas JSON
# - La aplicación debe estar ejecutándose en localhost:8080
#
# USO:
# ./test-endpoints.sh
#
# @author CebandoIdeas
# @version 1.0
##############################################################################

# Configuración
API_BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin123"
REGULAR_USER="user"
REGULAR_PASS="user123"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Contadores para estadísticas
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

##############################################################################
# FUNCIONES UTILITARIAS
##############################################################################

# Función para imprimir encabezados de sección
print_header() {
    echo -e "\n${BLUE}╔════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  $1${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════════════════╝${NC}\n"
}

# Función para imprimir subsecciones
print_section() {
    echo -e "\n${CYAN}▶ $1${NC}"
    echo -e "${CYAN}────────────────────────────────────────────────────────────${NC}"
}

# Función para validar que la aplicación esté ejecutándose
check_application() {
    echo -e "${YELLOW}🔍 Verificando que la aplicación esté ejecutándose...${NC}"
    
    if curl -s -f "$API_BASE_URL/api/public/info" > /dev/null; then
        echo -e "${GREEN}✅ Aplicación está ejecutándose en $API_BASE_URL${NC}"
        return 0
    else
        echo -e "${RED}❌ Error: La aplicación no está ejecutándose en $API_BASE_URL${NC}"
        echo -e "${RED}   Por favor, ejecuta: mvn spring-boot:run${NC}"
        exit 1
    fi
}

# Función para verificar prerequisitos
check_prerequisites() {
    echo -e "${YELLOW}🔧 Verificando prerequisitos...${NC}"
    
    # Verificar curl
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}❌ Error: curl no está instalado${NC}"
        exit 1
    fi
    
    # Verificar jq
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}❌ Error: jq no está instalado${NC}"
        echo -e "${YELLOW}   Instala jq: brew install jq (macOS) o sudo apt install jq (Ubuntu)${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Todos los prerequisitos están instalados${NC}"
}

# Función para realizar una petición HTTP y validar respuesta
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    local description=$5
    local auth_header=$6
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "\n${PURPLE}🧪 Test $TOTAL_TESTS: $description${NC}"
    echo -e "   ${method} ${endpoint}"
    
    # Construir comando curl
    local curl_cmd="curl -s -w '%{http_code}'"
    
    if [[ -n "$auth_header" ]]; then
        curl_cmd="$curl_cmd -H 'Authorization: Bearer $auth_header'"
    fi
    
    curl_cmd="$curl_cmd -H 'Content-Type: application/json'"
    
    if [[ -n "$data" ]]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi
    
    curl_cmd="$curl_cmd -X $method $API_BASE_URL$endpoint"
    
    # Ejecutar petición
    local response=$(eval $curl_cmd)
    local status_code=${response: -3}
    local body=${response%???}
    
    # Validar código de estado
    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "   ${GREEN}✅ Estado HTTP: $status_code (esperado: $expected_status)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Mostrar respuesta si es JSON válido
        if echo "$body" | jq . > /dev/null 2>&1; then
            echo -e "   ${GREEN}📄 Respuesta:${NC}"
            echo "$body" | jq . | sed 's/^/      /'
        elif [[ -n "$body" ]]; then
            echo -e "   ${GREEN}📄 Respuesta: $body${NC}"
        fi
        
        # Retornar el body para usar en tests posteriores
        echo "$body"
        return 0
    else
        echo -e "   ${RED}❌ Estado HTTP: $status_code (esperado: $expected_status)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        
        if [[ -n "$body" ]]; then
            echo -e "   ${RED}📄 Respuesta de error:${NC}"
            if echo "$body" | jq . > /dev/null 2>&1; then
                echo "$body" | jq . | sed 's/^/      /'
            else
                echo "      $body"
            fi
        fi
        return 1
    fi
}

# Función para extraer valor de JSON
extract_json_value() {
    local json=$1
    local key=$2
    echo "$json" | jq -r ".$key // empty"
}

##############################################################################
# TESTS DE ENDPOINTS
##############################################################################

# Test de endpoint público
test_public_endpoints() {
    print_section "🌐 Testing Endpoints Públicos"
    
    test_endpoint "GET" "/api/public/info" "" "200" "Información pública del sistema"
}

# Test de autenticación
test_authentication() {
    print_section "🔐 Testing Autenticación"
    
    # Test login exitoso admin
    local login_response=$(test_endpoint "POST" "/api/auth/login" \
        "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}" \
        "200" "Login exitoso como administrador")
    
    if [[ $? -eq 0 ]]; then
        ADMIN_TOKEN=$(extract_json_value "$login_response" "token")
        echo -e "   ${GREEN}🎫 Token de admin extraído: ${ADMIN_TOKEN:0:30}...${NC}"
    fi
    
    # Test login exitoso usuario regular
    local user_login_response=$(test_endpoint "POST" "/api/auth/login" \
        "{\"username\":\"$REGULAR_USER\",\"password\":\"$REGULAR_PASS\"}" \
        "200" "Login exitoso como usuario regular")
    
    if [[ $? -eq 0 ]]; then
        USER_TOKEN=$(extract_json_value "$user_login_response" "token")
        echo -e "   ${GREEN}🎫 Token de usuario extraído: ${USER_TOKEN:0:30}...${NC}"
    fi
    
    # Test login fallido
    test_endpoint "POST" "/api/auth/login" \
        "{\"username\":\"admin\",\"password\":\"wrongpassword\"}" \
        "401" "Login fallido con contraseña incorrecta"
    
    # Test login con usuario inexistente
    test_endpoint "POST" "/api/auth/login" \
        "{\"username\":\"noexiste\",\"password\":\"cualquiera\"}" \
        "401" "Login fallido con usuario inexistente"
}

# Test de registro
test_registration() {
    print_section "📝 Testing Registro de Usuarios"
    
    # Generar username único usando timestamp
    local timestamp=$(date +%s)
    local unique_username="testuser_$timestamp"
    local unique_email="test_$timestamp@ejemplo.com"
    
    # Test registro exitoso
    test_endpoint "POST" "/api/auth/register" \
        "{\"username\":\"$unique_username\",\"email\":\"$unique_email\",\"password\":\"test123\"}" \
        "200" "Registro exitoso de nuevo usuario"
    
    # Test registro con usuario duplicado (usando admin que ya existe)
    test_endpoint "POST" "/api/auth/register" \
        "{\"username\":\"admin\",\"email\":\"admin2@ejemplo.com\",\"password\":\"test123\"}" \
        "400" "Registro fallido con username duplicado"
    
    # Test registro con email duplicado
    test_endpoint "POST" "/api/auth/register" \
        "{\"username\":\"otrousuario\",\"email\":\"admin@ejemplo.com\",\"password\":\"test123\"}" \
        "400" "Registro fallido con email duplicado"
    
    # Test registro con datos inválidos
    test_endpoint "POST" "/api/auth/register" \
        "{\"username\":\"\",\"email\":\"invalid-email\",\"password\":\"123\"}" \
        "400" "Registro fallido con datos inválidos"
}

# Test de endpoints protegidos
test_protected_endpoints() {
    print_section "🛡️ Testing Endpoints Protegidos"
    
    # Test acceso sin token
    test_endpoint "GET" "/api/protected/user" "" "401" \
        "Acceso denegado sin token de autenticación"
    
    # Test acceso con token inválido
    test_endpoint "GET" "/api/protected/user" "" "401" \
        "Acceso denegado con token inválido" "token_invalido"
    
    if [[ -n "$USER_TOKEN" ]]; then
        # Test acceso exitoso a endpoint de usuario
        test_endpoint "GET" "/api/protected/user" "" "200" \
            "Acceso exitoso a endpoint de usuario con token válido" "$USER_TOKEN"
        
        # Test acceso denegado a endpoint de admin con usuario regular
        test_endpoint "GET" "/api/protected/admin" "" "403" \
            "Acceso denegado a endpoint admin con usuario regular" "$USER_TOKEN"
    fi
    
    if [[ -n "$ADMIN_TOKEN" ]]; then
        # Test acceso exitoso a endpoint de usuario con admin
        test_endpoint "GET" "/api/protected/user" "" "200" \
            "Acceso exitoso a endpoint de usuario con token de admin" "$ADMIN_TOKEN"
        
        # Test acceso exitoso a endpoint de admin
        test_endpoint "GET" "/api/protected/admin" "" "200" \
            "Acceso exitoso a endpoint de admin con token de admin" "$ADMIN_TOKEN"
    fi
}

# Test de gestión de usuarios
test_user_management() {
    print_section "👥 Testing Gestión de Usuarios"
    
    if [[ -z "$ADMIN_TOKEN" ]]; then
        echo -e "${YELLOW}⚠️  Saltando tests de gestión de usuarios (no hay token de admin)${NC}"
        return
    fi
    
    # Test listar usuarios (solo admin)
    test_endpoint "GET" "/api/users" "" "200" \
        "Listar todos los usuarios como admin" "$ADMIN_TOKEN"
    
    # Test obtener usuario específico
    test_endpoint "GET" "/api/users/1" "" "200" \
        "Obtener usuario por ID como admin" "$ADMIN_TOKEN"
    
    # Test obtener usuario inexistente
    test_endpoint "GET" "/api/users/999" "" "404" \
        "Obtener usuario inexistente" "$ADMIN_TOKEN"
    
    if [[ -n "$USER_TOKEN" ]]; then
        # Test acceso denegado a gestión de usuarios con usuario regular
        test_endpoint "GET" "/api/users" "" "403" \
            "Acceso denegado a listado de usuarios con usuario regular" "$USER_TOKEN"
    fi
}

# Test de casos edge
test_edge_cases() {
    print_section "🧪 Testing Casos Edge"
    
    # Test endpoint inexistente
    test_endpoint "GET" "/api/inexistente" "" "404" \
        "Endpoint inexistente"
    
    # Test método HTTP no permitido
    test_endpoint "PUT" "/api/public/info" "" "405" \
        "Método HTTP no permitido"
    
    # Test payload JSON malformado
    test_endpoint "POST" "/api/auth/login" \
        "{\"username\":\"admin\",\"password\":" "400" \
        "JSON malformado en petición"
    
    # Test campo requerido faltante
    test_endpoint "POST" "/api/auth/login" \
        "{\"username\":\"admin\"}" "400" \
        "Campo requerido faltante"
}

# Función para mostrar estadísticas finales
show_statistics() {
    print_header "📊 ESTADÍSTICAS FINALES"
    
    echo -e "${BLUE}Total de tests ejecutados: $TOTAL_TESTS${NC}"
    echo -e "${GREEN}Tests exitosos: $PASSED_TESTS${NC}"
    echo -e "${RED}Tests fallidos: $FAILED_TESTS${NC}"
    
    local success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "${CYAN}Tasa de éxito: $success_rate%${NC}"
    
    if [[ $FAILED_TESTS -eq 0 ]]; then
        echo -e "\n${GREEN}🎉 ¡Todos los tests pasaron exitosamente!${NC}"
        echo -e "${GREEN}✅ La API está funcionando correctamente${NC}"
        return 0
    else
        echo -e "\n${RED}❌ Algunos tests fallaron${NC}"
        echo -e "${YELLOW}⚠️  Revisa los logs anteriores para más detalles${NC}"
        return 1
    fi
}

##############################################################################
# FUNCIÓN PRINCIPAL
##############################################################################

main() {
    print_header "🚀 JWT Demo API - Script de Pruebas Automáticas"
    
    echo -e "${BLUE}Este script probará todos los endpoints de la API JWT Demo${NC}"
    echo -e "${BLUE}para verificar que la funcionalidad esté trabajando correctamente.${NC}"
    
    # Verificar prerequisitos
    check_prerequisites
    
    # Verificar que la aplicación esté ejecutándose
    check_application
    
    echo -e "\n${YELLOW}🏁 Iniciando tests...${NC}"
    
    # Ejecutar todos los tests
    test_public_endpoints
    test_authentication
    test_registration
    test_protected_endpoints
    test_user_management
    test_edge_cases
    
    # Mostrar estadísticas
    show_statistics
    
    # Código de salida basado en resultado
    if [[ $FAILED_TESTS -eq 0 ]]; then
        exit 0
    else
        exit 1
    fi
}

##############################################################################
# MANEJO DE ARGUMENTOS Y EJECUCIÓN
##############################################################################

# Verificar argumentos de línea de comandos
while [[ $# -gt 0 ]]; do
    case $1 in
        --help|-h)
            echo "Uso: $0 [opciones]"
            echo ""
            echo "Opciones:"
            echo "  --help, -h     Mostrar esta ayuda"
            echo "  --url URL      URL base de la API (default: http://localhost:8080)"
            echo "  --verbose, -v  Modo verboso"
            echo ""
            echo "Ejemplos:"
            echo "  $0                                    # Usar configuración por defecto"
            echo "  $0 --url http://localhost:9090       # Usar puerto diferente"
            echo "  $0 --verbose                         # Modo verboso"
            exit 0
            ;;
        --url)
            API_BASE_URL="$2"
            shift 2
            ;;
        --verbose|-v)
            set -x  # Habilitar modo verboso
            shift
            ;;
        *)
            echo "Opción desconocida: $1"
            echo "Usa --help para ver las opciones disponibles"
            exit 1
            ;;
    esac
done

# Ejecutar función principal
main

##############################################################################
# EJERCICIOS PROPUESTOS PARA ESTUDIANTES:
#
# 1. BÁSICO: Agrega un test para verificar que el endpoint /api/public/health
#    retorne información sobre el estado de la aplicación
#
# 2. INTERMEDIO: Implementa tests de performance que midan el tiempo de respuesta
#    de cada endpoint y muestren estadísticas
#
# 3. AVANZADO: Agrega tests de carga concurrente usando múltiples procesos curl
#    en paralelo para simular tráfico real
#
# 4. DESAFÍO: Implementa un modo de "smoke test" que ejecute solo los tests
#    críticos para verificación rápida en CI/CD
#
# 5. PROFESIONAL: Integra este script con herramientas de CI/CD como GitHub Actions
#    para ejecutar automáticamente en cada push
##############################################################################
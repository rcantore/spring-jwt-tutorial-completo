# Testing de APIs REST con Spring Boot - Un Proyecto Completo

## El Problema

Tu API JWT está funcionando perfectamente en desarrollo. Los endpoints responden, la autenticación funciona, los roles están bien configurados. Pero llega el momento crítico: un viernes a las 5 PM, subes un cambio "trivial" y la aplicación explota en producción. El login deja de funcionar. Los usuarios con rol ADMIN no pueden acceder a sus recursos. El CEO está llamando.

¿Cómo evitamos este escenario? Con una suite de tests robusta que actúe como tu red de seguridad. Este proyecto implementa una estrategia de testing completa que cubre desde funciones unitarias hasta flujos completos de usuario, garantizando que cada cambio se valide automáticamente antes de llegar a producción.

## La Arquitectura de Testing

El sistema de tests se estructura en capas, igual que tu aplicación. Cada capa tiene su propósito específico y trabaja en conjunto para garantizar la calidad del código:

### 1. TestDataHelper - La Fábrica de Datos
**Ubicación**: `/src/test/java/com/ejemplo/jwtdemo/TestDataHelper.java`

Imagina tener que crear un usuario de prueba en cada test. Username, password, email, roles... código repetido por todos lados. TestDataHelper es tu fábrica centralizada de objetos de prueba.

```java
// En lugar de esto en cada test:
User user = new User();
user.setUsername("testuser");
user.setPassword("Test123!");
user.setEmail("test@example.com");
// ... más configuración

// Simplemente haces:
User user = TestDataHelper.createTestUser();
```

**El patrón en acción:**
- Factory Methods que devuelven objetos pre-configurados
- Builders para casos complejos que necesitan customización
- Datos realistas que reflejan casos de uso reales
- Casos edge listos para usar (usuarios sin roles, emails inválidos, etc.)

### 2. JwtServiceTest - Testing Quirúrgico de Servicios
**Ubicación**: `/src/test/java/com/ejemplo/jwtdemo/service/JwtServiceTest.java`

Los tests unitarios son como cirugía de precisión: aíslan completamente el componente a probar. No hay base de datos, no hay red, no hay Spring Context. Solo tu código y sus dependencias mockeadas.

**La anatomía de un test unitario perfecto:**
```java
@Test
@DisplayName("Should generate valid JWT token for authenticated user")
void testGenerateToken() {
    // Arrange: Preparar el escenario
    UserDetails userDetails = createUserDetails("john");
    
    // Act: Ejecutar la acción
    String token = jwtService.generateToken(userDetails);
    
    // Assert: Verificar el resultado
    assertThat(token).isNotNull()
                     .contains(".")
                     .hasSize(3); // JWT tiene 3 partes
}
```

**Técnicas avanzadas que implementa:**
- `ReflectionTestUtils` para inyectar valores en campos privados (como el secreto JWT)
- `@Nested` classes para agrupar tests relacionados (tokens válidos vs inválidos)
- Manejo de tiempo con tokens expirados usando manipulación de fechas
- Verificación de excepciones específicas para casos de error

### 3. AuthControllerTest - El Simulador de Cliente HTTP
**Ubicación**: `/src/test/java/com/ejemplo/jwtdemo/controller/AuthControllerTest.java`

¿Cómo probas un endpoint REST sin levantar todo el servidor? MockMvc es tu simulador de peticiones HTTP. Puedes enviar requests, verificar responses, validar headers, todo sin un servidor real.

**El flujo de testing de un endpoint:**
```
Test → MockMvc → Spring DispatcherServlet → Controller → Mocked Service
                                                ↓
Test ← Assertions ← Response ← Controller ← Mocked Response
```

**Escenarios reales que cubre:**
- Login exitoso con credenciales válidas
- Fallo de autenticación con password incorrecto
- Registro con validación de campos (email formato, password strength)
- Manejo de errores 400, 401, 403, 500
- Verificación de estructura JSON en responses
- Headers de autorización y content-type

### 4. UserControllerTest - El Guardian de los Roles
**Ubicación**: `/src/test/java/com/ejemplo/jwtdemo/controller/UserControllerTest.java`

Spring Security permite proteger endpoints por roles. ¿Pero cómo verificas que realmente funciona? Este test suite simula usuarios con diferentes roles y verifica accesos.

**Matriz de autorización que prueba:**
```
Endpoint            | Anonymous | USER  | ADMIN
--------------------|-----------|-------|-------
GET /api/user/me    | 401       | 200   | 200
GET /api/admin/users| 401       | 403   | 200
DELETE /api/users/1 | 401       | 403   | 200
```

**Conceptos sofisticados:**
- `@WithMockUser` para simular diferentes identidades
- Testing de paginación con `Pageable` y `Page<T>`
- Verificación de que `@PreAuthorize` realmente funciona
- Diferencia entre 401 (no autenticado) y 403 (no autorizado)

### 5. SecurityIntegrationTest - La Prueba de Fuego
**Ubicación**: `/src/test/java/com/ejemplo/jwtdemo/SecurityIntegrationTest.java`

Los tests de integración son tu última línea de defensa. Aquí no hay mocks: base de datos real (H2 en memoria), Spring Context completo, filtros de seguridad activos. Es lo más cercano a producción que puedes estar.

**Flujo completo que valida:**
```
1. Crear usuario en BD → 2. Login → 3. Obtener JWT
       ↓
4. Usar JWT → 5. Acceder recurso protegido → 6. Verificar respuesta
       ↓
7. Token expira → 8. Intento fallido → 9. 401 Unauthorized
```

**Lo que realmente prueba:**
- El filtro JWT intercepta correctamente las peticiones
- Los tokens se validan contra el secreto configurado
- La cadena de seguridad completa funciona end-to-end
- Los datos persisten correctamente en la BD
- Las transacciones se comportan como esperas

## Ejecutando la Suite de Tests

### La Ejecución Masiva
```bash
mvn test
```
Un comando, cientos de verificaciones. En segundos sabrás si tu código está listo para producción o si hay bombas esperando explotar.

### Testing Selectivo - Cuando Sabes Dónde Mirar
```bash
# Verificar solo la lógica JWT
mvn test -Dtest=JwtServiceTest

# Probar endpoints de autenticación
mvn test -Dtest=AuthControllerTest

# Validar control de acceso por roles
mvn test -Dtest=UserControllerTest

# La prueba definitiva: integración completa
mvn test -Dtest=SecurityIntegrationTest
```

### Estrategias de Ejecución

**Tests Rápidos (< 1 segundo)**: Los unitarios
```bash
mvn test -Dtest=JwtServiceTest
```
Perfectos para desarrollo iterativo. Cambias código, ejecutas, verificas. El ciclo de feedback instantáneo.

**Tests de Capa Media (2-5 segundos)**: Los controladores
```bash
mvn test -Dtest="*ControllerTest"
```
Validan tu API sin levantar infraestructura pesada. Ideal antes de hacer commit.

**Tests Pesados (> 5 segundos)**: Integración
```bash
mvn test -Dtest=SecurityIntegrationTest
```
Tu red de seguridad final. Ejecutar antes de merge a main o deploy.

## La Matriz de Cobertura

### Qué Tests Usar y Cuándo

```
Escenario                    | Unit | Controller | Integration
-----------------------------|------|------------|-------------
Lógica de negocio pura       | ✓    |            |
Validación de DTOs           |      | ✓          |
Serialización JSON           |      | ✓          |
Autorización por roles       |      | ✓          | ✓
Filtros de seguridad         |      |            | ✓
Transacciones BD             |      |            | ✓
Flujos completos de usuario  |      |            | ✓
```

### La Pirámide de Testing en Acción

```
        /\
       /  \  Integration Tests (10%)
      /    \ "¿Funciona todo junto?"
     /------\
    /        \ Controller Tests (30%)
   /          \ "¿Mi API responde correctamente?"
  /------------\
 /              \ Unit Tests (60%)
/________________\ "¿Mi lógica está bien?"
```

## Conceptos Clave del Testing

### Testing Unitario vs Integración

| Aspecto | Unitario | Integración |
|---------|----------|-------------|
| **Velocidad** | Muy rápido | Más lento |
| **Alcance** | Una clase | Sistema completo |
| **Dependencies** | Mockeadas | Reales |
| **Base de datos** | No usa | H2 en memoria |
| **Configuración** | Mínima | Completa |
| **Cuándo usar** | Lógica de negocio | Flujos críticos |

### Annotations Importantes

| Annotation | Propósito | Cuándo Usar |
|------------|-----------|-------------|
| `@ExtendWith(MockitoExtension.class)` | Tests unitarios | Servicios aislados |
| `@WebMvcTest` | Tests de controladores | APIs REST |
| `@SpringBootTest` | Tests de integración | Flujos completos |
| `@MockBean` | Mock en contexto Spring | Dependencies externas |
| `@WithMockUser` | Usuario simulado | Tests de autorización |
| `@Transactional` | Rollback automático | Tests con BD |

### Estrategias de Assertions

```java
// JUnit básico
assertEquals(expected, actual);

// AssertJ (preferido)
assertThat(actual).isEqualTo(expected);

// Hamcrest (MockMvc)
.andExpect(jsonPath("$.username", is("testuser")))
```

## Patrones y Anti-Patrones

### El Test que Sí Deberías Escribir

```java
@Test
@DisplayName("Should deny access to admin endpoint with USER role")
void adminEndpoint_WithUserRole_Returns403() {
    // Given: Un usuario con rol USER
    // When: Intenta acceder a /api/admin/users
    // Then: Recibe 403 Forbidden
}
```
**Por qué funciona**: Nombre descriptivo, caso específico, verificación clara.

### El Test que NO Deberías Escribir

```java
@Test
void test1() {
    // 500 líneas de código
    // Prueba 20 cosas diferentes
    // Si falla, no sabes por qué
}
```
**Por qué falla**: Nombre genérico, hace demasiado, imposible de debuggear.

## Arquitectura de Testing Avanzada

### Testing por Capas - El Approach Quirúrgico

**Capa Repository**: No testeamos. Spring Data JPA ya está probado. Solo testearías queries custom complejas.

**Capa Service**: Tests unitarios puros. Mockeas repositories y dependencies.
```java
@Mock
private UserRepository userRepository;

@InjectMocks
private UserService userService;
```

**Capa Controller**: MockMvc + mocks de servicios. Pruebas la capa web aislada.
```java
@WebMvcTest(AuthController.class)
@MockBean
private AuthService authService;
```

**Capa Integración**: Todo real. Base de datos, seguridad, transacciones.
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
```

### El Flujo de CI/CD con Tests

```
Developer Push → GitHub Actions → mvn test → ¿Pass?
                                               ↓ Yes
                                          Build Docker Image
                                               ↓
                                          Deploy to Staging
                                               ↓
                                          Run E2E Tests
                                               ↓
                                          Deploy to Production
```

## Debugging de Tests Fallidos

### Cuando un Test Falla

**Paso 1**: Lee el nombre del test y el mensaje de error
```
adminEndpoint_WithUserRole_Returns403
Expected: 403
Actual: 200
```

**Paso 2**: Activa logs de Spring Security
```properties
logging.level.org.springframework.security=DEBUG
```

**Paso 3**: Usa el debugger en el punto exacto
```java
mockMvc.perform(get("/api/admin/users"))  // ← Breakpoint aquí
       .andExpect(status().isForbidden());
```

### Los Errores Más Comunes y Sus Soluciones

**"401 en lugar de 403"**: El usuario no está autenticado. Agrega `@WithMockUser`.

**"No qualifying bean"**: Falta `@MockBean` para una dependencia.

**"Transaction rolled back"**: Normal en tests con `@Transactional`. Es lo esperado.

**"Port already in use"**: Usa `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`.

## Métricas de Calidad

### Coverage - La Métrica Engañosa

80% de cobertura no significa código a prueba de balas. Podrías tener:
```java
@Test
void coverageHack() {
    service.complexMethod(null, null, null);
    // Sin assertions - pasa si no explota
}
```

### Métricas que Realmente Importan

1. **Mutation Coverage**: ¿Tus tests detectan cambios en el código?
2. **Casos Edge Cubiertos**: Nulls, strings vacíos, números negativos
3. **Escenarios de Error**: Cada excepción tiene su test
4. **Tiempo de Ejecución**: Suite completa < 2 minutos

## Testing en el Mundo Real

### Cuando No Testear

- Getters/Setters simples
- Configuración de frameworks
- Código generado
- Wrappers triviales de librerías

### Cuando Siempre Testear

- Lógica de negocio
- Cálculos y transformaciones
- Manejo de errores
- Integraciones con sistemas externos
- Seguridad y autorización

### El Test Como Documentación Viva

Un buen test explica cómo usar tu código:
```java
@Test
@DisplayName("JWT token expires after configured time")
void tokenExpiration() {
    // This test documents that:
    // 1. Tokens have expiration
    // 2. Expired tokens are rejected
    // 3. The error message is clear
}
```

## Evolución del Testing

### Nivel 1: Tests Básicos (Donde Estás)
- Tests unitarios para servicios críticos
- Tests de controladores para endpoints principales
- Un test de integración de smoke

### Nivel 2: Tests Comprehensivos
- Coverage > 80%
- Tests de casos edge
- Tests de performance
- Contract testing entre servicios

### Nivel 3: Testing Continuo
- Tests ejecutándose en cada commit
- Mutation testing activo
- Tests E2E automatizados
- Monitoring de tests en producción (Synthetic monitoring)

## El Valor Real del Testing

No se trata de alcanzar 100% de coverage. Se trata de dormir tranquilo sabiendo que tu código tiene una red de seguridad. Cada test es una inversión en la mantenibilidad futura del proyecto.

Cuando un test falla, no es una molestia - es el sistema salvándote de un bug en producción. Cuando todos pasan, no es perfección - es confianza para seguir evolucionando el código.

Los tests no son overhead. Son la diferencia entre hacer deployment los viernes a las 5 PM con confianza o pasar el fin de semana arreglando producción.
# Sistema de Autenticación JWT con Spring Boot

## El Desafío

Imagina que estás construyendo una API REST moderna que necesita manejar miles de usuarios concurrentes. Los sistemas tradicionales de sesiones con cookies no escalan bien y complican el despliegue en múltiples servidores. La solución: autenticación stateless con JWT (JSON Web Tokens).

Este proyecto implementa un sistema de autenticación completo sin mantener estado en el servidor, permitiendo que cualquier instancia de tu aplicación pueda validar tokens de manera independiente.

## Arquitectura del Sistema

El sistema se construye sobre 5 componentes fundamentales que trabajan en conjunto como engranajes de un reloj:

### 1. UserRepository - La Puerta a los Datos
**Ubicación**: `/src/main/java/com/ejemplo/jwtdemo/repository/UserRepository.java`

Los repositorios en Spring son interfaces mágicas que eliminan el código repetitivo. Al extender `JpaRepository`, automáticamente tenes acceso a métodos como `save()`, `findAll()`, `deleteById()` sin escribir una sola línea de SQL.

```java
// Ejemplo de uso en tu código:
Optional<User> user = userRepository.findByUsername("john_doe");
boolean emailExists = userRepository.existsByEmail("user@example.com");
```

**¿Por qué es importante?** 
- Abstrae completamente la capa de datos
- Los métodos personalizados siguen convenciones de nombres que Spring traduce a SQL
- El uso de `Optional` previene los temidos `NullPointerException`

### 2. UserDetailsImpl
**Ubicación**: `/src/main/java/com/ejemplo/jwtdemo/service/UserDetailsImpl.java`

Spring Security tiene su propio lenguaje para hablar de usuarios. Esta clase actúa como traductor entre tu modelo de datos y lo que Spring Security espera.

**El patrón Adapter en acción:**
Tu entidad `User` probablemente tiene campos como `username`, `password`, `roles`. Spring Security necesita una interfaz `UserDetails` con métodos específicos como `getAuthorities()`, `isAccountNonExpired()`. UserDetailsImpl hace esa traducción.

**Detalle crucial**: Los roles se convierten en authorities con el prefijo "ROLE_". Un usuario con rol "ADMIN" se convierte en authority "ROLE_ADMIN". Esto permite usar anotaciones como `@PreAuthorize("hasRole('ADMIN')")`.

### 3. CustomUserDetailsService - El Detective de Usuarios
**Ubicación**: `/src/main/java/com/ejemplo/jwtdemo/service/CustomUserDetailsService.java`

Cuando alguien intenta autenticarse, Spring Security necesita saber: "Quién es este usuario?". Este servicio es el detective que busca esa información.

**Flujo interno:**
1. Recibe un username
2. Consulta la base de datos usando UserRepository
3. Envuelve el usuario encontrado en UserDetailsImpl
4. Spring Security hace el resto de la validación

**Pro tip**: La anotación `@Transactional` garantiza que las relaciones lazy (como los roles) se carguen correctamente dentro de la misma transacción.

### 4. JwtAuthenticationFilter - El Guardián de la Puerta (You shall not pass!)
**Ubicación**: `/src/main/java/com/ejemplo/jwtdemo/filter/JwtAuthenticationFilter.java`

Este filtro intercepta CADA petición HTTP antes de que llegue a tu controlador. Es como el guardia de seguridad en la entrada de un edificio.

**Proceso de validación paso a paso:**
```
Request entrante → ¿Tiene header Authorization?
                   ↓ Sí
                   ¿Empieza con "Bearer "?
                   ↓ Sí
                   Extraer token → Validar firma → ¿Expirado?
                   ↓ No
                   Cargar usuario → Establecer autenticación
                   ↓
                   Request continúa al controller
```

**Manejo inteligente de errores**: Si el token es inválido, el filtro simplemente no establece autenticación. Spring Security lo tratará como un usuario anónimo, permitiendo que las reglas de seguridad decidan si denegar o permitir acceso.

### 5. SecurityConfig - El Director de Orquesta (ponele)
**Ubicación**: `/src/main/java/com/ejemplo/jwtdemo/config/SecurityConfig.java`

Esta configuración ensambla todas las piezas y define las reglas del juego.

**Decisiones arquitectónicas clave:**
- **STATELESS**: Sin sesiones = mejor escalabilidad horizontal
- **CORS habilitado**: Tu frontend puede estar en otro dominio
- **CSRF deshabilitado**: Innecesario en APIs REST sin cookies
- **Endpoints públicos vs protegidos**: Define quién puede acceder a qué

## El Flujo Completo de Autenticación

### Escenario 1: Login Inicial
```
Cliente                     API                         Database
   |                         |                              |
   |--POST /api/auth/login-->|                              |
   |  {username, password}   |                              |
   |                         |--Buscar usuario------------>|
   |                         |<--Datos del usuario---------|
   |                         |                              |
   |                         |--Validar password           |
   |                         |--Generar JWT                |
   |<--{token, user info}----|                              |
```

### Escenario 2: Request Autenticada
```
Cliente                     Filter                    Controller
   |                         |                            |
   |--GET /api/user/profile->|                            |
   |  Bearer: <token>        |                            |
   |                         |--Validar token             |
   |                         |--Cargar usuario            |
   |                         |--Set SecurityContext       |
   |                         |-------------------------->|
   |                         |                            |
   |<--{profile data}--------|<--Proceso normal----------|
```

## Configuración Práctica

### Variables de Entorno Esenciales
```properties
# El secreto debe ser complejo y único por ambiente
app.jwt.secret=mySecretKey12345678901234567890123456789012345678901234567890
app.jwt.expiration=86400000  # 24 horas en milisegundos

# Para debugging durante desarrollo
logging.level.org.springframework.security=DEBUG
```

### Testing Manual con cURL

**Registro de usuario:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123!","email":"test@example.com"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123!"}'
```

**Request autenticada:**
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Conceptos de Seguridad en Acción

### Autenticación Stateless
A diferencia de las sesiones tradicionales donde el servidor recuerda quién eres, aquí cada request debe probar su identidad. Es como mostrar tu identificación cada vez que entras a un edificio, en lugar de que el guardia te recuerde.

**Ventajas:**
- Puedes escalar horizontalmente sin compartir sesiones
- Los microservicios pueden validar tokens independientemente
- No hay problemas de sincronización entre servidores

### Sistema de Roles y Authorities
Spring Security distingue entre roles (conceptuales) y authorities (permisos específicos). Un rol "ADMIN" se convierte en authority "ROLE_ADMIN", pero también podrías tener authorities granulares como "WRITE_ARTICLES" o "DELETE_USERS".

```java
// En tu controller:
@PreAuthorize("hasRole('ADMIN')")  // Busca ROLE_ADMIN
@GetMapping("/admin/users")

@PreAuthorize("hasAuthority('WRITE_ARTICLES')")  // Busca exactamente WRITE_ARTICLES
@PostMapping("/articles")
```

### Protección CORS
CORS (Cross-Origin Resource Sharing) permite que tu frontend en `http://localhost:3000` hable con tu API en `http://localhost:8080`. Sin esto, el navegador bloquearía las peticiones por seguridad.

## Evolución del Proyecto

### Fase 1: Lo que ya tienes ✓
- Autenticación básica con JWT
- Manejo de usuarios y roles
- Filtros de seguridad configurados

### Fase 2: Mejoras inmediatas
1. **Refresh Tokens**: Los tokens que expiran cada 24 horas son molestos. Implementa un sistema de refresh token de larga duración.
2. **Rate Limiting**: Previene ataques de fuerza bruta limitando intentos de login.
3. **Auditoría**: Registra quién hace qué y cuándo.

### Fase 3: Nivel empresarial
1. **Multi-factor Authentication (MFA)**: Agrega un segundo factor de autenticación
2. **OAuth2/OpenID Connect**: Permite login con Google, GitHub, etc.
3. **Token Revocation**: Sistema para invalidar tokens comprometidos
4. **Gestión de Sesiones**: Dashboard para ver sesiones activas

## Patrones de Diseño Aplicados

### Adapter Pattern
`UserDetailsImpl` adapta tu modelo de dominio al esperado por Spring Security sin modificar ninguna de las dos partes.

### Repository Pattern
`UserRepository` encapsula la lógica de acceso a datos, permitiendo cambiar de base de datos sin tocar la lógica de negocio.

### Chain of Responsibility
Los filtros de Spring Security forman una cadena donde cada uno decide si procesar o pasar al siguiente.

### Dependency Injection
Todos los componentes se inyectan vía constructor, facilitando el testing y reduciendo acoplamiento.

## Troubleshooting Común

**"401 Unauthorized" en todas las requests**
- Verifica que el token se esté enviando correctamente en el header
- Revisa que el secreto JWT sea el mismo al generar y validar
- Confirma que el token no haya expirado

**"403 Forbidden" con token válido**
- El usuario está autenticado pero no tiene el rol necesario
- Revisa las anotaciones @PreAuthorize en tu controller
- Verifica que los roles se estén cargando correctamente desde la BD

**El filtro no se ejecuta**
- Asegúrate que SecurityConfig esté anotado con @Configuration
- Verifica que el filtro esté registrado antes de UsernamePasswordAuthenticationFilter

## Recursos para Profundizar

- **JWT.io**: Decodifica y verifica tokens JWT visualmente
- **Spring Security Reference**: La documentación oficial es densa pero completa
- **OWASP Authentication Cheat Sheet**: Mejores prácticas de seguridad

Este sistema de autenticación es la base sobre la cual construirás features más complejas. Cada componente tiene su responsabilidad clara, facilitando el mantenimiento y la evolución del código. La autenticación stateless con JWT es el estándar de facto para APIs REST modernas, y ahora tienes una implementación completa y funcional.
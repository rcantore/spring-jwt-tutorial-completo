package com.ejemplo.jwtdemo;

import com.ejemplo.jwtdemo.dto.LoginRequest;
import com.ejemplo.jwtdemo.dto.RegisterRequest;
import com.ejemplo.jwtdemo.entity.Role;
import com.ejemplo.jwtdemo.entity.User;
import com.ejemplo.jwtdemo.service.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

/**
 * Clase helper para crear datos de prueba en los tests.
 * 
 * CONCEPTOS EDUCATIVOS DEMOSTRADOS:
 * ===================================
 * 
 * 1. **Test Data Builder Pattern**: 
 *    - Centraliza la creación de objetos de prueba
 *    - Proporciona datos consistentes y realistas
 *    - Facilita el mantenimiento de los tests
 * 
 * 2. **Factory Methods**:
 *    - Métodos estáticos que crean objetos preconfigurados
 *    - Diferentes variaciones para diferentes escenarios de test
 *    - Nombres descriptivos que indican el propósito del objeto
 * 
 * 3. **Separación de Responsabilidades**:
 *    - Los tests se enfocan en la lógica, no en la creación de datos
 *    - Los datos están centralizados y son reutilizables
 *    - Cambios en la estructura de datos solo requieren modificar esta clase
 * 
 * 4. **Datos Realistas**:
 *    - Usa valores que podrían aparecer en un entorno real
 *    - Incluye casos edge (nombres cortos, emails complejos, etc.)
 *    - Proporciona tanto casos válidos como inválidos
 * 
 * MEJORES PRÁCTICAS DE TESTING:
 * ==============================
 * - Usa constantes para valores reutilizados
 * - Proporciona métodos para casos comunes y casos edge
 * - Incluye documentación sobre cuándo usar cada método
 * - Mantén la clase simple y enfocada solo en datos
 */
public class TestDataHelper {
    
    // ================================
    // CONSTANTES PARA DATOS DE PRUEBA
    // ================================
    
    // Usuarios predefinidos para tests consistentes
    public static final String VALID_USERNAME = "testuser";
    public static final String VALID_PASSWORD = "password123";
    public static final String VALID_EMAIL = "testuser@ejemplo.com";
    
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String ADMIN_EMAIL = "admin@ejemplo.com";
    
    public static final String USER_USERNAME = "usuario";
    public static final String USER_PASSWORD = "user123";
    public static final String USER_EMAIL = "usuario@ejemplo.com";
    
    // Datos inválidos para tests de validación
    public static final String INVALID_EMAIL = "email-invalido";
    public static final String SHORT_PASSWORD = "123";
    public static final String EMPTY_STRING = "";
    public static final String WHITESPACE_STRING = "   ";
    
    // Tokens para diferentes escenarios
    public static final String INVALID_TOKEN = "token.invalido.aqui";
    public static final String MALFORMED_TOKEN = "esto-no-es-un-jwt";
    
    // ================================
    // FACTORY METHODS PARA ENTIDADES
    // ================================
    
    /**
     * Crea un usuario básico para tests generales.
     * 
     * Usa este método cuando necesites un usuario simple y válido
     * sin características especiales.
     * 
     * @return Usuario con datos válidos básicos
     */
    public static User createValidUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername(VALID_USERNAME);
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD); // En tests reales, esto estaría hasheado
        user.setEnabled(true);
        user.setRoles(Set.of(createUserRole()));
        return user;
    }
    
    /**
     * Crea un usuario con rol de administrador.
     * 
     * Úsalo para tests que requieren permisos administrativos
     * como gestión de usuarios, acceso a endpoints protegidos, etc.
     * 
     * @return Usuario con rol ADMIN
     */
    public static User createAdminUser() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setEnabled(true);
        admin.setRoles(Set.of(createAdminRole()));
        return admin;
    }
    
    /**
     * Crea un usuario regular (sin privilegios administrativos).
     * 
     * Útil para tests de autorización donde necesitas verificar
     * que usuarios normales no pueden acceder a funciones de admin.
     * 
     * @return Usuario con rol USER solamente
     */
    public static User createRegularUser() {
        User user = new User();
        user.setId(3L);
        user.setUsername(USER_USERNAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user.setEnabled(true);
        user.setRoles(Set.of(createUserRole()));
        return user;
    }
    
    /**
     * Crea un usuario desactivado.
     * 
     * Para tests que verifican el comportamiento del sistema
     * con usuarios que han sido desactivados.
     * 
     * @return Usuario con enabled = false
     */
    public static User createDisabledUser() {
        User user = createValidUser();
        user.setId(4L);
        user.setUsername("usuariodesactivado");
        user.setEmail("desactivado@ejemplo.com");
        user.setEnabled(false);
        return user;
    }
    
    /**
     * Crea un usuario con datos personalizados.
     * 
     * Útil cuando necesitas control específico sobre los datos
     * pero quieres aprovechar la configuración base.
     * 
     * @param username nombre de usuario personalizado
     * @param email email personalizado
     * @param enabled estado del usuario
     * @return Usuario con los datos especificados
     */
    public static User createUserWithData(String username, String email, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(VALID_PASSWORD);
        user.setEnabled(enabled);
        user.setRoles(Set.of(createUserRole()));
        return user;
    }
    
    // ================================
    // FACTORY METHODS PARA ROLES
    // ================================
    
    /**
     * Crea el rol USER básico.
     * 
     * @return Rol con nombre "USER"
     */
    public static Role createUserRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        return role;
    }
    
    /**
     * Crea el rol ADMIN.
     * 
     * @return Rol con nombre "ADMIN"
     */
    public static Role createAdminRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName("ADMIN");
        return role;
    }
    
    // ================================
    // FACTORY METHODS PARA DTOs
    // ================================
    
    /**
     * Crea un LoginRequest válido con credenciales por defecto.
     * 
     * Perfecto para tests de login exitoso.
     * 
     * @return LoginRequest con credenciales válidas
     */
    public static LoginRequest createValidLoginRequest() {
        return new LoginRequest(VALID_USERNAME, VALID_PASSWORD);
    }
    
    /**
     * Crea un LoginRequest para el usuario admin.
     * 
     * @return LoginRequest con credenciales de administrador
     */
    public static LoginRequest createAdminLoginRequest() {
        return new LoginRequest(ADMIN_USERNAME, ADMIN_PASSWORD);
    }
    
    /**
     * Crea un LoginRequest con credenciales inválidas.
     * 
     * Útil para tests de autenticación fallida.
     * 
     * @return LoginRequest con password incorrecta
     */
    public static LoginRequest createInvalidLoginRequest() {
        return new LoginRequest(VALID_USERNAME, "passwordIncorrecta");
    }
    
    /**
     * Crea un LoginRequest con username que no existe.
     * 
     * Para tests de usuario no encontrado.
     * 
     * @return LoginRequest con username inexistente
     */
    public static LoginRequest createNonExistentUserLoginRequest() {
        return new LoginRequest("usuarioInexistente", VALID_PASSWORD);
    }
    
    /**
     * Crea un LoginRequest con datos inválidos para validación.
     * 
     * Útil para tests de validación de DTOs.
     * 
     * @return LoginRequest que falla validaciones de Jakarta Validation
     */
    public static LoginRequest createInvalidFormatLoginRequest() {
        return new LoginRequest(EMPTY_STRING, SHORT_PASSWORD);
    }
    
    /**
     * Crea un RegisterRequest válido.
     * 
     * Para tests de registro de usuario exitoso.
     * 
     * @return RegisterRequest con datos válidos
     */
    public static RegisterRequest createValidRegisterRequest() {
        return new RegisterRequest(
            "nuevoUsuario", 
            "nuevo@ejemplo.com", 
            "password123@!",
            "password123@!"
        );
    }
    
    /**
     * Crea un RegisterRequest con email duplicado.
     * 
     * Para tests de validación de unicidad.
     * 
     * @return RegisterRequest con email que ya existe
     */
    public static RegisterRequest createDuplicateEmailRegisterRequest() {
        return new RegisterRequest(
            "otroUsuario", 
            VALID_EMAIL, // Email que ya existe
            "password123@!",
            "password123@!"
        );
    }
    
    /**
     * Crea un RegisterRequest con datos inválidos.
     * 
     * Para tests de validación de formato.
     * 
     * @return RegisterRequest que falla validaciones
     */
    public static RegisterRequest createInvalidRegisterRequest() {
        return new RegisterRequest(
            "us", // Username muy corto
            INVALID_EMAIL, 
            SHORT_PASSWORD,
            SHORT_PASSWORD
        );
    }
    
    // ================================
    // FACTORY METHODS PARA SPRING SECURITY
    // ================================
    
    /**
     * Crea UserDetails para un usuario regular.
     * 
     * Útil para tests de seguridad que requieren un principal autenticado.
     * 
     * @return UserDetails con rol USER
     */
    public static UserDetails createUserDetails() {
        User user = createValidUser();
        return new UserDetailsImpl(user);
    }
    
    /**
     * Crea UserDetails para un administrador.
     * 
     * Para tests que requieren permisos administrativos.
     * 
     * @return UserDetails con rol ADMIN
     */
    public static UserDetails createAdminUserDetails() {
        User admin = createAdminUser();
        return new UserDetailsImpl(admin);
    }
    
    /**
     * Crea UserDetails para un usuario desactivado.
     * 
     * Para tests de usuarios deshabilitados.
     * 
     * @return UserDetails con enabled = false
     */
    public static UserDetails createDisabledUserDetails() {
        User user = createDisabledUser();
        return new UserDetailsImpl(user);
    }
    
    // ================================
    // MÉTODOS DE UTILIDAD
    // ================================
    
    /**
     * Genera un token JWT simulado para tests.
     * 
     * IMPORTANTE: Este no es un token real de JWT, es solo para tests
     * que no requieren validación criptográfica real.
     * 
     * @param username nombre de usuario para el token
     * @return String que simula un token JWT
     */
    public static String createMockJwtToken(String username) {
        // Simulación de un token JWT (no real)
        // Formato: header.payload.signature (base64 encoded)
        String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String payload = java.util.Base64.getEncoder()
            .encodeToString(("{\"sub\":\"" + username + "\",\"exp\":1234567890}").getBytes());
        String signature = "signature";
        
        return header + "." + payload + "." + signature;
    }
    
    /**
     * Crea una lista de usuarios para tests de paginación.
     * 
     * Útil para tests de endpoints que devuelven múltiples usuarios.
     * 
     * @param count cantidad de usuarios a crear
     * @return Lista de usuarios con datos únicos
     */
    public static java.util.List<User> createUserList(int count) {
        java.util.List<User> users = new java.util.ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId((long) (i + 1));
            user.setUsername("usuario" + i);
            user.setEmail("usuario" + i + "@ejemplo.com");
            user.setPassword(VALID_PASSWORD);
            user.setEnabled(true);
            user.setRoles(Set.of(createUserRole()));
            users.add(user);
        }
        
        return users;
    }
    
    /**
     * Valida si una cadena parece un token JWT válido.
     * 
     * Útil para assertions en tests sin necesidad de validación criptográfica.
     * 
     * @param token token a validar
     * @return true si tiene formato de JWT (3 partes separadas por puntos)
     */
    public static boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = token.split("\\.");
        return parts.length == 3 && 
               !parts[0].isEmpty() && 
               !parts[1].isEmpty() && 
               !parts[2].isEmpty();
    }
    
    /**
     * Crea headers HTTP con token JWT válido.
     * 
     * Útil para tests de integración que necesitan simular
     * requests autenticados.
     * 
     * @param token token JWT a incluir en headers
     * @return Map con headers HTTP incluyendo Authorization
     */
    public static java.util.Map<String, String> createAuthHeaders(String token) {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
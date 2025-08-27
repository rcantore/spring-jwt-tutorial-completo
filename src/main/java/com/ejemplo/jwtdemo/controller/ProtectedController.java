package com.ejemplo.jwtdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para endpoints que requieren autenticación JWT.
 * 
 * Este controlador demuestra:
 * - Endpoints que requieren token JWT válido
 * - Diferentes niveles de autorización por roles
 * - Acceso al contexto de seguridad de Spring
 * - Uso de @PreAuthorize para control granular de acceso
 * 
 * Configuración de seguridad:
 * - Todos los endpoints bajo /api/protected/** requieren autenticación
 * - Algunos endpoints tienen restricciones adicionales por rol
 * - El token JWT debe enviarse en el header: Authorization: Bearer {token}
 * 
 * Conceptos de autorización demostrados:
 * - Autenticación: ¿Quién eres? (token válido)
 * - Autorización: ¿Qué puedes hacer? (roles/permisos)
 */
@Slf4j
@RestController
@RequestMapping("/api/protected")
public class ProtectedController {
    
    /**
     * Endpoint básico que requiere solo autenticación.
     * 
     * HTTP GET /api/protected/user
     * 
     * Accesible para cualquier usuario autenticado, sin importar el rol.
     * Demuestra cómo obtener información del usuario desde el contexto de seguridad.
     * 
     * @return información del usuario autenticado
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        log.info("Acceso a información de usuario protegida");
        
        // Obtener información del usuario autenticado actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList()));
        userInfo.put("authenticated", authentication.isAuthenticated());
        userInfo.put("timestamp", LocalDateTime.now());
        userInfo.put("message", "Esta información solo está disponible para usuarios autenticados");
        
        return ResponseEntity.ok(userInfo);
    }
    
    /**
     * Endpoint que requiere rol de ADMIN.
     * 
     * HTTP GET /api/protected/admin
     * 
     * Demuestra:
     * - Control de acceso basado en roles
     * - @PreAuthorize para autorización declarativa
     * - Separación entre autenticación y autorización
     * 
     * Solo usuarios con rol ROLE_ADMIN pueden acceder.
     * Si un usuario autenticado pero sin el rol correcto intenta acceder,
     * recibe 403 FORBIDDEN.
     * 
     * @return información solo para administradores
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminInfo() {
        log.info("Acceso a información de administrador");
        
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("message", "¡Bienvenido, Administrador!");
        adminInfo.put("adminFeatures", List.of(
            "Gestión de usuarios",
            "Configuración del sistema",
            "Acceso a logs",
            "Estadísticas globales",
            "Operaciones privilegiadas"
        ));
        adminInfo.put("timestamp", LocalDateTime.now());
        adminInfo.put("accessLevel", "ADMIN");
        
        return ResponseEntity.ok(adminInfo);
    }
    
    /**
     * Endpoint que requiere rol específico de USER.
     * 
     * HTTP GET /api/protected/user-only
     * 
     * Demuestra diferenciación entre roles USER y ADMIN.
     * En aplicaciones reales, podrías tener diferentes niveles:
     * - GUEST: acceso mínimo
     * - USER: funcionalidades básicas
     * - MODERATOR: funcionalidades intermedias
     * - ADMIN: acceso completo
     * 
     * @return información específica para usuarios regulares
     */
    @GetMapping("/user-only")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getUserOnlyInfo() {
        log.info("Acceso a información específica de usuario");
        
        Map<String, Object> userOnlyInfo = new HashMap<>();
        userOnlyInfo.put("message", "Contenido específico para usuarios regulares");
        userOnlyInfo.put("userFeatures", List.of(
            "Perfil personal",
            "Configuración de cuenta",
            "Historial de actividades",
            "Notificaciones personales"
        ));
        userOnlyInfo.put("timestamp", LocalDateTime.now());
        userOnlyInfo.put("accessLevel", "USER");
        
        return ResponseEntity.ok(userOnlyInfo);
    }
    
    /**
     * Endpoint con autorización compleja usando SpEL (Spring Expression Language).
     * 
     * HTTP GET /api/protected/advanced
     * 
     * Demuestra:
     * - Expresiones complejas en @PreAuthorize
     * - Combinación de múltiples condiciones
     * - Flexibilidad del sistema de autorización
     * 
     * Accesible para usuarios que:
     * - Tienen rol ADMIN, O
     * - Tienen rol USER y están autenticados (ejemplo de lógica compleja)
     * 
     * @return información con autorización avanzada
     */
    @GetMapping("/advanced")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and isAuthenticated())")
    public ResponseEntity<Map<String, Object>> getAdvancedInfo() {
        log.info("Acceso a endpoint con autorización avanzada");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        Map<String, Object> advancedInfo = new HashMap<>();
        advancedInfo.put("message", "Contenido con autorización avanzada");
        advancedInfo.put("username", authentication.getName());
        advancedInfo.put("isAdmin", isAdmin);
        advancedInfo.put("accessType", isAdmin ? "Acceso de administrador" : "Acceso de usuario");
        advancedInfo.put("timestamp", LocalDateTime.now());
        advancedInfo.put("availableActions", isAdmin ? 
            List.of("Ver", "Crear", "Modificar", "Eliminar", "Administrar") :
            List.of("Ver", "Crear", "Modificar propio"));
        
        return ResponseEntity.ok(advancedInfo);
    }
    
    /**
     * Endpoint de prueba para verificar conectividad autenticada.
     * 
     * HTTP GET /api/protected/ping
     * 
     * Útil para:
     * - Verificar que el token JWT funciona
     * - Testing de conectividad autenticada
     * - Debugging de problemas de autenticación
     * - Monitoreo de sesiones activas
     * 
     * @return mensaje simple confirmando autenticación
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(
            String.format("Pong! Usuario '%s' autenticado correctamente a las %s",
                authentication.getName(),
                LocalDateTime.now())
        );
    }
}
package com.ejemplo.jwtdemo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ejemplo.jwtdemo.dto.JwtResponse;
import com.ejemplo.jwtdemo.dto.LoginRequest;
import com.ejemplo.jwtdemo.dto.RegisterRequest;
import com.ejemplo.jwtdemo.dto.UserResponse;
import com.ejemplo.jwtdemo.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para operaciones de autenticación.
 * 
 * Este controlador maneja:
 * - Registro de nuevos usuarios
 * - Login de usuarios existentes
 * - Obtención de perfil del usuario actual
 * 
 * Conceptos de REST API demostrados:
 * - @RestController: Combina @Controller + @ResponseBody
 * - @RequestMapping: Define la ruta base para todos los endpoints
 * - Métodos HTTP específicos (@PostMapping, @GetMapping)
 * - Validación automática con @Valid
 * - Códigos de estado HTTP apropiados
 * - Estructura consistente de respuestas
 * 
 * Patrones aplicados:
 * - Controller Layer: Maneja peticiones HTTP y delega lógica al servicio
 * - DTO Pattern: Usa DTOs para entrada y salida, nunca expone entidades
 * - Validation: Valida datos de entrada automáticamente
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Endpoint para registro de nuevos usuarios.
     * 
     * HTTP POST /api/auth/register
     * 
     * Características:
     * - @Valid activa las validaciones de Jakarta Validation en RegisterRequest
     * - Si las validaciones fallan, se lanza MethodArgumentNotValidException
     * - El GlobalExceptionHandler captura y convierte errores en respuestas apropiadas
     * - Devuelve 201 CREATED para indicar que se creó un nuevo recurso
     * 
     * @param registerRequest datos del nuevo usuario (validados automáticamente)
     * @return información del usuario registrado con estado 201
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Petición de registro recibida para: {}", registerRequest.getUsername());
        
        UserResponse userResponse = authService.register(registerRequest);
        
        // 201 CREATED es el código apropiado cuando se crea un nuevo recurso
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
    
    /**
     * Endpoint para login de usuarios.
     * 
     * HTTP POST /api/auth/login
     * 
     * Proceso:
     * 1. Valida el formato de las credenciales (@Valid)
     * 2. Delega la autenticación al AuthService
     * 3. Si es exitoso, devuelve token JWT y datos del usuario
     * 4. Si falla, el GlobalExceptionHandler maneja las excepciones apropiadas
     * 
     * @param loginRequest credenciales del usuario
     * @return token JWT y información del usuario autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Petición de login recibida para: {}", loginRequest.getUsername());
        
        JwtResponse jwtResponse = authService.login(loginRequest);
        
        // 200 OK es apropiado para login exitoso (no se crea un nuevo recurso)
        return ResponseEntity.ok(jwtResponse);
    }
    
    /**
     * Endpoint para obtener el perfil del usuario actualmente autenticado.
     * 
     * HTTP GET /api/auth/profile
     * 
     * Este endpoint demuestra:
     * - Acceso a información del usuario autenticado
     * - No requiere parámetros (usa el contexto de seguridad)
     * - Requiere autenticación (JWT válido en header Authorization)
     * 
     * El filtro JwtAuthenticationFilter procesará el token JWT y establecerá
     * el contexto de seguridad antes de que este método sea ejecutado.
     * 
     * @return información del usuario actual
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        log.info("Petición de perfil de usuario autenticado");
        
        UserResponse userResponse = authService.getCurrentUserProfile();
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Endpoint de prueba para verificar que la autenticación funciona.
     * 
     * HTTP GET /api/auth/test
     * 
     * Este es un endpoint simple que requiere autenticación pero no hace
     * operaciones complejas. Útil para:
     * - Probar que los tokens JWT funcionan correctamente
     * - Verificar la configuración de seguridad
     * - Debugging de problemas de autenticación
     */
    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> testAuthentication() {
        return ResponseEntity.ok("Si puedes ver este mensaje, tu token JWT es válido!");
    }
}
package com.ejemplo.jwtdemo.controller;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ejemplo.jwtdemo.dto.UserResponse;
import com.ejemplo.jwtdemo.entity.User;
import com.ejemplo.jwtdemo.exception.UserNotFoundException;
import com.ejemplo.jwtdemo.repository.UserRepository;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para gestión de usuarios (operaciones CRUD).
 * 
 * Este controlador demuestra:
 * - Operaciones CRUD completas en REST API
 * - Paginación y ordenamiento con Spring Data
 * - Validación de parámetros con Jakarta Validation
 * - Control de acceso basado en roles
 * - Conversión de entidades a DTOs
 * - Manejo de recursos no encontrados
 * 
 * Endpoints disponibles:
 * - GET /api/users - Listar usuarios (paginado)
 * - GET /api/users/{id} - Obtener usuario específico
 * - PUT /api/users/{id}/toggle-status - Activar/desactivar usuario
 * - DELETE /api/users/{id} - Eliminar usuario
 * 
 * Todos los endpoints requieren rol ADMIN (excepto donde se especifique).
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserRepository userRepository;
    
    /**
     * Lista todos los usuarios con paginación y ordenamiento.
     * 
     * HTTP GET /api/users?page=0&size=10&sort=username
     * 
     * Parámetros de query opcionales:
     * - page: número de página (default: 0)
     * - size: tamaño de página (default: 20)
     * - sort: campo para ordenar (default: id)
     * - direction: dirección de ordenamiento (asc/desc, default: asc)
     * 
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * Conceptos demostrados:
     * - Paginación para manejar grandes volúmenes de datos
     * - Ordenamiento configurable
     * - Parámetros opcionales con valores por defecto
     * - Conversión automática de Page<Entity> usando streams
     * 
     * @param page número de página
     * @param size cantidad de elementos por página
     * @param sortBy campo para ordenar
     * @param direction dirección del ordenamiento
     * @return página de usuarios con metadatos de paginación
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        log.info("Solicitando página {} de usuarios, tamaño: {}, ordenado por: {} {}", 
                page, size, sortBy, direction);
        
        // Crear especificación de ordenamiento
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        
        // Crear especificación de paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Obtener página de usuarios de la base de datos
        Page<User> usersPage = userRepository.findAll(pageable);
        
        // Convertir Page<User> a Page<UserResponse>
        Page<UserResponse> userResponsePage = usersPage.map(this::convertToUserResponse);
        
        log.info("Devolviendo {} usuarios de {} totales", 
                userResponsePage.getNumberOfElements(), 
                userResponsePage.getTotalElements());
        
        return ResponseEntity.ok(userResponsePage);
    }
    
    /**
     * Obtiene un usuario específico por su ID.
     * 
     * HTTP GET /api/users/{id}
     * 
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * Manejo de errores:
     * - Si el usuario no existe, lanza UserNotFoundException
     * - El GlobalExceptionHandler convierte esto en respuesta 404
     * 
     * @param id identificador único del usuario
     * @return información del usuario solicitado
     * @throws UserNotFoundException si el usuario no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable @Min(1) Long id) {
        
        log.info("Solicitando usuario con ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));
        
        UserResponse userResponse = convertToUserResponse(user);
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Busca usuarios por nombre de usuario (búsqueda parcial).
     * 
     * HTTP GET /api/users/search?username=john
     * 
     * Demuestra:
     * - Búsqueda con patrón LIKE
     * - Query methods de Spring Data JPA
     * - Búsqueda case-insensitive
     * 
     * @param username parte del nombre de usuario a buscar
     * @return lista de usuarios que coinciden con el patrón
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<UserResponse>> searchUsersByUsername(
            @RequestParam String username) {
        
        log.info("Buscando usuarios con username que contenga: {}", username);
        
        // Usando query method de Spring Data JPA
        java.util.List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);
        
        java.util.List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        
        log.info("Encontrados {} usuarios que coinciden con: {}", userResponses.size(), username);
        
        return ResponseEntity.ok(userResponses);
    }
    
    /**
     * Activa o desactiva un usuario (soft delete).
     * 
     * HTTP PUT /api/users/{id}/toggle-status
     * 
     * En lugar de eliminar usuarios completamente, es mejor práctica
     * desactivarlos para mantener integridad referencial y auditoría.
     * 
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param id identificador del usuario
     * @return usuario con estado actualizado
     * @throws UserNotFoundException si el usuario no existe
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable @Min(1) Long id) {
        
        log.info("Alternando estado del usuario con ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));
        
        // Alternar el estado enabled
        boolean previousStatus = user.isEnabled();
        user.setEnabled(!previousStatus);
        
        User updatedUser = userRepository.save(user);
        
        log.info("Usuario {} cambió de estado {} a {}", 
                user.getUsername(), previousStatus, updatedUser.isEnabled());
        
        UserResponse userResponse = convertToUserResponse(updatedUser);
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Elimina un usuario completamente de la base de datos.
     * 
     * HTTP DELETE /api/users/{id}
     * 
     * ADVERTENCIA: Esta es una eliminación física (hard delete).
     * En aplicaciones de producción, considera:
     * - Soft delete (marcar como eliminado)
     * - Archivado en lugar de eliminación
     * - Restricciones de integridad referencial
     * 
     * Solo accesible para usuarios con rol ADMIN.
     * 
     * @param id identificador del usuario a eliminar
     * @return respuesta vacía con estado 204 No Content
     * @throws UserNotFoundException si el usuario no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Min(1) Long id) {
        
        log.info("Eliminando usuario con ID: {}", id);
        
        // Verificar que el usuario existe antes de intentar eliminar
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));
        
        userRepository.delete(user);
        
        log.info("Usuario {} eliminado exitosamente", user.getUsername());
        
        // 204 No Content es el código apropiado para eliminaciones exitosas
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Obtiene estadísticas básicas de usuarios.
     * 
     * HTTP GET /api/users/stats
     * 
     * Útil para dashboards de administración y monitoreo.
     * 
     * @return estadísticas de usuarios en el sistema
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getUserStats() {
        log.info("Solicitando estadísticas de usuarios");
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long inactiveUsers = totalUsers - activeUsers;
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);
        stats.put("activationRate", totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Convierte una entidad User a UserResponse DTO.
     * 
     * Esta conversión:
     * - Omite información sensible (contraseña)
     * - Simplifica roles a nombres de strings
     * - Estructura los datos para respuesta de API
     * 
     * @param user entidad de usuario
     * @return DTO de respuesta de usuario
     */
    private UserResponse convertToUserResponse(User user) {
        java.util.Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.isEnabled(),
            roleNames
        );
    }
}
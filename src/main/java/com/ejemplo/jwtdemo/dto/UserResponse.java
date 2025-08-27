package com.ejemplo.jwtdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para devolver información del usuario sin datos sensibles.
 * 
 * Principios aplicados:
 * - Nunca exponer la contraseña (ni siquiera hasheada)
 * - Solo incluir datos que el cliente realmente necesita
 * - Estructura consistente para respuestas de usuario
 * 
 * Este DTO se usa en:
 * - Respuestas de perfil de usuario
 * - Listados de usuarios (para administradores)
 * - Cualquier endpoint que devuelva datos de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    /**
     * ID único del usuario.
     */
    private Long id;
    
    /**
     * Nombre de usuario público.
     */
    private String username;
    
    /**
     * Email del usuario.
     * 
     * Nota: En algunos casos podrías querer ocultar o enmascarar
     * el email dependiendo de los permisos del usuario que hace la petición.
     */
    private String email;
    
    /**
     * Estado de activación de la cuenta.
     * 
     * Útil para:
     * - Mostrar estado de cuenta en interfaces de administración
     * - Implementar funcionalidades de suspensión de usuarios
     * - Control de acceso granular
     */
    private boolean enabled;
    
    /**
     * Roles asignados al usuario.
     * 
     * Se devuelven como Set<String> en lugar de entidades Role completas
     * para simplificar la respuesta y evitar problemas de serialización.
     */
    private Set<String> roles;
    
    /**
     * Constructor de conveniencia para crear respuestas básicas.
     */
    public UserResponse(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = true;
    }
}
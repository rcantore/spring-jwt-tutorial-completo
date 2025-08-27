package com.ejemplo.jwtdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para enviar la respuesta después de una autenticación exitosa.
 * 
 * Este DTO encapsula toda la información que el cliente necesita
 * después de un login exitoso, incluyendo el token JWT y datos del usuario.
 * 
 * Ventajas de usar este DTO:
 * - Estructura consistente en las respuestas de autenticación
 * - Fácil extensión para agregar más campos en el futuro
 * - Separación clara entre token y datos de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    /**
     * Token JWT que el cliente debe incluir en las siguientes peticiones.
     * 
     * Este token contiene:
     * - Información del usuario (claims)
     * - Fecha de expiración
     * - Firma digital para verificar autenticidad
     */
    private String token;
    
    /**
     * Tipo de token - siempre "Bearer" para JWT.
     * 
     * Esto indica al cliente cómo debe enviar el token:
     * Authorization: Bearer {token}
     */
    private String type = "Bearer";
    
    /**
     * ID único del usuario autenticado.
     */
    private Long id;
    
    /**
     * Nombre de usuario.
     */
    private String username;
    
    /**
     * Email del usuario.
     */
    private String email;
    
    /**
     * Lista de roles/permisos del usuario.
     * 
     * Enviamos los roles al cliente para que pueda:
     * - Mostrar/ocultar elementos de UI según permisos
     * - Hacer validaciones de frontend (nunca confiar solo en frontend)
     * - Mejorar la experiencia de usuario
     */
    private List<String> roles;
    
    /**
     * Constructor personalizado que establece el tipo por defecto.
     * Útil cuando solo queremos especificar el token y otros datos básicos.
     */
    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
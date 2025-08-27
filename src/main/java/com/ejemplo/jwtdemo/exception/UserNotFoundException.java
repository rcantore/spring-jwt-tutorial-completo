package com.ejemplo.jwtdemo.exception;

/**
 * Excepción para indicar que un usuario solicitado no fue encontrado.
 * 
 * Se lanza típicamente en operaciones como:
 * - Búsqueda de usuario por ID para operaciones de administración
 * - Actualización de perfil de usuario inexistente
 * - Operaciones que requieren un usuario específico
 * 
 * Diferencia con InvalidCredentialsException:
 * - Esta se usa para operaciones generales donde necesitamos un usuario específico
 * - InvalidCredentialsException se usa específicamente para procesos de autenticación
 */
public class UserNotFoundException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message mensaje descriptivo del error
     */
    public UserNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * @param message mensaje descriptivo del error
     * @param cause excepción que causó este error
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor de conveniencia para crear excepción con ID de usuario.
     * 
     * @param userId ID del usuario que no fue encontrado
     */
    public static UserNotFoundException withId(Long userId) {
        return new UserNotFoundException("Usuario con ID " + userId + " no encontrado");
    }
    
    /**
     * Constructor de conveniencia para crear excepción con nombre de usuario.
     * 
     * @param username nombre del usuario que no fue encontrado
     */
    public static UserNotFoundException withUsername(String username) {
        return new UserNotFoundException("Usuario '" + username + "' no encontrado");
    }
}
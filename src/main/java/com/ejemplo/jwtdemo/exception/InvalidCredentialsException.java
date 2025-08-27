package com.ejemplo.jwtdemo.exception;

/**
 * Excepción para indicar credenciales inválidas durante el proceso de autenticación.
 * 
 * Se lanza cuando:
 * - El usuario no existe
 * - La contraseña es incorrecta
 * - La cuenta está deshabilitada
 * 
 * Nota de seguridad:
 * Por razones de seguridad, no debemos especificar si el error fue por usuario
 * inexistente o contraseña incorrecta. Esto previene ataques de enumeración
 * de usuarios donde un atacante podría determinar qué usuarios existen en el sistema.
 */
public class InvalidCredentialsException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message mensaje descriptivo del error
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * @param message mensaje descriptivo del error
     * @param cause excepción que causó este error
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
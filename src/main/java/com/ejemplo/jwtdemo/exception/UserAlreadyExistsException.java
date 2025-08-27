package com.ejemplo.jwtdemo.exception;

/**
 * Excepción personalizada para indicar que un usuario ya existe en el sistema.
 * 
 * Ventajas de crear excepciones específicas:
 * - Semántica clara: el nombre de la excepción explica exactamente el problema
 * - Manejo diferenciado: podemos capturar y manejar específicamente este error
 * - Código más limpio: evita usar excepciones genéricas para casos específicos
 * - Mejor logging: facilita identificar tipos específicos de errores en logs
 * 
 * Extiende RuntimeException porque:
 * - No requiere declaración explícita en throws
 * - Se puede lanzar desde cualquier punto del código
 * - Spring Boot puede capturarla automáticamente en @ControllerAdvice
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message mensaje descriptivo del error
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * Útil cuando esta excepción es resultado de otra excepción
     * (ej: SQLException por violación de constraint de unicidad).
     * 
     * @param message mensaje descriptivo del error
     * @param cause excepción que causó este error
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
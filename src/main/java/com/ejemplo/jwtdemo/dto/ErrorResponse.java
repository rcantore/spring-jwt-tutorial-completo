package com.ejemplo.jwtdemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas.
 * 
 * Beneficios de estandarizar respuestas de error:
 * - Consistencia en toda la API
 * - Facilita el manejo de errores en el frontend
 * - Mejor experiencia para desarrolladores que consumen la API
 * - Logging y debugging más eficientes
 * 
 * Sigue el patrón RFC 7807 (Problem Details for HTTP APIs) adaptado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Timestamp de cuando ocurrió el error.
     * 
     * Útil para:
     * - Correlación con logs del servidor
     * - Debugging de problemas temporales
     * - Experiencia de usuario (mostrar "hace X minutos")
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP.
     * 
     * Aunque ya viene en la respuesta HTTP, incluirlo aquí:
     * - Facilita el manejo programático
     * - Ayuda en debugging
     * - Consistencia con estándares de API
     */
    private int status;
    
    /**
     * Nombre del error HTTP (ej: "Bad Request", "Unauthorized").
     */
    private String error;
    
    /**
     * Mensaje principal del error.
     * 
     * Debe ser:
     * - Claro y descriptivo
     * - Apropiado para mostrar al usuario final
     * - En español para este proyecto educativo
     */
    private String message;
    
    /**
     * Ruta donde ocurrió el error.
     * 
     * Ayuda a:
     * - Identificar rápidamente el endpoint problemático
     * - Correlacionar con logs del servidor
     * - Debugging más eficiente
     */
    private String path;
    
    /**
     * Lista de errores de validación detallados.
     * 
     * Especialmente útil para errores 400 (Bad Request) donde
     * múltiples campos pueden tener problemas de validación.
     * 
     * Ejemplo: ["El email es obligatorio", "La contraseña debe tener al menos 8 caracteres"]
     */
    private List<String> validationErrors;
    
    /**
     * Constructor para errores simples sin validaciones.
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    
    /**
     * Constructor de conveniencia que establece el timestamp automáticamente.
     */
    public static ErrorResponse create(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path);
    }
    
    /**
     * Constructor para errores con validaciones múltiples.
     */
    public static ErrorResponse withValidations(int status, String error, String message, String path, List<String> validationErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
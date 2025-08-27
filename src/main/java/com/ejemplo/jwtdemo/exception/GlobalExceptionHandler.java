package com.ejemplo.jwtdemo.exception;

import com.ejemplo.jwtdemo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la aplicación.
 * 
 * @RestControllerAdvice combina:
 * - @ControllerAdvice: Permite manejar excepciones de múltiples controladores
 * - @ResponseBody: Convierte automáticamente las respuestas a JSON
 * 
 * Ventajas del manejo centralizado de excepciones:
 * - Respuestas consistentes en toda la API
 * - Separación de responsabilidades (controladores se enfocan en lógica de negocio)
 * - Mantenimiento más fácil
 * - Logging centralizado de errores
 * - Transformación automática de excepciones internas a respuestas HTTP apropiadas
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja errores de validación de Jakarta Validation en DTOs.
     * 
     * Se activa cuando un @Valid en un controlador falla.
     * Extrae todos los errores de validación y los convierte en una respuesta amigable.
     * 
     * @param ex excepción de validación
     * @param request petición HTTP que causó el error
     * @return respuesta con detalles de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        log.warn("Errores de validación en {}: {}", request.getRequestURI(), ex.getMessage());
        
        // Extraer todos los errores de validación
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.withValidations(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Los datos proporcionados no son válidos",
                request.getRequestURI(),
                validationErrors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Maneja violaciones de constraints de validación.
     * 
     * Se activa para validaciones a nivel de método o parámetros.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        log.warn("Violación de constraints en {}: {}", request.getRequestURI(), ex.getMessage());
        
        List<String> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.withValidations(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "Los datos no cumplen con las restricciones requeridas",
                request.getRequestURI(),
                validationErrors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Maneja intentos de crear usuarios que ya existen.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {
        
        log.warn("Intento de crear usuario duplicado en {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.CONFLICT.value(),
                "User Already Exists",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Maneja usuarios no encontrados.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {
        
        log.warn("Usuario no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Maneja credenciales inválidas para login.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        
        log.warn("Credenciales inválidas en {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid Credentials",
                "Credenciales inválidas",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Maneja errores de autenticación de Spring Security.
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationError(
            AuthenticationException ex,
            HttpServletRequest request) {
        
        log.warn("Error de autenticación en {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                "Error de autenticación",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Maneja errores de acceso denegado (falta de permisos).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                "No tienes permisos para acceder a este recurso",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Maneja cualquier excepción no contemplada específicamente.
     * 
     * Este es el "catch-all" que garantiza que nunca devolvamos
     * una respuesta sin estructura o que exponga información interna.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ha ocurrido un error interno. Por favor, inténtalo de nuevo más tarde.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
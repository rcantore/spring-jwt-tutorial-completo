package com.ejemplo.jwtdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para endpoints públicos que no requieren autenticación.
 * 
 * Este controlador demuestra:
 * - Endpoints accesibles sin token JWT
 * - Diferentes tipos de respuestas REST
 * - Uso de @PathVariable para parámetros de URL
 * - Estructuras de respuesta variadas (String, Map, List)
 * 
 * Casos de uso típicos para endpoints públicos:
 * - Información general de la aplicación
 * - Endpoints de salud y monitoreo
 * - Contenido público (catálogos, información corporativa)
 * - APIs que necesitan ser consumidas por sistemas externos sin autenticación
 * 
 * Configuración de seguridad:
 * Los endpoints bajo /api/public/** están configurados como públicos
 * en SecurityConfig.java usando .requestMatchers("/api/public/**").permitAll()
 */
@Slf4j
@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    /**
     * Endpoint básico de bienvenida.
     * 
     * HTTP GET /api/public/welcome
     * 
     * Demuestra:
     * - Respuesta simple de texto
     * - Endpoint completamente público
     * - Estructura básica de controlador REST
     * 
     * @return mensaje de bienvenida
     */
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        log.info("Acceso al endpoint público de bienvenida");
        
        return ResponseEntity.ok(
            "¡Bienvenido a la API JWT Demo! Este endpoint es público y no requiere autenticación."
        );
    }
    
    /**
     * Endpoint que devuelve información del sistema.
     * 
     * HTTP GET /api/public/info
     * 
     * Demuestra:
     * - Respuesta con estructura Map (se convierte a JSON automáticamente)
     * - Información útil para monitoreo o debugging
     * - Timestamp dinámico
     * 
     * @return información del sistema
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        log.info("Petición de información del sistema");
        
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("application", "JWT Demo API");
        systemInfo.put("version", "1.0.0");
        systemInfo.put("timestamp", LocalDateTime.now());
        systemInfo.put("status", "running");
        systemInfo.put("description", "API de demostración de JWT con Spring Boot");
        
        return ResponseEntity.ok(systemInfo);
    }
    
    /**
     * Endpoint que devuelve una lista de características públicas.
     * 
     * HTTP GET /api/public/features
     * 
     * Demuestra:
     * - Respuesta con lista (se convierte a JSON array)
     * - Contenido estático que podría venir de una base de datos
     * - Información útil para interfaces públicas
     * 
     * @return lista de características de la aplicación
     */
    @GetMapping("/features")
    public ResponseEntity<List<String>> getPublicFeatures() {
        log.info("Petición de características públicas");
        
        List<String> features = List.of(
            "Autenticación JWT segura",
            "Registro de usuarios con validaciones",
            "Endpoints protegidos por roles",
            "Manejo centralizado de excepciones",
            "Documentación de API integrada",
            "Base de datos H2 para desarrollo",
            "Logging estructurado",
            "Validaciones de entrada robustas"
        );
        
        return ResponseEntity.ok(features);
    }
    
    /**
     * Endpoint con parámetro de path variable.
     * 
     * HTTP GET /api/public/echo/{message}
     * 
     * Demuestra:
     * - Uso de @PathVariable para capturar parámetros de URL
     * - Procesamiento dinámico de entrada
     * - Validación básica de parámetros
     * 
     * Ejemplos:
     * - GET /api/public/echo/hello -> "Echo: hello"
     * - GET /api/public/echo/testing -> "Echo: testing"
     * 
     * @param message mensaje a hacer echo
     * @return mensaje con prefijo "Echo: "
     */
    @GetMapping("/echo/{message}")
    public ResponseEntity<String> echo(@PathVariable String message) {
        log.info("Echo solicitado para mensaje: {}", message);
        
        // Validación básica del parámetro
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body("Error: El mensaje no puede estar vacío");
        }
        
        // Límite de longitud para prevenir abuse
        if (message.length() > 100) {
            return ResponseEntity.badRequest()
                .body("Error: El mensaje no puede exceder 100 caracteres");
        }
        
        return ResponseEntity.ok("Echo: " + message);
    }
    
    /**
     * Endpoint de health check.
     * 
     * HTTP GET /api/public/health
     * 
     * Útil para:
     * - Monitoreo de infraestructura
     * - Load balancers que verifican si la aplicación responde
     * - Sistemas de alertas
     * - DevOps y monitoring
     * 
     * @return estado de salud de la aplicación
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        // En aplicaciones reales, aquí podrías verificar:
        // - Conectividad con la base de datos
        // - Estado de servicios externos
        // - Uso de memoria/CPU
        // - Otros componentes críticos
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", "Sistema funcionando correctamente");
        
        return ResponseEntity.ok(health);
    }
}
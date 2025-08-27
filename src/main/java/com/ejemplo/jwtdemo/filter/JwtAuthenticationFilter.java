package com.ejemplo.jwtdemo.filter;

import com.ejemplo.jwtdemo.service.CustomUserDetailsService;
import com.ejemplo.jwtdemo.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta todas las requests HTTP
 * 
 * Este filtro extiende OncePerRequestFilter para garantizar que se ejecute
 * exactamente una vez por request, evitando problemas con forwards internos.
 * 
 * Responsabilidades del filtro:
 * 1. Extraer el token JWT del header Authorization
 * 2. Validar el formato y firma del token
 * 3. Extraer información del usuario del token
 * 4. Cargar detalles completos del usuario desde la base de datos
 * 5. Establecer la autenticación en el SecurityContext
 * 
 * Conceptos clave:
 * - Filter Chain: Cadena de filtros que procesa requests antes del controller
 * - SecurityContext: Almacena información de autenticación para el thread actual
 * - Stateless Authentication: No mantiene sesión, cada request debe incluir credenciales
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * Constructor con inyección de dependencias
     * 
     * @param jwtService - Servicio para operaciones con tokens JWT
     * @param userDetailsService - Servicio para cargar detalles del usuario
     */
    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Método principal del filtro - se ejecuta en cada request HTTP
     * 
     * @param request - La solicitud HTTP entrante
     * @param response - La respuesta HTTP
     * @param filterChain - La cadena de filtros para continuar el procesamiento
     * 
     * Flujo de ejecución:
     * 1. Extraer token del header Authorization
     * 2. Si no hay token, continuar sin autenticar
     * 3. Si hay token, validarlo y extraer username
     * 4. Si el usuario no está ya autenticado, cargar detalles y autenticar
     * 5. Continuar con la cadena de filtros
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Paso 1: Extraer el token JWT del header Authorization
            String jwt = parseJwtFromRequest(request);
            
            // Paso 2: Si no hay token, continuar sin autenticación
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Paso 3: Extraer username del token JWT
            String username = jwtService.getUsernameFromToken(jwt);
            
            // Paso 4: Si tenemos username y no hay autenticación previa
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Cargar detalles completos del usuario desde la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validar que el token sea válido para este usuario
                if (jwtService.validateToken(jwt, userDetails)) {
                    
                    // Crear objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // No password needed for JWT authentication
                                    userDetails.getAuthorities()
                            );
                    
                    // Agregar detalles adicionales de la request (IP, session, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Establecer la autenticación en el SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT ha expirado: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token expirado\"}");
            response.setContentType("application/json");
            return;
            
        } catch (SignatureException e) {
            logger.error("Firma del token JWT inválida: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token con firma inválida\"}");
            response.setContentType("application/json");
            return;
            
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Token malformado\"}");
            response.setContentType("application/json");
            return;
            
        } catch (Exception e) {
            logger.error("Error procesando token JWT: " + e.getMessage());
            // No bloquear el request por errores inesperados, solo no autenticar
        }
        
        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extrae el token JWT del header Authorization
     * 
     * @param request - La solicitud HTTP
     * @return String - El token JWT sin el prefijo "Bearer ", o null si no está presente
     * 
     * Formato esperado del header: "Authorization: Bearer <token>"
     * 
     * Proceso:
     * 1. Obtener el valor del header "Authorization"
     * 2. Verificar que tenga contenido y empiece con "Bearer "
     * 3. Extraer solo la parte del token (sin "Bearer ")
     */
    private String parseJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        // Verificar que el header tenga contenido y el formato correcto
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Extraer el token removiendo "Bearer " (7 caracteres)
            return headerAuth.substring(7);
        }
        
        return null;
    }
    
    /**
     * Método para determinar si se debe filtrar una request específica
     * 
     * @param request - La solicitud HTTP
     * @return boolean - false significa que siempre se ejecuta el filtro
     * 
     * Nota: Retornamos false para procesar todas las requests.
     * En un escenario real, podrías excluir endpoints públicos como:
     * - /api/auth/login
     * - /api/auth/register  
     * - /api/public/**
     * 
     * Ejemplo de implementación con exclusiones:
     * 
     * @Override
     * protected boolean shouldNotFilter(HttpServletRequest request) {
     *     String path = request.getRequestURI();
     *     return path.startsWith("/api/auth/") || 
     *            path.startsWith("/api/public/") ||
     *            path.equals("/");
     * }
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Por ahora, aplicar el filtro a todas las requests
        // La lógica de endpoints públicos se maneja en SecurityConfig
        return false;
    }
}

/*
 * NOTAS EDUCATIVAS ADICIONALES:
 * 
 * 1. ¿Por qué OncePerRequestFilter?
 *    - Garantiza ejecución única por request
 *    - Evita problemas con forwards/includes internos
 *    - Maneja correctamente requests asíncronas
 * 
 * 2. ¿Por qué validar el token en cada request?
 *    - Arquitectura stateless: no hay sesión del lado del servidor
 *    - El token puede haber expirado desde la última validación
 *    - Permite revocación inmediata de permisos
 * 
 * 3. Manejo de errores:
 *    - Errores críticos (token expirado) → respuesta 401
 *    - Errores de formato → respuesta 400
 *    - Errores inesperados → continúa sin autenticar
 * 
 * 4. Orden en la cadena de filtros:
 *    - Este filtro debe ejecutarse ANTES de los filtros de autorización
 *    - La configuración del orden se hace en SecurityConfig
 * 
 * 5. Performance:
 *    - Se carga el usuario completo en cada request autenticado
 *    - En producción, considera implementar cache con TTL corto
 *    - Alternative: incluir roles en el token JWT (trade-off seguridad vs performance)
 */
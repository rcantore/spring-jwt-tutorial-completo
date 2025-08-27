package com.ejemplo.jwtdemo.config;

import com.ejemplo.jwtdemo.filter.JwtAuthenticationFilter;
import com.ejemplo.jwtdemo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración principal de Spring Security para autenticación JWT
 * 
 * Esta clase configura todos los componentes necesarios para implementar
 * un sistema de autenticación stateless basado en tokens JWT.
 * 
 * Conceptos clave:
 * - Stateless Authentication: No mantiene sesión del lado del servidor
 * - Filter Chain: Cadena de filtros que procesan requests HTTP
 * - Authentication Provider: Componente que realiza la autenticación
 * - CORS: Configuración para requests cross-origin
 * 
 * Anotaciones importantes:
 * - @EnableWebSecurity: Activa la configuración de seguridad web
 * - @EnableMethodSecurity: Permite autorización a nivel de método (@PreAuthorize, @PostAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Constructor con inyección de dependencias
     */
    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * Configuración principal de la cadena de filtros de seguridad
     * 
     * @param http - Objeto HttpSecurity para configurar seguridad HTTP
     * @return SecurityFilterChain - Cadena de filtros configurada
     * 
     * Configuraciones aplicadas:
     * 1. CORS habilitado con configuración personalizada
     * 2. CSRF deshabilitado (no necesario en APIs stateless)
     * 3. Autorización de endpoints configurada
     * 4. Gestión de sesiones deshabilitada (STATELESS)
     * 5. Proveedor de autenticación personalizado
     * 6. Filtro JWT agregado antes del filtro de autenticación estándar
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configurar CORS - Permitir requests cross-origin
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Deshabilitar CSRF - No necesario para APIs REST stateless
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar autorización de requests
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos - No requieren autenticación
                .requestMatchers(
                    "/api/auth/login",        // Login endpoint
                    "/api/auth/register",     // Register endpoint
                    "/api/public/**",         // Endpoints públicos generales
                    "/h2-console/**",         // Consola H2 para desarrollo
                    "/actuator/health",       // Health check
                    "/swagger-ui/**",         // Documentación Swagger
                    "/v3/api-docs/**"         // OpenAPI docs
                ).permitAll()
                
                // Endpoints para administradores
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Endpoints para usuarios normales o administradores
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar gestión de sesiones como STATELESS
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar el proveedor de autenticación
            .authenticationProvider(authenticationProvider())
            
            // Agregar nuestro filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configuración CORS para permitir requests desde el frontend
     * 
     * @return CorsConfigurationSource - Configuración de CORS
     * 
     * Configuraciones:
     * - Orígenes permitidos: localhost en puertos comunes de desarrollo
     * - Métodos permitidos: Todos los métodos HTTP estándar
     * - Headers permitidos: Todos, incluyendo Authorization
     * - Credenciales: Habilitadas para cookies/auth headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos - Configurar según tu frontend
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",    // React development server
            "http://localhost:4200",    // Angular development server
            "http://localhost:8080",    // Vue.js development server
            "http://localhost:5173"     // Vite development server
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Proveedor de autenticación personalizado
     * 
     * @return AuthenticationProvider - Proveedor que usa nuestro UserDetailsService
     * 
     * El DaoAuthenticationProvider:
     * 1. Usa nuestro CustomUserDetailsService para cargar usuarios
     * 2. Usa BCryptPasswordEncoder para verificar contraseñas
     * 3. Maneja el proceso completo de autenticación usuario/contraseña
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Gestor de autenticación - Requerido para el proceso de login
     * 
     * @param config - Configuración de autenticación de Spring
     * @return AuthenticationManager - Gestor de autenticación
     * 
     * Usado en el controlador de autenticación para validar credenciales
     * durante el proceso de login y generar tokens JWT
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Codificador de contraseñas usando BCrypt
     * 
     * @return PasswordEncoder - Implementación de BCrypt
     * 
     * BCrypt características:
     * - Algoritmo de hashing adaptativo
     * - Incorpora salt automáticamente
     * - Resistente a ataques rainbow table
     * - Configurable en cuanto a costo computacional (por defecto: 10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

/*
 * NOTAS EDUCATIVAS ADICIONALES:
 * 
 * 1. ¿Por qué SessionCreationPolicy.STATELESS?
 *    - No crea ni usa sesiones HTTP
 *    - Cada request debe incluir todas las credenciales necesarias
 *    - Permite escalabilidad horizontal fácil
 *    - No hay problemas de session fixation
 * 
 * 2. ¿Por qué deshabilitar CSRF?
 *    - CSRF protege contra ataques cross-site request forgery
 *    - Relevante para aplicaciones con estado de sesión
 *    - En APIs stateless con JWT, el token mismo previene CSRF
 *    - El token debe enviarse explícitamente en cada request
 * 
 * 3. Orden de los filtros:
 *    - JwtAuthenticationFilter se ejecuta ANTES que UsernamePasswordAuthenticationFilter
 *    - Esto permite que JWT establezca autenticación antes de otros filtros
 *    - Spring Security tiene un orden predefinido de filtros
 * 
 * 4. Configuración de endpoints:
 *    - Más específico a menos específico (orden importante)
 *    - /api/admin/** antes que /api/**
 *    - anyRequest() siempre al final
 * 
 * 5. Roles vs Authorities:
 *    - hasRole("ADMIN") busca authority "ROLE_ADMIN"
 *    - hasAuthority("ADMIN") busca authority exacta "ADMIN"  
 *    - Nuestro UserDetailsImpl agrega prefijo "ROLE_" automáticamente
 * 
 * 6. Desarrollo vs Producción:
 *    - H2 console solo para desarrollo
 *    - CORS origins deben configurarse específicamente en producción
 *    - Considera agregar rate limiting en producción
 *    - Logs de seguridad más detallados en desarrollo
 */
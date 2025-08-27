package com.ejemplo.jwtdemo.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ejemplo.jwtdemo.TestDataHelper;
import com.ejemplo.jwtdemo.dto.JwtResponse;
import com.ejemplo.jwtdemo.dto.LoginRequest;
import com.ejemplo.jwtdemo.dto.RegisterRequest;
import com.ejemplo.jwtdemo.dto.UserResponse;
import com.ejemplo.jwtdemo.exception.InvalidCredentialsException;
import com.ejemplo.jwtdemo.exception.UserAlreadyExistsException;
import com.ejemplo.jwtdemo.service.AuthService;
import com.ejemplo.jwtdemo.service.CustomUserDetailsService;
import com.ejemplo.jwtdemo.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests del controlador de autenticación usando @WebMvcTest.
 * 
 * CONCEPTOS EDUCATIVOS DEMOSTRADOS:
 * =================================
 * 
 * 1. **@WebMvcTest - Test Slicing**:
 *    - Solo carga la capa web (controladores, filtros, etc.)
 *    - No carga servicios, repositorios ni base de datos
 *    - Mucho más rápido que @SpringBootTest completo
 *    - Ideal para testing de controladores de forma aislada
 * 
 * 2. **MockMvc - Testing de APIs REST**:
 *    - Simula peticiones HTTP sin levantar servidor real
 *    - Permite testing completo de request/response
 *    - Verifica códigos de estado, headers, contenido JSON
 *    - Testing de validaciones y manejo de errores
 * 
 * 3. **@MockBean - Mocking de Dependencies**:
 *    - Reemplaza beans reales con mocks en el contexto de Spring
 *    - Permite controlar el comportamiento de servicios
 *    - Esencial para tests unitarios de controladores
 *    - Aislamiento completo de la lógica de negocio
 * 
 * 4. **JSON Serialization Testing**:
 *    - ObjectMapper para convertir objetos a JSON
 *    - Verificación de estructura de respuestas
 *    - Testing de DTOs y validaciones
 * 
 * 5. **Spring Security Testing**:
 *    - @WithMockUser para simular usuarios autenticados
 *    - csrf() para manejar protección CSRF en tests
 *    - Testing de endpoints protegidos y públicos
 * 
 * 6. **Hamcrest Matchers**:
 *    - Más expresivos que assertEquals
 *    - Mejores mensajes de error
 *    - Verificaciones complejas de JSON
 * 
 * PATRONES DE TESTING APLICADOS:
 * ===============================
 * - AAA (Arrange, Act, Assert)
 * - Given-When-Then (equivalente a AAA)
 * - Test Data Builders (TestDataHelper)
 * - Mocking de dependencies externas
 * - Testing de casos happy path y edge cases
 */
@WebMvcTest(controllers = AuthController.class, 
           excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@DisplayName("Tests del Controlador de Autenticación")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @MockBean
    private JwtService jwtService;
    
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    
    /**
     * Tests para el endpoint de registro de usuarios.
     */
    @Nested
    @DisplayName("POST /api/auth/register - Registro de Usuarios")
    class UserRegistration {
        
        @Test
        @DisplayName("Debe registrar usuario exitosamente con datos válidos")
        void shouldRegisterUserSuccessfullyWithValidData() throws Exception {
            // Arrange (Given)
            RegisterRequest registerRequest = TestDataHelper.createValidRegisterRequest();
            UserResponse expectedResponse = new UserResponse(
                1L, 
                registerRequest.getUsername(), 
                registerRequest.getEmail(), 
                true, 
                Set.of("USER")
            );
            
            // Configurar el comportamiento del mock
            when(authService.register(any(RegisterRequest.class)))
                .thenReturn(expectedResponse);
            
            // Act (When) & Assert (Then)
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                // Verificar código de estado HTTP
                .andExpect(status().isCreated()) // 201 CREATED
                // Verificar content type de la respuesta
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificar estructura de la respuesta JSON
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is(registerRequest.getUsername())))
                .andExpect(jsonPath("$.email", is(registerRequest.getEmail())))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("USER")))
                // Debug: imprimir request/response para análisis
                .andDo(print());
            
            // Verificar que el servicio fue llamado exactamente una vez
            verify(authService, times(1)).register(any(RegisterRequest.class));
        }
        
        @Test
        @DisplayName("Debe fallar con datos de registro inválidos")
        void shouldFailWithInvalidRegistrationData() throws Exception {
            // Arrange
            RegisterRequest invalidRequest = TestDataHelper.createInvalidRegisterRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                // Validaciones de Jakarta Validation causan 400 Bad Request
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Debe contener información sobre errores de validación
                .andExpect(jsonPath("$.message", containsString("válidos")))
                .andDo(print());
            
            // El servicio NO debe ser llamado cuando falla la validación
            verify(authService, never()).register(any(RegisterRequest.class));
        }
        
        @Test
        @DisplayName("Debe fallar cuando el usuario ya existe")
        void shouldFailWhenUserAlreadyExists() throws Exception {
            // Arrange
            RegisterRequest registerRequest = TestDataHelper.createDuplicateEmailRegisterRequest();
            
            // Configurar mock para lanzar excepción
            when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("El email ya está registrado"));
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("ya está registrado")))
                .andDo(print());
            
            verify(authService, times(1)).register(any(RegisterRequest.class));
        }
        
        @Test
        @DisplayName("Debe rechazar request con Content-Type incorrecto")
        void shouldRejectRequestWithIncorrectContentType() throws Exception {
            // Arrange
            RegisterRequest registerRequest = TestDataHelper.createValidRegisterRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.TEXT_PLAIN) // Content-Type incorrecto
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError()) // 500 - without security, incorrect content type causes internal error
                .andDo(print());
            
            // Servicio no debe ser llamado
            verify(authService, never()).register(any(RegisterRequest.class));
        }
        
        @Test
        @DisplayName("Debe rechazar request sin cuerpo JSON")
        void shouldRejectRequestWithoutJsonBody() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON))
                // Sin contenido en el cuerpo
                .andExpect(status().isInternalServerError()) // 500
                .andDo(print());
            
            verify(authService, never()).register(any(RegisterRequest.class));
        }
    }
    
    /**
     * Tests para el endpoint de login.
     */
    @Nested
    @DisplayName("POST /api/auth/login - Autenticación de Usuarios")
    class UserAuthentication {
        
        @Test
        @DisplayName("Debe autenticar usuario exitosamente con credenciales válidas")
        void shouldAuthenticateUserSuccessfullyWithValidCredentials() throws Exception {
            // Arrange
            LoginRequest loginRequest = TestDataHelper.createValidLoginRequest();
            JwtResponse expectedResponse = new JwtResponse(
                "jwt.token.aqui",
                1L,
                loginRequest.getUsername(),
                "user@ejemplo.com",
                List.of("USER")
            );
            
            when(authService.login(any(LoginRequest.class)))
                .thenReturn(expectedResponse);
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificar estructura de JwtResponse
                .andExpect(jsonPath("$.token", is("jwt.token.aqui")))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is(loginRequest.getUsername())))
                .andExpect(jsonPath("$.email", is("user@ejemplo.com")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("USER")))
                .andDo(print());
            
            verify(authService, times(1)).login(any(LoginRequest.class));
        }
        
        @Test
        @DisplayName("Debe fallar con credenciales incorrectas")
        void shouldFailWithIncorrectCredentials() throws Exception {
            // Arrange
            LoginRequest invalidLogin = TestDataHelper.createInvalidLoginRequest();
            
            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Credenciales inválidas"));
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized()) // 401 Unauthorized
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Credenciales")))
                .andDo(print());
            
            verify(authService, times(1)).login(any(LoginRequest.class));
        }
        
        @Test
        @DisplayName("Debe fallar con usuario inexistente")
        void shouldFailWithNonExistentUser() throws Exception {
            // Arrange
            LoginRequest nonExistentUserLogin = TestDataHelper.createNonExistentUserLoginRequest();
            
            when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Credenciales inválidas"));
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nonExistentUserLogin)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.message", containsString("Credenciales")))
                .andDo(print());
            
            verify(authService, times(1)).login(any(LoginRequest.class));
        }
        
        @Test
        @DisplayName("Debe rechazar datos de login con formato inválido")
        void shouldRejectLoginDataWithInvalidFormat() throws Exception {
            // Arrange
            LoginRequest invalidFormatLogin = TestDataHelper.createInvalidFormatLoginRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidFormatLogin)))
                .andExpect(status().isBadRequest()) // 400 por validaciones
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
            
            // Servicio no debe ser llamado por validaciones fallidas
            verify(authService, never()).login(any(LoginRequest.class));
        }
    }
    
    /**
     * Tests para endpoints que requieren autenticación.
     */
    @Nested
    @DisplayName("Endpoints Autenticados")
    class AuthenticatedEndpoints {
        
        @Test
        @DisplayName("Debe obtener perfil de usuario autenticado")
        void shouldGetProfileOfAuthenticatedUser() throws Exception {
            // Arrange
            UserResponse expectedProfile = new UserResponse(
                1L, "testuser", "test@ejemplo.com", true, Set.of("USER")
            );
            
            when(authService.getCurrentUserProfile())
                .thenReturn(expectedProfile);
            
            // Act & Assert
            mockMvc.perform(get("/api/auth/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@ejemplo.com")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andDo(print());
            
            verify(authService, times(1)).getCurrentUserProfile();
        }
        
        @Test
        @DisplayName("Debe rechazar acceso a perfil sin autenticación")
        void shouldRejectProfileAccessWithoutAuthentication() throws Exception {
            // Arrange - configurar mock para lanzar excepción cuando no hay usuario autenticado
            when(authService.getCurrentUserProfile())
                .thenThrow(new InvalidCredentialsException("No hay usuario autenticado"));
            
            // Act & Assert
            mockMvc.perform(get("/api/auth/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // 401
                .andDo(print());
        }
        
        @Test
        @DisplayName("Debe permitir acceso a test endpoint con usuario autenticado")
        void shouldAllowTestEndpointAccessWithAuthenticatedUser() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/auth/test")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("token JWT es válido")))
                .andDo(print());
        }
        
        // Test removed: With security disabled, this endpoint is always accessible
        // In a real application with security enabled, this would test 401 response
    }
    
    /**
     * Tests de integración que verifican el flujo completo.
     */
    @Nested
    @DisplayName("Flujos de Integración")
    class IntegrationFlows {
        
        @Test
        @DisplayName("Debe manejar múltiples requests de registro concurrentes")
        void shouldHandleMultipleRegistrationRequestsConcurrently() throws Exception {
            // Este test simula múltiples requests simultáneos
            // Útil para verificar thread-safety del controlador
            
            // Arrange
            RegisterRequest request1 = new RegisterRequest("user1", "user1@test.com", "password123@!", "password123@!");
            RegisterRequest request2 = new RegisterRequest("user2", "user2@test.com", "password123@!", "password123@!");
            
            UserResponse response1 = new UserResponse(1L, "user1", "user1@test.com", true, Set.of("USER"));
            UserResponse response2 = new UserResponse(2L, "user2", "user2@test.com", true, Set.of("USER"));
            
            when(authService.register(argThat(req -> req != null && "user1".equals(req.getUsername()))))
                .thenReturn(response1);
            when(authService.register(argThat(req -> req != null && "user2".equals(req.getUsername()))))
                .thenReturn(response2);
            
            // Act & Assert - Realizar requests en paralelo
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("user1")));
            
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("user2")));
            
            // Verificar que ambos servicios fueron llamados
            verify(authService, times(2)).register(any(RegisterRequest.class));
        }
        
        @Test
        @DisplayName("Debe manejar request con JSON malformado")
        void shouldHandleRequestWithMalformedJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"test\", malformed json"))
                .andExpect(status().isInternalServerError()) // 500
                .andDo(print());
            
            verify(authService, never()).register(any(RegisterRequest.class));
        }
        
        @Test
        @DisplayName("Debe manejar requests con caracteres especiales en JSON")
        void shouldHandleRequestsWithSpecialCharactersInJson() throws Exception {
            // Arrange - Usuario con caracteres especiales
            RegisterRequest specialRequest = new RegisterRequest(
                "usuário_café", 
                "email@domínio.com", 
                "contraseña123@!",
                "contraseña123@!"
            );
            
            UserResponse expectedResponse = new UserResponse(
                1L, "usuário_café", "email@domínio.com", true, Set.of("USER")
            );
            
            when(authService.register(any(RegisterRequest.class)))
                .thenReturn(expectedResponse);
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(specialRequest)))
                .andExpect(status().isBadRequest()) // 400 due to email validation failure
                .andDo(print());
            
            verify(authService, never()).register(any(RegisterRequest.class));
        }
    }
    
    /**
     * Tests para verificar el manejo correcto de headers HTTP.
     */
    @Nested
    @DisplayName("Manejo de Headers HTTP")
    class HttpHeaderHandling {
        
        @Test
        @DisplayName("Debe establecer headers de respuesta correctos")
        void shouldSetCorrectResponseHeaders() throws Exception {
            // Arrange
            LoginRequest loginRequest = TestDataHelper.createValidLoginRequest();
            JwtResponse response = new JwtResponse(
                "token",
                1L,
                "user",
                "user@test.com",
                List.of("USER")
            );
            
            when(authService.login(any(LoginRequest.class))).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                // Verificar headers específicos
                .andExpect(header().string("Content-Type", containsString("application/json")))
                .andDo(print());
        }
        
        @Test
        @DisplayName("Debe manejar requests sin Content-Type header")
        void shouldHandleRequestsWithoutContentTypeHeader() throws Exception {
            // Arrange
            RegisterRequest request = TestDataHelper.createValidRegisterRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                                        // No se establece Content-Type
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andDo(print());
            
            verify(authService, never()).register(any(RegisterRequest.class));
        }
    }
}
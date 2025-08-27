package com.ejemplo.jwtdemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.ejemplo.jwtdemo.TestDataHelper;

/**
 * Tests unitarios para JwtService.
 * 
 * CONCEPTOS EDUCATIVOS DEMOSTRADOS:
 * =================================
 * 
 * 1. **Tests Unitarios vs Tests de Integración**:
 *    - Los tests unitarios prueban una clase de forma aislada
 *    - No cargan el contexto completo de Spring (son más rápidos)
 *    - Usan mocks para dependencies externas
 *    - Se enfocan en la lógica de negocio específica
 * 
 * 2. **Estructura de Tests con JUnit 5**:
 *    - @ExtendWith para configurar extensiones (Mockito, Spring, etc.)
 *    - @DisplayName para nombres descriptivos de tests
 *    - @Nested para agrupar tests relacionados
 *    - @BeforeEach para setup común
 * 
 * 3. **Testing de Servicios de Seguridad**:
 *    - Verificación de generación de tokens
 *    - Validación de tokens válidos e inválidos
 *    - Testing de expiración y malformación
 *    - Extracción segura de claims
 * 
 * 4. **AssertJ vs JUnit Assertions**:
 *    - assertThat() es más legible y expresivo
 *    - Mejores mensajes de error
 *    - API fluida y chaineable
 *    - Soporte para assertions complejas
 * 
 * 5. **ReflectionTestUtils**:
 *    - Permite establecer campos privados para testing
 *    - Útil para inyectar valores de configuración
 *    - Evita la necesidad de constructors solo para tests
 * 
 * MEJORES PRÁCTICAS APLICADAS:
 * ============================
 * - Tests independientes y repetibles
 * - Nombres de tests descriptivos
 * - Agrupación lógica con @Nested
 * - Testing de casos edge y errores
 * - Uso de datos de prueba consistentes
 * - Verificación de comportamientos específicos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para JwtService")
class JwtServiceTest {
    
    @InjectMocks
    private JwtService jwtService;
    
    // Configuración de prueba que simula application.properties
    private static final String TEST_SECRET = "myTestSecretKeyForJWT123456789012345678901234567890";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hora en milisegundos
    
    /**
     * Setup que se ejecuta antes de cada test.
     * 
     * CONCEPTO: @BeforeEach vs @BeforeAll
     * - @BeforeEach: Se ejecuta antes de CADA test (estado fresco)
     * - @BeforeAll: Se ejecuta una vez antes de TODOS los tests (estado compartido)
     * 
     * Usamos @BeforeEach porque queremos que cada test tenga un estado limpio
     * del JwtService con la configuración correcta.
     */
    @BeforeEach
    void setUp() {
        // ReflectionTestUtils permite establecer campos privados sin setters
        // Esto es útil para inyectar valores de configuración en tests
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
    }
    
    /**
     * Grupo de tests para la funcionalidad de generación de tokens.
     * 
     * CONCEPTO: @Nested permite agrupar tests relacionados
     * - Mejora la organización del código
     * - Permite setup específico por grupo
     * - Genera reportes más legibles
     */
    @Nested
    @DisplayName("Generación de Tokens JWT")
    class TokenGeneration {
        
        @Test
        @DisplayName("Debe generar token válido con username simple")
        void shouldGenerateValidTokenWithUsername() {
            // Arrange (Preparación)
            String username = TestDataHelper.VALID_USERNAME;
            
            // Act (Acción)
            String token = jwtService.generateToken(username);
            
            // Assert (Verificación)
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(TestDataHelper.isValidJwtFormat(token)).isTrue();
            
            // Verificación adicional: el token debe contener el username
            String extractedUsername = jwtService.getUsernameFromToken(token);
            assertThat(extractedUsername).isEqualTo(username);
        }
        
        @Test
        @DisplayName("Debe generar token válido con UserDetails")
        void shouldGenerateValidTokenWithUserDetails() {
            // Arrange
            UserDetails userDetails = TestDataHelper.createUserDetails();
            
            // Act
            String token = jwtService.generateToken(userDetails);
            
            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(TestDataHelper.isValidJwtFormat(token)).isTrue();
            
            // El username extraído debe coincidir con el de UserDetails
            String extractedUsername = jwtService.getUsernameFromToken(token);
            assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
        }
        
        @Test
        @DisplayName("Debe generar tokens diferentes para usuarios diferentes")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            // Arrange
            String username1 = "usuario1";
            String username2 = "usuario2";
            
            // Act
            String token1 = jwtService.generateToken(username1);
            String token2 = jwtService.generateToken(username2);
            
            // Assert
            assertThat(token1).isNotEqualTo(token2);
            
            // Pero ambos deben ser válidos
            assertThat(jwtService.getUsernameFromToken(token1)).isEqualTo(username1);
            assertThat(jwtService.getUsernameFromToken(token2)).isEqualTo(username2);
        }
        
        @Test
        @DisplayName("Debe incluir fecha de expiración futura en el token")
        void shouldIncludeFutureExpirationDate() {
            // Arrange
            String username = TestDataHelper.VALID_USERNAME;
            Date beforeGeneration = new Date();
            
            // Act
            String token = jwtService.generateToken(username);
            Date afterGeneration = new Date();
            
            // Assert
            Date expirationDate = jwtService.getExpirationDateFromToken(token);
            
            // La fecha de expiración debe estar en el futuro
            assertThat(expirationDate).isAfter(beforeGeneration);
            assertThat(expirationDate).isAfter(afterGeneration);
            
            // Y debe ser aproximadamente ahora + expiration time
            Date expectedExpiration = new Date(afterGeneration.getTime() + TEST_EXPIRATION);
            assertThat(expirationDate).isBefore(expectedExpiration);
        }
    }
    
    /**
     * Grupo de tests para validación de tokens.
     */
    @Nested
    @DisplayName("Validación de Tokens JWT")
    class TokenValidation {
        
        @Test
        @DisplayName("Debe validar token correcto con UserDetails coincidentes")
        void shouldValidateCorrectTokenWithMatchingUserDetails() {
            // Arrange
            UserDetails userDetails = TestDataHelper.createUserDetails();
            String token = jwtService.generateToken(userDetails);
            
            // Act
            Boolean isValid = jwtService.validateToken(token, userDetails);
            
            // Assert
            assertThat(isValid).isTrue();
        }
        
        @Test
        @DisplayName("Debe rechazar token con UserDetails no coincidentes")
        void shouldRejectTokenWithNonMatchingUserDetails() {
            // Arrange
            UserDetails userDetails1 = TestDataHelper.createUserDetails();
            UserDetails userDetails2 = TestDataHelper.createAdminUserDetails();
            String token = jwtService.generateToken(userDetails1);
            
            // Act
            Boolean isValid = jwtService.validateToken(token, userDetails2);
            
            // Assert
            assertThat(isValid).isFalse();
        }
        
        @Test
        @DisplayName("Debe identificar token válido sin UserDetails")
        void shouldIdentifyValidTokenWithoutUserDetails() {
            // Arrange
            String token = jwtService.generateToken(TestDataHelper.VALID_USERNAME);
            
            // Act
            Boolean isValid = jwtService.isTokenValid(token);
            
            // Assert
            assertThat(isValid).isTrue();
        }
        
        @Test
        @DisplayName("Debe rechazar token malformado")
        void shouldRejectMalformedToken() {
            // Arrange
            String malformedToken = TestDataHelper.MALFORMED_TOKEN;
            
            // Act
            Boolean isValid = jwtService.isTokenValid(malformedToken);
            
            // Assert
            assertThat(isValid).isFalse();
        }
        
        @Test
        @DisplayName("Debe rechazar token null")
        void shouldRejectNullToken() {
            // Act & Assert
            assertThat(jwtService.isTokenValid(null)).isFalse();
        }
        
        @Test
        @DisplayName("Debe rechazar token vacío")
        void shouldRejectEmptyToken() {
            // Act & Assert
            assertThat(jwtService.isTokenValid("")).isFalse();
            assertThat(jwtService.isTokenValid("   ")).isFalse();
        }
    }
    
    /**
     * Grupo de tests para extracción de información de tokens.
     */
    @Nested
    @DisplayName("Extracción de Información del Token")
    class TokenInformationExtraction {
        
        @Test
        @DisplayName("Debe extraer username correctamente")
        void shouldExtractUsernameCorrectly() {
            // Arrange
            String expectedUsername = TestDataHelper.VALID_USERNAME;
            String token = jwtService.generateToken(expectedUsername);
            
            // Act
            String extractedUsername = jwtService.getUsernameFromToken(token);
            
            // Assert
            assertThat(extractedUsername).isEqualTo(expectedUsername);
        }
        
        @Test
        @DisplayName("Debe extraer fecha de expiración correctamente")
        void shouldExtractExpirationDateCorrectly() {
            // Arrange
            String username = TestDataHelper.VALID_USERNAME;
            Date beforeGeneration = new Date();
            String token = jwtService.generateToken(username);
            Date afterGeneration = new Date(System.currentTimeMillis() + TEST_EXPIRATION);
            
            // Act
            Date expirationDate = jwtService.getExpirationDateFromToken(token);
            
            // Assert
            assertThat(expirationDate).isNotNull();
            assertThat(expirationDate).isAfter(beforeGeneration);
            assertThat(expirationDate).isBefore(afterGeneration);
        }
        
        @Test
        @DisplayName("Debe lanzar excepción al extraer username de token inválido")
        void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
            // Arrange
            String invalidToken = TestDataHelper.INVALID_TOKEN;
            
            // Act & Assert
            assertThatThrownBy(() -> jwtService.getUsernameFromToken(invalidToken))
                .isInstanceOf(Exception.class);
        }
        
        @Test
        @DisplayName("Debe extraer claims personalizados correctamente")
        void shouldExtractCustomClaimsCorrectly() {
            // Arrange
            UserDetails userDetails = TestDataHelper.createAdminUserDetails();
            String token = jwtService.generateToken(userDetails);
            
            // Act - Extraer el claim de authorities
            Object authorities = jwtService.getClaimFromToken(token, claims -> claims.get("authorities"));
            
            // Assert
            assertThat(authorities).isNotNull();
            // Las authorities deberían estar presentes en el token
        }
    }
    
    /**
     * Grupo de tests para funcionalidad de expiración de tokens.
     */
    @Nested
    @DisplayName("Expiración de Tokens JWT")
    class TokenExpiration {
        
        @Test
        @DisplayName("Token recién creado no debe estar expirado")
        void shouldNotBeExpiredWhenJustCreated() {
            // Arrange
            String username = TestDataHelper.VALID_USERNAME;
            String token = jwtService.generateToken(username);
            
            // Act
            Boolean isExpired = jwtService.isTokenExpired(token);
            
            // Assert
            assertThat(isExpired).isFalse();
        }
        
        @Test
        @DisplayName("Debe detectar token expirado")
        void shouldDetectExpiredToken() {
            // Este test requiere crear un token con tiempo de expiración muy corto
            // Para fines educativos, simulamos un token expirado
            
            // Arrange - Configuramos expiración muy corta (1ms)
            ReflectionTestUtils.setField(jwtService, "expiration", 1L);
            
            String username = TestDataHelper.VALID_USERNAME;
            String token = jwtService.generateToken(username);
            
            // Act - Esperamos un poco para que expire
            try {
                Thread.sleep(10); // 10ms debería ser suficiente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Assert - El token debe estar expirado o lanzar ExpiredJwtException
            try {
                Boolean isExpired = jwtService.isTokenExpired(token);
                assertThat(isExpired).isTrue();
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Si lanza ExpiredJwtException, el token definitivamente está expirado
                // Esto es el comportamiento esperado
                assertThat(e).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
            }
            
            // Restaurar configuración original para otros tests
            ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
        }
        
        @Test
        @DisplayName("Token expirado debe fallar validación con UserDetails")
        void shouldFailValidationWhenTokenIsExpired() {
            // Arrange - Token con expiración muy corta
            ReflectionTestUtils.setField(jwtService, "expiration", 1L);
            
            UserDetails userDetails = TestDataHelper.createUserDetails();
            String token = jwtService.generateToken(userDetails);
            
            // Esperar a que expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Act & Assert - La validación debe fallar o lanzar excepción
            try {
                Boolean isValid = jwtService.validateToken(token, userDetails);
                assertThat(isValid).isFalse();
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Si lanza ExpiredJwtException, la validación definitivamente falló
                // Esto es el comportamiento esperado para tokens expirados
                assertThat(e).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
            }
            
            // Restaurar configuración
            ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
        }
    }
    
    /**
     * Grupo de tests para casos edge y manejo de errores.
     */
    @Nested
    @DisplayName("Casos Edge y Manejo de Errores")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Debe manejar username con caracteres especiales")
        void shouldHandleUsernameWithSpecialCharacters() {
            // Arrange
            String specialUsername = "user@domain.com";
            
            // Act
            String token = jwtService.generateToken(specialUsername);
            String extractedUsername = jwtService.getUsernameFromToken(token);
            
            // Assert
            assertThat(extractedUsername).isEqualTo(specialUsername);
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }
        
        @Test
        @DisplayName("Debe manejar username muy largo")
        void shouldHandleLongUsername() {
            // Arrange
            String longUsername = "a".repeat(100); // Username de 100 caracteres
            
            // Act
            String token = jwtService.generateToken(longUsername);
            String extractedUsername = jwtService.getUsernameFromToken(token);
            
            // Assert
            assertThat(extractedUsername).isEqualTo(longUsername);
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }
        
        @Test
        @DisplayName("Debe rechazar token con firma incorrecta")
        void shouldRejectTokenWithIncorrectSignature() {
            // Arrange
            String username = TestDataHelper.VALID_USERNAME;
            String validToken = jwtService.generateToken(username);
            
            // Modificar la firma del token (corromper el token)
            String[] tokenParts = validToken.split("\\.");
            String corruptedToken = tokenParts[0] + "." + tokenParts[1] + ".firmaIncorrecta";
            
            // Act
            Boolean isValid = jwtService.isTokenValid(corruptedToken);
            
            // Assert
            assertThat(isValid).isFalse();
        }
        
        @Test
        @DisplayName("Debe lanzar excepción con configuración de secret inválida")
        void shouldThrowExceptionWithInvalidSecretConfiguration() {
            // Arrange - Secret muy corto (los JWTs requieren secrets de cierta longitud)
            ReflectionTestUtils.setField(jwtService, "secret", "corto");
            
            String username = TestDataHelper.VALID_USERNAME;
            
            // Act & Assert
            assertThatThrownBy(() -> jwtService.generateToken(username))
                .isInstanceOf(Exception.class);
            
            // Restaurar configuración válida
            ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        }
    }
    
    /**
     * Grupo de tests para verificar la configuración y setup.
     */
    @Nested
    @DisplayName("Configuración y Setup")
    class ConfigurationAndSetup {
        
        @Test
        @DisplayName("JwtService debe estar correctamente inicializado")
        void shouldBeCorrectlyInitialized() {
            // Assert
            assertThat(jwtService).isNotNull();
            
            // Verificar que la configuración se estableció correctamente
            Object secret = ReflectionTestUtils.getField(jwtService, "secret");
            Object expiration = ReflectionTestUtils.getField(jwtService, "expiration");
            
            assertThat(secret).isEqualTo(TEST_SECRET);
            assertThat(expiration).isEqualTo(TEST_EXPIRATION);
        }
        
        @Test
        @DisplayName("Debe usar algoritmo HMAC SHA-256 para firma")
        void shouldUseHMACSHA256ForSigning() {
            // Este test verifica indirectamente el algoritmo verificando
            // que el token generado puede ser validado correctamente
            
            // Arrange
            String username = TestDataHelper.VALID_USERNAME;
            
            // Act
            String token = jwtService.generateToken(username);
            
            // Assert - Si podemos validar el token, el algoritmo es correcto
            assertThat(jwtService.isTokenValid(token)).isTrue();
            
            // Verificación adicional: el header del JWT debe indicar HS256
            String[] parts = token.split("\\.");
            String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
            assertThat(header).contains("HS256");
        }
        
        @Test
        @DisplayName("Debe generar tokens con estructura JWT estándar")
        void shouldGenerateTokensWithStandardJWTStructure() {
            // Arrange
            String username = TestDataHelper.VALID_USERNAME;
            
            // Act
            String token = jwtService.generateToken(username);
            
            // Assert - JWT debe tener exactamente 3 partes separadas por puntos
            String[] parts = token.split("\\.");
            assertThat(parts).hasSize(3);
            
            // Cada parte debe ser base64 válida y no vacía
            assertThat(parts[0]).isNotEmpty(); // Header
            assertThat(parts[1]).isNotEmpty(); // Payload
            assertThat(parts[2]).isNotEmpty(); // Signature
            
            // Debe poder decodificar el payload
            assertThatNoException().isThrownBy(() -> {
                byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
                String payload = new String(payloadBytes);
                // El payload debe contener el username
                assertThat(payload).contains(username);
            });
        }
    }
}
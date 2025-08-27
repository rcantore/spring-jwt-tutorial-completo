package com.ejemplo.jwtdemo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.ejemplo.jwtdemo.TestDataHelper;
import com.ejemplo.jwtdemo.entity.User;
import com.ejemplo.jwtdemo.repository.UserRepository;
import com.ejemplo.jwtdemo.service.CustomUserDetailsService;
import com.ejemplo.jwtdemo.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests del controlador de usuarios con enfoque en autorización por roles.
 * 
 * CONCEPTOS EDUCATIVOS DEMOSTRADOS:
 * =================================
 * 
 * 1. **Testing de Autorización por Roles**:
 *    - @WithMockUser con diferentes roles (USER, ADMIN)
 *    - Verificación de acceso denegado (403 Forbidden)
 *    - Testing de @PreAuthorize annotations
 *    - Diferenciación entre autenticación y autorización
 * 
 * 2. **Testing de Operaciones CRUD**:
 *    - Create: No implementado en este controlador (está en AuthController)
 *    - Read: GET endpoints con paginación y búsqueda
 *    - Update: PUT endpoint para cambiar estado de usuario
 *    - Delete: DELETE endpoint para eliminación
 * 
 * 3. **Testing de Paginación con Spring Data**:
 *    - Mocking de Page<T> objects
 *    - Verificación de parámetros de paginación
 *    - Testing de ordenamiento y filtrado
 *    - Assertions sobre metadatos de página (total, size, etc.)
 * 
 * 4. **Testing de Path Variables y Query Parameters**:
 *    - @PathVariable validation testing
 *    - @RequestParam con valores por defecto
 *    - Testing de parámetros inválidos
 *    - Validación con @Min, @Max annotations
 * 
 * 5. **Testing de Repository Layer Mocking**:
 *    - Mocking directo de Repository (para simplicidad didáctica)
 *    - En apps reales, mejor mockear el Service layer
 *    - Demostración de Optional handling
 * 
 * 6. **HTTP Status Codes Específicos**:
 *    - 200 OK: Operaciones exitosas
 *    - 403 Forbidden: Sin permisos suficientes
 *    - 404 Not Found: Recurso no existe
 *    - 204 No Content: Eliminación exitosa
 * 
 * MEJORES PRÁCTICAS DEMOSTRADAS:
 * ===============================
 * - Tests independientes para cada nivel de autorización
 * - Verificación de casos happy path y casos de error
 * - Testing de límites en paginación
 * - Uso de datos realistas en tests
 * - Separación clara de concerns en tests
 */
@WebMvcTest(controllers = UserController.class)
@EnableMethodSecurity(prePostEnabled = true)
@DisplayName("Tests del Controlador de Usuarios")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private JwtService jwtService;
    
    @MockBean
    private CustomUserDetailsService userDetailsService;
    
    /**
     * Tests para el listado de usuarios con paginación.
     */
    @Nested
    @DisplayName("GET /api/users - Listado de Usuarios")
    class UserListing {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder listar usuarios con paginación por defecto")
        void shouldAllowAdminToListUsersWithDefaultPagination() throws Exception {
            // Arrange
            List<User> users = TestDataHelper.createUserList(5);
            Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 5);
            
            when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(userPage);
            
            // Act & Assert
            mockMvc.perform(get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificar estructura de página
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)))
                // Verificar estructura de usuarios en el contenido
                .andExpect(jsonPath("$.content[0].username", is("usuario0")))
                .andExpect(jsonPath("$.content[0].email", is("usuario0@ejemplo.com")))
                .andExpect(jsonPath("$.content[0].enabled", is(true)))
                .andDo(print());
            
            // Verificar que se llamó al repository con paginación correcta
            verify(userRepository, times(1)).findAll(any(Pageable.class));
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder usar parámetros de paginación personalizados")
        void shouldAllowAdminToUseCustomPaginationParameters() throws Exception {
            // Arrange
            List<User> users = TestDataHelper.createUserList(3);
            Page<User> userPage = new PageImpl<>(users, PageRequest.of(1, 5), 10);
            
            when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(userPage);
            
            // Act & Assert
            mockMvc.perform(get("/api/users")
                    .param("page", "1")
                    .param("size", "5")
                    .param("sortBy", "username")
                    .param("direction", "desc")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.number", is(1))) // Página actual
                .andExpect(jsonPath("$.size", is(5))) // Tamaño de página
                .andDo(print());
            
            // Verificar que se usaron los parámetros correctos
            verify(userRepository, times(1)).findAll(argThat((Pageable pageable) -> 
                pageable.getPageNumber() == 1 && 
                pageable.getPageSize() == 5 &&
                pageable.getSort().isSorted()
            ));
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Usuario regular NO debe poder listar usuarios")
        void shouldNotAllowRegularUserToListUsers() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()) // 403 Forbidden
                .andDo(print());
            
            // Repository no debe ser llamado
            verify(userRepository, never()).findAll(any(Pageable.class));
        }
        
        @Test
        @DisplayName("Usuario sin autenticar NO debe poder listar usuarios")
        void shouldNotAllowUnauthenticatedUserToListUsers() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // 401 Unauthorized
                .andDo(print());
            
            verify(userRepository, never()).findAll(any(Pageable.class));
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe rechazar parámetros de paginación inválidos")
        void shouldRejectInvalidPaginationParameters() throws Exception {
            // Act & Assert - página negativa
            mockMvc.perform(get("/api/users")
                    .param("page", "-1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // 400 Bad Request
                .andDo(print());
            
            // Act & Assert - tamaño de página 0
            mockMvc.perform(get("/api/users")
                    .param("size", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
            
            verify(userRepository, never()).findAll(any(Pageable.class));
        }
    }
    
    /**
     * Tests para obtener usuario específico por ID.
     */
    @Nested
    @DisplayName("GET /api/users/{id} - Obtener Usuario por ID")
    class GetUserById {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder obtener usuario existente por ID")
        void shouldAllowAdminToGetExistingUserById() throws Exception {
            // Arrange
            User user = TestDataHelper.createValidUser();
            user.setId(1L);
            
            when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
            
            // Act & Assert
            mockMvc.perform(get("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is(TestDataHelper.VALID_USERNAME)))
                .andExpect(jsonPath("$.email", is(TestDataHelper.VALID_EMAIL)))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andDo(print());
            
            verify(userRepository, times(1)).findById(1L);
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe devolver 404 cuando el usuario no existe")
        void shouldReturn404WhenUserDoesNotExist() throws Exception {
            // Arrange
            when(userRepository.findById(999L))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            mockMvc.perform(get("/api/users/999")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andDo(print());
            
            verify(userRepository, times(1)).findById(999L);
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Usuario regular NO debe poder obtener otros usuarios")
        void shouldNotAllowRegularUserToGetOtherUsers() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()) // 403 Forbidden
                .andDo(print());
            
            verify(userRepository, never()).findById(any());
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe validar que el ID sea un número positivo")
        void shouldValidateIdIsPositiveNumber() throws Exception {
            // Act & Assert - ID negativo
            mockMvc.perform(get("/api/users/-1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
            
            // ID 0 también debería fallar
            mockMvc.perform(get("/api/users/0")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
            
            verify(userRepository, never()).findById(any());
        }
    }
    
    /**
     * Tests para búsqueda de usuarios por username.
     */
    @Nested
    @DisplayName("GET /api/users/search - Búsqueda de Usuarios")
    class UserSearch {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder buscar usuarios por username")
        void shouldAllowAdminToSearchUsersByUsername() throws Exception {
            // Arrange
            List<User> matchingUsers = List.of(
                TestDataHelper.createUserWithData("john_doe", "john@test.com", true),
                TestDataHelper.createUserWithData("johnny", "johnny@test.com", true)
            );
            
            when(userRepository.findByUsernameContainingIgnoreCase("john"))
                .thenReturn(matchingUsers);
            
            // Act & Assert
            mockMvc.perform(get("/api/users/search")
                    .param("username", "john")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("john_doe")))
                .andExpect(jsonPath("$[1].username", is("johnny")))
                .andDo(print());
            
            verify(userRepository, times(1)).findByUsernameContainingIgnoreCase("john");
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Búsqueda debe devolver lista vacía cuando no hay coincidencias")
        void shouldReturnEmptyListWhenNoMatches() throws Exception {
            // Arrange
            when(userRepository.findByUsernameContainingIgnoreCase("nonexistent"))
                .thenReturn(List.of());
            
            // Act & Assert
            mockMvc.perform(get("/api/users/search")
                    .param("username", "nonexistent")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andDo(print());
            
            verify(userRepository, times(1)).findByUsernameContainingIgnoreCase("nonexistent");
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Usuario regular NO debe poder buscar usuarios")
        void shouldNotAllowRegularUserToSearchUsers() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/search")
                    .param("username", "test")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
            
            verify(userRepository, never()).findByUsernameContainingIgnoreCase(any());
        }
    }
    
    /**
     * Tests para alternar el estado de un usuario.
     */
    @Nested
    @DisplayName("PUT /api/users/{id}/toggle-status - Alternar Estado de Usuario")
    class ToggleUserStatus {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder activar usuario desactivado")
        void shouldAllowAdminToActivateDisabledUser() throws Exception {
            // Arrange
            User disabledUser = TestDataHelper.createDisabledUser();
            disabledUser.setId(1L);
            
            User enabledUser = TestDataHelper.createDisabledUser();
            enabledUser.setId(1L);
            enabledUser.setEnabled(true); // Estado cambiado
            
            when(userRepository.findById(1L))
                .thenReturn(Optional.of(disabledUser));
            when(userRepository.save(any(User.class)))
                .thenReturn(enabledUser);
            
            // Act & Assert
            mockMvc.perform(put("/api/users/1/toggle-status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.enabled", is(true))) // Ahora activado
                .andDo(print());
            
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).save(argThat(user -> user.isEnabled()));
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder desactivar usuario activo")
        void shouldAllowAdminToDeactivateActiveUser() throws Exception {
            // Arrange
            User activeUser = TestDataHelper.createValidUser();
            activeUser.setId(1L);
            
            User deactivatedUser = TestDataHelper.createValidUser();
            deactivatedUser.setId(1L);
            deactivatedUser.setEnabled(false); // Estado cambiado
            
            when(userRepository.findById(1L))
                .thenReturn(Optional.of(activeUser));
            when(userRepository.save(any(User.class)))
                .thenReturn(deactivatedUser);
            
            // Act & Assert
            mockMvc.perform(put("/api/users/1/toggle-status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false))) // Ahora desactivado
                .andDo(print());
            
            verify(userRepository, times(1)).save(argThat(user -> !user.isEnabled()));
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe devolver 404 al intentar cambiar estado de usuario inexistente")
        void shouldReturn404WhenTogglingNonExistentUser() throws Exception {
            // Arrange
            when(userRepository.findById(999L))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            mockMvc.perform(put("/api/users/999/toggle-status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
            
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Usuario regular NO debe poder cambiar estado de usuarios")
        void shouldNotAllowRegularUserToToggleUserStatus() throws Exception {
            // Act & Assert
            mockMvc.perform(put("/api/users/1/toggle-status")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
            
            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).save(any());
        }
    }
    
    /**
     * Tests para eliminación de usuarios.
     */
    @Nested
    @DisplayName("DELETE /api/users/{id} - Eliminación de Usuarios")
    class UserDeletion {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder eliminar usuario existente")
        void shouldAllowAdminToDeleteExistingUser() throws Exception {
            // Arrange
            User userToDelete = TestDataHelper.createValidUser();
            userToDelete.setId(1L);
            
            when(userRepository.findById(1L))
                .thenReturn(Optional.of(userToDelete));
            
            // Act & Assert
            mockMvc.perform(delete("/api/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // 204 No Content
                .andDo(print());
            
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).delete(userToDelete);
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe devolver 404 al intentar eliminar usuario inexistente")
        void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
            // Arrange
            when(userRepository.findById(999L))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            mockMvc.perform(delete("/api/users/999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
            
            verify(userRepository, never()).delete(any(User.class));
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Usuario regular NO debe poder eliminar usuarios")
        void shouldNotAllowRegularUserToDeleteUsers() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
            
            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).delete(any(User.class));
        }
    }
    
    /**
     * Tests para estadísticas de usuarios.
     */
    @Nested
    @DisplayName("GET /api/users/stats - Estadísticas de Usuarios")
    class UserStatistics {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Admin debe poder obtener estadísticas de usuarios")
        void shouldAllowAdminToGetUserStatistics() throws Exception {
            // Arrange
            when(userRepository.count()).thenReturn(100L);
            when(userRepository.countByEnabledTrue()).thenReturn(85L);
            
            // Act & Assert
            mockMvc.perform(get("/api/users/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalUsers", is(100)))
                .andExpect(jsonPath("$.activeUsers", is(85)))
                .andExpect(jsonPath("$.inactiveUsers", is(15))) // 100 - 85
                .andExpect(jsonPath("$.activationRate", is(85.0))) // 85/100 * 100
                .andDo(print());
            
            verify(userRepository, times(1)).count();
            verify(userRepository, times(1)).countByEnabledTrue();
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe manejar caso cuando no hay usuarios en el sistema")
        void shouldHandleWhenNoUsersInSystem() throws Exception {
            // Arrange
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.countByEnabledTrue()).thenReturn(0L);
            
            // Act & Assert
            mockMvc.perform(get("/api/users/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(0)))
                .andExpect(jsonPath("$.activeUsers", is(0)))
                .andExpect(jsonPath("$.inactiveUsers", is(0)))
                .andExpect(jsonPath("$.activationRate", is(0.0))) // Sin división por cero
                .andDo(print());
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("Usuario regular NO debe poder obtener estadísticas")
        void shouldNotAllowRegularUserToGetStatistics() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
            
            verify(userRepository, never()).count();
            verify(userRepository, never()).countByEnabledTrue();
        }
    }
    
    /**
     * Tests de casos edge y manejo de errores.
     */
    @Nested
    @DisplayName("Casos Edge y Manejo de Errores")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe manejar repositorio que lanza excepciones")
        void shouldHandleRepositoryExceptions() throws Exception {
            // Arrange
            when(userRepository.findAll(any(Pageable.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));
            
            // Act & Assert
            mockMvc.perform(get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // 500
                .andDo(print());
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe manejar IDs extremadamente grandes")
        void shouldHandleExtremelyLargeIds() throws Exception {
            // Arrange
            Long largeId = Long.MAX_VALUE;
            when(userRepository.findById(largeId))
                .thenReturn(Optional.empty());
            
            // Act & Assert
            mockMvc.perform(get("/api/users/" + largeId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
            
            verify(userRepository, times(1)).findById(largeId);
        }
        
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Debe manejar parámetros de búsqueda con caracteres especiales")
        void shouldHandleSearchParametersWithSpecialCharacters() throws Exception {
            // Arrange
            String specialSearch = "user@domain.com";
            when(userRepository.findByUsernameContainingIgnoreCase(specialSearch))
                .thenReturn(List.of());
            
            // Act & Assert
            mockMvc.perform(get("/api/users/search")
                    .param("username", specialSearch)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andDo(print());
            
            verify(userRepository, times(1)).findByUsernameContainingIgnoreCase(specialSearch);
        }
    }
}
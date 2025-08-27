package com.ejemplo.jwtdemo.service;

import com.ejemplo.jwtdemo.entity.User;
import com.ejemplo.jwtdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio personalizado para cargar detalles del usuario durante la autenticación
 * 
 * Esta clase implementa la interfaz UserDetailsService de Spring Security,
 * que es el contrato principal para obtener información del usuario durante
 * el proceso de autenticación.
 * 
 * Conceptos importantes:
 * - UserDetailsService: Interfaz central para cargar datos específicos del usuario
 * - Lazy Loading: Los roles se cargan de manera perezosa con @Transactional
 * - Exception Handling: Manejo específico de usuarios no encontrados
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Constructor con inyección de dependencias
     * 
     * @param userRepository - Repositorio para acceso a datos de usuarios
     * 
     * Nota: Usamos inyección por constructor (mejor práctica) en lugar de @Autowired en campo
     */
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Carga un usuario por su username - Método principal de UserDetailsService
     * 
     * @param username - El nombre de usuario para buscar
     * @return UserDetails - Los detalles del usuario encapsulados en UserDetailsImpl
     * @throws UsernameNotFoundException - Si el usuario no existe
     * 
     * Flujo del método:
     * 1. Busca el usuario en la base de datos por username
     * 2. Si existe, lo convierte a UserDetailsImpl
     * 3. Si no existe, lanza excepción específica
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar el usuario en la base de datos
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con username: " + username
                ));
        
        // Convertir nuestra entidad User a UserDetails usando nuestro adaptador
        return UserDetailsImpl.create(user);
    }
    
    /**
     * Método de utilidad para cargar usuario por ID
     * 
     * @param id - El ID del usuario
     * @return UserDetails - Los detalles del usuario
     * @throws UsernameNotFoundException - Si el usuario no existe
     * 
     * Útil para casos donde tenemos el ID del usuario (ej: desde un token JWT)
     * y necesitamos cargar sus detalles completos
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + id
                ));
        
        return UserDetailsImpl.create(user);
    }
    
    /**
     * Método de utilidad para verificar si un usuario existe
     * 
     * @param username - El nombre de usuario a verificar
     * @return boolean - true si existe, false si no
     * 
     * Útil para validaciones durante el registro de nuevos usuarios
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Método de utilidad para verificar si un email ya está en uso
     * 
     * @param email - El email a verificar
     * @return boolean - true si existe, false si no
     * 
     * Útil para validaciones de email único durante el registro
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Obtiene el usuario completo por username
     * 
     * @param username - El nombre de usuario
     * @return User - La entidad User completa
     * @throws UsernameNotFoundException - Si el usuario no existe
     * 
     * Diferencia con loadUserByUsername:
     * - Este método retorna User (nuestra entidad)
     * - loadUserByUsername retorna UserDetails (interfaz de Spring Security)
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con username: " + username
                ));
    }
}

/*
 * NOTAS EDUCATIVAS ADICIONALES:
 * 
 * 1. ¿Por qué @Transactional?
 *    - Asegura que la sesión de Hibernate permanezca activa
 *    - Permite la carga lazy de relaciones (como roles)
 *    - readOnly = true optimiza la transacción para solo lectura
 * 
 * 2. ¿Por qué Optional en el repositorio pero excepción aquí?
 *    - El repositorio usa Optional para indicar "puede no existir"
 *    - Spring Security espera una excepción específica si el usuario no existe
 *    - orElseThrow() convierte elegantemente Optional a excepción
 * 
 * 3. Patrón de diseño utilizado:
 *    - Service Layer: Encapsula la lógica de negocio
 *    - Adapter: UserDetailsImpl adapta User a UserDetails
 *    - Repository: Abstrae el acceso a datos
 * 
 * 4. Escalabilidad:
 *    - Los métodos adicionales facilitan extensiones futuras
 *    - La separación de responsabilidades permite fácil testing
 *    - La inyección por constructor facilita el mocking en tests
 */
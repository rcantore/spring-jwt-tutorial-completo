package com.ejemplo.jwtdemo.repository;

import com.ejemplo.jwtdemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para el acceso a datos de la entidad User
 * 
 * Conceptos importantes:
 * - JpaRepository: Proporciona métodos CRUD automáticos
 * - Optional: Manejo seguro de valores que pueden ser null
 * - Query Methods: Spring Data genera automáticamente las consultas basándose en el nombre del método
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario
     * 
     * @param username - El nombre de usuario a buscar
     * @return Optional<User> - El usuario envuelto en Optional para manejo seguro de null
     * 
     * Explicación: Spring Data JPA genera automáticamente la consulta:
     * SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca un usuario por su email
     * 
     * @param email - El email a buscar
     * @return Optional<User> - El usuario envuelto en Optional
     * 
     * Nota: Útil para procesos de recuperación de contraseña o validación de email único
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el username especificado
     * 
     * @param username - El nombre de usuario a verificar
     * @return boolean - true si existe, false si no existe
     * 
     * Ventaja: Más eficiente que findByUsername cuando solo necesitamos verificar existencia
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email especificado
     * 
     * @param email - El email a verificar
     * @return boolean - true si existe, false si no existe
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuarios cuyo nombre de usuario contenga la cadena especificada (case-insensitive).
     * 
     * @param username parte del nombre de usuario a buscar
     * @return lista de usuarios que coinciden con el patrón
     * 
     * Consulta generada: SELECT * FROM users WHERE UPPER(username) LIKE UPPER('%?%')
     * Útil para funcionalidades de búsqueda y autocompletado
     */
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    /**
     * Cuenta cuántos usuarios están activos (enabled = true).
     * 
     * @return número de usuarios activos
     * 
     * Consulta generada: SELECT COUNT(*) FROM users WHERE enabled = true
     * Útil para estadísticas y dashboards administrativos
     */
    long countByEnabledTrue();
}
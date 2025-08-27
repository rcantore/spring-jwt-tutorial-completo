package com.ejemplo.jwtdemo.repository;

import com.ejemplo.jwtdemo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de base de datos de la entidad Role.
 * 
 * Conceptos de Spring Data JPA demostrados:
 * - Extensión de JpaRepository para operaciones CRUD automáticas
 * - Query methods por convención de nombres
 * - Uso de Optional para manejo seguro de valores que pueden ser null
 * 
 * JpaRepository<Role, Long> proporciona automáticamente:
 * - save(Role) - crear/actualizar
 * - findById(Long) - buscar por ID
 * - findAll() - obtener todos
 * - delete(Role) - eliminar
 * - existsById(Long) - verificar existencia
 * - count() - contar registros
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca un rol por su nombre.
     * 
     * Spring Data JPA genera automáticamente la implementación basándose en:
     * - findBy: indica que es una consulta de búsqueda
     * - Name: el campo por el cual buscar (debe coincidir con el atributo de la entidad)
     * 
     * Query SQL generada: SELECT * FROM roles WHERE name = ?
     * 
     * @param name nombre del rol a buscar
     * @return Optional<Role> - puede estar vacío si no se encuentra el rol
     */
    Optional<Role> findByName(String name);
    
    /**
     * Verifica si existe un rol con el nombre especificado.
     * 
     * Útil para validaciones antes de crear nuevos roles:
     * - Evitar duplicados
     * - Validaciones de negocio
     * - Chequeos de integridad
     * 
     * Query SQL generada: SELECT COUNT(*) FROM roles WHERE name = ?
     * 
     * @param name nombre del rol a verificar
     * @return true si existe, false si no existe
     */
    boolean existsByName(String name);
    
    /**
     * Elimina un rol por su nombre.
     * 
     * Ejemplo de método de eliminación personalizado.
     * Útil cuando tenemos el nombre pero no el ID completo.
     * 
     * Query SQL generada: DELETE FROM roles WHERE name = ?
     * 
     * Nota: En aplicaciones reales, considera usar eliminación lógica
     * (marcar como inactivo) en lugar de eliminación física.
     * 
     * @param name nombre del rol a eliminar
     */
    void deleteByName(String name);
}
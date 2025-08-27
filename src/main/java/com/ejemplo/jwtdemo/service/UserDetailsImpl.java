package com.ejemplo.jwtdemo.service;

import com.ejemplo.jwtdemo.entity.Role;
import com.ejemplo.jwtdemo.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación personalizada de UserDetails de Spring Security
 * 
 * Esta clase actúa como un adaptador entre nuestra entidad User
 * y la interfaz UserDetails que Spring Security necesita.
 * 
 * Conceptos clave:
 * - UserDetails: Interfaz central de Spring Security que contiene información del usuario
 * - GrantedAuthority: Representa un permiso otorgado a un usuario
 * - Adapter Pattern: Permite que clases incompatibles trabajen juntas
 */
public class UserDetailsImpl implements UserDetails {
    
    private final User user;
    
    /**
     * Constructor que recibe nuestra entidad User
     * 
     * @param user - La entidad User de nuestro dominio
     */
    public UserDetailsImpl(User user) {
        this.user = user;
    }
    
    /**
     * Convierte los roles del usuario en authorities de Spring Security
     * 
     * @return Collection<GrantedAuthority> - Lista de permisos del usuario
     * 
     * Explicación del proceso:
     * 1. Obtiene el Set de roles del usuario
     * 2. Convierte cada Role en un SimpleGrantedAuthority
     * 3. Prefija cada rol con "ROLE_" (convención de Spring Security)
     * 4. Retorna la colección de authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Retorna la contraseña del usuario
     * 
     * @return String - La contraseña (normalmente hasheada)
     * 
     * Importante: Spring Security usará esto para verificar credenciales
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    /**
     * Retorna el nombre de usuario único
     * 
     * @return String - El username del usuario
     * 
     * Nota: Este es el identificador principal para autenticación
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    /**
     * Indica si la cuenta no ha expirado
     * 
     * @return boolean - true si la cuenta no ha expirado
     * 
     * Implementación actual: Siempre retorna true
     * En un sistema real, podrías implementar lógica de expiración de cuentas
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * Indica si la cuenta no está bloqueada
     * 
     * @return boolean - true si la cuenta no está bloqueada
     * 
     * Implementación actual: Siempre retorna true
     * En un sistema real, podrías implementar lógica de bloqueo por intentos fallidos
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * Indica si las credenciales no han expirado
     * 
     * @return boolean - true si las credenciales no han expirado
     * 
     * Implementación actual: Siempre retorna true
     * En un sistema real, podrías forzar cambio de contraseña periódico
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * Indica si el usuario está habilitado
     * 
     * @return boolean - true si el usuario está habilitado
     * 
     * Uso del campo 'enabled' de nuestra entidad User
     * Esto permite desactivar usuarios sin eliminarlos de la base de datos
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
    
    /**
     * Método de utilidad para obtener la entidad User original
     * 
     * @return User - La entidad User completa
     * 
     * Útil cuando necesitamos acceso a campos adicionales de User
     * que no están en la interfaz UserDetails
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Obtiene el ID del usuario
     * 
     * @return Long - El ID del usuario
     */
    public Long getId() {
        return user.getId();
    }
    
    /**
     * Obtiene el email del usuario
     * 
     * @return String - El email del usuario
     */
    public String getEmail() {
        return user.getEmail();
    }
    
    /**
     * Método estático factory para crear UserDetailsImpl
     * 
     * @param user - La entidad User
     * @return UserDetailsImpl - Nueva instancia
     * 
     * Patrón Factory Method: Proporciona una forma elegante de crear instancias
     */
    public static UserDetailsImpl create(User user) {
        return new UserDetailsImpl(user);
    }
}
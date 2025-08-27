package com.ejemplo.jwtdemo.service;

import com.ejemplo.jwtdemo.dto.JwtResponse;
import com.ejemplo.jwtdemo.dto.LoginRequest;
import com.ejemplo.jwtdemo.dto.RegisterRequest;
import com.ejemplo.jwtdemo.dto.UserResponse;
import com.ejemplo.jwtdemo.entity.Role;
import com.ejemplo.jwtdemo.entity.User;
import com.ejemplo.jwtdemo.exception.InvalidCredentialsException;
import com.ejemplo.jwtdemo.exception.UserAlreadyExistsException;
import com.ejemplo.jwtdemo.repository.RoleRepository;
import com.ejemplo.jwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación que maneja el registro y login de usuarios.
 * 
 * Responsabilidades de este servicio:
 * - Registro de nuevos usuarios con validaciones de negocio
 * - Autenticación de usuarios existentes
 * - Generación de tokens JWT
 * - Conversión entre entidades y DTOs
 * - Manejo de roles por defecto
 * 
 * Patrones aplicados:
 * - Service Layer: Encapsula lógica de negocio
 * - DTO Pattern: Separa representación interna de externa
 * - Dependency Injection: Usando constructor y @RequiredArgsConstructor
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * Proceso del registro:
     * 1. Validar que el usuario no existe (username y email únicos)
     * 2. Validar que las contraseñas coinciden
     * 3. Hashear la contraseña
     * 4. Asignar rol por defecto
     * 5. Guardar en base de datos
     * 6. Convertir a DTO de respuesta
     * 
     * @param registerRequest datos del nuevo usuario
     * @return información del usuario registrado
     * @throws UserAlreadyExistsException si el usuario ya existe
     * @throws IllegalArgumentException si las contraseñas no coinciden
     */
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        log.info("Iniciando registro de usuario: {}", registerRequest.getUsername());
        
        // Validación 1: Verificar que el username no existe
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException(
                "Ya existe un usuario con el nombre: " + registerRequest.getUsername()
            );
        }
        
        // Validación 2: Verificar que el email no existe
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException(
                "Ya existe un usuario con el email: " + registerRequest.getEmail()
            );
        }
        
        // Validación 3: Verificar que las contraseñas coinciden
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        // Crear nuevo usuario
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        
        // Hashear contraseña - NUNCA almacenar contraseñas en texto plano
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEnabled(true);
        
        // Asignar rol por defecto
        Set<Role> defaultRoles = getDefaultRoles();
        newUser.setRoles(defaultRoles);
        
        // Guardar en base de datos
        User savedUser = userRepository.save(newUser);
        
        log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());
        
        // Convertir a DTO de respuesta
        return convertToUserResponse(savedUser);
    }
    
    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * Proceso de login:
     * 1. Autenticar con Spring Security
     * 2. Obtener información del usuario autenticado
     * 3. Generar token JWT
     * 4. Preparar respuesta con token y datos del usuario
     * 
     * @param loginRequest credenciales del usuario
     * @return respuesta con token JWT y datos del usuario
     * @throws InvalidCredentialsException si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.getUsername());
        
        try {
            // Autenticar usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Establecer contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Obtener detalles del usuario autenticado
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Generar token JWT
            String jwt = jwtService.generateToken(userDetails.getUsername());
            
            // Obtener roles como strings
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());
            
            log.info("Login exitoso para usuario: {}", userDetails.getUsername());
            
            // Crear respuesta con token y datos del usuario
            return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
            );
            
        } catch (Exception e) {
            log.warn("Login fallido para usuario: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }
    
    /**
     * Obtiene el perfil del usuario autenticado actualmente.
     * 
     * @return información del usuario actual
     * @throws InvalidCredentialsException si no hay usuario autenticado
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("No hay usuario autenticado");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
        
        return convertToUserResponse(user);
    }
    
    /**
     * Obtiene los roles por defecto para usuarios nuevos.
     * 
     * Crea el rol "USER" si no existe en la base de datos.
     * En aplicaciones más complejas, esto podría ser configurable
     * o basarse en el contexto del registro.
     * 
     * @return conjunto de roles por defecto
     */
    private Set<Role> getDefaultRoles() {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    log.info("Creando rol por defecto: ROLE_USER");
                    Role newRole = new Role("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        return roles;
    }
    
    /**
     * Convierte una entidad User a UserResponse DTO.
     * 
     * Esta conversión:
     * - Omite información sensible (contraseña)
     * - Simplifica roles a nombres (strings)
     * - Estructura los datos para respuesta de API
     * 
     * @param user entidad de usuario
     * @return DTO de respuesta de usuario
     */
    private UserResponse convertToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.isEnabled(),
            roleNames
        );
    }
}
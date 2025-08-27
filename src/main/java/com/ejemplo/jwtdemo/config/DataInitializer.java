package com.ejemplo.jwtdemo.config;

import com.ejemplo.jwtdemo.entity.Role;
import com.ejemplo.jwtdemo.entity.User;
import com.ejemplo.jwtdemo.repository.RoleRepository;
import com.ejemplo.jwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Inicializador de datos para la aplicación JWT Demo
 * 
 * Esta clase implementa CommandLineRunner, lo que significa que su método run()
 * se ejecutará automáticamente después de que Spring Boot termine de cargar
 * todos los componentes pero antes de que la aplicación esté completamente lista.
 * 
 * CONCEPTOS EDUCATIVOS CLAVE:
 * 
 * 1. COMMANDLINERUNNER:
 *    - Interface funcional que permite ejecutar código al arranque
 *    - Ideal para tareas de inicialización: poblar BD, verificar configuraciones, etc.
 *    - Se ejecuta después de que el ApplicationContext esté completamente cargado
 *    - Útil para datos de prueba, configuraciones iniciales, migraciones, etc.
 * 
 * 2. INYECCIÓN DE DEPENDENCIAS:
 *    - @RequiredArgsConstructor (Lombok) genera constructor con dependencias final
 *    - Spring automáticamente inyecta UserRepository, RoleRepository y PasswordEncoder
 *    - No necesitamos @Autowired gracias a la inyección por constructor
 * 
 * 3. ENCRIPTACIÓN DE CONTRASEÑAS:
 *    - NUNCA almacenar contraseñas en texto plano en la base de datos
 *    - BCryptPasswordEncoder utiliza el algoritmo bcrypt (muy seguro)
 *    - Cada vez que encriptas la misma contraseña, obtienes un hash diferente (salt)
 *    - Es computacionalmente costoso de descifrar (resistente a ataques de fuerza bruta)
 * 
 * 4. TRANSACCIONALIDAD:
 *    - Aunque no usamos @Transactional aquí, cada operación del repository es transaccional
 *    - Si alguna operación falla, se puede hacer rollback automáticamente
 *    - En un entorno real, marcarías este método como @Transactional para mayor control
 * 
 * FLUJO DE EJECUCIÓN:
 * 
 * 1. Spring Boot arranca y carga todos los componentes
 * 2. Se crea la base de datos H2 en memoria con las tablas necesarias
 * 3. Spring detecta esta clase como CommandLineRunner
 * 4. Inyecta automáticamente las dependencias (repositories, passwordEncoder)
 * 5. Ejecuta el método run()
 * 6. Se crean los roles y usuarios de ejemplo
 * 7. La aplicación queda lista para recibir peticiones
 * 
 * @author CebandoIdeas
 * @version 1.0
 */
@Component  // Marca esta clase como componente de Spring para ser detectada automáticamente
@RequiredArgsConstructor  // Lombok genera constructor con todas las dependencias final
@Slf4j  // Lombok genera automáticamente el logger: private static final Logger log = ...
public class DataInitializer implements CommandLineRunner {
    
    /*
     * INYECCIÓN DE DEPENDENCIAS POR CONSTRUCTOR:
     * 
     * Spring automáticamente inyectará estas dependencias cuando cree una instancia
     * de DataInitializer. Esto es más seguro que @Autowired en campos porque:
     * 
     * 1. Garantiza que las dependencias no sean null
     * 2. Permite hacer los campos final (inmutables)
     * 3. Facilita las pruebas unitarias (puedes pasar mocks en el constructor)
     * 4. Es la práctica recomendada por el equipo de Spring
     */
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder inyectado desde SecurityConfig
    
    /**
     * Método que se ejecuta automáticamente al arranque de la aplicación
     * 
     * ORDEN DE EJECUCIÓN:
     * 1. Se crean las tablas en la base de datos (por Hibernate DDL)
     * 2. Se ejecuta este método
     * 3. Se puebla la base de datos con datos iniciales
     * 4. La aplicación queda lista para recibir peticiones
     * 
     * @param args Argumentos de línea de comandos (raramente utilizados en CommandLineRunner)
     * @throws Exception Si ocurre algún error durante la inicialización
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Iniciando poblado de datos iniciales...");
        
        /*
         * VERIFICACIÓN DE ESTADO:
         * 
         * Verificamos si ya existen datos para evitar duplicados.
         * Esto es especialmente útil en entornos donde la aplicación
         * se reinicia frecuentemente durante el desarrollo.
         */
        if (roleRepository.count() > 0) {
            log.info("✅ Los datos ya existen, saltando inicialización");
            return;
        }
        
        // Inicializar roles y usuarios
        inicializarRoles();
        inicializarUsuarios();
        
        log.info("✅ Datos iniciales creados exitosamente");
        log.info("📊 Resumen:");
        log.info("   - Roles creados: {}", roleRepository.count());
        log.info("   - Usuarios creados: {}", userRepository.count());
    }
    
    /**
     * Crea los roles básicos del sistema
     * 
     * ROLES EN UN SISTEMA JWT:
     * 
     * - USER: Usuario básico con permisos limitados
     * - ADMIN: Administrador con permisos completos
     * - MODERATOR: Rol intermedio (opcional, para sistemas más complejos)
     * 
     * PATRÓN DE NOMENCLATURA:
     * - Prefijo "ROLE_" es una convención de Spring Security
     * - Aunque no es obligatorio, es altamente recomendado
     * - Facilita la configuración de seguridad y autorización
     */
    private void inicializarRoles() {
        log.info("📝 Creando roles del sistema...");
        
        /*
         * CREACIÓN DE ROLES:
         * 
         * Creamos los roles básicos que utilizará nuestra aplicación.
         * En un sistema real, podrías tener roles más específicos como:
         * - ROLE_CUSTOMER
         * - ROLE_EMPLOYEE
         * - ROLE_MANAGER
         * - ROLE_SUPER_ADMIN
         */
        
        Role userRole = new Role("ROLE_USER");
        Role adminRole = new Role("ROLE_ADMIN");
        
        // Guardar en la base de datos
        roleRepository.save(userRole);
        roleRepository.save(adminRole);
        
        log.info("✅ Roles creados: ROLE_USER, ROLE_ADMIN");
        
        /*
         * NOTA EDUCATIVA:
         * 
         * ¿Por qué prefijo "ROLE_"?
         * 
         * Spring Security espera este prefijo por defecto al usar:
         * - @PreAuthorize("hasRole('USER')")  // Spring agrega "ROLE_" automáticamente
         * - @PreAuthorize("hasAuthority('ROLE_USER')")  // Debes especificar el prefijo completo
         * 
         * También facilita la configuración en SecurityConfig:
         * - .requestMatchers("/admin/**").hasRole("ADMIN")  // Sin prefijo
         * - .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")  // Con prefijo
         */
    }
    
    /**
     * Crea usuarios de ejemplo con diferentes roles para pruebas
     * 
     * USUARIOS DE PRUEBA:
     * 
     * 1. admin/admin123 - Administrador del sistema
     * 2. user/user123 - Usuario básico
     * 3. juan/password123 - Usuario adicional para pruebas
     * 
     * SEGURIDAD DE CONTRASEÑAS:
     * 
     * - Todas las contraseñas se encriptan con BCrypt antes de almacenar
     * - BCrypt incluye automáticamente un "salt" único para cada contraseña
     * - Es computacionalmente costoso, lo que dificulta ataques de fuerza bruta
     * - Cada encriptación de la misma contraseña produce un hash diferente
     */
    private void inicializarUsuarios() {
        log.info("👥 Creando usuarios de ejemplo...");
        
        // Recuperar roles de la base de datos
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Role USER no encontrado"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("Role ADMIN no encontrado"));
        
        /*
         * USUARIO ADMINISTRADOR:
         * 
         * Credenciales: admin / admin123
         * Roles: ADMIN, USER (un admin también puede ser user)
         * 
         * En muchos sistemas, los administradores también tienen permisos de usuario
         * básico, por eso asignamos ambos roles.
         */
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ejemplo.com");
            // Encriptar contraseña antes de guardar
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            // Asignar ambos roles al administrador
            admin.setRoles(Set.of(adminRole, userRole));
            
            userRepository.save(admin);
            log.info("✅ Usuario administrador creado: admin / admin123");
            
            /*
             * DEMOSTRACIÓN DE ENCRIPTACIÓN:
             * 
             * Si ejecutas esto varias veces, verás que el hash es diferente cada vez:
             */
            log.debug("🔐 Hash de contraseña admin: {}", admin.getPassword());
        }
        
        /*
         * USUARIO BÁSICO:
         * 
         * Credenciales: user / user123
         * Roles: USER únicamente
         */
        if (!userRepository.findByUsername("user").isPresent()) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@ejemplo.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEnabled(true);
            user.setRoles(Set.of(userRole));
            
            userRepository.save(user);
            log.info("✅ Usuario básico creado: user / user123");
        }
        
        /*
         * USUARIO ADICIONAL PARA PRUEBAS:
         * 
         * Credenciales: juan / password123
         * Roles: USER únicamente
         * 
         * Es útil tener varios usuarios para probar funcionalidades como:
         * - Listado de usuarios
         * - Búsquedas
         * - Paginación
         * - etc.
         */
        if (!userRepository.findByUsername("juan").isPresent()) {
            User juan = new User();
            juan.setUsername("juan");
            juan.setEmail("juan.perez@ejemplo.com");
            juan.setPassword(passwordEncoder.encode("password123"));
            juan.setEnabled(true);
            juan.setRoles(Set.of(userRole));
            
            userRepository.save(juan);
            log.info("✅ Usuario de prueba creado: juan / password123");
        }
        
        /*
         * USUARIO DESHABILITADO PARA PRUEBAS:
         * 
         * Este usuario está deshabilitado para probar el manejo de cuentas inactivas
         * en el proceso de autenticación.
         */
        if (!userRepository.findByUsername("disabled").isPresent()) {
            User disabledUser = new User();
            disabledUser.setUsername("disabled");
            disabledUser.setEmail("disabled@ejemplo.com");
            disabledUser.setPassword(passwordEncoder.encode("disabled123"));
            disabledUser.setEnabled(false);  // Usuario deshabilitado
            disabledUser.setRoles(Set.of(userRole));
            
            userRepository.save(disabledUser);
            log.info("✅ Usuario deshabilitado creado: disabled / disabled123 (enabled=false)");
        }
        
        log.info("👥 Usuarios creados exitosamente");
        
        /*
         * MOSTRAR INFORMACIÓN ÚTIL PARA PRUEBAS:
         */
        log.info("");
        log.info("🔑 CREDENCIALES PARA PRUEBAS:");
        log.info("   👨‍💼 Administrador: admin / admin123");
        log.info("   👤 Usuario básico: user / user123");
        log.info("   👤 Usuario de prueba: juan / password123");
        log.info("   🚫 Usuario deshabilitado: disabled / disabled123");
        log.info("");
        log.info("🌐 Endpoints de prueba:");
        log.info("   POST /api/auth/login - Iniciar sesión");
        log.info("   POST /api/auth/register - Registrar nuevo usuario");
        log.info("   GET /api/users - Listar usuarios (requiere ADMIN)");
        log.info("   GET /api/protected/admin - Endpoint solo para administradores");
        log.info("   GET /api/protected/user - Endpoint para usuarios autenticados");
        log.info("   GET /api/public/info - Endpoint público (sin autenticación)");
        log.info("");
    }
    
    /*
     * EJERCICIOS PROPUESTOS PARA ESTUDIANTES:
     * 
     * 1. BÁSICO: Agrega un nuevo rol "ROLE_MODERATOR" y crea un usuario con ese rol
     * 
     * 2. INTERMEDIO: Modifica el código para leer las credenciales desde application.properties
     *    usando @Value o @ConfigurationProperties
     * 
     * 3. AVANZADO: Implementa un sistema de carga de datos desde un archivo JSON o CSV
     * 
     * 4. DESAFÍO: Agrega validaciones para asegurar que los emails sean únicos y válidos
     * 
     * 5. PROFESIONAL: Implementa un mecanismo para actualizar datos existentes sin duplicar
     *    información (upsert pattern)
     */
}
package com.ejemplo.jwtdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación JWT Demo con Spring Boot
 * 
 * Esta es la clase que actúa como punto de entrada para nuestra aplicación Spring Boot.
 * La anotación @SpringBootApplication es una meta-anotación que combina:
 * 
 * 1. @Configuration: Indica que esta clase puede contener definiciones de beans
 * 2. @EnableAutoConfiguration: Habilita la configuración automática de Spring Boot
 * 3. @ComponentScan: Permite el escaneo de componentes en el paquete actual y subpaquetes
 * 
 * CONCEPTOS EDUCATIVOS:
 * 
 * 1. PUNTO DE ENTRADA (Entry Point):
 *    - Spring Boot necesita una clase principal marcada con @SpringBootApplication
 *    - Esta clase debe contener el método main() que arranca la aplicación
 *    - SpringApplication.run() inicia el contenedor de Spring y la aplicación web
 * 
 * 2. AUTO-CONFIGURACIÓN:
 *    - Spring Boot examina el classpath y configura automáticamente los beans necesarios
 *    - Por ejemplo, detecta H2 en el classpath y configura una base de datos en memoria
 *    - Detecta Spring Security y configura la seguridad básica automáticamente
 * 
 * 3. CONTENEDOR DE INVERSIÓN DE CONTROL (IoC Container):
 *    - Spring Boot crea y gestiona todos los objetos (beans) de nuestra aplicación
 *    - Se encarga de la inyección de dependencias automáticamente
 *    - Maneja el ciclo de vida completo de los componentes
 * 
 * ESTRUCTURA DEL PROYECTO:
 * 
 * src/main/java/com/ejemplo/jwtdemo/
 * ├── JwtDemoApplication.java        <- Clase principal (ESTA CLASE)
 * ├── config/                        <- Configuraciones de Spring
 * │   ├── SecurityConfig.java        <- Configuración de seguridad
 * │   └── DataInitializer.java       <- Inicialización de datos
 * ├── controller/                    <- Controladores REST
 * │   ├── AuthController.java        <- Login/registro
 * │   ├── UserController.java        <- Gestión de usuarios
 * │   ├── PublicController.java      <- Endpoints públicos
 * │   └── ProtectedController.java   <- Endpoints protegidos
 * ├── dto/                          <- Objetos de transferencia de datos
 * ├── entity/                       <- Entidades JPA
 * ├── exception/                    <- Manejo de excepciones
 * ├── filter/                       <- Filtros de seguridad
 * ├── repository/                   <- Repositorios JPA
 * └── service/                      <- Lógica de negocio
 * 
 * FLUJO DE ARRANQUE:
 * 
 * 1. main() llama a SpringApplication.run()
 * 2. Spring Boot escanea el classpath y encuentra las dependencias
 * 3. Configura automáticamente:
 *    - Servidor web embebido (Tomcat)
 *    - Base de datos H2 en memoria
 *    - Spring Security con JWT
 *    - JPA/Hibernate para persistencia
 * 4. Escanea componentes (@Component, @Service, @Repository, @Controller)
 * 5. Crea instancias de todos los beans y resuelve dependencias
 * 6. Ejecuta CommandLineRunners (como DataInitializer)
 * 7. Inicia el servidor web en el puerto configurado (8080)
 * 
 * PATRONES DE DISEÑO UTILIZADOS:
 * 
 * - Inversión de Control (IoC): Spring gestiona las dependencias
 * - Inyección de Dependencias: Los objetos reciben sus dependencias automáticamente
 * - Singleton: Los beans de Spring son singleton por defecto
 * - Factory: SpringApplication actúa como factory para crear el contexto
 * - Template Method: Spring Boot sigue este patrón para la configuración
 * 
 * @author CebandoIdeas
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
public class JwtDemoApplication {

    /**
     * Método principal que arranca la aplicación Spring Boot
     * 
     * EXPLICACIÓN TÉCNICA:
     * 
     * SpringApplication.run() realiza las siguientes operaciones:
     * 
     * 1. Crea un ApplicationContext (contenedor de Spring)
     * 2. Registra shutdown hooks para limpieza adecuada
     * 3. Carga la configuración de application.properties
     * 4. Escanea y registra todos los componentes de Spring
     * 5. Configura automáticamente las características detectadas
     * 6. Inicia el servidor web embebido
     * 7. Publica eventos de arranque para componentes que los necesiten
     * 
     * PERSONALIZACIÓN AVANZADA:
     * 
     * Si necesitas personalizar el comportamiento del arranque, puedes usar:
     * 
     * SpringApplication app = new SpringApplication(JwtDemoApplication.class);
     * app.setBannerMode(Banner.Mode.OFF);           // Deshabilita el banner
     * app.setDefaultProperties(properties);         // Propiedades por defecto
     * app.setAdditionalProfiles("dev", "debug");   // Perfiles adicionales
     * app.run(args);
     * 
     * @param args Argumentos de línea de comandos pasados al programa
     *             Ejemplos de uso:
     *             - --server.port=8090 (cambiar puerto)
     *             - --spring.profiles.active=dev (activar perfil)
     *             - --logging.level.root=DEBUG (nivel de logging)
     */
    public static void main(String[] args) {
        /*
         * MOMENTO EDUCATIVO:
         * 
         * Esta línea es donde toda la magia comienza. SpringApplication.run():
         * 
         * 1. Detecta que estamos en un entorno web (por spring-boot-starter-web)
         * 2. Configura Tomcat como servidor embebido
         * 3. Configura H2 como base de datos en memoria
         * 4. Configura Spring Security automáticamente
         * 5. Escanea todas nuestras clases anotadas (@Service, @Repository, etc.)
         * 6. Inyecta todas las dependencias necesarias
         * 7. Ejecuta DataInitializer para poblar la base de datos
         * 8. Arranca el servidor en http://localhost:8080
         * 
         * ¡Y todo esto con una sola línea de código!
         * Esto es el poder de la convención sobre configuración de Spring Boot.
         */
        SpringApplication.run(JwtDemoApplication.class, args);
        
        /*
         * NOTA EDUCATIVA:
         * 
         * Una vez que esta línea se ejecuta exitosamente, verás en la consola:
         * 
         * 1. El banner de Spring Boot (el logo ASCII)
         * 2. Información sobre la configuración automática
         * 3. Conexión a la base de datos H2
         * 4. Inicialización de las tablas de la base de datos
         * 5. Ejecución del DataInitializer (creación de usuarios y roles)
         * 6. Mensaje final: "Started JwtDemoApplication in X.XXX seconds"
         * 
         * A partir de ese momento, la aplicación estará lista para recibir
         * peticiones HTTP en http://localhost:8080
         */
    }
    
    /*
     * EJERCICIOS PROPUESTOS PARA ESTUDIANTES:
     * 
     * 1. BÁSICO: Modifica el puerto de la aplicación a 9090 usando application.properties
     * 
     * 2. INTERMEDIO: Agrega un @Bean en esta clase que retorne un PasswordEncoder
     *    y observa cómo Spring lo inyecta automáticamente en otros componentes
     * 
     * 3. AVANZADO: Implementa un ApplicationListener<ApplicationReadyEvent>
     *    para ejecutar código después de que la aplicación esté completamente iniciada
     * 
     * 4. DESAFÍO: Crea múltiples perfiles (dev, prod) con diferentes configuraciones
     *    de base de datos y observa cómo cambia el comportamiento
     */
}
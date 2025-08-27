package com.ejemplo.jwtdemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir los datos de registro de un nuevo usuario.
 * 
 * Este DTO demuestra:
 * - Validaciones más complejas para el registro
 * - Uso de expresiones regulares para validar formatos
 * - Separación entre datos de entrada y entidades de base de datos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    /**
     * Nombre de usuario único en el sistema.
     * 
     * Aplicamos validaciones estrictas:
     * - Solo caracteres alfanuméricos y guiones bajos
     * - Longitud controlada para evitar problemas de UI y BD
     */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", 
             message = "El nombre de usuario solo puede contener letras, números y guiones bajos")
    private String username;
    
    /**
     * Email del usuario - debe ser único y válido.
     * 
     * La validación @Email usa una expresión regular interna de Jakarta Validation
     * para verificar el formato básico de email.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar un email válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    /**
     * Contraseña para la nueva cuenta.
     * 
     * Aplicamos validaciones de seguridad básicas:
     * - Longitud mínima para resistir ataques de fuerza bruta
     * - Patrón que requiere al menos una letra, un número y un carácter especial
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "La contraseña debe contener al menos una letra, un número y un carácter especial (@$!%*?&)")
    private String password;
    
    /**
     * Confirmación de contraseña para evitar errores de tipeo.
     * 
     * Nota: La validación de coincidencia se realizará en el servicio,
     * ya que las validaciones de Jakarta no pueden comparar fácilmente campos entre sí.
     */
    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;
}
package com.ejemplo.jwtdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para recibir las credenciales de login.
 * 
 * Conceptos clave:
 * - Los DTOs son objetos diseñados específicamente para transferir datos entre capas
 * - Separan la representación interna (entidades) de la externa (API REST)
 * - Permiten validaciones específicas para cada operación
 * - Mejoran la seguridad al exponer solo los campos necesarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * Nombre de usuario o email para el login.
     * 
     * Validaciones aplicadas:
     * - @NotBlank: Garantiza que no sea null, vacío o solo espacios
     * - @Size: Limita la longitud del campo (buena práctica de seguridad)
     */
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;
    
    /**
     * Contraseña del usuario.
     * 
     * Nota importante: En un DTO de request, la contraseña se recibe en texto plano
     * pero debe ser hasheada antes de compararla con la base de datos.
     */
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;
}
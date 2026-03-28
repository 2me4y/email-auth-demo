package com.javaee.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO для формы регистрации.
 * Аннотации @NotBlank, @Email, @Size — Bean Validation (Jakarta EE).
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 50, message = "Имя: от 2 до 50 символов")
    private String fullName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль минимум 6 символов")
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;
}

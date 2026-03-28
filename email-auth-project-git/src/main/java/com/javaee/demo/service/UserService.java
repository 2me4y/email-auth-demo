package com.javaee.demo.service;

import com.javaee.demo.dto.RegisterDto;
import com.javaee.demo.entity.User;

public interface UserService {

    /**
     * Регистрация нового пользователя.
     * Создаёт аккаунт и АСИНХРОННО отправляет письмо подтверждения.
     */
    User register(RegisterDto dto);

    /**
     * Подтверждение email по токену из письма.
     */
    void verifyEmail(String token);

    /**
     * Найти пользователя по email.
     */
    User findByEmail(String email);
}

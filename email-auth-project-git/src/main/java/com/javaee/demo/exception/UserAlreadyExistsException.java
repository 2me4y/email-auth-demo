package com.javaee.demo.exception;

/**
 * Пользователь с таким email уже существует.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Пользователь с email '" + email + "' уже зарегистрирован");
    }
}

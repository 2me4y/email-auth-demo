package com.javaee.demo.exception;

/**
 * Токен подтверждения не найден или истёк.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

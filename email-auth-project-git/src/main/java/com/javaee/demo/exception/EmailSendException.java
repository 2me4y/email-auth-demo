package com.javaee.demo.exception;

/**
 * Ошибка при отправке письма через SMTP.
 */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}

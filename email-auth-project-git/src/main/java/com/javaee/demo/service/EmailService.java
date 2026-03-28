package com.javaee.demo.service;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс сервиса отправки email.
 * Возвращаем CompletableFuture<Void> — асинхронный результат.
 */
public interface EmailService {

    /**
     * Асинхронная отправка письма с подтверждением email.
     *
     * @param to    адрес получателя
     * @param token токен подтверждения
     * @return CompletableFuture для отслеживания статуса
     */
    CompletableFuture<Void> sendVerificationEmail(String to, String token);

    /**
     * Асинхронная отправка приветственного письма после подтверждения.
     */
    CompletableFuture<Void> sendWelcomeEmail(String to, String fullName);
}

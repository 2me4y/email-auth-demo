package com.javaee.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserExists(UserAlreadyExistsException ex, Model model) {
        log.warn("Дубликат email: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "auth/register";
    }

    @ExceptionHandler(InvalidTokenException.class)
    public String handleInvalidToken(InvalidTokenException ex, Model model) {
        log.warn("Неверный токен: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "auth/token-error";
    }

    @ExceptionHandler(EmailSendException.class)
    public String handleEmailError(EmailSendException ex, Model model) {
        log.error("Ошибка отправки email: {}", ex.getMessage(), ex.getCause());
        model.addAttribute("error", "Не удалось отправить письмо. Попробуйте позже.");
        return "error/email-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Непредвиденная ошибка: {}", ex.getMessage(), ex);
        model.addAttribute("error", "Произошла внутренняя ошибка сервера.");
        return "error/500";
    }
}

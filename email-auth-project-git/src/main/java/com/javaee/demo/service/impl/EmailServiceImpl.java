package com.javaee.demo.service.impl;

import com.javaee.demo.exception.EmailSendException;
import com.javaee.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendVerificationEmail(String to, String token) {
        log.info("[{}] Отправка письма подтверждения: {}", Thread.currentThread().getName(), to);

        try {
            String confirmUrl = baseUrl + "/auth/verify?token=" + token;
            sendHtmlEmail(to, "✉ Подтвердите ваш email", buildVerificationHtml(to, confirmUrl));
            log.info("[{}] Письмо отправлено: {}", Thread.currentThread().getName(), to);
            return CompletableFuture.completedFuture(null);

        } catch (MessagingException e) {
            log.error("[{}] Ошибка отправки {}: {}", Thread.currentThread().getName(), to, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new EmailSendException("Не удалось отправить письмо на " + to, e));
            return future;
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendWelcomeEmail(String to, String fullName) {
        log.info("[{}] Приветственное письмо: {}", Thread.currentThread().getName(), to);

        try {
            sendHtmlEmail(to, "🎉 Добро пожаловать, " + fullName + "!", buildWelcomeHtml(fullName));
            return CompletableFuture.completedFuture(null);

        } catch (MessagingException e) {
            log.error("Ошибка welcome email: {}", e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new EmailSendException("Ошибка welcome email", e));
            return future;
        }
    }

    private void sendHtmlEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String buildVerificationHtml(String email, String confirmUrl) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #4F46E5; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0;">Подтверждение Email</h1>
                </div>
                <div style="padding: 30px; background: #f9f9f9;">
                    <p>Здравствуйте!</p>
                    <p>Вы зарегистрировались с адресом: <strong>%s</strong></p>
                    <p>Нажмите кнопку ниже, чтобы подтвердить email:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: #4F46E5; color: white; padding: 15px 30px;
                              text-decoration: none; border-radius: 8px; font-size: 16px;">
                            Подтвердить Email
                        </a>
                    </div>
                    <p style="color: #666; font-size: 14px;">
                        Ссылка действительна 24 часа.<br>
                        Если вы не регистрировались — просто проигнорируйте это письмо.
                    </p>
                </div>
            </div>
            """.formatted(email, confirmUrl);
    }

    private String buildWelcomeHtml(String fullName) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #10B981; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0;">🎉 Добро пожаловать!</h1>
                </div>
                <div style="padding: 30px; background: #f9f9f9;">
                    <p>Привет, <strong>%s</strong>!</p>
                    <p>Ваш аккаунт успешно активирован. Теперь вы можете войти в систему.</p>
                </div>
            </div>
            """.formatted(fullName);
    }
}

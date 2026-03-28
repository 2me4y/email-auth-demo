package com.javaee.demo.service.impl;

import com.javaee.demo.dto.RegisterDto;
import com.javaee.demo.entity.User;
import com.javaee.demo.exception.InvalidTokenException;
import com.javaee.demo.exception.UserAlreadyExistsException;
import com.javaee.demo.repository.UserRepository;
import com.javaee.demo.service.EmailService;
import com.javaee.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.token.expiration-hours}")
    private int tokenExpirationHours;

    @Override
    @Transactional
    public User register(RegisterDto dto) {
        log.info("Регистрация пользователя: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail());
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        String token = UUID.randomUUID().toString();

        User user = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(false)
                .verificationToken(token)
                .tokenExpiry(LocalDateTime.now().plusHours(tokenExpirationHours))
                .build();

        userRepository.save(user);
        log.info("Пользователь сохранён: {}", user.getEmail());

        emailService.sendVerificationEmail(user.getEmail(), token)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки письма для {}: {}", user.getEmail(), ex.getMessage());
                    } else {
                        log.info("Письмо отправлено для: {}", user.getEmail());
                    }
                });

        return user;
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Верификация токена: {}", token);

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException(
                        "Токен не найден. Возможно, ссылка уже использована."
                ));

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException(
                    "Ссылка устарела. Токен действителен " + tokenExpirationHours + " часов."
            );
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        log.info("Аккаунт подтверждён: {}", user.getEmail());

        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + email));
    }
}

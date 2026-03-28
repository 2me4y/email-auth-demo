package com.javaee.demo.service;

import com.javaee.demo.dto.RegisterDto;
import com.javaee.demo.entity.User;
import com.javaee.demo.exception.InvalidTokenException;
import com.javaee.demo.exception.UserAlreadyExistsException;
import com.javaee.demo.repository.UserRepository;
import com.javaee.demo.service.impl.EmailServiceImpl;
import com.javaee.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UNIT ТЕСТЫ для UserService.
 *
 * @ExtendWith(MockitoExtension.class) — подключает Mockito без Spring контекста.
 * @Mock — создаёт "заглушку" для зависимостей.
 * @InjectMocks — создаёт реальный экземпляр класса и внедряет моки.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailServiceImpl emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Внедряем значения @Value через ReflectionTestUtils
        ReflectionTestUtils.setField(userService, "tokenExpirationHours", 24);
    }

    // ─────────────────────────────────────────────
    // ТЕСТЫ РЕГИСТРАЦИИ
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void register_success() {
        // ARRANGE — подготовка данных
        RegisterDto dto = new RegisterDto("Иван Петров", "ivan@test.com", "pass123", "pass123");

        when(userRepository.existsByEmail("ivan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(emailService.sendVerificationEmail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // ACT — вызываем метод
        User result = userService.register(dto);

        // ASSERT — проверяем результат
        assertThat(result.getEmail()).isEqualTo("ivan@test.com");
        assertThat(result.getFullName()).isEqualTo("Иван Петров");
        assertThat(result.isEnabled()).isFalse();  // до подтверждения email
        assertThat(result.getVerificationToken()).isNotNull();
        assertThat(result.getPassword()).isEqualTo("$2a$hashed"); // пароль хешируется

        // Проверяем что save и sendEmail были вызваны ровно по 1 разу
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("ivan@test.com"), anyString());
    }

    @Test
    @DisplayName("Регистрация с уже занятым email → UserAlreadyExistsException")
    void register_duplicateEmail_throwsException() {
        RegisterDto dto = new RegisterDto("Аня", "exists@test.com", "pass123", "pass123");

        // Имитируем что email уже существует
        when(userRepository.existsByEmail("exists@test.com")).thenReturn(true);

        // Ожидаем исключение
        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("exists@test.com");

        // save НЕ должен вызываться
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Регистрация с несовпадающими паролями → IllegalArgumentException")
    void register_passwordMismatch_throwsException() {
        RegisterDto dto = new RegisterDto("Боб", "bob@test.com", "pass123", "WRONG");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пароли не совпадают");
    }

    // ─────────────────────────────────────────────
    // ТЕСТЫ ВЕРИФИКАЦИИ
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Успешное подтверждение email по токену")
    void verifyEmail_success() {
        String token = "valid-token-uuid";
        User user = User.builder()
                .email("test@test.com")
                .fullName("Тест")
                .verificationToken(token)
                .tokenExpiry(LocalDateTime.now().plusHours(10)) // ещё не истёк
                .enabled(false)
                .build();

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(emailService.sendWelcomeEmail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // ACT
        userService.verifyEmail(token);

        // Проверяем что пользователь активирован
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getVerificationToken()).isNull(); // токен очищен
        verify(userRepository).save(user);
        verify(emailService).sendWelcomeEmail(eq("test@test.com"), anyString());
    }

    @Test
    @DisplayName("Верификация с несуществующим токеном → InvalidTokenException")
    void verifyEmail_tokenNotFound_throwsException() {
        when(userRepository.findByVerificationToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail("bad-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Токен не найден");
    }

    @Test
    @DisplayName("Верификация с истёкшим токеном → InvalidTokenException")
    void verifyEmail_expiredToken_throwsException() {
        String token = "expired-token";
        User user = User.builder()
                .email("old@test.com")
                .verificationToken(token)
                .tokenExpiry(LocalDateTime.now().minusHours(1)) // уже истёк!
                .build();

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.verifyEmail(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("устарела");
    }
}

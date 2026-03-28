# Email Auth Demo

Spring Boot приложение с регистрацией через email и асинхронной отправкой писем.

## Технологии

- Spring Boot 3.2
- Spring Security + BCrypt
- Spring Mail + Async (@Async + CompletableFuture)
- H2 in-memory database
- Thymeleaf
- Mailtrap (SMTP sandbox для разработки)

## Запуск

### 1. Получи Mailtrap credentials

1. Зарегистрируйся на [mailtrap.io](https://mailtrap.io) (бесплатно)
2. Email Testing → Inboxes → My Inbox
3. Вкладка **SMTP Settings** → выбери **Spring Boot**
4. Скопируй `Username` и `Password`

### 2. Задай переменные окружения

**Mac / Linux:**
```bash
export MAIL_USERNAME=твой_username
export MAIL_PASSWORD=твой_password
```

**Windows (PowerShell):**
```powershell
$env:MAIL_USERNAME="твой_username"
$env:MAIL_PASSWORD="твой_password"
```

**IntelliJ IDEA:**

Run → Edit Configurations → Environment variables:
```
MAIL_USERNAME=твой_username;MAIL_PASSWORD=твой_password
```

### 3. Запусти приложение

```bash
mvn spring-boot:run
```

Открой [http://localhost:8080](http://localhost:8080)

### 4. Проверь письма

Все письма появляются в твоём Mailtrap inbox — никуда реально не уходят.

## Структура проекта

```
src/main/java/com/javaee/demo/
├── config/
│   ├── AsyncConfig.java        # Пул потоков для @Async
│   └── SecurityConfig.java     # Spring Security
├── controller/
│   ├── AuthController.java     # Регистрация, логин, верификация
│   └── DashboardController.java
├── service/
│   ├── EmailService.java
│   ├── UserService.java
│   └── impl/
│       ├── EmailServiceImpl.java       # Async отправка писем
│       ├── UserServiceImpl.java        # Бизнес-логика
│       └── UserDetailsServiceImpl.java # Spring Security
├── entity/User.java
├── dto/RegisterDto.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── EmailSendException.java
│   ├── InvalidTokenException.java
│   └── UserAlreadyExistsException.java
└── repository/UserRepository.java
```

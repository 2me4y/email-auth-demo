package com.javaee.demo.controller;

import com.javaee.demo.dto.RegisterDto;
import com.javaee.demo.entity.User;
import com.javaee.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerDto") RegisterDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Пароли не совпадают");
            return "auth/register";
        }

        User user = userService.register(dto);
        log.info("Новый пользователь зарегистрирован: {}", user.getEmail());

        redirectAttributes.addFlashAttribute("successMessage",
                "Регистрация прошла успешно! Проверьте почту: " + user.getEmail());

        return "redirect:/auth/check-email";
    }

    @GetMapping("/check-email")
    public String checkEmail() {
        return "auth/check-email";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        userService.verifyEmail(token);
        model.addAttribute("message", "Email успешно подтверждён! Теперь вы можете войти.");
        return "auth/verified";
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Неверный email или пароль. Или аккаунт не подтверждён.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Вы успешно вышли из системы.");
        }
        return "auth/login";
    }
}

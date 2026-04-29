package ru.ivanov.diplom.inventory_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ivanov.diplom.inventory_system.dto.auth.AuthResponse;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.service.CurrentUserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public AuthResponse me() {
        AppUser user = currentUserService.getCurrentUser();
        return new AuthResponse(
                user.getUsername(),
                user.getRole().name(),
                user.getEmployee().getLastName()
                        + " "
                        + user.getEmployee().getFirstName()
                        + " "
                        + user.getEmployee().getMiddleName()
        );
    }
}

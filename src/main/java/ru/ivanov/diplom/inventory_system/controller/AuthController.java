package ru.ivanov.diplom.inventory_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ivanov.diplom.inventory_system.dto.auth.AuthResponse;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.entity.Permission;
import ru.ivanov.diplom.inventory_system.service.CurrentUserService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public AuthResponse me() {
        AppUser user = currentUserService.getCurrentUser();

        List<String> permissions = user.getPermissions()
                .stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getEmployee().getFullName(),
                user.getMustChangePassword(),
                permissions
        );
    }
}

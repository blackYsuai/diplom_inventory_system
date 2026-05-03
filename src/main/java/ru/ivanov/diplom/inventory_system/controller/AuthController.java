package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.auth.AuthResponse;
import ru.ivanov.diplom.inventory_system.dto.auth.LoginRequest;
import ru.ivanov.diplom.inventory_system.dto.auth.LoginResponse;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.entity.Permission;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;
import ru.ivanov.diplom.inventory_system.service.CurrentUserService;
import ru.ivanov.diplom.inventory_system.service.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CurrentUserService currentUserService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        AppUser user = appUserRepository.findByUsernameWithDetails(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

        String token = jwtService.generateToken(userDetails);

        List<String> permissions = user.getPermissions()
                .stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getEmployee().getFullName(),
                user.getMustChangePassword(),
                permissions
        );
    }

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

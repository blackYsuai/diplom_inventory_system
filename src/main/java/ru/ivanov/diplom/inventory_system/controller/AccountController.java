package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.account.AccountProfileResponse;
import ru.ivanov.diplom.inventory_system.dto.account.ChangePasswordRequest;
import ru.ivanov.diplom.inventory_system.dto.account.ChangePasswordResponse;
import ru.ivanov.diplom.inventory_system.service.AccountService;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/profile")
    public AccountProfileResponse getProfile() {
        return accountService.getProfile();
    }

    @PatchMapping("/change-password")
    public ChangePasswordResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return accountService.changePassword(request);
    }
}

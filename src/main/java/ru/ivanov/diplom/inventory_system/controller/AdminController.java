package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.admin.*;
import ru.ivanov.diplom.inventory_system.service.AdminDashboardService;
import ru.ivanov.diplom.inventory_system.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_MANAGE')")
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminDashboardService.getDashboard();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUserById(@PathVariable Long id) {
        return adminUserService.getUserById(id);
    }

    @PostMapping("/users")
    public AdminUserCreateResponse createUser(
            @Valid @RequestBody AdminUserCreateRequest request
    ) {
        return adminUserService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return adminUserService.updateUser(id, request);
    }

    @PatchMapping("/users/{id}/activate")
    public AdminUserResponse activateUser(@PathVariable Long id) {
        return adminUserService.activateUser(id);
    }

    @PatchMapping("/users/{id}/deactivate")
    public AdminUserResponse deactivateUser(@PathVariable Long id) {
        return adminUserService.deactivateUser(id);
    }

    @PatchMapping("/users/{id}/role")
    public AdminUserResponse changeRole(
            @PathVariable Long id,
            @Valid @RequestBody SetUserRoleRequest request
    ) {
        return adminUserService.changeRole(id, request);
    }

    @PatchMapping("/users/{id}/permissions")
    public AdminUserResponse setPermissions(
            @PathVariable Long id,
            @RequestBody SetUserPermissionsRequest request
    ) {
        return adminUserService.setPermissions(id, request);
    }

    @PatchMapping("/users/{id}/reset-password")
    public PasswordResetResponse resetPassword(@PathVariable Long id) {
        return adminUserService.resetPassword(id);
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> getAllPermissions() {
        return adminUserService.getAllPermissions();
    }

    @PatchMapping("/users/{id}/permissions/add")
    public AdminUserResponse addPermissions(
            @PathVariable Long id,
            @RequestBody SetUserPermissionsRequest request
    ) {
        return adminUserService.addPermissions(id, request);
    }

    @PatchMapping("/users/{id}/permissions/remove")
    public AdminUserResponse removePermissions(
            @PathVariable Long id,
            @RequestBody SetUserPermissionsRequest request
    ) {
        return adminUserService.removePermissions(id, request);
    }
}

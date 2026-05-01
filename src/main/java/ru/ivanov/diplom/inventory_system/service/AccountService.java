package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.account.AccountProfileResponse;
import ru.ivanov.diplom.inventory_system.dto.account.ChangePasswordRequest;
import ru.ivanov.diplom.inventory_system.dto.account.ChangePasswordResponse;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.entity.Department;
import ru.ivanov.diplom.inventory_system.entity.Employee;
import ru.ivanov.diplom.inventory_system.entity.Permission;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AccountProfileResponse getProfile() {
        AppUser user = currentUserService.getCurrentUser();
        return toProfileResponse(user);
    }

    @Transactional
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.repeatNewPassword())) {
            throw new BadRequestException("Новый пароль и подтверждение не совпадают");
        }

        AppUser currentUser = currentUserService.getCurrentUser();

        AppUser user = appUserRepository.findByIdWithDetails(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Текущий пользователь не найден"
                ));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Текущий пароль указан неверно");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordExpiresAt(null);

        appUserRepository.save(user);

        return new ChangePasswordResponse("Пароль успешно изменен");
    }

    private AccountProfileResponse toProfileResponse(AppUser user) {
        Employee employee = user.getEmployee();
        Department department = employee != null ? employee.getDepartment() : null;

        List<String> permissionCodes = user.getPermissions() == null
                ? List.of()
                : user.getPermissions()
                .stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        return new AccountProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                user.getMustChangePassword(),

                employee != null ? employee.getId() : null,
                employee != null ? employee.getFullName() : null,
                employee != null ? employee.getPosition() : null,
                employee != null ? employee.getPhone() : null,
                employee != null ? employee.getEmail() : null,

                department != null ? department.getId() : null,
                department != null ? department.getName() : null,

                permissionCodes
        );
    }
}

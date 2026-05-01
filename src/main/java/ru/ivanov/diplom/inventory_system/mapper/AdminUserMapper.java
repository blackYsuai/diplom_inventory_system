package ru.ivanov.diplom.inventory_system.mapper;

import org.springframework.stereotype.Component;
import ru.ivanov.diplom.inventory_system.dto.admin.AdminUserResponse;
import ru.ivanov.diplom.inventory_system.dto.admin.PermissionResponse;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.entity.Department;
import ru.ivanov.diplom.inventory_system.entity.Employee;
import ru.ivanov.diplom.inventory_system.entity.Permission;

import java.util.Comparator;
import java.util.List;


@Component
public class AdminUserMapper {
    public AdminUserResponse toResponse(AppUser user) {
        Employee employee = user.getEmployee();
        Department department = employee != null ? employee.getDepartment() : null;

        List<String> permissionCodes = user.getPermissions() == null
                ? List.of()
                : user.getPermissions()
                .stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                user.getMustChangePassword(),
                user.getPasswordExpiresAt(),
                user.getCreatedAt(),

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

    public PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription()
        );
    }

    public List<PermissionResponse> toPermissionResponses(List<Permission> permissions) {
        return permissions.stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionResponse)
                .toList();
    }
}

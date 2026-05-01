package ru.ivanov.diplom.inventory_system.dto.admin;

import jakarta.validation.constraints.NotNull;
import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;

public record SetUserRoleRequest(
        @NotNull(message = "Роль обязательна")
        UserRole role
) {
}

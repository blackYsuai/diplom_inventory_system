package ru.ivanov.diplom.inventory_system.dto.admin;


import java.util.List;

public record SetUserPermissionsRequest(
        List<String> permissionCodes
) {
}

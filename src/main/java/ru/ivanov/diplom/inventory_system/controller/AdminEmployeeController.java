package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.admin.employee.AdminEmployeeCreateRequest;
import ru.ivanov.diplom.inventory_system.dto.admin.employee.AdminEmployeeResponse;
import ru.ivanov.diplom.inventory_system.dto.admin.employee.AdminEmployeeUpdateRequest;
import ru.ivanov.diplom.inventory_system.service.AdminEmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_MANAGE')")
public class AdminEmployeeController {
    private final AdminEmployeeService adminEmployeeService;

    @GetMapping
    public List<AdminEmployeeResponse> getAllEmployees() {
        return adminEmployeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public AdminEmployeeResponse getEmployeeById(@PathVariable Long id) {
        return adminEmployeeService.getEmployeeById(id);
    }

    @PostMapping
    public AdminEmployeeResponse createEmployee(
            @Valid @RequestBody AdminEmployeeCreateRequest request
    ) {
        return adminEmployeeService.createEmployee(request);
    }

    @PutMapping("/{id}")
    public AdminEmployeeResponse updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody AdminEmployeeUpdateRequest request
    ) {
        return adminEmployeeService.updateEmployee(id, request);
    }
}

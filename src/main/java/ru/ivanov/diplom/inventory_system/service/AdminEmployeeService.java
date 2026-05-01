package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.admin.employee.AdminEmployeeCreateRequest;
import ru.ivanov.diplom.inventory_system.dto.admin.employee.AdminEmployeeResponse;
import ru.ivanov.diplom.inventory_system.dto.admin.employee.AdminEmployeeUpdateRequest;
import ru.ivanov.diplom.inventory_system.entity.Department;
import ru.ivanov.diplom.inventory_system.entity.Employee;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;
import ru.ivanov.diplom.inventory_system.repository.DepartmentRepository;
import ru.ivanov.diplom.inventory_system.repository.EmployeeRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<AdminEmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(AdminEmployeeResponse::fullName))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminEmployeeResponse getEmployeeById(Long id) {
        Employee employee = getEmployee(id);
        return toResponse(employee);
    }

    @Transactional
    public AdminEmployeeResponse createEmployee(AdminEmployeeCreateRequest request) {
        validateRequiredEmployeeData(request.lastName(), request.firstName());

        if (request.email() != null
                && !request.email().isBlank()
                && employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Сотрудник с email " + request.email() + " уже существует");
        }

        Department department = getDepartment(request.departmentId());

        Employee employee = Employee.builder()
                .lastName(request.lastName().trim())
                .firstName(request.firstName().trim())
                .middleName(normalizeBlank(request.middleName()))
                .position(normalizeBlank(request.position()))
                .phone(normalizeBlank(request.phone()))
                .email(normalizeBlank(request.email()))
                .department(department)
                .build();

        Employee saved = employeeRepository.save(employee);

        return toResponse(saved);
    }

    @Transactional
    public AdminEmployeeResponse updateEmployee(Long id, AdminEmployeeUpdateRequest request) {
        Employee employee = getEmployee(id);

        if (request.lastName() != null && !request.lastName().isBlank()) {
            employee.setLastName(request.lastName().trim());
        }

        if (request.firstName() != null && !request.firstName().isBlank()) {
            employee.setFirstName(request.firstName().trim());
        }

        if (request.middleName() != null) {
            employee.setMiddleName(normalizeBlank(request.middleName()));
        }

        if (request.position() != null) {
            employee.setPosition(normalizeBlank(request.position()));
        }

        if (request.phone() != null) {
            employee.setPhone(normalizeBlank(request.phone()));
        }

        if (request.email() != null) {
            String email = normalizeBlank(request.email());

            if (email != null
                    && !email.equals(employee.getEmail())
                    && employeeRepository.existsByEmail(email)) {
                throw new BadRequestException("Сотрудник с email " + email + " уже существует");
            }

            employee.setEmail(email);
        }

        if (request.departmentId() != null) {
            employee.setDepartment(getDepartment(request.departmentId()));
        }

        Employee saved = employeeRepository.save(employee);

        return toResponse(saved);
    }

    private AdminEmployeeResponse toResponse(Employee employee) {
        Department department = employee.getDepartment();

        return new AdminEmployeeResponse(
                employee.getId(),
                employee.getLastName(),
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getFullName(),
                employee.getPosition(),
                employee.getPhone(),
                employee.getEmail(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null,
                appUserRepository.existsByEmployeeId(employee.getId())
        );
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Сотрудник с id " + id + " не найден"
                ));
    }

    private Department getDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Подразделение с id " + departmentId + " не найдено"
                ));
    }

    private void validateRequiredEmployeeData(String lastName, String firstName) {
        if (lastName == null || lastName.isBlank()) {
            throw new BadRequestException("Фамилия сотрудника обязательна");
        }

        if (firstName == null || firstName.isBlank()) {
            throw new BadRequestException("Имя сотрудника обязательно");
        }
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

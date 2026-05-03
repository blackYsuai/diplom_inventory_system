package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.admin.*;
import ru.ivanov.diplom.inventory_system.entity.AppUser;
import ru.ivanov.diplom.inventory_system.entity.Department;
import ru.ivanov.diplom.inventory_system.entity.Employee;
import ru.ivanov.diplom.inventory_system.entity.Permission;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.mapper.AdminUserMapper;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;
import ru.ivanov.diplom.inventory_system.repository.DepartmentRepository;
import ru.ivanov.diplom.inventory_system.repository.EmployeeRepository;
import ru.ivanov.diplom.inventory_system.repository.PermissionRepository;
import ru.ivanov.diplom.inventory_system.util.TemporaryPasswordGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private static final int TEMP_PASSWORD_LIFETIME_DAYS = 3;

    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionRepository permissionRepository;

    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final AdminUserMapper adminUserMapper;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return appUserRepository.findAllWithDetails()
                .stream()
                .map(adminUserMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        AppUser user = getUserWithDetails(id);
        return adminUserMapper.toResponse(user);
    }

    @Transactional
    public AdminUserCreateResponse createUser(AdminUserCreateRequest request) {
        Employee employee = resolveOrCreateEmployee(request);

        if (appUserRepository.existsByEmployeeId(employee.getId())) {
            throw new BadRequestException("Для данного сотрудника уже создана учетная запись");
        }

        String username = resolveUsername(request, employee);

        if (appUserRepository.existsByUsername(username)) {
            throw new BadRequestException("Пользователь с логином " + username + " уже существует");
        }

        String temporaryPassword = temporaryPasswordGenerator.generate();

        Set<Permission> permissions = resolvePermissions(request.permissionCodes());

        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .role(request.role())
                .active(true)
                .mustChangePassword(true)
                .passwordExpiresAt(LocalDateTime.now().plusDays(TEMP_PASSWORD_LIFETIME_DAYS))
                .employee(employee)
                .permissions(permissions)
                .build();

        AppUser savedUser = appUserRepository.save(user);

        AppUser userWithDetails = getUserWithDetails(savedUser.getId());

        return new AdminUserCreateResponse(
                adminUserMapper.toResponse(userWithDetails),
                temporaryPassword
        );
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        AppUser user = getUserWithDetails(id);
        Employee employee = user.getEmployee();

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

            if (email != null && !email.equals(employee.getEmail()) && employeeRepository.existsByEmail(email)) {
                throw new BadRequestException("Сотрудник с email " + email + " уже существует");
            }

            employee.setEmail(email);
        }

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Подразделение с id " + request.departmentId() + " не найдено"
                    ));

            employee.setDepartment(department);
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.active() != null) {
            user.setActive(request.active());
        }

        if (request.permissionCodes() != null) {
            user.setPermissions(resolvePermissions(request.permissionCodes()));
        }

        employeeRepository.save(employee);
        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    @Transactional
    public AdminUserResponse activateUser(Long id) {
        AppUser user = getUserWithDetails(id);
        user.setActive(true);
        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    @Transactional
    public AdminUserResponse deactivateUser(Long id) {
        AppUser user = getUserWithDetails(id);
        user.setActive(false);
        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    @Transactional
    public AdminUserResponse changeRole(Long id, SetUserRoleRequest request) {
        AppUser user = getUserWithDetails(id);
        user.setRole(request.role());
        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    @Transactional
    public AdminUserResponse setPermissions(Long id, SetUserPermissionsRequest request) {
        AppUser user = getUserWithDetails(id);
        user.setPermissions(resolvePermissions(request.permissionCodes()));
        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    @Transactional
    public PasswordResetResponse resetPassword(Long id) {
        AppUser user = getUserWithDetails(id);

        String temporaryPassword = temporaryPasswordGenerator.generate();

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(TEMP_PASSWORD_LIFETIME_DAYS));

        appUserRepository.save(user);

        return new PasswordResetResponse(
                user.getId(),
                user.getUsername(),
                temporaryPassword
        );
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return adminUserMapper.toPermissionResponses(permissionRepository.findAll());
    }

    @Transactional
    public AdminUserResponse addPermissions(Long id, SetUserPermissionsRequest request) {
        AppUser user = getUserWithDetails(id);

        Set<Permission> permissionsToAdd = resolvePermissions(request.permissionCodes());

        user.getPermissions().addAll(permissionsToAdd);

        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    @Transactional
    public AdminUserResponse removePermissions(Long id, SetUserPermissionsRequest request) {
        AppUser user = getUserWithDetails(id);

        if (request.permissionCodes() == null || request.permissionCodes().isEmpty()) {
            throw new BadRequestException("Необходимо указать права для удаления");
        }

        Set<String> codesToRemove = request.permissionCodes()
                .stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());

        user.getPermissions().removeIf(permission ->
                codesToRemove.contains(permission.getCode())
        );

        appUserRepository.save(user);

        return adminUserMapper.toResponse(getUserWithDetails(id));
    }

    private Employee resolveOrCreateEmployee(AdminUserCreateRequest request) {
        if (request.employeeId() != null) {
            return employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Сотрудник с id " + request.employeeId() + " не найден"
                    ));
        }

        validateNewEmployeeData(request);

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Подразделение с id " + request.departmentId() + " не найдено"
                ));

        if (request.email() != null
                && !request.email().isBlank()
                && employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Сотрудник с email " + request.email() + " уже существует");
        }

        Employee employee = Employee.builder()
                .lastName(request.lastName())
                .firstName(request.firstName())
                .middleName(request.middleName())
                .position(request.position())
                .phone(request.phone())
                .email(request.email())
                .department(department)
                .build();

        return employeeRepository.save(employee);
    }

    private void validateNewEmployeeData(AdminUserCreateRequest request) {
        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new BadRequestException("Фамилия сотрудника обязательна");
        }

        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new BadRequestException("Имя сотрудника обязательно");
        }

        if (request.departmentId() == null) {
            throw new BadRequestException("Подразделение сотрудника обязательно");
        }
    }

    private String resolveUsername(AdminUserCreateRequest request, Employee employee) {
        if (request.username() != null && !request.username().isBlank()) {
            return request.username().trim();
        }

        String base = "user" + employee.getId();

        if (!appUserRepository.existsByUsername(base)) {
            return base;
        }

        return base + "_" + System.currentTimeMillis();
    }

    private Set<Permission> resolvePermissions(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return new HashSet<>();
        }

        List<String> normalizedCodes = permissionCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();

        List<Permission> permissions = permissionRepository.findAllByCodeIn(normalizedCodes);

        Set<String> foundCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        List<String> unknownCodes = normalizedCodes.stream()
                .filter(code -> !foundCodes.contains(code))
                .toList();

        if (!unknownCodes.isEmpty()) {
            throw new BadRequestException("Неизвестные права доступа: " + unknownCodes);
        }

        return new HashSet<>(permissions);
    }

    private AppUser getUserWithDetails(Long id) {
        return appUserRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь с id " + id + " не найден"
                ));
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.PermissionRequest;
import ru.nsu.tois.dto.request.RoleRequest;
import ru.nsu.tois.dto.request.UserAccountRequest;
import ru.nsu.tois.dto.response.PermissionResponse;
import ru.nsu.tois.dto.response.RoleResponse;
import ru.nsu.tois.dto.response.UserAccountResponse;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.user.Permission;
import ru.nsu.tois.entity.user.Role;
import ru.nsu.tois.entity.user.UserAccount;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public PermissionResponse toDto(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getCode());
    }

    public Permission toEntity(PermissionRequest request) {
        Permission permission = new Permission();
        permission.setCode(request.code());
        return permission;
    }

    public RoleResponse toDto(Role role) {
        Set<Integer> permissionIds = role.getPermissions() == null
                ? Set.of()
                : role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());

        return new RoleResponse(role.getId(), role.getName(), permissionIds);
    }

    public Role toEntity(RoleRequest request) {
        Role role = new Role();
        role.setName(request.name());
        role.setPermissions(request.permissionIds() == null
                ? new HashSet<>()
                : request.permissionIds().stream()
                .map(this::permissionReference)
                .collect(Collectors.toCollection(HashSet::new)));
        return role;
    }

    public UserAccountResponse toDto(UserAccount userAccount) {
        return new UserAccountResponse(
                userAccount.getId(),
                userAccount.getLogin(),
                userAccount.getRole() == null ? null : userAccount.getRole().getId(),
                userAccount.getEmployee() == null ? null : userAccount.getEmployee().getId(),
                userAccount.getIsActive(),
                userAccount.getCreatedAt()
        );
    }

    public UserAccount toEntity(UserAccountRequest request) {
        UserAccount userAccount = new UserAccount();
        userAccount.setLogin(request.login());
        userAccount.setPasswordHash(request.password());
        userAccount.setRole(roleReference(request.roleId()));
        userAccount.setEmployee(employeeReference(request.employeeId()));
        userAccount.setIsActive(request.isActive());
        userAccount.setCreatedAt(request.createdAt());
        return userAccount;
    }

    private Permission permissionReference(Integer id) {
        Permission permission = new Permission();
        permission.setId(id);
        return permission;
    }

    private Role roleReference(Integer id) {
        Role role = new Role();
        role.setId(id);
        return role;
    }

    private Employee employeeReference(Integer id) {
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }
}

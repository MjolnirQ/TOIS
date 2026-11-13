package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.PermissionResponse;
import ru.nsu.tois.dto.response.RoleResponse;
import ru.nsu.tois.dto.response.UserAccountResponse;
import ru.nsu.tois.entity.user.Permission;
import ru.nsu.tois.entity.user.Role;
import ru.nsu.tois.entity.user.UserAccount;
import ru.nsu.tois.mapper.UserMapper;
import ru.nsu.tois.repository.employees.EmployeeRepository;
import ru.nsu.tois.repository.user.PermissionRepository;
import ru.nsu.tois.repository.user.RoleRepository;
import ru.nsu.tois.repository.user.UserAccountRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PagedResponse<PermissionResponse> findAllPermissions(Integer page, Integer size) {
        return PaginationUtils.toResponse(permissionRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(userMapper::toDto));
    }

    @Transactional(readOnly = true)
    public PermissionResponse findPermissionById(Integer id) {
        return userMapper.toDto(requirePermission(id));
    }

    public PermissionResponse createPermission(PermissionRequest request) {
        return userMapper.toDto(permissionRepository.save(userMapper.toEntity(request)));
    }

    public PermissionResponse updatePermission(Integer id, PermissionRequest request) {
        requirePermission(id);
        Permission permission = userMapper.toEntity(request);
        permission.setId(id);
        return userMapper.toDto(permissionRepository.save(permission));
    }

    public void deletePermission(Integer id) {
        requirePermission(id);
        permissionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RoleResponse> findAllRoles(Integer page, Integer size) {
        return PaginationUtils.toResponse(roleRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(userMapper::toDto));
    }

    @Transactional(readOnly = true)
    public RoleResponse findRoleById(Integer id) {
        return userMapper.toDto(requireRole(id));
    }

    public RoleResponse createRole(RoleRequest request) {
        validatePermissionIds(request);
        return userMapper.toDto(roleRepository.save(userMapper.toEntity(request)));
    }

    public RoleResponse updateRole(Integer id, RoleRequest request) {
        requireRole(id);
        validatePermissionIds(request);
        Role role = userMapper.toEntity(request);
        role.setId(id);
        return userMapper.toDto(roleRepository.save(role));
    }

    public void deleteRole(Integer id) {
        requireRole(id);
        if (userAccountRepository.existsByRole_Id(id)) {
            throw new IllegalStateException("Cannot delete an employee referenced by trade documents.");
        }
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Set<PermissionResponse> findRolePermissions(Integer roleId) {
        Role role = requireRole(roleId);
        if (role.getPermissions() == null) {
            return Set.of();
        }

        return role.getPermissions().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toSet());
    }

    public RoleResponse addPermissionToRole(Integer roleId, Integer permissionId) {
        Role role = requireRole(roleId);
        Permission permission = requirePermission(permissionId);

        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        role.getPermissions().add(permission);
        return userMapper.toDto(roleRepository.save(role));
    }

    public RoleResponse removePermissionFromRole(Integer roleId, Integer permissionId) {
        Role role = requireRole(roleId);
        requirePermission(permissionId);

        if (role.getPermissions() != null) {
            role.getPermissions().removeIf(permission -> permissionId.equals(permission.getId()));
        }

        return userMapper.toDto(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserAccountResponse> findAllUserAccounts(Integer page, Integer size) {
        return PaginationUtils.toResponse(userAccountRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(userMapper::toDto));
    }

    @Transactional(readOnly = true)
    public UserAccountResponse findUserAccountById(Integer id) {
        return userMapper.toDto(requireUserAccount(id));
    }

    @Transactional(readOnly = true)
    public UserAccountResponse findUserAccountByLogin(String login) { return userMapper.toDto(requireUserAccount(login)); }

    public UserAccountResponse createUserAccount(UserAccountRequest request) {
        validateUserAccountReferences(request);
        UserAccount userAccount = userMapper.toEntity(request);
        applyUserAccountDefaults(userAccount);
        encodePassword(userAccount);
        return userMapper.toDto(userAccountRepository.save(userAccount));
    }

    public UserAccountResponse updateUserAccount(Integer id, UserAccountRequest request) {
        UserAccount userAccount = requireUserAccount(id);

        validateUserAccountReferences(request);

        userAccount.setLogin(request.login().trim());
        userAccount.setRole(requireRole(request.roleId()));
        userAccount.setEmployee(
                employeeRepository.findById(request.employeeId())
                        .orElseThrow(() -> notFound("Employee", request.employeeId()))
        );

        if (request.isActive() != null) {
            userAccount.setIsActive(request.isActive());
        }

        if (request.password() != null && !request.password().isBlank()) {
            userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return userMapper.toDto(userAccountRepository.save(userAccount));
    }

    public void deleteUserAccount(Integer id) {
        requireUserAccount(id);
        userAccountRepository.deleteById(id);
    }

    private void validatePermissionIds(RoleRequest request) {
        if (request.permissionIds() == null) {
            return;
        }
        request.permissionIds().forEach(this::requirePermission);
    }

    private void validateUserAccountReferences(UserAccountRequest request) {
        requireRole(request.roleId());
        employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> notFound("Employee", request.employeeId()));
    }

    private void applyUserAccountDefaults(UserAccount userAccount) {
        if (userAccount.getIsActive() == null) {
            userAccount.setIsActive(true);
        }
        if (userAccount.getCreatedAt() == null) {
            userAccount.setCreatedAt(LocalDateTime.now());
        }
    }

    private void encodePassword(UserAccount userAccount) {
        userAccount.setPasswordHash(passwordEncoder.encode(userAccount.getPasswordHash()));
    }

    private Permission requirePermission(Integer id) {
        return permissionRepository.findById(id).orElseThrow(() -> notFound("Permission", id));
    }

    private Role requireRole(Integer id) {
        return roleRepository.findById(id).orElseThrow(() -> notFound("Role", id));
    }

    public UserAccount requireUserAccount(Integer id) {
        return userAccountRepository.findById(id).orElseThrow(() -> notFound("UserAccount", id));
    }

    public UserAccount requireUserAccount(String login) {
        return userAccountRepository.findByLogin(login).orElseThrow(() -> notFound("UserAccount", login));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }

    private EntityNotFoundException notFound(String entityName, String details) {
        return new EntityNotFoundException(entityName + " not found: " + details);
    }

    public UserAccountResponse updateUserAccountCredentials(
            Integer id,
            UserAccountCredentialsUpdateRequest request
    ) {
        UserAccount userAccount = requireUserAccount(id);

        if (request.login() != null && !request.login().isBlank()) {
            userAccount.setLogin(request.login().trim());
        }

        if (request.password() != null && !request.password().isBlank()) {
            userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return userMapper.toDto(userAccountRepository.save(userAccount));
    }

    public UserAccountResponse updateUserAccountActive(Integer id, UserAccountActiveUpdateRequest request) {
        UserAccount userAccount = requireUserAccount(id);
        userAccount.setIsActive(request.isActive());
        return userMapper.toDto(userAccountRepository.save(userAccount));
    }
}

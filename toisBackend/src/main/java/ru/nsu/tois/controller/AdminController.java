package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.PermissionResponse;
import ru.nsu.tois.dto.response.RoleResponse;
import ru.nsu.tois.dto.response.UserAccountResponse;
import ru.nsu.tois.service.UserService;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_READ + "')")
    public PagedResponse<RoleResponse> findAllRoles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return userService.findAllRoles(page, size);
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_READ + "')")
    public RoleResponse findRoleById(@PathVariable Integer id) {
        return userService.findRoleById(id);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_WRITE + "')")
    public RoleResponse createRole(@Valid @RequestBody RoleRequest roleRequest) {
        return userService.createRole(roleRequest);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_WRITE + "')")
    public RoleResponse updateRole(@PathVariable Integer id,
            @Valid @RequestBody RoleRequest roleRequest) {
        return userService.updateRole(id, roleRequest);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_WRITE + "')")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        userService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_PERMISSIONS_READ + "')")
    public PagedResponse<PermissionResponse> findAllPermissions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return userService.findAllPermissions(page, size);
    }

    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_PERMISSIONS_READ + "')")
    public PermissionResponse findPermissionById(@PathVariable Integer id) {
        return userService.findPermissionById(id);
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_PERMISSIONS_WRITE + "')")
    public PermissionResponse createPermission(@Valid @RequestBody PermissionRequest permissionRequest) {
        return userService.createPermission(permissionRequest);
    }

    @PutMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_PERMISSIONS_WRITE + "')")
    public PermissionResponse updatePermission(
            @PathVariable Integer id,
            @Valid @RequestBody PermissionRequest permissionRequest
    ) {
        return userService.updatePermission(id, permissionRequest);
    }

    @DeleteMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_PERMISSIONS_WRITE + "')")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer id) {
        userService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_READ + "')")
    public Set<PermissionResponse> findRolePermissions(@PathVariable Integer roleId) {
        return userService.findRolePermissions(roleId);
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_WRITE + "')")
    public RoleResponse addPermissionToRole(
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId
    ) {
        return userService.addPermissionToRole(roleId, permissionId);
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_ROLES_WRITE + "')")
    public ResponseEntity<Void> removePermissionFromRole(
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId
    ) {
        userService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user-accounts")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_READ + "')")
    public PagedResponse<UserAccountResponse> findAllUserAccounts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return userService.findAllUserAccounts(page, size);
    }

    @GetMapping("/user-accounts/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_READ + "')")
    public UserAccountResponse findUserAccountById(@PathVariable Integer id) {
        return userService.findUserAccountById(id);
    }

    @PostMapping("/user-accounts")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_WRITE + "')")
    public UserAccountResponse createUserAccount(@Valid @RequestBody UserAccountRequest request) {
        return userService.createUserAccount(request);
    }

    @PutMapping("/user-accounts/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_WRITE + "')")
    public UserAccountResponse updateUserAccount(
            @PathVariable Integer id,
            @Valid @RequestBody UserAccountRequest request
    ) {
        return userService.updateUserAccount(id, request);
    }

    @DeleteMapping("/user-accounts/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_WRITE + "')")
    public ResponseEntity<Void> deleteUserAccount(@PathVariable Integer id) {
        userService.deleteUserAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user-accounts/{id}/credentials")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_WRITE + "')")
    public UserAccountResponse updateUserAccountCredentials(
            @PathVariable Integer id,
            @RequestBody UserAccountCredentialsUpdateRequest request
    ) {
        return userService.updateUserAccountCredentials(id, request);
    }

    @PatchMapping("/user-accounts/{id}/active")
    @PreAuthorize("hasAuthority('" + PermissionCodes.ADMIN_USERS_WRITE + "')")
    public UserAccountResponse updateUserAccountActive(
            @PathVariable Integer id,
            @Valid @RequestBody UserAccountActiveUpdateRequest request
    ) {
        return userService.updateUserAccountActive(id, request);
    }
}



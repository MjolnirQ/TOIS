package ru.nsu.tois.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.nsu.tois.client.api.dto.request.*;
import ru.nsu.tois.client.api.dto.response.PermissionResponse;
import ru.nsu.tois.client.api.dto.response.RoleResponse;
import ru.nsu.tois.client.api.dto.response.UserAccountResponse;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminClient extends BaseHttpClient {
    public AdminClient(
            String baseUrl,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        super(baseUrl, httpClient, objectMapper);
    }

    public List<RoleResponse> findAllRoles(String token) {
        return getRows("/admin/roles?size=500", token, RoleResponse.class);
    }

    public RoleResponse createRole(String token, String name, Set<Integer> permissionIds) {
        return post("/admin/roles", token, new RoleRequest(name, permissionIds), RoleResponse.class);
    }

    public RoleResponse updateRole(String token, Integer roleId, String name, Set<Integer> permissionIds) {
        return put("/admin/roles/" + roleId, token, new RoleRequest(name, permissionIds), RoleResponse.class);
    }

    public void deleteRole(String token, Integer roleId) {
        delete("/admin/roles/" + roleId, token);
    }

    public List<PermissionResponse> findAllPermissions(String token) {
        return getRows("/admin/permissions?size=500", token, PermissionResponse.class);
    }

    public PermissionResponse createPermission(String token, String code) {
        return post("/admin/permissions", token, new PermissionRequest(code), PermissionResponse.class);
    }

    public PermissionResponse updatePermission(String token, Integer permissionId, String code) {
        return put("/admin/permissions/" + permissionId, token, new PermissionRequest(code), PermissionResponse.class);
    }

    public void deletePermission(String token, Integer permissionId) {
        delete("/admin/permissions/" + permissionId, token);
    }

    public List<UserAccountResponse> findAllUserAccounts(String token) {
        return getRows("/admin/user-accounts?size=500", token, UserAccountResponse.class);
    }

    public UserAccountResponse createUserAccount(
            String token,
            String login,
            String password,
            Integer roleId,
            Integer employeeId,
            Boolean isActive
    ) {
        return post(
                "/admin/user-accounts",
                token,
                new UserAccountRequest(login, password, roleId, employeeId, isActive, null),
                UserAccountResponse.class
        );
    }

    public UserAccountResponse updateUserAccount(
            String token,
            Integer userAccountId,
            String login,
            String password,
            Integer roleId,
            Integer employeeId,
            Boolean isActive
    ) {
        return put(
                "/admin/user-accounts/" + userAccountId,
                token,
                new UserAccountRequest(login, password, roleId, employeeId, isActive, null),
                UserAccountResponse.class
        );
    }

    public void deleteUserAccount(String token, Integer userAccountId) {
        delete("/admin/user-accounts/" + userAccountId, token);
    }

    private <T> List<T> getRows(String path, String token, Class<T> itemType) {
        Object response = get(path, token, new TypeReference<Object>() {});
        Object rows = response;
        if (response instanceof Map<?, ?> map) {
            rows = map.get("rows");
        }
        if (!(rows instanceof List<?> list)) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        for (Object row : list) {
            result.add(objectMapper().convertValue(row, itemType));
        }
        return result;
    }

    public UserAccountResponse updateUserAccountCredentials(
            String token,
            Integer userAccountId,
            String login,
            String password
    ) {
        return patch(
                "/admin/user-accounts/" + userAccountId + "/credentials",
                token,
                new UserAccountCredentialsUpdateRequest(login, password),
                UserAccountResponse.class
        );
    }

    public UserAccountResponse updateUserAccountActive(
            String token,
            Integer userAccountId,
            Boolean isActive
    ) {
        return patch(
                "/admin/user-accounts/" + userAccountId + "/active",
                token,
                new UserAccountActiveUpdateRequest(isActive),
                UserAccountResponse.class
        );
    }


}

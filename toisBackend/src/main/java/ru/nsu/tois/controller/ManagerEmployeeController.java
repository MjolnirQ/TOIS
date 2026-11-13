package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.EmployeeRequest;
import ru.nsu.tois.dto.response.EmployeeResponse;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.service.EmployeeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/employees")
public class ManagerEmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_EMPLOYEES_READ + "')")
    public PagedResponse<EmployeeResponse> findAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return employeeService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_EMPLOYEES_READ + "')")
    public EmployeeResponse findById(@PathVariable Integer id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_EMPLOYEES_WRITE + "')")
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_EMPLOYEES_WRITE + "')")
    public EmployeeResponse update(@PathVariable Integer id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_EMPLOYEES_WRITE + "')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

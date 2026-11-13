package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.SectionManagerAssignmentRequest;
import ru.nsu.tois.dto.request.SellerAssignmentRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SectionManagerAssignmentResponse;
import ru.nsu.tois.dto.response.SellerAssignmentResponse;
import ru.nsu.tois.service.AssignmentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerAssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping("/seller-assignments")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_READ + "')")
    public PagedResponse<SellerAssignmentResponse> findAllSellerAssignments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return assignmentService.findAllSellerAssignments(page, size);
    }

    @GetMapping("/seller-assignments/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_READ + "')")
    public SellerAssignmentResponse findSellerAssignmentById(@PathVariable Integer id) {
        return assignmentService.findSellerAssignmentById(id);
    }

    @PostMapping("/seller-assignments")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_WRITE + "')")
    public SellerAssignmentResponse createSellerAssignment(@Valid @RequestBody SellerAssignmentRequest request) {
        return assignmentService.createSellerAssignment(request);
    }

    @PutMapping("/seller-assignments/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_WRITE + "')")
    public SellerAssignmentResponse updateSellerAssignment(
            @PathVariable Integer id,
            @Valid @RequestBody SellerAssignmentRequest request
    ) {
        return assignmentService.updateSellerAssignment(id, request);
    }

    @DeleteMapping("/seller-assignments/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_WRITE + "')")
    public ResponseEntity<Void> deleteSellerAssignment(@PathVariable Integer id) {
        assignmentService.deleteSellerAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/section-manager-assignments")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_READ + "')")
    public PagedResponse<SectionManagerAssignmentResponse> findAllSectionManagerAssignments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return assignmentService.findAllSectionManagerAssignments(page, size);
    }

    @GetMapping("/section-manager-assignments/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_READ + "')")
    public SectionManagerAssignmentResponse findSectionManagerAssignmentById(@PathVariable Integer id) {
        return assignmentService.findSectionManagerAssignmentById(id);
    }

    @PostMapping("/section-manager-assignments")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_WRITE + "')")
    public SectionManagerAssignmentResponse createSectionManagerAssignment(
            @Valid @RequestBody SectionManagerAssignmentRequest request
    ) {
        return assignmentService.createSectionManagerAssignment(request);
    }

    @PutMapping("/section-manager-assignments/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_WRITE + "')")
    public SectionManagerAssignmentResponse updateSectionManagerAssignment(
            @PathVariable Integer id,
            @Valid @RequestBody SectionManagerAssignmentRequest request
    ) {
        return assignmentService.updateSectionManagerAssignment(id, request);
    }

    @DeleteMapping("/section-manager-assignments/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_ASSIGNMENTS_WRITE + "')")
    public ResponseEntity<Void> deleteSectionManagerAssignment(@PathVariable Integer id) {
        assignmentService.deleteSectionManagerAssignment(id);
        return ResponseEntity.noContent().build();
    }
}

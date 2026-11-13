package ru.nsu.tois.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.request.HallRequest;
import ru.nsu.tois.dto.request.SectionRequest;
import ru.nsu.tois.dto.request.TradePointRequest;
import ru.nsu.tois.dto.response.HallResponse;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SectionResponse;
import ru.nsu.tois.dto.response.TradePointResponse;
import ru.nsu.tois.service.TradePointService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerTradePointController {
    private final TradePointService tradePointService;

    @GetMapping("/trade-points")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_READ + "')")
    public PagedResponse<TradePointResponse> findAllTradePoints(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return tradePointService.findAllTradePoints(page, size);
    }

    @GetMapping("/trade-points/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_READ + "')")
    public TradePointResponse findTradePointById(@PathVariable Integer id) {
        return tradePointService.findTradePointById(id);
    }

    @PostMapping("/trade-points")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public TradePointResponse createTradePoint(@Valid @RequestBody TradePointRequest request) {
        return tradePointService.createTradePoint(request);
    }

    @PutMapping("/trade-points/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public TradePointResponse updateTradePoint(@PathVariable Integer id, @Valid @RequestBody TradePointRequest request) {
        return tradePointService.updateTradePoint(id, request);
    }

    @DeleteMapping("/trade-points/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public ResponseEntity<Void> deleteTradePoint(@PathVariable Integer id) {
        tradePointService.deleteTradePoint(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/halls")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_READ + "')")
    public PagedResponse<HallResponse> findAllHalls(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return tradePointService.findAllHalls(page, size);
    }

    @GetMapping("/halls/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_READ + "')")
    public HallResponse findHallById(@PathVariable Integer id) {
        return tradePointService.findHallById(id);
    }

    @PostMapping("/halls")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public HallResponse createHall(@Valid @RequestBody HallRequest request) {
        return tradePointService.createHall(request);
    }

    @PutMapping("/halls/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public HallResponse updateHall(@PathVariable Integer id, @Valid @RequestBody HallRequest request) {
        return tradePointService.updateHall(id, request);
    }

    @DeleteMapping("/halls/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public ResponseEntity<Void> deleteHall(@PathVariable Integer id) {
        tradePointService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sections")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_READ + "')")
    public PagedResponse<SectionResponse> findAllSections(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return tradePointService.findAllSections(page, size);
    }

    @GetMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_READ + "')")
    public SectionResponse findSectionById(@PathVariable Integer id) {
        return tradePointService.findSectionById(id);
    }

    @PostMapping("/sections")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public SectionResponse createSection(@Valid @RequestBody SectionRequest request) {
        return tradePointService.createSection(request);
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public SectionResponse updateSection(@PathVariable Integer id, @Valid @RequestBody SectionRequest request) {
        return tradePointService.updateSection(id, request);
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_TRADE_POINTS_WRITE + "')")
    public ResponseEntity<Void> deleteSection(@PathVariable Integer id) {
        tradePointService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}

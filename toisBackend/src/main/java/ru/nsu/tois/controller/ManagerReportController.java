package ru.nsu.tois.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.nsu.tois.authentication.PermissionCodes;
import ru.nsu.tois.dto.response.CountedReportResponse;
import ru.nsu.tois.service.ReportService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/reports")
public class ManagerReportController {
    private final ReportService reportService;

    @GetMapping("/suppliers-for-product")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public CountedReportResponse suppliersForProduct(@RequestParam Integer productId) {
        return reportService.suppliersForProduct(productId);
    }

    @GetMapping("/suppliers-delivered-product")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public CountedReportResponse suppliersDeliveredProduct(
            @RequestParam Integer productId,
            @RequestParam Integer minQuantity,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return reportService.suppliersDeliveredProduct(productId, minQuantity, from, to);
    }

    @GetMapping("/customers-bought-product")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public CountedReportResponse customersBoughtProduct(
            @RequestParam Integer productId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.customersBoughtProduct(productId, from, to);
    }

    @GetMapping("/customers-bought-product-at-least")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public CountedReportResponse customersBoughtProductAtLeast(
            @RequestParam Integer productId,
            @RequestParam Integer minQuantity
    ) {
        return reportService.customersBoughtProductAtLeast(productId, minQuantity);
    }

    @GetMapping("/stock-prices")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> productStockAndPrices(
            @RequestParam Integer productId,
            @RequestParam(required = false) String tradePointType,
            @RequestParam(required = false) Integer tradePointId
    ) {
        if (tradePointType != null && tradePointId != null) {
            throw new IllegalArgumentException("Specify either a trade point or a trade point type");
        }
        if (tradePointId != null) {
            return reportService.productStockAndPricesAtTradePoint(productId, tradePointId);
        }
        if (tradePointType != null) {
            return reportService.productStockAndPricesByType(productId, tradePointType);
        }
        return reportService.productStockAndPrices(productId);
    }

    @GetMapping("/sales-per-seller")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> salesPerSeller(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String tradePointType
    ) {
        return reportService.salesPerSeller(from, to, tradePointType);
    }

    @GetMapping("/seller-output")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> sellerOutput(
            @RequestParam Integer sellerId,
            @RequestParam Integer tradePointId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.sellerOutput(sellerId, tradePointId, from, to);
    }

    @GetMapping("/product-sales-volume")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> productSalesVolume(
            @RequestParam Integer productId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String tradePointType,
            @RequestParam(required = false) Integer tradePointId
    ) {
        return reportService.productSalesVolume(productId, from, to, tradePointType, tradePointId);
    }

    @GetMapping("/seller-salaries")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> sellerSalaries(
            @RequestParam(required = false) String tradePointType,
            @RequestParam(required = false) Integer tradePointId
    ) {
        return reportService.sellerSalaries(tradePointType, tradePointId);
    }

    @GetMapping("/supplier-deliveries-for-product")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> supplierDeliveriesForProduct(
            @RequestParam Integer supplierId,
            @RequestParam Integer productId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return reportService.supplierDeliveriesForProduct(supplierId, productId, from, to);
    }

    @GetMapping("/deliveries-by-order-number")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> deliveriesByOrderNumber(@RequestParam String orderNumber) {
        return reportService.deliveriesByOrderNumber(orderNumber);
    }

    @GetMapping("/sales-to-area")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> salesToTradeAreaRatio(
            @RequestParam(required = false) String tradePointType,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.salesToTradeAreaRatio(tradePointType, from, to);
    }

    @GetMapping("/sales-to-halls")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> salesToHallCountRatio(
            @RequestParam(required = false) String tradePointType,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.salesToHallCountRatio(tradePointType, from, to);
    }

    @GetMapping("/sales-to-counters")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> salesToCounterCountRatio(
            @RequestParam(required = false) String tradePointType,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.salesToCounterCountRatio(tradePointType, from, to);
    }

    @GetMapping("/profitability")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> tradePointProfitability(
            @RequestParam Integer tradePointId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.tradePointProfitability(tradePointId, from, to);
    }

    @GetMapping("/product-customers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> productCustomers(
            @RequestParam Integer productId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String tradePointType,
            @RequestParam(required = false) Integer tradePointId
    ) {
        return reportService.productCustomers(productId, from, to, tradePointType, tradePointId);
    }

    @GetMapping("/turnover")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> turnover(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String tradePointType,
            @RequestParam(required = false) Integer tradePointId
    ) {
        return reportService.turnover(from, to, tradePointType, tradePointId);
    }

    @GetMapping("/active-customers")
    @PreAuthorize("hasAuthority('" + PermissionCodes.MANAGER_REPORTS_READ + "')")
    public List<Map<String, Object>> activeCustomers(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String tradePointType,
            @RequestParam(required = false) Integer tradePointId
    ) {
        return reportService.activeCustomers(from, to, tradePointType, tradePointId);
    }
}

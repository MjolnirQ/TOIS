package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.*;
import ru.nsu.tois.dto.response.*;
import ru.nsu.tois.entity.tradePoints.*;

@Component
public class TradePointMapper {
    public TradePointResponse toDto(TradePoint tradePoint) {
        return new TradePointResponse(
                tradePoint.getId(),
                tradePoint.getName(),
                tradePoint.getAddress(),
                tradePoint.getType(),
                tradePoint.getTradeArea(),
                tradePoint.getUtilityPayments(),
                tradePoint.getCounterCount(),
                tradePoint.getRentPayment()
        );
    }

    public TradePoint toEntity(TradePointRequest request) {
        TradePoint tradePoint = new TradePoint();
        tradePoint.setName(request.name());
        tradePoint.setAddress(request.address());
        tradePoint.setType(request.type());
        tradePoint.setTradeArea(request.tradeArea());
        tradePoint.setUtilityPayments(request.utilityPayments());
        tradePoint.setCounterCount(request.counterCount());
        tradePoint.setRentPayment(request.rentPayment());
        return tradePoint;
    }

    public DepartmentStoreResponse toDto(DepartmentStore departmentStore) {
        return new DepartmentStoreResponse(departmentStore.getId());
    }

    public ShopResponse toDto(Shop shop) {
        return new ShopResponse(shop.getId());
    }

    public StallResponse toDto(Stall stall) {
        return new StallResponse(stall.getId());
    }

    public KioskResponse toDto(Kiosk kiosk) {
        return new KioskResponse(kiosk.getId());
    }

    public HallResponse toDto(Hall hall) {
        return new HallResponse(
                hall.getId(),
                hall.getTradePoint() == null ? null : hall.getTradePoint().getId(),
                hall.getFloor(),
                hall.getArea()
        );
    }

    public Hall toEntity(HallRequest request) {
        Hall hall = new Hall();
        hall.setTradePoint(tradePointReference(request.tradePointId()));
        hall.setFloor(request.floor());
        hall.setArea(request.area());
        return hall;
    }

    public SectionResponse toDto(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getDepartmentStore() == null ? null : section.getDepartmentStore().getId()
        );
    }

    public Section toEntity(SectionRequest request) {
        Section section = new Section();
        section.setDepartmentStore(departmentStoreReference(request.departmentStoreId()));
        return section;
    }

    private TradePoint tradePointReference(Integer id) {
        TradePoint tradePoint = new TradePoint();
        tradePoint.setId(id);
        return tradePoint;
    }

    private DepartmentStore departmentStoreReference(Integer id) {
        DepartmentStore departmentStore = new DepartmentStore();
        departmentStore.setId(id);
        return departmentStore;
    }
}

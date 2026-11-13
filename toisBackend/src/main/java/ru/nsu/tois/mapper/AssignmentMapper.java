package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.SectionManagerAssignmentRequest;
import ru.nsu.tois.dto.request.SellerAssignmentRequest;
import ru.nsu.tois.dto.response.SectionManagerAssignmentResponse;
import ru.nsu.tois.dto.response.SellerAssignmentResponse;
import ru.nsu.tois.entity.assignments.SectionManagerAssignment;
import ru.nsu.tois.entity.assignments.SellerAssignment;
import ru.nsu.tois.entity.employees.SectionManager;
import ru.nsu.tois.entity.employees.Seller;
import ru.nsu.tois.entity.tradePoints.Hall;
import ru.nsu.tois.entity.tradePoints.Section;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Component
public class AssignmentMapper {
    public SectionManagerAssignmentResponse toDto(SectionManagerAssignment assignment) {
        return new SectionManagerAssignmentResponse(
                assignment.getId(),
                assignment.getManager() == null ? null : assignment.getManager().getId(),
                assignment.getTradePoint() == null ? null : assignment.getTradePoint().getId(),
                assignment.getSection() == null ? null : assignment.getSection().getId()
        );
    }

    public SectionManagerAssignment toEntity(SectionManagerAssignmentRequest request) {
        SectionManagerAssignment assignment = new SectionManagerAssignment();
        assignment.setManager(sectionManagerReference(request.managerId()));
        assignment.setTradePoint(tradePointReference(request.tradePointId()));
        assignment.setSection(sectionReference(request.sectionId()));
        return assignment;
    }

    public SellerAssignmentResponse toDto(SellerAssignment assignment) {
        return new SellerAssignmentResponse(
                assignment.getId(),
                assignment.getSeller() == null ? null : assignment.getSeller().getId(),
                assignment.getTradePoint() == null ? null : assignment.getTradePoint().getId(),
                assignment.getHall() == null ? null : assignment.getHall().getId()
        );
    }

    public SellerAssignment toEntity(SellerAssignmentRequest request) {
        SellerAssignment assignment = new SellerAssignment();
        assignment.setSeller(sellerReference(request.sellerId()));
        assignment.setTradePoint(tradePointReference(request.tradePointId()));
        assignment.setHall(request.hallId() == null ? null : hallReference(request.hallId()));
        return assignment;
    }

    private SectionManager sectionManagerReference(Integer id) {
        SectionManager sectionManager = new SectionManager();
        sectionManager.setId(id);
        return sectionManager;
    }

    private Seller sellerReference(Integer id) {
        Seller seller = new Seller();
        seller.setId(id);
        return seller;
    }

    private TradePoint tradePointReference(Integer id) {
        TradePoint tradePoint = new TradePoint();
        tradePoint.setId(id);
        return tradePoint;
    }

    private Section sectionReference(Integer id) {
        Section section = new Section();
        section.setId(id);
        return section;
    }

    private Hall hallReference(Integer id) {
        Hall hall = new Hall();
        hall.setId(id);
        return hall;
    }
}

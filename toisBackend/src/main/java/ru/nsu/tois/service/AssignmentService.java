package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.SectionManagerAssignmentRequest;
import ru.nsu.tois.dto.request.SellerAssignmentRequest;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SectionManagerAssignmentResponse;
import ru.nsu.tois.dto.response.SellerAssignmentResponse;
import ru.nsu.tois.entity.assignments.SectionManagerAssignment;
import ru.nsu.tois.entity.assignments.SellerAssignment;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.EmployeePosition;
import ru.nsu.tois.entity.tradePoints.Section;
import ru.nsu.tois.mapper.AssignmentMapper;
import ru.nsu.tois.repository.assignments.SectionManagerAssignmentRepository;
import ru.nsu.tois.repository.assignments.SellerAssignmentRepository;
import ru.nsu.tois.repository.employees.EmployeeRepository;
import ru.nsu.tois.repository.tradePoints.HallRepository;
import ru.nsu.tois.repository.tradePoints.SectionRepository;
import ru.nsu.tois.repository.tradePoints.TradePointRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {
    private final SectionManagerAssignmentRepository sectionManagerAssignmentRepository;
    private final SellerAssignmentRepository sellerAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final TradePointRepository tradePointRepository;
    private final SectionRepository sectionRepository;
    private final HallRepository hallRepository;
    private final AssignmentMapper assignmentMapper;

    @Transactional(readOnly = true)
    public PagedResponse<SectionManagerAssignmentResponse> findAllSectionManagerAssignments(Integer page, Integer size) {
        return PaginationUtils.toResponse(sectionManagerAssignmentRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(assignmentMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SectionManagerAssignmentResponse findSectionManagerAssignmentById(Integer id) {
        return assignmentMapper.toDto(requireSectionManagerAssignment(id));
    }

    public SectionManagerAssignmentResponse createSectionManagerAssignment(SectionManagerAssignmentRequest request) {
        validateSectionManagerAssignmentRequest(request);
        return assignmentMapper.toDto(sectionManagerAssignmentRepository.save(assignmentMapper.toEntity(request)));
    }

    public SectionManagerAssignmentResponse updateSectionManagerAssignment(Integer id, SectionManagerAssignmentRequest request) {
        requireSectionManagerAssignment(id);
        validateSectionManagerAssignmentRequest(request);
        SectionManagerAssignment assignment = assignmentMapper.toEntity(request);
        assignment.setId(id);
        return assignmentMapper.toDto(sectionManagerAssignmentRepository.save(assignment));
    }

    public void deleteSectionManagerAssignment(Integer id) {
        requireSectionManagerAssignment(id);
        sectionManagerAssignmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SellerAssignmentResponse> findAllSellerAssignments(Integer page, Integer size) {
        return PaginationUtils.toResponse(sellerAssignmentRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(assignmentMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SellerAssignmentResponse findSellerAssignmentById(Integer id) {
        return assignmentMapper.toDto(requireSellerAssignment(id));
    }

    public SellerAssignmentResponse createSellerAssignment(SellerAssignmentRequest request) {
        validateSellerAssignmentRequest(request);
        return assignmentMapper.toDto(sellerAssignmentRepository.save(assignmentMapper.toEntity(request)));
    }

    public SellerAssignmentResponse updateSellerAssignment(Integer id, SellerAssignmentRequest request) {
        requireSellerAssignment(id);
        validateSellerAssignmentRequest(request);
        SellerAssignment assignment = assignmentMapper.toEntity(request);
        assignment.setId(id);
        return assignmentMapper.toDto(sellerAssignmentRepository.save(assignment));
    }

    public void deleteSellerAssignment(Integer id) {
        requireSellerAssignment(id);
        sellerAssignmentRepository.deleteById(id);
    }

    private void validateSectionManagerAssignmentRequest(SectionManagerAssignmentRequest request) {
        requireEmployeeWithPosition(request.managerId(), EmployeePosition.SECTION_MANAGER);
        tradePointRepository.findById(request.tradePointId()).orElseThrow(() -> notFound("TradePoint", request.tradePointId()));
        Section section = sectionRepository.findById(request.sectionId()).orElseThrow(() -> notFound("Section", request.sectionId()));
        Integer departmentStoreId = section.getDepartmentStore() == null ? null : section.getDepartmentStore().getId();
        if (!request.tradePointId().equals(departmentStoreId)) {
            throw new IllegalArgumentException("Section does not belong to department store: " + request.sectionId());
        }
    }

    private void validateSellerAssignmentRequest(SellerAssignmentRequest request) {
        requireEmployeeWithPosition(request.sellerId(), EmployeePosition.SELLER);
        tradePointRepository.findById(request.tradePointId()).orElseThrow(() -> notFound("TradePoint", request.tradePointId()));
        if (request.hallId() != null) {
            hallRepository.findById(request.hallId()).orElseThrow(() -> notFound("Hall", request.hallId()));
        }
    }

    private Employee requireEmployeeWithPosition(Integer id, EmployeePosition position) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> notFound("Employee", id));
        if (employee.getPosition() != position) {
            throw new IllegalArgumentException("Employee " + id + " is not " + position);
        }
        return employee;
    }

    private SectionManagerAssignment requireSectionManagerAssignment(Integer id) {
        return sectionManagerAssignmentRepository.findById(id).orElseThrow(() -> notFound("SectionManagerAssignment", id));
    }

    private SellerAssignment requireSellerAssignment(Integer id) {
        return sellerAssignmentRepository.findById(id).orElseThrow(() -> notFound("SellerAssignment", id));
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }
}

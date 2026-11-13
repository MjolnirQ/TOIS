package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.HallRequest;
import ru.nsu.tois.dto.request.SectionRequest;
import ru.nsu.tois.dto.request.TradePointRequest;
import ru.nsu.tois.dto.response.HallResponse;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.dto.response.SectionResponse;
import ru.nsu.tois.dto.response.TradePointResponse;
import ru.nsu.tois.entity.tradePoints.Hall;
import ru.nsu.tois.entity.tradePoints.Section;
import ru.nsu.tois.entity.tradePoints.TradePoint;
import ru.nsu.tois.entity.tradePoints.TradePointType;
import ru.nsu.tois.mapper.TradePointMapper;
import ru.nsu.tois.repository.tradePoints.HallRepository;
import ru.nsu.tois.repository.tradePoints.SectionRepository;
import ru.nsu.tois.repository.tradePoints.TradePointRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TradePointService {
    private final TradePointRepository tradePointRepository;
    private final HallRepository hallRepository;
    private final SectionRepository sectionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TradePointMapper tradePointMapper;

    @Transactional(readOnly = true)
    public PagedResponse<TradePointResponse> findAllTradePoints(Integer page, Integer size) {
        return PaginationUtils.toResponse(tradePointRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(tradePointMapper::toDto));
    }

    @Transactional(readOnly = true)
    public TradePointResponse findTradePointById(Integer id) {
        return tradePointMapper.toDto(requireTradePoint(id));
    }

    public TradePointResponse createTradePoint(TradePointRequest request) {
        return tradePointMapper.toDto(tradePointRepository.save(tradePointMapper.toEntity(request)));
    }

    public TradePointResponse updateTradePoint(Integer id, TradePointRequest request) {
        requireTradePoint(id);
        TradePoint tradePoint = tradePointMapper.toEntity(request);
        tradePoint.setId(id);
        return tradePointMapper.toDto(tradePointRepository.save(tradePoint));
    }

    public void deleteTradePoint(Integer id) {
        requireTradePoint(id);
        ensureTradePointHasNoHistory(id);
        jdbcTemplate.update("update seller_assignment set hall_id = null where hall_id in (select id from hall where trade_point_id = ?)", id);
        jdbcTemplate.update("delete from section_manager_assignment where section_id in (select id from section where department_store_id = ?)", id);
        jdbcTemplate.update("delete from section_manager_assignment where trade_point_id = ?", id);
        jdbcTemplate.update("delete from seller_assignment where trade_point_id = ?", id);
        jdbcTemplate.update("delete from trade_point_product_price where trade_point_id = ?", id);
        jdbcTemplate.update("delete from trade_point_product_stock where trade_point_id = ?", id);
        tradePointRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HallResponse> findAllHalls(Integer page, Integer size) {
        return PaginationUtils.toResponse(hallRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(tradePointMapper::toDto));
    }

    @Transactional(readOnly = true)
    public HallResponse findHallById(Integer id) {
        return tradePointMapper.toDto(requireHall(id));
    }

    public HallResponse createHall(HallRequest request) {
        validateHallRequest(request);
        return tradePointMapper.toDto(hallRepository.save(tradePointMapper.toEntity(request)));
    }

    public HallResponse updateHall(Integer id, HallRequest request) {
        requireHall(id);
        validateHallRequest(request);
        Hall hall = tradePointMapper.toEntity(request);
        hall.setId(id);
        return tradePointMapper.toDto(hallRepository.save(hall));
    }

    public void deleteHall(Integer id) {
        requireHall(id);
        jdbcTemplate.update("update seller_assignment set hall_id = null where hall_id = ?", id);
        hallRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SectionResponse> findAllSections(Integer page, Integer size) {
        return PaginationUtils.toResponse(sectionRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(tradePointMapper::toDto));
    }

    @Transactional(readOnly = true)
    public SectionResponse findSectionById(Integer id) {
        return tradePointMapper.toDto(requireSection(id));
    }

    public SectionResponse createSection(SectionRequest request) {
        validateDepartmentStore(request.departmentStoreId());
        return tradePointMapper.toDto(sectionRepository.save(tradePointMapper.toEntity(request)));
    }

    public SectionResponse updateSection(Integer id, SectionRequest request) {
        requireSection(id);
        validateDepartmentStore(request.departmentStoreId());
        Section section = tradePointMapper.toEntity(request);
        section.setId(id);
        return tradePointMapper.toDto(sectionRepository.save(section));
    }

    public void deleteSection(Integer id) {
        requireSection(id);
        jdbcTemplate.update("delete from section_manager_assignment where section_id = ?", id);
        sectionRepository.deleteById(id);
    }

    private void validateHallRequest(HallRequest request) {
        TradePoint tradePoint = requireTradePoint(request.tradePointId());
        if (tradePoint.getType() != TradePointType.SHOP && tradePoint.getType() != TradePointType.DEPARTMENT_STORE) {
            throw new IllegalArgumentException("Hall can reference only shop or department store: " + request.tradePointId());
        }
    }

    private void validateDepartmentStore(Integer tradePointId) {
        TradePoint tradePoint = requireTradePoint(tradePointId);
        if (tradePoint.getType() != TradePointType.DEPARTMENT_STORE) {
            throw new IllegalArgumentException("Trade point is not department store: " + tradePointId);
        }
    }

    private TradePoint requireTradePoint(Integer id) {
        return tradePointRepository.findById(id).orElseThrow(() -> notFound("TradePoint", id));
    }

    private Hall requireHall(Integer id) {
        return hallRepository.findById(id).orElseThrow(() -> notFound("Hall", id));
    }

    private Section requireSection(Integer id) {
        return sectionRepository.findById(id).orElseThrow(() -> notFound("Section", id));
    }

    private void ensureTradePointHasNoHistory(Integer id) {
        int historyRows = count("""
                select
                    (select count(*) from sale where trade_point_id = ?) +
                    (select count(*) from supply_request where trade_point_id = ?) +
                    (select count(*) from product_transfer where sender_trade_point_id = ? or receiver_trade_point_id = ?)
                """, id, id, id, id);
        if (historyRows > 0) {
            throw new IllegalStateException("Cannot delete a trade point referenced by trade documents.");
        }
    }

    private int count(String sql, Object... params) {
        Number result = jdbcTemplate.queryForObject(sql, Number.class, params);
        return result == null ? 0 : result.intValue();
    }

    private EntityNotFoundException notFound(String entityName, Integer id) {
        return new EntityNotFoundException(entityName + " not found: " + id);
    }
}

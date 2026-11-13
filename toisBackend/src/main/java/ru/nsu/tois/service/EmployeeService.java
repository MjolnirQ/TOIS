package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.dto.request.EmployeeRequest;
import ru.nsu.tois.dto.response.EmployeeResponse;
import ru.nsu.tois.dto.response.PagedResponse;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.mapper.EmployeeMapper;
import ru.nsu.tois.repository.employees.EmployeeRepository;
import ru.nsu.tois.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> findAll(Integer page, Integer size) {
        return PaginationUtils.toResponse(employeeRepository
                .findAll(PaginationUtils.request(page, size, "id"))
                .map(employeeMapper::toDto));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Integer id) {
        return employeeMapper.toDto(requireEmployee(id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        return employeeMapper.toDto(employeeRepository.save(employeeMapper.toEntity(request)));
    }

    public EmployeeResponse update(Integer id, EmployeeRequest request) {
        requireEmployee(id);
        Employee employee = employeeMapper.toEntity(request);
        employee.setId(id);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public void delete(Integer id) {
        requireEmployee(id);
        ensureEmployeeHasNoHistory(id);
        jdbcTemplate.update("delete from user_account where employee_id = ?", id);
        jdbcTemplate.update("delete from section_manager_assignment where manager_id = ?", id);
        jdbcTemplate.update("delete from seller_assignment where seller_id = ?", id);
        employeeRepository.deleteById(id);
    }

    private Employee requireEmployee(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    private void ensureEmployeeHasNoHistory(Integer id) {
        int historyRows = count("""
                select
                    (select count(*) from sale where seller_id = ?) +
                    (select count(*) from supplier_order where manager_id = ?)
                """, id, id);
        if (historyRows > 0) {
            throw new IllegalStateException("Cannot delete a product referenced by trade documents.");
        }
    }

    private int count(String sql, Object... params) {
        Number result = jdbcTemplate.queryForObject(sql, Number.class, params);
        return result == null ? 0 : result.intValue();
    }
}

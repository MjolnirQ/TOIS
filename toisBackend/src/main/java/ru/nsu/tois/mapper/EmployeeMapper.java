package ru.nsu.tois.mapper;

import org.springframework.stereotype.Component;
import ru.nsu.tois.dto.request.EmployeeRequest;
import ru.nsu.tois.dto.response.EmployeeResponse;
import ru.nsu.tois.dto.response.SectionManagerResponse;
import ru.nsu.tois.dto.response.SellerResponse;
import ru.nsu.tois.dto.response.TradeManagerResponse;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.SectionManager;
import ru.nsu.tois.entity.employees.Seller;
import ru.nsu.tois.entity.employees.TradeManager;

@Component
public class EmployeeMapper {
    public EmployeeResponse toDto(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getPosition(),
                employee.getSalary()
        );
    }

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFullName(request.fullName());
        employee.setPosition(request.position());
        employee.setSalary(request.salary());
        return employee;
    }

    public SellerResponse toDto(Seller seller) {
        return new SellerResponse(seller.getId());
    }

    public SectionManagerResponse toDto(SectionManager sectionManager) {
        return new SectionManagerResponse(sectionManager.getId());
    }

    public TradeManagerResponse toDto(TradeManager tradeManager) {
        return new TradeManagerResponse(tradeManager.getId());
    }
}

package ru.nsu.tois.repository.employees;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.employees.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}

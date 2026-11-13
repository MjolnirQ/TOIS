package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.EmployeePosition;
import ru.nsu.tois.entity.user.UserAccount;
import ru.nsu.tois.repository.user.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public UserAccount requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Current user is not authenticated");
        }

        return userAccountRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("UserAccount not found: " + authentication.getName()));
    }

    @Transactional(readOnly = true)
    public Employee requireCurrentEmployee(EmployeePosition position) {
        Employee employee = requireCurrentUser().getEmployee();
        if (employee == null) {
            throw new IllegalStateException("Current user has no employee");
        }
        if (employee.getPosition() != position) {
            throw new IllegalArgumentException("Current employee is not " + position);
        }
        return employee;
    }
}

package ru.nsu.tois.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import ru.nsu.tois.entity.employees.Employee;
import ru.nsu.tois.entity.employees.EmployeePosition;
import ru.nsu.tois.entity.user.Role;
import ru.nsu.tois.entity.user.UserAccount;
import ru.nsu.tois.repository.employees.EmployeeRepository;
import ru.nsu.tois.repository.user.RoleRepository;
import ru.nsu.tois.repository.user.UserAccountRepository;

import java.time.LocalDateTime;

@Configuration
public class InitialAdminConfig {
    private static final String ADMIN_LOGIN = "superadmin";

    @Bean
    ApplicationRunner initialAdminRunner(
            @Value("${tois.bootstrap.admin-password}") String password,
            EmployeeRepository employees,
            RoleRepository roles,
            UserAccountRepository accounts,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transaction
    ) {
        return args -> transaction.executeWithoutResult(status -> {
            if (password.isBlank() || accounts.existsByLogin(ADMIN_LOGIN)) {
                return;
            }

            Role role = roles.findByName("SUPERADMIN_ROLE").orElseThrow();
            Employee employee = new Employee();
            employee.setFullName("Administrator");
            employee.setPosition(EmployeePosition.TRADE_MANAGER);
            employee.setSalary(0);

            UserAccount account = new UserAccount();
            account.setLogin(ADMIN_LOGIN);
            account.setPasswordHash(passwordEncoder.encode(password));
            account.setRole(role);
            account.setEmployee(employees.save(employee));
            account.setIsActive(true);
            account.setCreatedAt(LocalDateTime.now());
            accounts.save(account);
        });
    }
}

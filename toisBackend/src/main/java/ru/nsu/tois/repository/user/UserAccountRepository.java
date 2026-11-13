package ru.nsu.tois.repository.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.user.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<UserAccount> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByRole_Id(Integer roleId);
}

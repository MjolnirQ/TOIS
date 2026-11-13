package ru.nsu.tois.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.user.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}

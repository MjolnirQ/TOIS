package ru.nsu.tois.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.user.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}

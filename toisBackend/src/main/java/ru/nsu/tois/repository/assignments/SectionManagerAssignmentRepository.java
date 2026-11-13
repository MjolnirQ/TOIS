package ru.nsu.tois.repository.assignments;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.assignments.SectionManagerAssignment;

public interface SectionManagerAssignmentRepository extends JpaRepository<SectionManagerAssignment, Integer> {
}

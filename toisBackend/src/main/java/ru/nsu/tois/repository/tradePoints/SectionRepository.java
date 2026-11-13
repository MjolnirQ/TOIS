package ru.nsu.tois.repository.tradePoints;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.tradePoints.Section;

public interface SectionRepository extends JpaRepository<Section, Integer> {
}

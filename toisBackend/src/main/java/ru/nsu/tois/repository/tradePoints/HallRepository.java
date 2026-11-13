package ru.nsu.tois.repository.tradePoints;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.tradePoints.Hall;

public interface HallRepository extends JpaRepository<Hall, Integer> {
}

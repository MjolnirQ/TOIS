package ru.nsu.tois.entity.assignments;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.employees.SectionManager;
import ru.nsu.tois.entity.tradePoints.Section;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Entity
@Table(name = "section_manager_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionManagerAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @NotNull
    private SectionManager manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_point_id")
    @NotNull
    private TradePoint tradePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @NotNull
    private Section section;
}

package ru.nsu.tois.entity.employees;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "section_manager")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionManager {
    @Id
    @Column(name = "employee_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "employee_id")
    @NotNull
    private Employee employee;
}

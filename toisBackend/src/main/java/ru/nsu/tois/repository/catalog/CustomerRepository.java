package ru.nsu.tois.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.catalog.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query("""
            select c
            from Customer c
            where c.fullName = :fullName
              and (
                  (:phoneNumber is null and c.phoneNumber is null)
                  or c.phoneNumber = :phoneNumber
              )
            """)
    Optional<Customer> findByFullNameAndPhoneNumber(
            @Param("fullName") String fullName,
            @Param("phoneNumber") String phoneNumber
    );
}

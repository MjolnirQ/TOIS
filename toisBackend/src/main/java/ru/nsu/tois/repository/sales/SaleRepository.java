package ru.nsu.tois.repository.sales;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.sales.Sale;

import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Integer> {
    List<Sale> findBySeller_Id(Integer sellerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sale from Sale sale where sale.id = :id")
    Optional<Sale> findByIdForUpdate(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Sale s set s.customer = null where s.customer.id = :customerId")
    int clearCustomer(@Param("customerId") Integer customerId);
}

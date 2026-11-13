package ru.nsu.tois.repository.transfers;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.nsu.tois.entity.transfers.ProductTransfer;

import java.util.Optional;

public interface ProductTransferRepository extends JpaRepository<ProductTransfer, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductTransfer> findLockedById(Integer id);
}

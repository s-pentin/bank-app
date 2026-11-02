package bank.transfer.repository;

import bank.transfer.model.TransferOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferOperationRepository extends JpaRepository<TransferOperation, Long> {
}

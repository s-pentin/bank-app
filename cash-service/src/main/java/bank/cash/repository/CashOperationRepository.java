package bank.cash.repository;

import bank.cash.model.CashOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
}

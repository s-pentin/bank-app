package bank.transfer.repository;

import bank.transfer.model.OutboxEvent;
import bank.transfer.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}

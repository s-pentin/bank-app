package bank.transfer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "transfer_operations", schema = "transfer")
public class TransferOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_login", nullable = false)
    private String fromLogin;

    @Column(name = "to_login", nullable = false)
    private String toLogin;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at")
    private Instant createdAt;

    protected TransferOperation() {
    }

    public TransferOperation(String fromLogin, String toLogin, BigDecimal amount) {
        this.fromLogin = fromLogin;
        this.toLogin = toLogin;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

}

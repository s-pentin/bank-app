package bank.cash.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "cash_operations", schema = "cash")
public class CashOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_login", nullable = false)
    private String userLogin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashOperationType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at")
    private Instant createdAt;

    protected CashOperation() {
    }

    public CashOperation(String userLogin, CashOperationType type, BigDecimal amount) {
        this.userLogin = userLogin;
        this.type = type;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

}

package bank.accounts;

import bank.accounts.dto.AccountLookupDto;
import bank.accounts.dto.AccountResponseDto;
import bank.accounts.dto.AccountUpdateRequestDto;
import bank.accounts.service.AccountService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
class AccountFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @MockBean
    JwtDecoder jwtDecoder;

    @Autowired
    AccountService accountService;

    @Test
    void fullFlow_createDepositIsolateEditTransfer() {
        // 1) создание юзеров (lazy-провижининг при первом обращении)
        AccountResponseDto ivan = accountService.getMyAccount("ivan");
        AccountResponseDto petr = accountService.getMyAccount("petr");
        assertEquals(0, ivan.balance().compareTo(BigDecimal.ZERO));
        assertEquals(0, petr.balance().compareTo(BigDecimal.ZERO));

        // 2) начисление денег на счёт ivan
        AccountResponseDto deposited = accountService.credit("ivan", new BigDecimal("1000.00"));
        assertEquals(0, deposited.balance().compareTo(new BigDecimal("1000.00")));

        // 3) доступ только к своему счёту:
        //    - getMyAccount возвращает только «свой» баланс (login берётся из JWT на уровне контроллера);
        //    - lookup отдаёт чужих только как login+ФИО, без баланса (в DTO нет поля balance).
        AccountResponseDto own = accountService.getMyAccount("ivan");
        assertEquals(0, own.balance().compareTo(new BigDecimal("1000.00")));

        List<AccountLookupDto> lookup = accountService.lookup("ivan", null);
        assertTrue(lookup.stream().noneMatch(a -> a.login().equals("ivan")), "свой аккаунт не должен быть в списке получателей");
        assertTrue(lookup.stream().anyMatch(a -> a.login().equals("petr")));

        // 4) редактирование своего профиля
        AccountResponseDto updated = accountService.updateMyAccount("ivan",
                new AccountUpdateRequestDto("Иван", "Иванов", LocalDate.of(1990, 1, 1)));
        assertEquals("Иван", updated.firstName());
        assertEquals("Иванов", updated.lastName());
        assertEquals(LocalDate.of(1990, 1, 1), updated.birthDate());

        // 5) перевод: списать у ivan, зачислить petr'у
        accountService.debit("ivan", new BigDecimal("300.00"));
        accountService.credit("petr", new BigDecimal("300.00"));

        // 6) проверка, что у petr'а деньги пришли, у ivan'а — списались
        AccountResponseDto ivanAfter = accountService.getMyAccount("ivan");
        AccountResponseDto petrAfter = accountService.getMyAccount("petr");
        assertEquals(0, ivanAfter.balance().compareTo(new BigDecimal("700.00")));
        assertEquals(0, petrAfter.balance().compareTo(new BigDecimal("300.00")));
    }
}

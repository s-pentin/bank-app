package bank.cash.controller;

import bank.cash.dto.CashOperationRequestDto;
import bank.cash.dto.CashOperationResponseDto;
import bank.cash.service.CashService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/deposit")
    public CashOperationResponseDto deposit(@AuthenticationPrincipal Jwt jwt,
                                            @RequestBody CashOperationRequestDto dto) {
        return cashService.deposit(login(jwt), dto.amount());
    }

    @PostMapping("/withdraw")
    public CashOperationResponseDto withdraw(@AuthenticationPrincipal Jwt jwt,
                                             @RequestBody CashOperationRequestDto dto) {
        return cashService.withdraw(login(jwt), dto.amount());
    }

    private String login(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
}

package bank.accounts.controller;

import bank.accounts.dto.AccountResponseDto;
import bank.accounts.dto.BalanceOperationRequestDto;
import bank.accounts.service.AccountService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountController {

    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{login}/debit")
    public AccountResponseDto debit(@PathVariable String login,
                                    @RequestBody BalanceOperationRequestDto dto) {
        return accountService.debit(login, dto.amount());
    }

    @PostMapping("/{login}/credit")
    public AccountResponseDto credit(@PathVariable String login,
                                     @RequestBody BalanceOperationRequestDto dto) {
        return accountService.credit(login, dto.amount());
    }
}

package bank.accounts.controller;

import bank.accounts.dto.AccountLookupDto;
import bank.accounts.dto.AccountResponseDto;
import bank.accounts.dto.AccountUpdateRequestDto;
import bank.accounts.service.AccountService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public AccountResponseDto getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        return accountService.getMyAccount(login(jwt));
    }

    @PatchMapping("/me")
    public AccountResponseDto updateMyAccount(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody AccountUpdateRequestDto dto) {
        return accountService.updateMyAccount(login(jwt), dto);
    }

    @GetMapping("/lookup")
    public List<AccountLookupDto> lookup(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam(required = false) String query) {
        return accountService.lookup(login(jwt), query);
    }

    private String login(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
}

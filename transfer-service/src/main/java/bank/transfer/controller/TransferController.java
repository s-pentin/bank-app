package bank.transfer.controller;

import bank.transfer.dto.TransferRequestDto;
import bank.transfer.dto.TransferResponseDto;
import bank.transfer.service.TransferService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponseDto transfer(@AuthenticationPrincipal Jwt jwt,
                                        @RequestBody TransferRequestDto dto) {
        return transferService.transfer(login(jwt), dto.toLogin(), dto.amount());
    }

    private String login(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
}

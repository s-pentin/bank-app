package bank.front.controller;

import bank.front.client.AccountsGatewayClient;
import bank.front.client.CashGatewayClient;
import bank.front.client.TransferGatewayClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private final AccountsGatewayClient accountsGatewayClient;
    private final CashGatewayClient cashGatewayClient;
    private final TransferGatewayClient transferGatewayClient;

    public MainController(AccountsGatewayClient accountsGatewayClient,
                          CashGatewayClient cashGatewayClient,
                          TransferGatewayClient transferGatewayClient) {
        this.accountsGatewayClient = accountsGatewayClient;
        this.cashGatewayClient = cashGatewayClient;
        this.transferGatewayClient = transferGatewayClient;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("account", accountsGatewayClient.getMyAccount());
        model.addAttribute("accounts", accountsGatewayClient.lookup());
        return "main";
    }

    @PostMapping("/account")
    public String updateAccount(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String birthDate,
                                RedirectAttributes ra) {
        try {
            accountsGatewayClient.updateMyAccount(firstName, lastName, LocalDate.parse(birthDate));
            ra.addFlashAttribute("success", "Данные сохранены");
        } catch (Exception e) {
            ra.addFlashAttribute("error", errorMessage(e));
        }
        return "redirect:/";
    }

    @PostMapping("/cash/deposit")
    public String deposit(@RequestParam BigDecimal amount, RedirectAttributes ra) {
        try {
            cashGatewayClient.deposit(amount);
            ra.addFlashAttribute("success", "Пополнение выполнено");
        } catch (Exception e) {
            ra.addFlashAttribute("error", errorMessage(e));
        }
        return "redirect:/";
    }

    @PostMapping("/cash/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, RedirectAttributes ra) {
        try {
            cashGatewayClient.withdraw(amount);
            ra.addFlashAttribute("success", "Снятие выполнено");
        } catch (Exception e) {
            ra.addFlashAttribute("error", errorMessage(e));
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String toLogin, @RequestParam BigDecimal amount, RedirectAttributes ra) {
        try {
            transferGatewayClient.transfer(toLogin, amount);
            ra.addFlashAttribute("success", "Перевод выполнен");
        } catch (Exception e) {
            ra.addFlashAttribute("error", errorMessage(e));
        }
        return "redirect:/";
    }

    private String errorMessage(Exception e) {
        if (e instanceof RestClientResponseException rcre) {
            String body = rcre.getResponseBodyAsString();
            return body != null && !body.isBlank() ? body : "Ошибка " + rcre.getStatusCode().value();
        }
        return e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка";
    }
}

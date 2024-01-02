package ro.boa.clinic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ro.boa.clinic.dto.AccountDetailsDto;
import ro.boa.clinic.dto.AccountRegistrationDto;
import ro.boa.clinic.service.AccountService;

@RequiredArgsConstructor
@RestController
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<Void> createAccount(@RequestBody AccountRegistrationDto accountRegistrationDto) {
        try {
            accountService.createPatientAccount(accountRegistrationDto.email(), accountRegistrationDto.password());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/accounts/0")
    public ResponseEntity<AccountDetailsDto> getCurrentAccountDetails() {
        return accountService.findAccountByEmail(accountService.getAuthenticatedUserEmail()).map(account -> {
            var accountDetailsDto = new AccountDetailsDto(account.getEmail(), account.getRole());
            return ResponseEntity.ok(accountDetailsDto);
        }).orElse(ResponseEntity.notFound().build());
    }
}

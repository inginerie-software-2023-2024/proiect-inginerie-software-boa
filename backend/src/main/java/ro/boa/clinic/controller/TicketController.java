package ro.boa.clinic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.boa.clinic.dto.TicketCreationRequestDto;
import ro.boa.clinic.dto.TicketDetailsResponseDto;
import ro.boa.clinic.dto.TicketResponseDto;
import ro.boa.clinic.service.AccountService;
import ro.boa.clinic.service.PatientService;
import ro.boa.clinic.service.TicketService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final PatientService patientService;
    private final AccountService accountService;

    @PostMapping(value = "/tickets")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<HttpStatus> createTicket(@RequestBody TicketCreationRequestDto ticketCreationRequest) {
        var patient = patientService.getAuthenticatedPatientProfile();
        ticketService.createTicket(ticketCreationRequest, patient);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/tickets/{id}")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<TicketDetailsResponseDto> getTicketDetails(@PathVariable Long id) {
        var ticketDetails = ticketService.getTicketDetails(id);
        return ResponseEntity.ok(ticketDetails);
    }

    @GetMapping(value = "/tickets/all")
    @PreAuthorize("hasRole('ROLE_PATIENT') || hasRole('ROLE_DOCTOR')")
    public ResponseEntity<List<TicketResponseDto>> getAllTickets() {
        var userEmail = accountService.getAuthenticatedUserEmail();
        var tickets = ticketService.getAllTickets(userEmail);
        return ResponseEntity.ok(tickets);
    }
}

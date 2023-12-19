package ro.boa.clinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ro.boa.clinic.dto.TicketCreationRequestDto;
import ro.boa.clinic.service.PatientService;
import ro.boa.clinic.service.TicketService;

@RestController
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private PatientService patientService;

    @PostMapping(value = "/tickets")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<HttpStatus> createTicket(@RequestBody TicketCreationRequestDto ticketCreationRequest) {
        var patient = patientService.getAuthenticatedPatientProfile();
        ticketService.createTicket(ticketCreationRequest, patient);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}

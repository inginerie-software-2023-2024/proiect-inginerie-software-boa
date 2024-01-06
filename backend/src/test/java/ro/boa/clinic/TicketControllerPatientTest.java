package ro.boa.clinic;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ro.boa.clinic.dto.TicketCreationRequestDto;
import ro.boa.clinic.dto.TicketUpdateRequestDto;
import ro.boa.clinic.model.Patient;
import ro.boa.clinic.model.Role;
import ro.boa.clinic.model.Status;
import ro.boa.clinic.model.Ticket;
import ro.boa.clinic.repository.TicketRepository;
import ro.boa.clinic.service.TicketService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TicketControllerPatientTest {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestTester requestTester;

    @Autowired
    private EntityTestUtils entityTestUtils;

    private Patient patient;

    @BeforeAll
    public void setUp() throws Exception {
        requestTester.createTestAccount(Role.PATIENT);
        patient = requestTester.createTestPatient();
        requestTester.authenticateAccount();
    }

    @Test
    void creationRequest_incorrectData_returnsError() throws Exception {
        entityTestUtils.createDoctor("Doctor", "Specialization");
        var ticketDto = new TicketCreationRequestDto("Title", "Description", "WrongSpecialization");

        mockMvc.perform(requestTester.authenticatedPost("/tickets", ticketDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creationRequest_validData_createsTicket() throws Exception {
        entityTestUtils.createDoctor("Doctor", "Specialization");
        var ticketDto = new TicketCreationRequestDto("Title", "Description", "Specialization");

        mockMvc.perform(requestTester.authenticatedPost("/tickets", ticketDto))
                .andExpect(status().isCreated());
        var createdTicket = ticketRepository.findByTitle(ticketDto.title());

        assertEquals(ticketDto.title(), createdTicket.getTitle());
        assertEquals(ticketDto.description(), createdTicket.getDescription());
        assertEquals(ticketDto.specialization(), createdTicket.getSpecialization());
    }

    @Test
    void creationRequest_validData_assignsFreestDoctor() throws Exception {
        var doctor1 = entityTestUtils.createDoctor("Doctor1", "Specialization");
        entityTestUtils.createDoctor("Doctor2", "OtherSpecialization");
        var doctor3 = entityTestUtils.createDoctor("Doctor3", "Specialization");
        var existingTicketDto = new TicketCreationRequestDto("Title1", "Description1", "Specialization");
        ticketService.createTicket(existingTicketDto, patient, doctor1);
        var newTicketDto = new TicketCreationRequestDto("Title2", "Description", "Specialization");

        mockMvc.perform(requestTester.authenticatedPost("/tickets", newTicketDto))
                .andExpect(status().isCreated());
        var createdTicket = ticketRepository.findWithDoctorByTitle(newTicketDto.title()).orElseThrow();
        var assignedDoctor = createdTicket.getDoctor();

        assertNotNull(assignedDoctor);
        assertEquals(doctor3.getId(), assignedDoctor.getId());
    }

    @Test
    void detailsRequest_validId_returnsDetails() throws Exception {
        String ticketDetails = "{\"status\":\"OPENED\",\"description\":\"Description\",\"specialization\":\"Specialization\",\"doctor\":null}";

        ticketRepository.save(new Ticket(1L, null, patient, "Title", "Description", "Specialization", Status.OPENED));
        mockMvc.perform(requestTester.authenticatedGet("/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(ticketDetails));
    }

    @Test
    void ticketListRequest_validUserUndefinedStatus_returnsTicketList() throws Exception {
        String ticketList = "[{\"id\":1,\"doctor\":null,\"title\":\"Title\",\"description\":\"Description\",\"specialization\":\"Specialization\",\"status\":\"OPENED\"},"
                + "{\"id\":2,\"doctor\":null,\"title\":\"Title1\",\"description\":\"Description1\",\"specialization\":\"Specialization1\",\"status\":\"CLOSED\"}]";

        ticketRepository.save(new Ticket(1L, null, patient, "Title", "Description", "Specialization", Status.OPENED));
        ticketRepository.save(new Ticket(2L, null, patient, "Title1", "Description1", "Specialization1", Status.CLOSED));

        mockMvc.perform(requestTester.authenticatedGet("/tickets"))
                .andExpect(status().isOk())
                .andExpect(content().string(ticketList));
    }

    @Test
    void ticketListRequest_validUser_returnsTicketList() throws Exception {
        String openedTicket = "[{\"id\":1,\"doctor\":null,\"title\":\"Title\",\"description\":\"Description\",\"specialization\":\"Specialization\",\"status\":\"OPENED\"}]";
        String closedTicket = "[{\"id\":2,\"doctor\":null,\"title\":\"Title1\",\"description\":\"Description1\",\"specialization\":\"Specialization1\",\"status\":\"CLOSED\"}]";


        ticketRepository.save(new Ticket(1L, null, patient, "Title", "Description", "Specialization", Status.OPENED));
        ticketRepository.save(new Ticket(2L, null, patient, "Title1", "Description1", "Specialization1", Status.CLOSED));

        mockMvc.perform(requestTester.authenticatedGet("/tickets").param("status", Status.OPENED.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(openedTicket));

        mockMvc.perform(requestTester.authenticatedGet("/tickets").param("status", Status.CLOSED.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(closedTicket));
    }

    @Test
    void updateTicketRequest_validId_updatesTicket() throws Exception {
        Ticket savedTicket = ticketRepository.save(new Ticket(1L, null, patient, "Title", "Description", "Specialization", Status.OPENED));

        String newTicketTitle = "Nu vad la departare";
        String newTicketDescription = "Ma joc toata ziua pe caluculator tomb raider";
        Status newTicketStatus = Status.CLOSED;

        TicketUpdateRequestDto ticketUpdateRequestDto = new TicketUpdateRequestDto(Optional.of(newTicketTitle), Optional.of(newTicketDescription), Optional.of(Status.CLOSED.toString()), Optional.empty());
        mockMvc.perform(requestTester.authenticatedPatch("/tickets/" + 1L, ticketUpdateRequestDto))
                .andExpect(status().isOk());

        assertEquals(savedTicket.getTitle(), newTicketTitle);
        assertEquals(savedTicket.getDescription(), newTicketDescription);
        assertEquals(savedTicket.getStatus(), newTicketStatus);

    }
}
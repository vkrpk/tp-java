package victork.fr.ticketapi.controller;

import victork.fr.ticketapi.dto.TicketRequest;
import victork.fr.ticketapi.dto.TicketResponse;
import victork.fr.ticketapi.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import victork.fr.ticketapi.utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testCreateTicket_Success() throws Exception {
        // Arrange
        TicketRequest request = new TicketRequest("Test Title", "Test Description");
        TicketResponse response = new TicketResponse(
                1L,
                "Test Title",
                "Test Description",
                false,
                LocalDateTime.now(),
                "user1",
                null,
                null
        );

        when(ticketService.createTicket(any(TicketRequest.class), eq("user1"))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("Test Title"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.resolved").value(false))
                .andExpect(jsonPath("$.createdBy").value("user1"));

        verify(ticketService).createTicket(any(TicketRequest.class), eq("user1"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testCreateTicket_InvalidRequest() throws Exception {
        // Arrange
        TicketRequest request = new TicketRequest("", ""); // Invalid request

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).createTicket(any(), any());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testCreateTicket_ServiceException() throws Exception {
        // Arrange
        TicketRequest request = new TicketRequest("Test Title", "Test Description");

        when(ticketService.createTicket(any(TicketRequest.class), eq("user1")))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Service error"));

        verify(ticketService).createTicket(any(TicketRequest.class), eq("user1"));
    }

    @Test
    void testCreateTicket_Unauthorized() throws Exception {
        // Arrange
        TicketRequest request = new TicketRequest("Test Title", "Test Description");

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(ticketService, never()).createTicket(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllTickets_Success() throws Exception {
        // Arrange
        List<TicketResponse> tickets = Arrays.asList(
                new TicketResponse(1L, "Ticket 1", "Description 1", false, LocalDateTime.now(), "user1", null, null),
                new TicketResponse(2L, "Ticket 2", "Description 2", true, LocalDateTime.now(), "user2", "admin", LocalDateTime.now())
        );

        when(ticketService.getAllTickets()).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titre").value("Ticket 1"))
                .andExpect(jsonPath("$[0].resolved").value(false))
                .andExpect(jsonPath("$[1].titre").value("Ticket 2"))
                .andExpect(jsonPath("$[1].resolved").value(true));

        verify(ticketService).getAllTickets();
    }

    @Test
    void testGetUnresolvedTickets_Success() throws Exception {
        // Arrange
        List<TicketResponse> tickets = Arrays.asList(
                new TicketResponse(1L, "Ticket 1", "Description 1", false, LocalDateTime.now(), "user1", null, null),
                new TicketResponse(2L, "Ticket 2", "Description 2", false, LocalDateTime.now(), "user2", null, null)
        );

        when(ticketService.getUnresolvedTickets()).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/public")
                .with(user("testUser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titre").value("Ticket 1"))
                .andExpect(jsonPath("$[0].resolved").value(false))
                .andExpect(jsonPath("$[1].titre").value("Ticket 2"))
                .andExpect(jsonPath("$[1].resolved").value(false));

        verify(ticketService).getUnresolvedTickets();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testResolveTicket_Success() throws Exception {
        // Arrange
        Long ticketId = 1L;
        TicketResponse response = new TicketResponse(
                ticketId,
                "Test Title",
                "Test Description",
                true,
                LocalDateTime.now(),
                "user1",
                "admin",
                LocalDateTime.now()
        );

        when(ticketService.resolveTicket(ticketId, "admin")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}/resolve", ticketId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.resolvedBy").value("admin"));

        verify(ticketService).resolveTicket(ticketId, "admin");
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testResolveTicket_NonAdmin() throws Exception {
        // Arrange
        Long ticketId = 1L;

        when(ticketService.resolveTicket(ticketId, "user1"))
                .thenThrow(new RuntimeException("Seuls les administrateurs peuvent résoudre les tickets"));

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}/resolve", ticketId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Seuls les administrateurs peuvent résoudre les tickets"));

        verify(ticketService).resolveTicket(ticketId, "user1");
    }

    @Test
    void testResolveTicket_Unauthorized() throws Exception {
        // Arrange
        Long ticketId = 1L;

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}/resolve", ticketId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(ticketService, never()).resolveTicket(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testResolveTicket_TicketNotFound() throws Exception {
        // Arrange
        Long ticketId = 999L;

        when(ticketService.resolveTicket(ticketId, "admin"))
                .thenThrow(new RuntimeException("Ticket non trouvé"));

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}/resolve", ticketId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ticket non trouvé"));

        verify(ticketService).resolveTicket(ticketId, "admin");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testResolveTicket_AlreadyResolved() throws Exception {
        // Arrange
        Long ticketId = 1L;

        when(ticketService.resolveTicket(ticketId, "admin"))
                .thenThrow(new RuntimeException("Ce ticket est déjà résolu"));

        // Act & Assert
        mockMvc.perform(put("/api/tickets/{id}/resolve", ticketId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ce ticket est déjà résolu"));

        verify(ticketService).resolveTicket(ticketId, "admin");
    }
}

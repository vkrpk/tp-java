package victork.fr.ticketapi.controller;

import victork.fr.ticketapi.dto.TicketRequest;
import victork.fr.ticketapi.dto.TicketResponse;
import victork.fr.ticketapi.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String pseudo = auth.getName();

            TicketResponse response = ticketService.createTicket(request, pseudo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/public")
    public ResponseEntity<List<TicketResponse>> getUnresolvedTickets() {
        List<TicketResponse> tickets = ticketService.getUnresolvedTickets();
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveTicket(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String pseudo = auth.getName();

            TicketResponse response = ticketService.resolveTicket(id, pseudo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
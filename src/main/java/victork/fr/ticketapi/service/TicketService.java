package victork.fr.ticketapi.service;

import victork.fr.ticketapi.dto.TicketRequest;
import victork.fr.ticketapi.dto.TicketResponse;
import victork.fr.ticketapi.entity.Ticket;
import victork.fr.ticketapi.entity.User;
import victork.fr.ticketapi.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserService userService;

    public TicketResponse createTicket(TicketRequest request, String creatorPseudo) {
        Optional<User> userOpt = userService.findByPseudo(creatorPseudo);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        User creator = userOpt.get();
        Ticket ticket = new Ticket(request.getTitre(), request.getDescription(), creator);

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToResponse(savedTicket);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getUnresolvedTickets() {
        return ticketRepository.findByResolvedFalse().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse resolveTicket(Long ticketId, String resolverPseudo) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket non trouvé");
        }

        Optional<User> userOpt = userService.findByPseudo(resolverPseudo);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Ticket ticket = ticketOpt.get();
        User resolver = userOpt.get();

        if (!resolver.isAdmin()) {
            throw new RuntimeException("Seuls les administrateurs peuvent résoudre les tickets");
        }

        if (ticket.isResolved()) {
            throw new RuntimeException("Ce ticket est déjà résolu");
        }

        ticket.setResolved(true);
        ticket.setResolvedBy(resolver);
        ticket.setResolvedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToResponse(savedTicket);
    }

    private TicketResponse convertToResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitre(),
                ticket.getDescription(),
                ticket.isResolved(),
                ticket.getCreatedAt(),
                ticket.getCreatedBy().getPseudo(),
                ticket.getResolvedBy() != null ? ticket.getResolvedBy().getPseudo() : null,
                ticket.getResolvedAt()
        );
    }
}

package victork.fr.ticketapi.repository;

import victork.fr.ticketapi.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByResolvedFalse();
    List<Ticket> findByResolvedTrue();
    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}

package victork.fr.ticketapi.dto;

import java.time.LocalDateTime;

public class TicketResponse {
    private Long id;
    private String titre;
    private String description;
    private boolean resolved;
    private LocalDateTime createdAt;
    private String createdBy;
    private String resolvedBy;
    private LocalDateTime resolvedAt;

    // Constructeurs
    public TicketResponse() {}

    public TicketResponse(Long id, String titre, String description, boolean resolved,
                          LocalDateTime createdAt, String createdBy, String resolvedBy,
                          LocalDateTime resolvedAt) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.resolved = resolved;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}

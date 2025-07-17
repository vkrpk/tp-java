package victork.fr.ticketapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketRequest {
    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(min = 5, max = 255, message = "Le titre doit contenir entre 5 et 255 caractères")
    private String titre;

    @NotBlank(message = "La description ne peut pas être vide")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    // Constructeurs
    public TicketRequest() {}

    public TicketRequest(String titre, String description) {
        this.titre = titre;
        this.description = description;
    }

    // Getters et Setters
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
}

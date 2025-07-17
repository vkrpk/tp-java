package victork.fr.ticketapi.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Le pseudo ne peut pas être vide")
    private String pseudo;

    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    private String password;

    // Constructeurs
    public LoginRequest() {}

    public LoginRequest(String pseudo, String password) {
        this.pseudo = pseudo;
        this.password = password;
    }

    // Getters et Setters
    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
package victork.fr.ticketapi.dto;

public class LoginResponse {
    private String token;
    private String pseudo;
    private boolean isAdmin;

    public LoginResponse(String token, String pseudo, boolean isAdmin) {
        this.token = token;
        this.pseudo = pseudo;
        this.isAdmin = isAdmin;
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

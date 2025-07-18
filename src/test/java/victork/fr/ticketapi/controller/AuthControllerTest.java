package victork.fr.ticketapi.controller;

import victork.fr.ticketapi.dto.LoginRequest;
import victork.fr.ticketapi.entity.User;
import victork.fr.ticketapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminPseudo;
    private String userPseudo;

    @BeforeEach
    void setUp() {
        // Configuration de MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Nettoyer la base de données
        userRepository.deleteAll();
        userRepository.flush(); // Force la synchronisation avec la base de données

        // Créer des utilisateurs de test avec des pseudos uniques
        String timestamp = String.valueOf(System.currentTimeMillis());
        adminPseudo = "admin" + timestamp;
        userPseudo = "user1" + timestamp;

        User admin = new User(adminPseudo, passwordEncoder.encode("admin123"), true);
        User user = new User(userPseudo, passwordEncoder.encode("user123"), false);

        userRepository.save(admin);
        userRepository.save(user);
        userRepository.flush(); // Force la synchronisation
    }

    @Test
    void testLoginSuccess_Admin() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(adminPseudo, "admin123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value(adminPseudo))
                .andExpect(jsonPath("$.admin").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testLoginSuccess_User() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(userPseudo, "user123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value(userPseudo))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testLoginFailure_UserNotFound() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("inexistant", "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Utilisateur non trouvé"));
    }

    @Test
    void testLoginFailure_WrongPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(userPseudo, "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Mot de passe incorrect"));
    }

    @Test
    void testLoginFailure_EmptyPseudo() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("", "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginFailure_NullPseudo() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(null, "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginFailure_EmptyPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(userPseudo, "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginFailure_NullPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(userPseudo, null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginFailure_InvalidJsonFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginFailure_MissingContentType() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user1", "user123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }
}

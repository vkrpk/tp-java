package victork.fr.ticketapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import victork.fr.ticketapi.dto.LoginRequest;
import victork.fr.ticketapi.dto.LoginResponse;
import victork.fr.ticketapi.dto.TicketRequest;
import victork.fr.ticketapi.dto.TicketResponse;
import victork.fr.ticketapi.entity.User;
import victork.fr.ticketapi.repository.TicketRepository;
import victork.fr.ticketapi.repository.UserRepository;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TicketIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Nettoyer la base de données
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Créer des utilisateurs de test
        User admin = new User("admin", passwordEncoder.encode("admin123"), true);
        User user = new User("user1", passwordEncoder.encode("user123"), false);
        userRepository.save(admin);
        userRepository.save(user);
    }

    @Test
    void testFullTicketWorkflow() throws Exception {
        // 1. Connexion utilisateur normal
        LoginRequest userLogin = new LoginRequest("user1", "user123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("user1"))
                .andExpect(jsonPath("$.admin").value(false))
                .andReturn();

        LoginResponse userLoginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponse.class);
        String userToken = userLoginResponse.getToken();

        // 2. Créer un ticket
        TicketRequest ticketRequest = new TicketRequest("Problème urgent", "Description détaillée du problème");
        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Problème urgent"))
                .andExpect(jsonPath("$.description").value("Description détaillée du problème"))
                .andExpect(jsonPath("$.resolved").value(false))
                .andExpect(jsonPath("$.createdBy").value("user1"))
                .andReturn();

        TicketResponse createdTicket = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TicketResponse.class);

        // 3. Vérifier que le ticket apparaît dans la liste publique
        mockMvc.perform(get("/api/tickets/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre").value("Problème urgent"))
                .andExpect(jsonPath("$[0].resolved").value(false));

        // 4. Connexion admin
        LoginRequest adminLogin = new LoginRequest("admin", "admin123");
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("admin"))
                .andExpect(jsonPath("$.admin").value(true))
                .andReturn();

        LoginResponse adminLoginResponse = objectMapper.readValue(
                adminLoginResult.getResponse().getContentAsString(),
                LoginResponse.class);
        String adminToken = adminLoginResponse.getToken();

        // 5. Résoudre le ticket
        mockMvc.perform(put("/api/tickets/" + createdTicket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.resolvedBy").value("admin"));

        // 6. Vérifier que le ticket n'apparaît plus dans la liste publique
        mockMvc.perform(get("/api/tickets/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 7. Vérifier que le ticket apparaît toujours dans la liste complète
        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resolved").value(true));
    }

    @Test
    void testSecurityAndPermissions() throws Exception {
        // 1. Créer un ticket sans authentification doit échouer
        TicketRequest ticketRequest = new TicketRequest("Test ticket", "Test description");
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isForbidden());

        // 2. Connexion utilisateur normal
        LoginRequest userLogin = new LoginRequest("user1", "user123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse userLoginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponse.class);
        String userToken = userLoginResponse.getToken();

        // 3. Créer un ticket avec utilisateur normal
        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isOk())
                .andReturn();

        TicketResponse createdTicket = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TicketResponse.class);

        // 4. Utilisateur normal ne peut pas résoudre le ticket
        mockMvc.perform(put("/api/tickets/" + createdTicket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 5. Connexion admin
        LoginRequest adminLogin = new LoginRequest("admin", "admin123");
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse adminLoginResponse = objectMapper.readValue(
                adminLoginResult.getResponse().getContentAsString(),
                LoginResponse.class);
        String adminToken = adminLoginResponse.getToken();

        // 6. Admin peut résoudre le ticket
        mockMvc.perform(put("/api/tickets/" + createdTicket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true));

        // 7. Tentative de résolution d'un ticket déjà résolu
        mockMvc.perform(put("/api/tickets/" + createdTicket.getId() + "/resolve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}

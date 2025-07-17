package victork.fr.ticketapi.service;

import victork.fr.ticketapi.dto.LoginRequest;
import victork.fr.ticketapi.dto.LoginResponse;
import victork.fr.ticketapi.entity.User;
import victork.fr.ticketapi.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userOpt = userService.findByPseudo(loginRequest.getPseudo());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        if (!userService.validatePassword(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String token = jwtUtil.generateToken(user.getPseudo(), user.isAdmin());

        return new LoginResponse(token, user.getPseudo(), user.isAdmin());
    }
}
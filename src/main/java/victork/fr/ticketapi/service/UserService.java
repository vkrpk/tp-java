package victork.fr.ticketapi.service;

import victork.fr.ticketapi.entity.User;
import victork.fr.ticketapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(String pseudo, String password, boolean isAdmin) {
        if (userRepository.existsByPseudo(pseudo)) {
            throw new RuntimeException("Un utilisateur avec ce pseudo existe déjà");
        }

        User user = new User();
        user.setPseudo(pseudo);
        user.setPassword(passwordEncoder.encode(password));
        user.setAdmin(isAdmin);

        return userRepository.save(user);
    }

    public Optional<User> findByPseudo(String pseudo) {
        return userRepository.findByPseudo(pseudo);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
package victork.fr.ticketapi.service;

import victork.fr.ticketapi.entity.User;
import victork.fr.ticketapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser_Success() {
        // Arrange
        String pseudo = "testuser";
        String password = "password123";
        String encodedPassword = "encoded_password";
        boolean isAdmin = false;

        User savedUser = new User(pseudo, encodedPassword, isAdmin);
        savedUser.setId(1L);

        when(userRepository.existsByPseudo(pseudo)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(pseudo, password, isAdmin);

        // Assert
        assertThat(result.getPseudo()).isEqualTo(pseudo);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.isAdmin()).isEqualTo(isAdmin);
        verify(userRepository).existsByPseudo(pseudo);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_AdminUser() {
        // Arrange
        String pseudo = "admin";
        String password = "admin123";
        String encodedPassword = "encoded_admin_password";
        boolean isAdmin = true;

        User savedUser = new User(pseudo, encodedPassword, isAdmin);
        savedUser.setId(1L);

        when(userRepository.existsByPseudo(pseudo)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(pseudo, password, isAdmin);

        // Assert
        assertThat(result.getPseudo()).isEqualTo(pseudo);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.isAdmin()).isTrue();
        verify(userRepository).existsByPseudo(pseudo);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_PseudoAlreadyExists() {
        // Arrange
        String pseudo = "existinguser";
        String password = "password123";
        boolean isAdmin = false;

        when(userRepository.existsByPseudo(pseudo)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(pseudo, password, isAdmin))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Un utilisateur avec ce pseudo existe déjà");

        verify(userRepository).existsByPseudo(pseudo);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindByPseudo_UserExists() {
        // Arrange
        String pseudo = "existinguser";
        User existingUser = new User(pseudo, "password", false);
        existingUser.setId(1L);

        when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.of(existingUser));

        // Act
        Optional<User> result = userService.findByPseudo(pseudo);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPseudo()).isEqualTo(pseudo);
        verify(userRepository).findByPseudo(pseudo);
    }

    @Test
    void testFindByPseudo_UserNotExists() {
        // Arrange
        String pseudo = "nonexistentuser";

        when(userRepository.findByPseudo(pseudo)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByPseudo(pseudo);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findByPseudo(pseudo);
    }

    @Test
    void testValidatePassword_Success() {
        // Arrange
        String rawPassword = "password123";
        String encodedPassword = "encoded_password";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        boolean result = userService.validatePassword(rawPassword, encodedPassword);

        // Assert
        assertThat(result).isTrue();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testValidatePassword_Failure() {
        // Arrange
        String rawPassword = "wrongpassword";
        String encodedPassword = "encoded_password";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        boolean result = userService.validatePassword(rawPassword, encodedPassword);

        // Assert
        assertThat(result).isFalse();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testValidatePassword_NullRawPassword() {
        // Arrange
        String rawPassword = null;
        String encodedPassword = "encoded_password";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        boolean result = userService.validatePassword(rawPassword, encodedPassword);

        // Assert
        assertThat(result).isFalse();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testValidatePassword_EmptyRawPassword() {
        // Arrange
        String rawPassword = "";
        String encodedPassword = "encoded_password";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        boolean result = userService.validatePassword(rawPassword, encodedPassword);

        // Assert
        assertThat(result).isFalse();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
}

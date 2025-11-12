package com.rishabh.ecom.auth;

import com.rishabh.ecom.auth.AuthDtos.JwtResponse;
import com.rishabh.ecom.auth.AuthDtos.Me;
import com.rishabh.ecom.user.Role;
import com.rishabh.ecom.user.User;
import com.rishabh.ecom.user.RoleRepository;
import com.rishabh.ecom.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEnabled(true);
        testUser.getRoles().add(userRole);
    }

    @Test
    @DisplayName("Should signup new user successfully")
    void shouldSignupNewUser() {
        // Given
        String email = "newuser@example.com";
        String password = "Password123!";
        String encodedPassword = "$2a$10$encoded";
        String token = "jwt-token";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString(), any(Set.class))).thenReturn(token);
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        // When
        JwtResponse response = authService.signup(email, password);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(anyString(), any(Set.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        String email = "existing@example.com";
        String password = "Password123!";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.signup(email, password))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Email already exists");
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "Password123!";
        String token = "jwt-token";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(Set.class))).thenReturn(token);
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        // When
        JwtResponse response = authService.login(email, password);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testUser.getPassword());
        verify(jwtService, times(1)).generateToken(anyString(), any(Set.class));
    }

    @Test
    @DisplayName("Should throw exception when email not found")
    void shouldThrowExceptionWhenEmailNotFound() {
        // Given
        String email = "notfound@example.com";
        String password = "Password123!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email or password");
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password incorrect")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        String email = "test@example.com";
        String password = "WrongPassword!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email or password");
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testUser.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when account disabled")
    void shouldThrowExceptionWhenAccountDisabled() {
        // Given
        String email = "test@example.com";
        String password = "Password123!";
        testUser.setEnabled(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Account is disabled");
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should get current user info")
    void shouldGetCurrentUserInfo() {
        // Given
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Me me = authService.me(email);

        // Then
        assertThat(me).isNotNull();
        assertThat(me.email()).isEqualTo(email);
        assertThat(me.roles()).contains("ROLE_USER");
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when user not found for me")
    void shouldThrowExceptionWhenUserNotFoundForMe() {
        // Given
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.me(email))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");
        verify(userRepository, times(1)).findByEmail(email);
    }
}


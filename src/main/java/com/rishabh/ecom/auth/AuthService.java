package com.rishabh.ecom.auth;

import com.rishabh.ecom.auth.AuthDtos.JwtResponse;
import com.rishabh.ecom.auth.AuthDtos.Me;
import com.rishabh.ecom.user.Role;
import com.rishabh.ecom.user.RoleRepository;
import com.rishabh.ecom.user.User;
import com.rishabh.ecom.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public JwtResponse signup(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        User user = new User(email, passwordEncoder.encode(rawPassword));
        user.getRoles().add(userRole);
        user = userRepository.save(user);

        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        String token = jwtService.generateToken(user.getEmail(), roleNames);
        return new JwtResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    public JwtResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.getEnabled()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        String token = jwtService.generateToken(user.getEmail(), roleNames);
        return new JwtResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    public Me me(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        return new Me(user.getEmail(), roleNames);
    }
}


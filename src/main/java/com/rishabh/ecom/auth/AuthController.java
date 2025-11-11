package com.rishabh.ecom.auth;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.JwtResponse> signup(@Valid @RequestBody AuthDtos.Signup request) {
        AuthDtos.JwtResponse response = authService.signup(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.JwtResponse> login(@Valid @RequestBody AuthDtos.Login request) {
        AuthDtos.JwtResponse response = authService.login(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    public ResponseEntity<AuthDtos.Me> me(Authentication authentication) {
        String email = authentication.getName();
        AuthDtos.Me response = authService.me(email);
        return ResponseEntity.ok(response);
    }
}


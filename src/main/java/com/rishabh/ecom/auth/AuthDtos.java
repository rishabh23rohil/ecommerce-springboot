package com.rishabh.ecom.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public final class AuthDtos {

    public record Signup(
            @Email @NotBlank String email,
            @Size(min = 8, max = 72) @NotBlank String password
    ) {}

    public record Login(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record JwtResponse(
            String token,
            String tokenType,
            long expiresInSeconds
    ) {}

    public record Me(
            String email,
            Set<String> roles
    ) {}
}


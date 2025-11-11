package com.rishabh.ecom.config;

import com.rishabh.ecom.auth.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/v1/healthz",
          "/actuator/health",
          "/v3/api-docs",
          "/v3/api-docs/**",
          "/swagger-ui",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/swagger-ui/index.html",
          "/swagger-resources/**",
          "/webjars/**",
          "/api/v1/auth/**"
        ).permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
          logger.error("Authentication failed for {}: {}", request.getRequestURI(), authException.getMessage());
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setContentType("application/json");
          response.getWriter().write("{\"error\":\"Unauthorized: " + authException.getMessage() + "\"}");
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          logger.error("Access denied for {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.setContentType("application/json");
          response.getWriter().write("{\"error\":\"Forbidden: " + accessDeniedException.getMessage() + "\"}");
        })
      )
      .formLogin(login -> login.disable())
      .httpBasic(basic -> basic.disable());

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

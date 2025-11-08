package com.rishabh.ecom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/v1/healthz",
          "/actuator/health",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html"
        ).permitAll()
        .anyRequest().permitAll()   // <- TEMPORARY until JWT (M4)
      )
      .formLogin(login -> login.disable())
      .httpBasic(basic -> basic.disable());

    return http.build();
  }
}

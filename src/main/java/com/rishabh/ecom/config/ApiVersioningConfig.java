package com.rishabh.ecom.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class ApiVersioningConfig {

    /**
     * API Versioning Strategy:
     * - URL-based versioning: /api/v1/, /api/v2/
     * - Header-based versioning: X-API-Version header (optional)
     * 
     * Current version: v1
     * Future versions: v2, v3, etc.
     */
    
    @Bean
    public FilterRegistrationBean<ApiVersionFilter> apiVersionFilter() {
        FilterRegistrationBean<ApiVersionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiVersionFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    public static class ApiVersionFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String path = request.getRequestURI();
            
            // Add API version header for tracking
            if (path.startsWith("/api/v1/")) {
                response.setHeader("X-API-Version", "v1");
            }
            
            filterChain.doFilter(request, response);
        }
    }
}


package com.ridelink.fare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig component.
 * SecurityConfig handles security and authentication concerns for the fare-payment service.
 *
 * This file is part of the RideLink fare-payment service and documents the
 * responsibility of the code defined in this class or helper.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // =========================================================================
    // SECTION 1: USER DETAILS SERVICE (JWT-Only Disabler)
    // =========================================================================
    /**
     * Replaces standard Spring Security username/password authentication.
     * Since this microservice relies 100% on stateless JWT tokens issued by auth-service,
     * traditional username lookups are intentionally disabled.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("JWT-only authentication");
        };
    }

    // =========================================================================
    // SECTION 2: SECURITY FILTER CHAIN (HTTP Request Authorization & Filters)
    // =========================================================================
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery) because REST APIs use Bearer tokens, not cookies
                .csrf(csrf -> csrf.disable())

                // 2. Enable Cross-Origin Resource Sharing with CorsConfig defaults
                .cors(Customizer.withDefaults())

                // 3. Make session management stateless (server does NOT maintain HttpSession state)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. URL Authorization rules:
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS pre-flight HTTP OPTIONS checks
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public access for API documentation (Swagger & OpenAPI UI)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        // Public health probe endpoint
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        // Require valid JWT authentication for all fare and payment business endpoints
                        .anyRequest().authenticated())

                // 6. Insert custom JwtAuthFilter BEFORE Spring's UsernamePasswordAuthenticationFilter
                //    so that incoming JWT is decoded and verified before authorization checks
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


package com.sponsorbox.security;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/creators/eligible").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/creators/verify-tiktok").permitAll()

                        // Endpoints admin — verification des createurs
                        .requestMatchers("/api/creators/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/creators/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/creators/*/reject").hasRole("ADMIN")

                        // Endpoints createur — lecture ouverte aux partenaires (profils, succes, temoignages)
                        .requestMatchers(HttpMethod.GET, "/api/creators/**").hasAnyRole("CREATOR", "PARTNER", "ADMIN")
                        .requestMatchers("/api/creators/**").hasAnyRole("CREATOR", "ADMIN")

                        // Endpoints partenaire
                        .requestMatchers("/api/partners/**").hasAnyRole("PARTNER", "ADMIN")

                        // Endpoints deals — accessibles aux createurs et partenaires
                        .requestMatchers("/api/deals/**").hasAnyRole("CREATOR", "PARTNER", "ADMIN")

                        // Endpoints messages — accessibles aux createurs et partenaires
                        .requestMatchers("/api/messages/**").hasAnyRole("CREATOR", "PARTNER", "ADMIN")

                        // Tout le reste necessite une authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return new UrlBasedCorsConfigurationSource() {{
            registerCorsConfiguration("/**", config);
        }};
    }
}

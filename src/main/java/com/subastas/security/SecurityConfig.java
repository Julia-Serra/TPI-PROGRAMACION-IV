package com.subastas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/registro.html",
                                "/subastas.html",
                                "/crear-subasta.html",
                                "/detalle.html",
                                "/perfil.html",
                                "/style.css",
                                "/app.js",
                                "/favicon.ico",
                                "/img/**"
                        ).permitAll()

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/usuarios/me").authenticated()

                        .requestMatchers(HttpMethod.GET, "/subastas/**")
                        .hasAnyRole("COMPRADOR", "VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/subastas/**")
                        .hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/subastas/**")
                        .hasAnyRole("VENDEDOR", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/subastas/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/pujas/**")
                        .hasAnyRole("COMPRADOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/pujas/**")
                        .hasAnyRole("COMPRADOR", "VENDEDOR", "ADMIN")

                        .requestMatchers("/disputas/**")
                        .hasAnyRole("COMPRADOR", "VENDEDOR", "ADMIN")

                        .requestMatchers("/usuarios/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
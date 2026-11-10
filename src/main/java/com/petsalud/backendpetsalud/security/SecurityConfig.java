
package com.petsalud.backendpetsalud.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/api/auth/login",
                    "/api/usuarios/registro"
                ).permitAll()

                .requestMatchers(
                    "/api/usuarios/**",
                    "/api/ventas/**"
                ).hasAuthority("GERENTE")


                .requestMatchers(
                    "/api/productos/**",
                    "/api/expedientes/**"
                ).hasAnyAuthority("EMPLEADO", "GERENTE")

   
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/citas/veterinario/**",
                    "/api/citas/entre-fechas"
                ).hasAnyAuthority("EMPLEADO", "GERENTE")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/citas/*/estado"
                ).hasAnyAuthority("EMPLEADO", "GERENTE")

                .requestMatchers(
                    "/api/citas/**",
                    "/api/mascotas/**"
                ).hasAnyAuthority(
                    "CLIENTE", "EMPLEADO", "GERENTE"
                )

                .requestMatchers(
                    "/api/notificaciones/**",
                    "/api/recompensas/**"
                ).hasAnyAuthority(
                    "CLIENTE", "EMPLEADO", "GERENTE"
                )
                .anyRequest().denyAll()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}

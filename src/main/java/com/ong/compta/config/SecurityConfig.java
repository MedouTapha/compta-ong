package com.ong.compta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**").permitAll()
                .requestMatchers(new AntPathRequestMatcher("/listes-paiement/*/valider", "POST")).hasRole("DIRECTEUR")
                .requestMatchers(new AntPathRequestMatcher("/decaissements/*/debloquer-budget", "POST")).hasRole("DIRECTEUR")
                .requestMatchers("/listes-paiement/*/imprimer").hasAnyRole("COMPTABLE", "DIRECTEUR")
                .requestMatchers("/etats/**").authenticated()
                .requestMatchers("/comptes/**", "/financements/**", "/destinations/**", "/budgets/**",
                        "/tresorerie/**", "/ecritures/**", "/enfants/**", "/tuteurs/**", "/aides/**",
                        "/listes-paiement/**", "/lignes-paiement/**", "/decaissements/**")
                        .hasRole("COMPTABLE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout.permitAll())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
}

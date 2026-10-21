package com.gestioncommerciale.backend.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Filtre qui lit le token JWT envoyé par le frontend et authentifie la requête.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Cette méthode configure les règles de sécurité de l'application.
    // Elle dit quelles routes sont publiques et lesquelles nécessitent un token.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Désactive CSRF pour une API REST simple.
                // Principe du moindre privilège : chaque route n'est ouverte qu'aux rôles qui en ont besoin.
                // Spring Security applique la première règle qui correspond : les règles précises
                // (méthode HTTP + chemin) passent donc avant les règles générales (chemin seul).
                .authorizeHttpRequests(auth -> auth
                        // Ces routes sont accessibles sans token.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()

                        // ADMIN uniquement : suppressions (commandes-fournisseurs n'expose aucun DELETE),
                        // journal d'audit et déclenchement manuel de l'alerte stock.
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/clients/**", "/api/produits/**", "/api/commandes/**", "/api/fournisseurs/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/audit-logs/**", "/api/notifications/**").hasRole("ADMIN")

                        // Les 3 rôles métier : consultation des clients et des produits
                        // (y compris la recherche par code-barres utilisée à la caisse).
                        .requestMatchers(HttpMethod.GET, "/api/clients/**", "/api/produits/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE", "CAISSIER")

                        // ADMIN et GESTIONNAIRE : création et modification des clients et des produits
                        // (les GET et DELETE ont déjà été traités plus haut).
                        .requestMatchers("/api/clients/**", "/api/produits/**").hasAnyRole("ADMIN", "GESTIONNAIRE")
                        // ADMIN et GESTIONNAIRE : tableau de bord, stock, fournisseurs (hors DELETE)
                        // et commandes d'achat.
                        .requestMatchers("/api/dashboard/**", "/api/stock/**", "/api/fournisseurs/**",
                                "/api/commandes-fournisseurs/**")
                        .hasAnyRole("ADMIN", "GESTIONNAIRE")

                        // Les 3 rôles métier : prise de commande, annulation et facture PDF (hors DELETE).
                        .requestMatchers("/api/commandes/**").hasAnyRole("ADMIN", "GESTIONNAIRE", "CAISSIER")

                        // Filet de sécurité : toute route non listée exige au moins d'être authentifié.
                        .anyRequest().authenticated())
                // On ne garde pas de session pour cette API, car on utilise des JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Le filtre JWT doit s'exécuter avant le filtre d'authentification standard,
                // sinon Spring Security ignore l'authentification qu'il pose dans le contexte.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // "Authorization" doit être listé explicitement : avec allowCredentials(true),
        // certains navigateurs n'acceptent pas "*" comme joker pour les en-têtes autorisés.
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Permet à Spring Security de gérer l'authentification des utilisateurs.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

package com.gestioncommerciale.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateToken() {
        JwtService jwtService = new JwtService("c2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQtcHJvamVjdA==", 3600000L);
        UserDetails user = User.withUsername("admin")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build();

        String token = jwtService.generateToken(user.getUsername(), "ROLE_ADMIN");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void doitRefuserDeDemarrerSansCleJwt() {
        // Plus de clé de secours dans le code : un secret vide doit bloquer le démarrage.
        for (String secretVide : new String[] { null, "", "   " }) {
            assertThatThrownBy(() -> new JwtService(secretVide, 3600000L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("JWT_SECRET");
        }
    }

    @Test
    void doitRefuserUneCleTropCourte() {
        // 31 caractères bruts, et "abcd" : du Base64 valide mais qui ne donne que 3 octets.
        for (String secretCourt : new String[] { "a".repeat(31), "abcd" }) {
            assertThatThrownBy(() -> new JwtService(secretCourt, 3600000L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("trop courte")
                    .hasMessageContaining("JWT_SECRET");
        }
    }

    @Test
    void doitAccepterUneCleBruteDe32Caracteres() {
        JwtService jwtService = new JwtService("a".repeat(32), 3600000L);

        String token = jwtService.generateToken("admin", "ROLE_ADMIN");

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }
}

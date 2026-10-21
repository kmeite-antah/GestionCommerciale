package com.gestioncommerciale.backend.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// Documentation Swagger : déclare le schéma "bearerAuth" (en-tête Authorization: Bearer <jwt>)
// et l'applique à toutes les routes. Swagger UI affiche alors un bouton "Authorize" où coller
// le token renvoyé par /auth/login. Les routes publiques s'en exemptent (@SecurityRequirements vide).
// Les règles d'accès réelles restent celles de SecurityConfig.
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Gestion Commerciale API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}

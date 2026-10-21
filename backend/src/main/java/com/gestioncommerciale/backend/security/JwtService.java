package com.gestioncommerciale.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Clé secrète utilisée pour signer les tokens JWT.
    // Elle permet de vérifier que le token n'a pas été modifié.
    private final SecretKey secretKey;

    // Durée de vie du token en millisecondes.
    private final long jwtExpirationMs;

    public JwtService(@Value("${jwt.secret:}") String secret,
                      @Value("${jwt.expiration-ms:3600000}") long jwtExpirationMs) {
        // Aucune clé de secours dans le code : une clé connue de tous permettrait de forger
        // des tokens valides. Sans secret configuré, l'application refuse de démarrer.
        // En local, le profil "dev" (application-dev.properties) fournit une clé de développement.
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Aucune clé JWT configurée (jwt.secret est vide). "
                            + "Définir la variable d'environnement JWT_SECRET (au moins 32 caractères).");
        }

        // On prépare la clé de signature du JWT.
        this.secretKey = buildSecretKey(secret);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // Taille minimale d'une clé HMAC-SHA256 : 256 bits, soit 32 octets.
    private static final int TAILLE_MIN_CLE_OCTETS = 32;

    // Crée une clé secrète compatible avec la librairie JWT.
    // Une clé trop courte est refusée : la compléter avec une suite fixe (connue de tous)
    // ne la rendrait pas plus sûre, seulement plus longue.
    private SecretKey buildSecretKey(String secret) {
        // Si la clé est encodée en Base64 (ex: openssl rand -base64 48) et assez longue une
        // fois décodée, on utilise les octets décodés.
        try {
            byte[] decodee = Base64.getDecoder().decode(secret);
            if (decodee.length >= TAILLE_MIN_CLE_OCTETS) {
                return Keys.hmacShaKeyFor(decodee);
            }
        } catch (IllegalArgumentException ex) {
            // Pas du Base64 valide : on utilise la chaîne brute ci-dessous.
        }

        byte[] brute = secret.getBytes(StandardCharsets.UTF_8);
        if (brute.length < TAILLE_MIN_CLE_OCTETS) {
            throw new IllegalStateException(
                    "Clé JWT trop courte (" + brute.length + " octets). "
                            + "Définir la variable d'environnement JWT_SECRET avec au moins "
                            + TAILLE_MIN_CLE_OCTETS + " caractères.");
        }
        return Keys.hmacShaKeyFor(brute);
    }

    // Génère un token JWT pour un utilisateur donné, en y intégrant son rôle
    // (ex: "ROLE_ADMIN") afin que le frontend puisse l'exploiter sans appel supplémentaire.
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username) // Le nom de l'utilisateur est stocké dans le token.
                .claim("role", role) // Rôle de l'utilisateur (ex: ROLE_ADMIN).
                .issuedAt(now)      // Date de création du token.
                .expiration(expiryDate) // Date d'expiration du token.
                .signWith(secretKey) // Signature cryptographique du token.
                .compact();
    }

    // Extrait le nom d'utilisateur depuis le token.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Vérifie si le token est encore valide pour cet utilisateur.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Vérifie si le token a expiré.
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Récupère la date d'expiration du token.
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Méthode interne pour lire une information précise dans le token.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}

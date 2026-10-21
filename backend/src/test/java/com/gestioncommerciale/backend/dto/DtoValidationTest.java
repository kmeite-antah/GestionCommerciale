package com.gestioncommerciale.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

// Les règles Bean Validation des DTO doivent être au moins aussi strictes que celles
// des formulaires Angular (téléphone à 10 chiffres, prix strictement positif).
class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initialiserValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fermerValidator() {
        factory.close();
    }

    private static ClientDTO clientValide() {
        return new ClientDTO(null, "Kouassi", "Jean", "jean.kouassi@email.com", "0102030405", "Abidjan, Cocody");
    }

    private static ProduitDTO produitValide() {
        return new ProduitDTO(null, "Clé USB 64Go", "", new BigDecimal("5000"), 10, "Informatique", null);
    }

    @Test
    void clientAvecTelephoneDe10ChiffresEstValide() {
        assertThat(validator.validate(clientValide())).isEmpty();
    }

    @Test
    void clientAvecTelephoneHorsFormatEstRefuse() {
        // Trop court, trop long, ou contenant autre chose que des chiffres.
        for (String telephone : new String[] { "010203040", "01020304050", "+225010203", "01 02 03 04" }) {
            ClientDTO client = clientValide();
            client.setTelephone(telephone);

            Set<ConstraintViolation<ClientDTO>> violations = validator.validate(client);

            assertThat(violations)
                    .as("téléphone \"%s\"", telephone)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Le numéro de téléphone doit comporter exactement 10 chiffres");
        }
    }

    @Test
    void produitAvecPrixPositifEstValide() {
        ProduitDTO produit = produitValide();
        produit.setPrix(new BigDecimal("0.01"));

        assertThat(validator.validate(produit)).isEmpty();
    }

    @Test
    void produitAvecPrixNulOuNegatifEstRefuse() {
        for (String prix : new String[] { "0", "0.00", "-1" }) {
            ProduitDTO produit = produitValide();
            produit.setPrix(new BigDecimal(prix));

            Set<ConstraintViolation<ProduitDTO>> violations = validator.validate(produit);

            assertThat(violations)
                    .as("prix %s", prix)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Le prix doit être supérieur à 0");
        }
    }
}

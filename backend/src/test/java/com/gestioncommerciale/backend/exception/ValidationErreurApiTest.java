package com.gestioncommerciale.backend.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// Vérifie que GlobalExceptionHandler transforme une erreur de validation @Valid
// en réponse 400 contenant le message du champ en faute.
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class ValidationErreurApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void telephoneInvalideRenvoie400AvecLeMessage() throws Exception {
        String client = """
                {"nom": "Kouassi", "prenom": "Jean", "email": "jean@email.com", "telephone": "01020304"}
                """;

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(client))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.telephone").value("Le numéro de téléphone doit comporter exactement 10 chiffres"));
    }

    @Test
    void prixNulRenvoie400AvecLeMessage() throws Exception {
        String produit = """
                {"nom": "Clé USB", "prix": 0, "quantite": 5}
                """;

        mockMvc.perform(post("/api/produits").contentType(MediaType.APPLICATION_JSON).content(produit))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.prix").value("Le prix doit être supérieur à 0"));
    }
}

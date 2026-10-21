package com.gestioncommerciale.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

// Prouve la matrice d'accès de SecurityConfig, rôle par rôle : le serveur refuse (403) ce
// que l'interface masque. Base H2 de test (src/test/resources/application.properties,
// profil "test") : le seeder des comptes de démo ne tourne pas, d'où @WithMockUser.
// Une requête autorisée peut renvoyer 200, 400 ou 404 selon les données : seule compte
// l'absence de 401 / 403.
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAutorisationsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    private static ResultMatcher autorise() {
        return result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403);
    }

    private static ResultMatcher refuse() {
        return result -> assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void leSeederDesComptesDeDemoNeTournePasEnTest() {
        assertThat(context.getBeanNamesForType(UtilisateurSeeder.class)).isEmpty();
    }

    // ===== Sans authentification =====

    @Test
    void anonymeNePeutPasConsulterLesProduits() throws Exception {
        mockMvc.perform(get("/api/produits")).andExpect(refuse());
    }

    @Test
    void routesPubliquesAccessiblesSansToken() throws Exception {
        // /auth/login n'est pas testé ici : avec de faux identifiants il renvoie 401 par conception.
        mockMvc.perform(get("/actuator/health")).andExpect(autorise());
        mockMvc.perform(get("/v3/api-docs")).andExpect(autorise());
    }

    // ===== CAISSIER =====

    @Test
    @WithMockUser(roles = "CAISSIER")
    void caissierNePeutPasSupprimerUnClient() throws Exception {
        mockMvc.perform(delete("/api/clients/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAISSIER")
    void caissierNePeutPasCreerUnProduit() throws Exception {
        mockMvc.perform(post("/api/produits").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAISSIER")
    void caissierNePeutPasConsulterLeTableauDeBord() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAISSIER")
    void caissierPeutConsulterLesProduits() throws Exception {
        mockMvc.perform(get("/api/produits")).andExpect(autorise());
    }

    @Test
    @WithMockUser(roles = "CAISSIER")
    void caissierPeutVendreMaisPasGererLeReste() throws Exception {
        mockMvc.perform(get("/api/clients")).andExpect(autorise());
        mockMvc.perform(get("/api/produits/recherche/code-barre").param("code", "123")).andExpect(autorise());
        mockMvc.perform(get("/api/commandes")).andExpect(autorise());
        mockMvc.perform(put("/api/commandes/999/annuler")).andExpect(autorise());

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/clients/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/produits/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/commandes/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/stock/entree").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/fournisseurs")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/commandes-fournisseurs")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/audit-logs")).andExpect(status().isForbidden());
    }

    // ===== GESTIONNAIRE =====

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnaireNePeutPasSupprimerUnFournisseur() throws Exception {
        mockMvc.perform(delete("/api/fournisseurs/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnaireNePeutPasConsulterLeJournalDAudit() throws Exception {
        mockMvc.perform(get("/api/audit-logs")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnairePeutConsulterLeTableauDeBord() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")).andExpect(autorise());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void gestionnaireGereSansSupprimer() throws Exception {
        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(autorise());
        mockMvc.perform(put("/api/produits/999").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(autorise());
        mockMvc.perform(get("/api/stock/historique")).andExpect(autorise());
        mockMvc.perform(get("/api/fournisseurs")).andExpect(autorise());
        mockMvc.perform(get("/api/commandes-fournisseurs")).andExpect(autorise());
        mockMvc.perform(patch("/api/commandes-fournisseurs/999/valider")).andExpect(autorise());

        mockMvc.perform(delete("/api/clients/1")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/produits/1")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/commandes/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/notifications/test-alerte-stock")).andExpect(status().isForbidden());
    }

    // ===== ADMIN =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPeutSupprimerUnClient() throws Exception {
        // Le client 1 n'existe pas dans la base H2 vide : 404 attendu, mais pas 403.
        mockMvc.perform(delete("/api/clients/1")).andExpect(autorise());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPeutConsulterLeJournalDAudit() throws Exception {
        mockMvc.perform(get("/api/audit-logs")).andExpect(autorise());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPeutSupprimerToutesLesRessources() throws Exception {
        mockMvc.perform(delete("/api/produits/999")).andExpect(autorise());
        mockMvc.perform(delete("/api/commandes/999")).andExpect(autorise());
        mockMvc.perform(delete("/api/fournisseurs/999")).andExpect(autorise());
    }
}

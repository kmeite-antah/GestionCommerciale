package com.gestioncommerciale.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.gestioncommerciale.backend.security.UtilisateurSeeder;

@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
	}

	// Les comptes de démonstration (mots de passe publics) ne sont créés qu'en profil "dev".
	@Test
	void seederDesComptesDeDemoInactifHorsProfilDev() {
		assertThat(context.getBeanNamesForType(UtilisateurSeeder.class)).isEmpty();
	}

}

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { HomeComponent } from './home';
import { AuthService } from '../../services/auth.service';

// La page d'accueil n'affiche que les blocs dont le serveur autorise les appels au rôle
// connecté. Les composants enfants sont remplacés par de simples balises pour ne tester
// que les conditions d'affichage de home.html, sans appel HTTP.
describe('HomeComponent - blocs affichés selon le rôle', () => {
  async function afficherPour(role: string): Promise<HTMLElement> {
    const authService = {
      getRole: () => role,
      hasRole: (r: string) => r === role,
      hasAnyRole: (roles: string[]) => roles.includes(role),
    };

    TestBed.overrideComponent(HomeComponent, {
      set: { imports: [], schemas: [CUSTOM_ELEMENTS_SCHEMA] },
    });
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [{ provide: AuthService, useValue: authService }],
    }).compileComponents();

    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it("le caissier voit clients, produits et commandes, mais ni l'historique de stock ni la gestion", async () => {
    const page = await afficherPour('ROLE_CAISSIER');

    expect(page.querySelector('app-client-list')).not.toBeNull();
    expect(page.querySelector('app-produit-list')).not.toBeNull();
    expect(page.querySelector('app-commande-list')).not.toBeNull();
    expect(page.querySelector('app-historique-stock')).toBeNull();
    expect(page.querySelector('app-dashboard')).toBeNull();
    expect(page.querySelector('app-fournisseur-list')).toBeNull();
    expect(page.querySelector('app-commande-fournisseur')).toBeNull();
    expect(page.querySelector('app-audit-logs')).toBeNull();
  });

  it("le gestionnaire voit l'historique de stock, mais pas le journal d'audit", async () => {
    const page = await afficherPour('ROLE_GESTIONNAIRE');

    expect(page.querySelector('app-historique-stock')).not.toBeNull();
    expect(page.querySelector('app-dashboard')).not.toBeNull();
    expect(page.querySelector('app-fournisseur-list')).not.toBeNull();
    expect(page.querySelector('app-audit-logs')).toBeNull();
  });

  it("l'administrateur voit tous les blocs", async () => {
    const page = await afficherPour('ROLE_ADMIN');

    expect(page.querySelector('app-historique-stock')).not.toBeNull();
    expect(page.querySelector('app-audit-logs')).not.toBeNull();
  });
});

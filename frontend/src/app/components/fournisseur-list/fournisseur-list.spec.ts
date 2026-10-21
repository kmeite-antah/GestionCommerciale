import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { FournisseurListComponent } from './fournisseur-list';
import { AuthService } from '../../services/auth.service';
import { FournisseurService } from '../../services/fournisseur.service';

// Le serveur réserve DELETE /api/fournisseurs à l'administrateur : le gestionnaire
// ne doit pas voir le bouton Supprimer.
describe('FournisseurListComponent - actions selon le rôle', () => {
  async function afficherPour(role: string): Promise<HTMLElement> {
    const authService = {
      getRole: () => role,
      hasRole: (r: string) => r === role,
      hasAnyRole: (roles: string[]) => roles.includes(role),
    };
    const fournisseurService = {
      getFournisseurs: () => of({
        content: [{ id: 1, nom: 'Sotra Distribution', email: 'contact@sotra.ci', telephone: '0102030405' }],
        totalElements: 1, totalPages: 1, number: 0, size: 5,
      }),
    };

    await TestBed.configureTestingModule({
      imports: [FournisseurListComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: FournisseurService, useValue: fournisseurService },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(FournisseurListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('le gestionnaire peut modifier mais ne voit pas Supprimer', async () => {
    const page = await afficherPour('ROLE_GESTIONNAIRE');

    expect(page.textContent).toContain('Sotra Distribution');
    expect(page.textContent).toContain('Modifier');
    expect(page.textContent).not.toContain('Supprimer');
  });

  it("l'administrateur voit Supprimer", async () => {
    const page = await afficherPour('ROLE_ADMIN');

    expect(page.textContent).toContain('Supprimer');
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ProduitListComponent } from './produit-list';
import { AuthService } from '../../services/auth.service';
import { ProduitService } from '../../services/produit.service';

describe('ProduitListComponent', () => {
  let component: ProduitListComponent;
  let fixture: ComponentFixture<ProduitListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProduitListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProduitListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

// Le serveur refuse au caissier POST/PUT produit et l'entrée de stock, et refuse DELETE
// au gestionnaire : l'interface doit masquer les mêmes actions pour chaque rôle.
describe('ProduitListComponent - actions selon le rôle', () => {
  async function afficherPour(role: string): Promise<HTMLElement> {
    const authService = {
      getRole: () => role,
      hasRole: (r: string) => r === role,
      hasAnyRole: (roles: string[]) => roles.includes(role),
    };
    const produitService = {
      getProduits: () => of({
        content: [{ id: 1, nom: 'Clé USB 64Go', description: '', prix: 5000, quantite: 50 }],
        totalElements: 1, totalPages: 1, number: 0, size: 5,
      }),
    };

    await TestBed.configureTestingModule({
      imports: [ProduitListComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ProduitService, useValue: produitService },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ProduitListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('le caissier ne voit ni formulaire, ni Modifier, ni Réapprovisionner, ni Supprimer', async () => {
    const page = await afficherPour('ROLE_CAISSIER');

    expect(page.textContent).toContain('Clé USB 64Go');
    expect(page.querySelector('form')).toBeNull();
    expect(page.textContent).not.toContain('Modifier');
    expect(page.textContent).not.toContain('Réapprovisionner');
    expect(page.textContent).not.toContain('Supprimer');
  });

  it('le gestionnaire voit le formulaire, Modifier et Réapprovisionner, mais pas Supprimer', async () => {
    const page = await afficherPour('ROLE_GESTIONNAIRE');

    expect(page.querySelector('form')).not.toBeNull();
    expect(page.textContent).toContain('Modifier');
    expect(page.textContent).toContain('Réapprovisionner');
    expect(page.textContent).not.toContain('Supprimer');
  });

  it("l'administrateur voit toutes les actions", async () => {
    const page = await afficherPour('ROLE_ADMIN');

    expect(page.querySelector('form')).not.toBeNull();
    expect(page.textContent).toContain('Modifier');
    expect(page.textContent).toContain('Réapprovisionner');
    expect(page.textContent).toContain('Supprimer');
  });
});

// Logique pure : on instancie la classe directement, les dépendances ne sont pas utilisées.
describe('ProduitListComponent - niveauStock', () => {
  const component = new ProduitListComponent(
    {} as any, {} as any, {} as any, {} as any, {} as any, {} as any
  );

  it('suit les seuils partagés avec le tableau de bord', () => {
    expect(component.niveauStock(0)).toBe('rupture');
    expect(component.niveauStock(1)).toBe('critique');
    expect(component.niveauStock(4)).toBe('critique');
    expect(component.niveauStock(5)).toBe('bas');
    expect(component.niveauStock(9)).toBe('bas');
    expect(component.niveauStock(10)).toBe('ok');
  });
});

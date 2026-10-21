import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ClientListComponent } from './client-list';
import { AuthService } from '../../services/auth.service';
import { ClientService } from '../../services/client';

describe('ClientListComponent', () => {
  let component: ClientListComponent;
  let fixture: ComponentFixture<ClientListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

// Le serveur refuse POST/PUT au caissier et DELETE au gestionnaire : l'interface doit
// masquer les mêmes actions pour chaque rôle.
describe('ClientListComponent - actions selon le rôle', () => {
  async function afficherPour(role: string): Promise<HTMLElement> {
    const authService = {
      getRole: () => role,
      hasRole: (r: string) => r === role,
      hasAnyRole: (roles: string[]) => roles.includes(role),
    };
    const clientService = {
      getClients: () => of({
        content: [{ id: 1, nom: 'Kouassi', prenom: 'Jean', email: 'jean@exemple.com', telephone: '0102030405' }],
        totalElements: 1, totalPages: 1, number: 0, size: 5,
      }),
    };

    await TestBed.configureTestingModule({
      imports: [ClientListComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ClientService, useValue: clientService },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ClientListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('le caissier ne voit ni formulaire, ni Modifier, ni Supprimer', async () => {
    const page = await afficherPour('ROLE_CAISSIER');

    expect(page.textContent).toContain('Kouassi');
    expect(page.querySelector('form')).toBeNull();
    expect(page.textContent).not.toContain('Modifier');
    expect(page.textContent).not.toContain('Supprimer');
  });

  it('le gestionnaire voit le formulaire et Modifier, mais pas Supprimer', async () => {
    const page = await afficherPour('ROLE_GESTIONNAIRE');

    expect(page.querySelector('form')).not.toBeNull();
    expect(page.textContent).toContain('Modifier');
    expect(page.textContent).not.toContain('Supprimer');
  });

  it("l'administrateur voit toutes les actions", async () => {
    const page = await afficherPour('ROLE_ADMIN');

    expect(page.querySelector('form')).not.toBeNull();
    expect(page.textContent).toContain('Modifier');
    expect(page.textContent).toContain('Supprimer');
  });
});

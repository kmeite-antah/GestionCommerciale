import { messageErreurApi } from './message-erreur-api';

describe('messageErreurApi', () => {
  it('renvoie le texte brut renvoyé par le backend (404, 409...)', () => {
    expect(messageErreurApi({ error: 'Cet email existe déjà' }, 'défaut')).toBe('Cet email existe déjà');
  });

  it("renvoie le premier message d'une erreur de validation { champ: message }", () => {
    const err = { error: { telephone: 'Le numéro de téléphone doit comporter exactement 10 chiffres' } };

    expect(messageErreurApi(err, 'défaut')).toBe('Le numéro de téléphone doit comporter exactement 10 chiffres');
  });

  it('renvoie le message par défaut si le corps est vide ou inexploitable', () => {
    expect(messageErreurApi({ error: null }, 'défaut')).toBe('défaut');
    expect(messageErreurApi({ error: '' }, 'défaut')).toBe('défaut');
    expect(messageErreurApi({ error: { statut: 500 } }, 'défaut')).toBe('défaut');
  });
});

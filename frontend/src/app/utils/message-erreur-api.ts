import { HttpErrorResponse } from '@angular/common/http';

// Extrait un message lisible d'une erreur renvoyée par le backend.
// GlobalExceptionHandler renvoie soit un texte simple (404, 409...), soit, pour une erreur
// de validation (400), un objet { champ: message } : on affiche alors le premier message.
export function messageErreurApi(err: HttpErrorResponse | { error?: unknown }, messageParDefaut: string): string {
  const corps = err?.error;

  if (typeof corps === 'string' && corps.trim() !== '') {
    return corps;
  }

  if (corps && typeof corps === 'object') {
    const premierMessage = Object.values(corps).find(
      (valeur): valeur is string => typeof valeur === 'string' && valeur.trim() !== ''
    );
    if (premierMessage) {
      return premierMessage;
    }
  }

  return messageParDefaut;
}

package com.gestioncommerciale.backend.service;

// Seuils de stock partagés par le tableau de bord et l'alerte quotidienne par email.
// Le frontend utilise les mêmes valeurs (src/app/constants/stock-seuils.ts) : les deux
// fichiers doivent rester identiques.
public final class StockSeuils {

    // Un produit est signalé (stock bas) dès que sa quantité est inférieure ou égale à ce seuil.
    public static final int SEUIL_STOCK_BAS = 9;

    // Sous ce seuil (strictement), le produit passe de "stock bas" à "rupture imminente".
    public static final int SEUIL_RUPTURE_IMMINENTE = 5;

    private StockSeuils() {
    }

    public static boolean estRuptureImminente(int quantite) {
        return quantite < SEUIL_RUPTURE_IMMINENTE;
    }
}

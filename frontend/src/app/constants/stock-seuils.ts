// Seuils de stock partagés par le tableau de bord et la liste des produits.
// Ils doivent rester identiques à ceux du backend (StockSeuils.java).

// Un produit est signalé (stock bas) dès que sa quantité est inférieure ou égale à ce seuil.
export const SEUIL_STOCK_BAS = 9;

// Sous ce seuil (strictement), le produit passe de "stock bas" à "rupture imminente".
export const SEUIL_RUPTURE_IMMINENTE = 5;

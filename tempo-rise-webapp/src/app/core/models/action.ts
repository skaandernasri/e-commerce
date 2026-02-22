type ActionType = 
  | "VIEW_PRODUCT"
  | "ADD_TO_CART"
  | "REMOVE_FROM_CART"
  | "PURCHASE"
  | "LEAVE_REVIEW"
  | "SEARCH_CLICK";

export interface Action {
  utilisateurId?: number | null;
  produitId?: number;
  typeAction?: ActionType;
  utilisateurAnonymeUuid?: string | null;
}

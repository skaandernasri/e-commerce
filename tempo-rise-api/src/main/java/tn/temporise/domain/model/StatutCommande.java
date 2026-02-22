package tn.temporise.domain.model;

public enum StatutCommande {

  /**
   * The order has been created and is currently being processed. Typically means the seller has
   * acknowledged the order but has not yet shipped it.
   */
  EN_COURS,

  /**
   * The order has been shipped to the customer. The parcel is on its way but not yet delivered.
   */
  EXPEDIEE,

  /**
   * The order has been successfully delivered to the customer. The delivery is confirmed but the
   * transaction may still be open (e.g., for potential return).
   */
  LIVREE,

  /**
   * The order has been cancelled by the customer or the seller. No further processing occurs and
   * stock is restored if previously reserved.
   */
  ANNULEE,

  /**
   * The order has been returned after delivery. Usually indicates that the customer sent back the
   * item and refund or replacement is pending.
   */
  RETOUR,

  /**
   * The order has been confirmed manually or automatically by the seller. Typically means the order
   * will definitely be prepared and cannot be cancelled.
   */
  CONFIRMEE,

  /**
   * The order is being prepared for shipment. This includes picking, packing, or quality checking
   * before dispatch.
   */
  EN_COURS_PREPARATION,

  /**
   * The order process is fully completed. The customer received the product, accepted it, and no
   * return or refund is possible.
   */
  TERMINEE,

  /**
   * The order has been placed but is waiting for confirmation or payment. Stock may be reserved but
   * no further action is taken until validation.
   */
  EN_ATTENTE

  // EN_ATTENTE → CONFIRMEE → EN_COURS_PREPARATION → EXPEDIEE → LIVREE → TERMINEE is the normal
  // flow.
  // ANNULEE and RETOUR are terminal exception states.
  // TERMINEE is a terminal success state.
}

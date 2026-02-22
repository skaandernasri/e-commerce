package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class PanierVariantId implements Serializable {
  private Long panier_id;
  private Long variant_id;

  public PanierVariantId(Long panierId, Long variant_id) {
    this.panier_id = panierId;
    this.variant_id = variant_id;
  }

  public PanierVariantId() {}


  // equals() and hashCode() required


  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass())
      return false;
    PanierVariantId that = (PanierVariantId) object;
    return Objects.equals(panier_id, that.panier_id) && Objects.equals(variant_id, that.variant_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(panier_id, variant_id);
  }

  @Override
  public String toString() {
    return "PanierVariantId{" + "panier_id=" + panier_id + ", variant_id=" + variant_id + '}';
  }
}

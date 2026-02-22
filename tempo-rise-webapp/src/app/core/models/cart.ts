import { Product, variantResponse } from "./product";

export interface CartVariant extends variantResponse {
  cartQuantity: number; // quantity the user added to cart (local state)
}

export interface CartProduct extends Product {
  variants: CartVariant[];
}
export interface Cart {
  id: number;
  articles: CartProduct[];  // articles are CartProducts, which have CartVariants
}

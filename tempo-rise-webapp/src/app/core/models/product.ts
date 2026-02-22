import { Avis } from "./avis";
import { Category } from "./category";
import { ImageProduct } from "./image-product";
import { Promotion } from "./promotion";

export interface Product {
    id: number;
    nom: string;
    description: string;
    composition: string;
    guide:string;
    faq:string;
    prix: number;
    categorie: Category;
    promotions?: Promotion[];
    imageProduits:ImageProduct[];
    avis?: Avis[]; 
    newPrice: number;
    prixTotal: number;
    marque: string;
    variants: variantResponse[];
    quantite: number;
    couleur: string;
    taille: string;
    actif: boolean;
    // dateSuppression: string;
    // dateModification: string;
    // dateCreation: string;
}
export interface ProductPaginatedResponse {
    content: Product[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    size: number;
    maxPrice: number;
  }
export interface variantResponse{
    id?: number | null;
    productId?: number;
    size: string;
    color: string;
    cartQuantity?: number;
    quantity: number;
    availableQuantity?: number;
    reservedQuantity?: number;
}
export interface variantRequest{
    produitId: number;
    variants: variantResponse[]
}





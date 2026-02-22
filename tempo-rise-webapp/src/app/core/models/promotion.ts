import { Product } from "./product";

export type PromotionType = "FIXED" | "PERCENTAGE";
export interface Promotion {
    id: number;
    nom: string;
    description: string;
    reduction: number;
    type: PromotionType;
    dateDebut: string;
    dateFin: string;
    status:string,
    produitId:number,
    produit?:Product
}

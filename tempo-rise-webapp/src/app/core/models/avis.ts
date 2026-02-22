import { Product } from "./product";
import { User } from "./user";

export interface Avis {
    id: number;
    note: number;
    commentaire: string;
    datePublication: string;
    utilisateurId: number;
    produitId: number;
    utilisateur:User 
    produit:Product
}
export interface avisRequest {
    note: number;
    commentaire: string;
    datePublication: string;
    utilisateurId: number | null;
    produitId: number | null ;
    
}

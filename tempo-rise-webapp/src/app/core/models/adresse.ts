import { User } from "./user";

export interface AdresseRequest {
    id: number;
    utilisateurId: number;
    ligne1: string;
    ligne2: string;
    ville: string;
    codePostal: string;
    pays: string;
    type: string;
}
export interface AdresseResponse {
    id: number;
    utilisateur:User;
    ligne1: string;
    ligne2: string;
    ville: string;
    codePostal: string;
    pays: string;
    type: string;
}
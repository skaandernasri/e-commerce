export interface User {
    id: number;
    nom:string;
    prenom:string;
    email:string;
    telephone:number;
    password:string;
    roles:string [];
    isverified:boolean;
    status:string;
    providerId:string;
    imageUrl:string;
    loyaltyGroup:number;
}
export interface UserAnonymeRequest {
    sessionToken?:string | null;
}
export interface UserAnonymeResponse {
    id: number;
    sessionToken:string;
    createdAt:string;
}
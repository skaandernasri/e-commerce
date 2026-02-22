import { OrderResponse } from "./order";

export interface FactureResponse {
    id:number;
    commande:OrderResponse;
    dateEmission: string;
    total: number;
}
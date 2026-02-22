import { OrderResponse } from "./order";

export interface InvoiceRequest{
    id: number | null;
    total: number;
    commandeId: number;
}
export interface InvoiceResponse{
    id:number;
    dateEmission: string;
    total: number;
    commande: OrderResponse;
}
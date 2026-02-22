import { Order } from "./order"
import { User } from "./user"

type type = "AIDE" | "RETOUR";
type statusContact = "EN_COURS" | "RESOLU";
type refundMethod = "CARTE_BANCAIRE" | "VIREMENT";
export interface ContactDto{
    id: number,
    email: string,
    subject: string,
    message: string,
    type: type,
    statusContact: statusContact,
    refundMethod?: refundMethod,
    isRefunded?: boolean,
    refundedAt: string,
    createdAt: string,
    updatedAt: string,
    user:User,
    commande?:Order
}
export interface ContactStatusUpdateDto{
    statusContact: statusContact,
    refundMethod?: refundMethod,
    isRefunded?: boolean,
    adminResponse?: string
}
export interface ContactCreateRequestDto{
    subject: string,
    message: string,
    type: type,
    commandeId?: number,
    userId?: number,
    email: string
}
export interface ContactResponseDto{
    content: ContactDto[],
    totalElements: number,
    totalPages: number,
    currentPage: number,
    size: number
}
import {  AdresseResponse } from "./adresse";
import { CartProduct } from "./cart";
import { CodePromo } from "./code-promo";
import { ImageProduct } from "./image-product";
import { User } from "./user";

export interface Order {
    //id: number;
    utilisateurId: number;
    nom: string;
    prenom: string;
    email: string;
    telephone: number;
    token: string;
    total: number;
    codePromo: number;
    adresseLivraisonId: number;
    adresseFacturationId: number;
    shipping: number;
    produits: CartProduct[];
    // dateCreation: string;
}

export interface OrderResponse {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    telephone: number;
    utilisateur:User;
    produits:LigneCommande[];
    date:string;
    statut:string;
    modePaiement:string;
    total:number;
    codePromo:CodePromo;
    adresseLivraison:AdresseResponse;
    adresseFacturation:AdresseResponse;
    paiements: [{
      id: number;
      date: string;
      paiementRef: string;
      status:string;
    }]
  }
  export interface filteredPagedOrderResponse {
    content: OrderResponse[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    globalTotalElements: number;
    totalAmmount: number;
    currentWeekOrders: number;
    lastWeekOrders: number;
    currentWeekOrdersTotalAmount: number;
    lastWeekOrdersTotalAmount: number;
  }
  export interface PaymentResponse{
    payUrl:string
    paymentRef:string
  }

  export interface PaymentDetails {
    payment: {
      id: string;
      status: 'completed' | 'pending' | 'expired';
      amount: number;
      token: string;
      orderId: string;
      type: string;
      convertedAmount: number;
      exchangeRate: number;
      webhook: string; // URI
      successUrl: string; // URI
      failUrl: string; // URI
      acceptedPaymentMethods: string[];
      transactions: { [key: string]: any }[]; // array of object with unknown structure
      failedTransactions: number;
      successfulTransactions: number;
      paymentDetails: {
        phoneNumber: string;
        email: string;
        name: string;
      };
      createdAt: string; // ISO date-time string
      updatedAt: string; // ISO date-time string
      [key: string]: any; // support additional properties
    };
  }
  export interface CustomerForm{
    nom: string;
    prenom: string;
    email: string;
    telephone: number;
  }
  export interface LigneCommande {
      variantId: number;
      productId?: number;
      nom?: string;
      prix?: number;
      categorie?: string;
      imageProduits?:ImageProduct[];
      prixTotal: number;
      quantite: number;
      couleur?: string;
      taille?: string;
  }
  export interface LigneCommandeRequest {
    variantId: number | null | undefined;
    quantite: number;
    prixTotal: number;
    shipping: number;
  }
  export const ORDER_STATUS_LIST: string[] = ['EN_COURS' , 'EXPEDIEE' , 'LIVREE' , 'TERMINEE' , 'ANNULEE' , 'RETOUR' , 'EN_COURS_PREPARATION' , 'EN_ATTENTE' , 'CONFIRMEE' ,  'ALL'];
  export type ORDER_STATUS_TYPES = 'EN_COURS' | 'EXPEDIEE' | 'LIVREE' | 'TERMINEE' | 'ANNULEE' | 'RETOUR' | 'EN_COURS_PREPARATION' | 'EN_ATTENTE' | 'CONFIRMEE' |  'ALL';
  export const ORDER_STATUS_RECORDS : Record<string, string[]> = {
    'ALL': ['ORDER_DETAILS.ALL_STATUS'],
    'EN_COURS': ['ORDER_DETAILS.IN_PROGRESS', 'bg-blue-100 text-blue-800'],
    'EXPEDIEE': ['ORDER_DETAILS.SHIPPED', 'bg-orange-100 text-orange-800'],
    'LIVREE': ['ORDER_DETAILS.DELIVERED', 'bg-green-100 text-green-800'],
    'TERMINEE': ['ORDER_DETAILS.COMPLETED', 'bg-green-100 text-green-800'],
    'ANNULEE':  ['ORDER_DETAILS.ANNULED', 'bg-red-100 text-red-800'],
    'RETOUR': ['ORDER_DETAILS.RETURN', 'bg-gray-100 text-gray-800'],
    'EN_COURS_PREPARATION': ['ORDER_DETAILS.IN_PROGRESS_PREPARATION', 'bg-blue-100 text-blue-800'],
    'EN_ATTENTE': ['ORDER_DETAILS.PENDING', 'bg-yellow-100 text-yellow-800'],
    'CONFIRMEE': ['ORDER_DETAILS.CONFIRMED', 'bg-cyan-100 text-cyan-800'],
  };
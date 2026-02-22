export type TypePageDto = "ALL" | "HOME" | "PRODUCT" | "CONTACT" | "ABOUT" | "BLOG" | "TERMS_AND_CONDITIONS" | "PRODUCT_DETAILS" | "DELIVERY_AND_RETURN" | "LEGAL_NOTICE";
export const TYPE_PAGE_VALUES: TypePageDto[] = ["ALL", "HOME", "PRODUCT", "CONTACT", "ABOUT", "BLOG", "TERMS_AND_CONDITIONS", "PRODUCT_DETAILS", "DELIVERY_AND_RETURN", "LEGAL_NOTICE"];

export type HomePageSectionType = "FAQ" | "HERO" | "OUR_PRODUCTS" | "OUR_REVIEWS" | "CONTENT1" | "CONTENT2" | "CONTENT3" | "CONTENT4" | "CONTENT5" | "CONTENT6" | "CONTENT7" | "CONTENT8"
export const HOME_PAGE_SECTIONS: HomePageSectionType[] = ["FAQ", "HERO", "OUR_PRODUCTS", "OUR_REVIEWS", "CONTENT1", "CONTENT2", "CONTENT3", "CONTENT4", "CONTENT5", "CONTENT6", "CONTENT7", "CONTENT8"];

export type AllPageSectionType = "FOOTER" | "HEADER" | "BANNER_SCROLLING" | "RIGHT_BARS"
export const ALL_PAGE_SECTIONS: AllPageSectionType[] = ["FOOTER", "HEADER", "BANNER_SCROLLING", "RIGHT_BARS"];

export type AboutPageSectionType  = "HTML"
export const ABOUT_PAGE_SECTIONS: AboutPageSectionType[] = ["HTML"];

export type TACPageSectionType  = "HTML"
export const TAC_PAGE_SECTIONS: TACPageSectionType[] = ["HTML"];

export type ProductsPageSectionType  = "OUR_PRODUCTS"
export const PRODUCTS_PAGE_SECTIONS: ProductsPageSectionType[] = ["OUR_PRODUCTS"];

export type BlogPageSectionType  = "OUR_BLOGS"
export const BLOG_PAGE_SECTIONS: BlogPageSectionType[] = ["OUR_BLOGS"];

export type ProductDetailsPageSectionType = "DESCRIPTION" | "MAIN" | "OUR_REVIEWS" | "CONTENT1"
export const PRODUCT_DETAILS_PAGE_SECTIONS: ProductDetailsPageSectionType[] = ["DESCRIPTION", "MAIN", "OUR_REVIEWS", "CONTENT1"];

export type ContactPageSectionType = "SOCIATY_INFO"
export const CONTACT_PAGE_SECTIONS: ContactPageSectionType[] = ["SOCIATY_INFO"];

export type DARPageSectionType = "HTML"
export const DAR_PAGE_SECTIONS: DARPageSectionType[] = ["HTML"]

export type LegalNoticePageSectionType = "HTML"
export const LEGAL_NOTICE_PAGE_SECTIONS: LegalNoticePageSectionType[] = ["HTML"]

export type AllPagesSectionTypes = HomePageSectionType | AllPageSectionType | AboutPageSectionType | TACPageSectionType | ProductsPageSectionType | BlogPageSectionType | ProductDetailsPageSectionType | ContactPageSectionType | DARPageSectionType | LegalNoticePageSectionType

export interface ParamSectionRequest {
  id?: number;
  titre: string;
  contenuJson: string;
  contenuHtml: string;
  type: AllPagesSectionTypes;
  typePage: TypePageDto;
  active: boolean;
}

export interface ParamSectionResponse extends ParamSectionRequest {
  imageUrl: string;
}

export interface PageWithSections {
  type: TypePageDto;
  sections: string[];
}

type sections = "/faq"

export interface ImageTextPair {
  index: number;
  imageUrl: string;
  text: string;
}
export interface SocialLink {
  index: number;
  url: string;
  link: string;
  alt: string;
}

export interface GroupTextLink {
  index: number;
  linkType: 'PAGE_LINK' | 'SECTION_LINK' | 'EXTERNAL_LINK';
  text: string;
  link: string;
  isLink: boolean;
  imageUrl: string;
  selectedPage: TypePageDto;
  selectedSection:AllPagesSectionTypes;
}

export interface FooterGroup {
  index: number;
  groupHeaderText: string;
  textAndLinks: GroupTextLink[];
}

 export const SECTION_TYPE_LABELS: Record<AllPagesSectionTypes, string> = {
    'HERO': 'Hero',
    'FOOTER': 'Footer',
    'HEADER': 'Header',
    'CONTENT1': 'Contenu 1',
    'CONTENT2': 'Contenu 2',
    'CONTENT3': 'Contenu 3',
    'CONTENT4': 'Contenu 4',
    'CONTENT5': 'Contenu 5',
    'CONTENT6': 'Contenu 6',
    'CONTENT7': 'Contenu 7',
    'CONTENT8': 'Contenu 8',
    'BANNER_SCROLLING': 'Bannière défilante',
    'FAQ': 'FAQ',
    'OUR_PRODUCTS': 'Nos produits',
    'OUR_REVIEWS': 'Nos avis',
    'HTML': 'Code html',
    'OUR_BLOGS': 'Nos blogs',
    "DESCRIPTION": "Description",
    "MAIN": "Main",
    "SOCIATY_INFO": "Informations sociétales",
    "RIGHT_BARS": "Bannieres de droite",
  };
export function GET_SECTION_TYPE_LABEL(type: AllPagesSectionTypes): string {
    return SECTION_TYPE_LABELS[type] || type;
  }
export const PAGE_TYPE_LABELS_ICONS_LINK_SECTIONS: Record<TypePageDto, [string, string, string, string[]]> = {
    ALL: ['Toutes les pages', 'layers','', []],
    HOME: ['Accueil', 'home', '/', [...HOME_PAGE_SECTIONS] ],
    PRODUCT: ['Produits', 'shopping_bag', '/products' ,[...PRODUCTS_PAGE_SECTIONS]],
    CONTACT: ['Contact', 'mail', '/contact', [...CONTACT_PAGE_SECTIONS]],
    ABOUT: ['À propos', 'info', '/about', [...ABOUT_PAGE_SECTIONS]],
    BLOG: ['Blog', 'article', '/blog', [...BLOG_PAGE_SECTIONS]],
    TERMS_AND_CONDITIONS: ['termes et conditions', 'gavel', '/terms-and-conditions', [...TAC_PAGE_SECTIONS]],
    PRODUCT_DETAILS: ['Détails du produit', 'inventory_2','', [...PRODUCT_DETAILS_PAGE_SECTIONS]],
    DELIVERY_AND_RETURN: ['Livraison et retour', 'local_shipping', '/delivery-and-return', [...DAR_PAGE_SECTIONS]],
    LEGAL_NOTICE: ['Avis juridique', 'description', '/legal-notice', [...LEGAL_NOTICE_PAGE_SECTIONS]]
  };

   export function GET_PAGE_TYPE_LABEL(type: TypePageDto): string {
    return PAGE_TYPE_LABELS_ICONS_LINK_SECTIONS[type][0] || type;
  }
 export function GET_PAGE_TYPE_ICON(type: TypePageDto): string {
    return PAGE_TYPE_LABELS_ICONS_LINK_SECTIONS[type][1] || type;
  }
   export function GET_PAGE_TYPE_LINK(type: TypePageDto): string {
    return PAGE_TYPE_LABELS_ICONS_LINK_SECTIONS[type][2] || type;
  }
  export function GET_PAGE_TYPE_SECTIONS(type: TypePageDto): string[] {
    return PAGE_TYPE_LABELS_ICONS_LINK_SECTIONS[type][3] || [];
  }

 
  

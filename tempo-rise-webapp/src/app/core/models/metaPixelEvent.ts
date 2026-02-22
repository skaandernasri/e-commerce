type action_source= "email" | "website" | "app" | "phone_call" | "chat" | "physical_store" | "system_generated" |
"business_messaging" | "other";
export type currency = 'TND' | 'USD' | 'EUR';
export interface MetaPixelEventRequest {
    data: MetaPixelEventRequestDataInner[];
}
export type contentType = 'product' | 'product_group';


interface MetaPixelEventRequestDataInner {
    event_name: string;
    event_time: number;
    event_id: string;
    action_source: action_source;
    event_source_url?: string;
    user_data?: MetaPixelEventRequestDataInnerUserData;
    custom_data?: MetaPixelEventRequestDataInnerCustomData;
}

interface MetaPixelEventRequestDataInnerUserData {
    em?: string[];
    client_user_agent: string;
    fbp?: string;
    fbc?: string;
}
interface MetaPixelEventRequestDataInnerCustomData {
    content_name?: string;
    content_ids?: string[];
    content_type?: contentType;
    contents?: MetaPixelEventRequestDataInnerCustomDataContentsInner[];
    value?: number;
    currency?: currency;
    num_items?: number;
}

interface MetaPixelEventRequestDataInnerCustomDataContentsInner {
    id: string;
    quantity: number;
}

